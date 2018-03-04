package com.ifarm.mina;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class CollectHandler extends IoHandlerAdapter {

	private static final Log ioHandler_log = LogFactory.getLog(CollectHandler.class);

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		super.sessionCreated(session);
		InetSocketAddress socketAddress = (InetSocketAddress) session.getRemoteAddress();
		InetAddress address = socketAddress.getAddress();
		System.out.println("client ip:" + address.getHostAddress());

	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		super.sessionOpened(session);
		System.out.println(this + "---" + session + "------" + "session open");
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		super.sessionClosed(session);
		System.out.println("session close");
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		// TODO Auto-generated method stub
		session.closeNow();
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(session, cause);
		ioHandler_log.error(cause);
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub
		// 更新数据到内存和数据库
		if ((int) session.getAttribute("num") == 0) {
			session.closeOnFlush();
		}

	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("send message:" + message);
	}

	@Override
	public void inputClosed(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		super.inputClosed(session);
		System.out.println("input closed");
	}

}
