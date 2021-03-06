package com.ifarm.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ifarm.mqtt.MqttService;
import com.ifarm.nosql.dao.InitRedisDao;
import com.ifarm.observer.IoControlData;
import com.ifarm.observer.UserControlData;
import com.ifarm.redis.util.ControlCommandRedisHelper;
import com.ifarm.redis.util.ControlTaskRedisHelper;
import com.ifarm.redis.util.WfmControlTaskRedisHelper;
import com.ifarm.service.CollectorDeviceValueService;
import com.ifarm.service.ControlTaskService;
import com.ifarm.service.FarmControlSystemService;
import com.ifarm.service.FarmControlSystemWFMService;
import com.ifarm.service.WFMControlTaskService;

public class CacheDataBase {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CacheDataBase.class);
	
	public static int timeout = 1000 * 60;
	public static boolean receiveFlat = true;
	public static Object sensorcachelock = new Object();
	public static String machineCode = "101";
	
	public static Map<String, String> sensorParamCodeMap = new HashMap<String, String>(); // 传感器code和类型的对应
	public static Map<String, JSONArray> topicThemeMap = new HashMap<String, JSONArray>();
	public static Map<String, Byte> controlBaseCommand = new HashMap<String, Byte>(); // 控制的基础命名
	public static Map<String, String> collectorDeviceTitle = new HashMap<String, String>(); // 采集设备的表头
	public static Map<String, String> collectorDeviceUnit = new HashMap<String, String>(); // 采集设备的参数对应的单位
	
	/**
	 * 初始化的data
	 */
	public static UserControlData userControlData;
	public static IoControlData ioControlData = new IoControlData(null);

	/**
	 * 内存数据库的配置，统一为concurrentHashMap
	 */
	//public static Map<Long, List<DeviceValueBase>> collcetorDeviceMainValueCacheMap = new ConcurrentHashMap<Long, List<DeviceValueBase>>(); // 50个点,key为设备id
	//public static ConcurrentMap<Long, JSONObject> collectorStateValueMap = new ConcurrentHashMap<Long, JSONObject>(); // 集中器状态
	//public static ConcurrentMap<Long, DeviceValueBase> collectorDeviceMainValueMap = new ConcurrentHashMap<Long, DeviceValueBase>(); // 采集设备的主要参数，包括光照强度，空气土壤温湿度,key为设备Id
	//public static ConcurrentMap<String, String> userToken = new ConcurrentHashMap<String, String>();
	//public static ConcurrentMap<String, String> userSignature = new ConcurrentHashMap<String, String>();
	//public static ConcurrentMap<String, String> managerToken = new ConcurrentHashMap<String, String>();
	public static ConcurrentMap<String, List<String>> sensorParameterMap = new ConcurrentHashMap<String, List<String>>(); // 每个传感器对应的参数
	//public static ConcurrentMap<Long, List<Long>> collectorDeviceAddCache = new ConcurrentHashMap<Long, List<Long>>(); // 增加传感器，key为集中器编号
	//public static Map<String, LinkedBlockingQueue<ControlTask>> controlTaskStateCache = new ConcurrentHashMap<String, LinkedBlockingQueue<ControlTask>>(); // task的缓存,key为用户的Id
	//public static Map<String, LinkedBlockingQueue<WFMControlTask>> wfmControlTaskStateCache = new ConcurrentHashMap<String, LinkedBlockingQueue<WFMControlTask>>(); // 水肥药task的缓存,key为用户的Id

	/**
	 * 控制的相关缓存
	 */
	//public static Map<Long, PriorityBlockingQueue<ControlCommand>> controlCommandCache = new ConcurrentHashMap<Long, PriorityBlockingQueue<ControlCommand>>();// 下发指令的缓存，key为集中器编号
	//public static Map<String, LinkedBlockingQueue<String>> userControlResultMessageCache = new ConcurrentHashMap<String, LinkedBlockingQueue<String>>(); // 返回给用户信息的缓存，key为用户ID
	//public static Map<Integer, Boolean> controlSystemStateMap = new ConcurrentHashMap<Integer, Boolean>(); // 控制系统的状态，当前正在运行的系统不能再立马执行任务
	//public static Map<Integer, Boolean> controlDeviceStateMap = new ConcurrentHashMap<Integer, Boolean>(); // 控制设备的状态，正在运行的设备也不能立马在做其他任务，除非该任务已结束
	/**
	 * 线程池的配置
	 */
	public static ExecutorService service = Executors.newFixedThreadPool(20);
	public static ExecutorService controlService = Executors.newFixedThreadPool(20);
	public static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 50, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(10));
	public static ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();
	/**
	 * 静态的service、dao变量
	 */
	public static ControlCacheCollection cacheCollection;// 回收控制对象的线程
	public static CollectorDeviceValueService sensorValuesService;
	public static ControlTaskService taskService;
	public static WFMControlTaskService wTaskService;
	public static InitRedisDao initRedisDao;
	public static FarmControlSystemService farmControlSystemService;
	public static FarmControlSystemWFMService farmControlSystemWFMService;
	public static ControlCommandRedisHelper commandRedisHelper;
	public static ControlTaskRedisHelper controlTaskRedisHelper;
	public static WfmControlTaskRedisHelper wfmControlTaskRedisHelper;
	
	public static MqttService mqttService;
	public static String userImagePath = "";
	public static String farmImagePath = "";
	public static String imageSavePath = "";
	public static String apkVersionPath = "";
	public static String farmLabel = "";
	public static boolean mqttThreadFlat = true;

	/**
	 * 传采集器通信配置项
	 */
	public static String configData = "";
	public static String configSensor = "";
	public static String configIp = "";
	public static String configTime = "";
	public static String configStop = "";
	public static int cacheSize = 0;

	public static Integer port = 0; // tcp监听的端口
	public static Integer controlPort = 0; // tcp监听控制的端口
	static {
		try {
			InetAddress inetAddress = InetAddress.getLocalHost();
			String hostIp = inetAddress.getHostAddress();
			imageSavePath = "http://" + hostIp + ":8080/IFarm/images/";
			apkVersionPath = "http://" + hostIp + ":8080/IFarm/apk/";
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void loadProperties() throws IOException {
		Properties properties = new Properties();
		properties.load(CacheDataBase.class.getClassLoader().getResourceAsStream("com/ifarm/util/baseInformation.properties"));
		userImagePath = imageSavePath + properties.getProperty("userImagePath");
		farmImagePath = imageSavePath + properties.getProperty("farmImagePath");
		farmLabel = properties.getProperty("farmLabel");
		port = Integer.valueOf(properties.getProperty("tcp.port"));
		controlPort = Integer.valueOf(properties.getProperty("tcp.controlPort"));
		configData = properties.getProperty("collector.data");
		configSensor = properties.getProperty("collector.sensor");
		configIp = properties.getProperty("collector.ipConfig");
		configTime = properties.getProperty("collector.time");
		configStop = properties.getProperty("collector.stop");
		cacheSize = Integer.parseInt(properties.getProperty("cacheSize"));
		cacheCollection.setOffset(Integer.parseInt(properties.getProperty("offset")));
		cacheCollection.setTimeout(Integer.parseInt(properties.getProperty("timeout")));
		LOGGER.info("port:" + port);
		LOGGER.info("imagePath:" + userImagePath + " and " + farmImagePath);
		LOGGER.info("config:" + configData + "、" + configSensor + "、" + configIp + "、" + configTime + "、" + configStop);
		Properties sensorProperties = new Properties();
		sensorProperties.load(CacheDataBase.class.getClassLoader().getResourceAsStream("com/ifarm/util/sensor.properties"));
		Enumeration<Object> enumeration = sensorProperties.keys();
		while (enumeration.hasMoreElements()) {
			String key = (String) enumeration.nextElement();
			sensorParamCodeMap.put(key, sensorProperties.getProperty(key));
		}
		Properties collectorHeaderProperties = new Properties();
		collectorHeaderProperties.load(CacheDataBase.class.getClassLoader().getResourceAsStream("com/ifarm/util/collectorDeviceTitle.properties"));
		Enumeration<Object> headerEnumeration = collectorHeaderProperties.keys();
		while (headerEnumeration.hasMoreElements()) {
			String key = (String) headerEnumeration.nextElement();
			collectorDeviceTitle.put(key, collectorHeaderProperties.getProperty(key));
		}
		LOGGER.info("collectorDeviceTitle" + collectorDeviceTitle);
	}

	public static Map<String, JSONObject> loadDataBaseTableString(Connection con, String table) throws SQLException {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Map<String, JSONObject> map = new ConcurrentHashMap<>();
		try {
			preparedStatement = con.prepareStatement("SELECT * FROM `" + table + "`");
			resultSet = preparedStatement.executeQuery();
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int column = resultSetMetaData.getColumnCount();
			while (resultSet.next()) {
				JSONObject jsonObject = new JSONObject();
				String key = resultSet.getString(1);
				for (int i = 1; i <= column; i++) {
					if (resultSet.getObject(i) != null) {
						jsonObject.put(resultSetMetaData.getColumnName(i), resultSet.getObject(i).toString());
					}
				}
				map.put(key, jsonObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			preparedStatement.close();
			resultSet.close();
		}
		return map;
	}

	public static void loadTopics(Connection con) throws SQLException {
		topicThemeMap.clear();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			StringBuffer stringBuffer = new StringBuffer("select * from topic order by topicTheme");
			preparedStatement = con.prepareStatement(stringBuffer.toString());
			resultSet = preparedStatement.executeQuery();
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int column = resultSetMetaData.getColumnCount();
			while (resultSet.next()) {
				String topicTheme = resultSet.getString(2);
				JSONObject jsonObject = new JSONObject();
				for (int i = 3; i <= column; i++) {
					if (resultSet.getObject(i) != null) {
						jsonObject.put(resultSetMetaData.getColumnName(i), resultSet.getObject(i).toString());
					}
				}
				if (topicThemeMap.containsKey(topicTheme)) {
					topicThemeMap.get(topicTheme).add(jsonObject);
				} else {
					JSONArray jsonArray = new JSONArray();
					jsonArray.add(jsonObject);
					topicThemeMap.put(topicTheme, jsonArray);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			LOGGER.info("topics:" + topicThemeMap);
			preparedStatement.close();
			resultSet.close();
		}

	}

	@SuppressWarnings("static-access")
	public static void loadMqttService() throws MqttException, UnsupportedEncodingException {
		for (int i = 0; i < mqttService.topics.length; i++) {
			String topic = mqttService.topics[i];
			mqttService.message = new MqttMessage();
			mqttService.message.setQos(Integer.valueOf(mqttService.qos[i]));
			mqttService.message.setRetained(true);
			mqttService.message.setPayload(topicThemeMap.get(topic).toString().getBytes("UTF-8"));
			mqttService.publish(mqttService.mqttTopics[i], mqttService.message);
		}
	}

	public static void initialize() throws Exception {
		ConDb conDb = new ConDb();
		Connection con = conDb.openCon();
		loadProperties();
		loadTopics(con);
		con.close();
		//CacheDataBase.initRedisDao.redisConnect();
		mqttService = new MqttService();
		controlService.execute(new Thread(cacheCollection));
		/*
		 * scheduled.scheduleAtFixedRate(new Runnable() {
		 * 
		 * @Override public void run() { // TODO Auto-generated method stub if
		 * (!mqttService.mqttClient.isConnected()) { mqttService.connect(); try
		 * { loadMqttService(); } catch (Exception e) { // e.printStackTrace();
		 * } } } }, 10, 60, TimeUnit.SECONDS);
		 */
	}
}
