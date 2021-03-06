package com.ifarm.mina;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ifarm.bean.ControlCommand;
import com.ifarm.observer.IoSessionObserver;
import com.ifarm.redis.util.ControlCommandRedisHelper;
import com.ifarm.util.ByteConvert;
import com.ifarm.util.CacheDataBase;
import com.ifarm.util.Constants;
import com.ifarm.util.ControlHandlerUtil;
import com.ifarm.util.ConvertData;

@SuppressWarnings("unchecked")
@Component
public class ControlByteIoHandler extends IoHandlerAdapter implements IoSessionObserver {
	private static final Log inHandler_log = LogFactory.getLog(ControlByteIoHandler.class);
	ConvertData convertData = new ConvertData();
	@Autowired
	private ControlCommandRedisHelper commandRedisHelper;
	
	public ControlByteIoHandler() {
		CacheDataBase.ioControlData.registerObserver(Constants.ioControlHandler, this);
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		super.sessionCreated(session);
		InetSocketAddress socketAddress = (InetSocketAddress) session.getRemoteAddress();
		InetAddress address = socketAddress.getAddress();
		inHandler_log.info("client ip:" + address.getHostAddress());
		LinkedList<ControlCommand> linkedList = new LinkedList<ControlCommand>();
		session.setAttribute("controlCommands", linkedList);

	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		super.sessionOpened(session);
		inHandler_log.info("io session:" + session);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		super.sessionClosed(session);
		CacheDataBase.ioControlData.removeObserver(session);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		// TODO Auto-generated method stub
		super.sessionIdle(session, status);
		session.closeNow();
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(session, cause);
		CacheDataBase.ioControlData.removeObserver(session);
	}

	public void handlerHeartBeatData(IoSession session) {
		long currentTime = System.currentTimeMillis() / 1000;
		byte[] bytes = new byte[6];
		bytes[0] = (byte) 0xFF;
		bytes[1] = 0;
		byte[] bye = ByteConvert.longToByte4(currentTime);
		for (int i = 0; i < bye.length; i++) {
			bytes[i + 2] = bye[i];
		}
		session.write(bytes);
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		byte[] arr = (byte[]) message;
		LinkedList<ControlCommand> commandsLinkedList = (LinkedList<ControlCommand>) session.getAttribute("controlCommands");
		if (arr[0] == (byte) 0xFE) { // 测试信号
			int networkSignal = arr[1];
			int voltage = arr[2];
			int temperature = arr[3];
			Long collectorId = convertData.byteToConvertLong(arr, 4, 4);
			session.setAttribute("collectorId", collectorId);
			CacheDataBase.ioControlData.registerObserver(collectorId, session);
			inHandler_log.info("信号强度:" + networkSignal + " 供电电压:" + voltage * 1.0 / 10 + " 温度:" + temperature + " 集中器编号:" + collectorId);
			ControlCommand command = commandRedisHelper.getLockRedisListValue(collectorId.toString());
			if (command != null) {
				session.write(command.commandToByte());
				commandsLinkedList.add(command);
			}
			/*PriorityBlockingQueue<ControlCommand> queue = CacheDataBase.controlCommandCache.get(collectorId);
			while (queue != null && queue.size() > 0) {
				ControlCommand command = queue.peek();
				session.write(command.commandToByte());
				commandsLinkedList.add(command);
				queue.take();
			}*/
		} else if (arr[0] == (byte) 0xFD) { // 设备的回复命令
			if (commandsLinkedList.size() > 0) {
				ControlCommand command = commandsLinkedList.pop();
				boolean flag = false;
				if (arr.length == 1) { // 只有fd，认为设备操作失败
					flag = false;
				} else {
					flag = true;
				}
				ControlHandlerUtil.controlDeviceReturnMessage(command, flag);
			} else {
				inHandler_log.error("异常回复:" + message);
			}
		}

	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub
		StringBuffer buffer = new StringBuffer();
		byte[] arr = (byte[]) message;
		for (int i = 0; i < arr.length; i++) {
			buffer.append(Integer.toHexString(arr[i] & 0xff) + " ");
		}
		inHandler_log.debug("server send message to " + session + " device:" + buffer);
	}

	@Override
	public void inputClosed(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		super.inputClosed(session);
	}

	@Override
	public void update(Long collectorId, IoSession ioSession) throws Exception {
		// TODO Auto-generated method stub
		LinkedList<ControlCommand> commandsLinkedList = (LinkedList<ControlCommand>) ioSession.getAttribute("controlCommands");
		ControlCommand command = commandRedisHelper.getLockRedisListValue(collectorId.toString());
		if (command != null) {
			ioSession.write(command.commandToByte()); // 如果抛出异常下面的就不执行，该指令下次依旧可以发送下去
			commandsLinkedList.add(command);
		}
		/*if (CacheDataBase.controlCommandCache.containsKey(collectorId)) {
			while (!CacheDataBase.controlCommandCache.get(collectorId).isEmpty()) {
				ControlCommand command = CacheDataBase.controlCommandCache.get(collectorId).peek();
				if (command != null) {
					ioSession.write(command.commandToByte()); // 如果抛出异常下面的就不执行，该指令下次依旧可以发送下去
					commandsLinkedList.add(command);
					CacheDataBase.controlCommandCache.get(collectorId).take();
				}
			}
		}*/
	}

}
