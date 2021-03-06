package com.ifarm.util;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JsonObjectUtil {
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String toJsonObjectString(Object object) {
		if (object == null) {
			return null;
		}
		JSONObject jsonObject = new JSONObject();
		Field[] fields = object.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			try {
				if (fields[i].get(object) != null) {
					if (fields[i].get(object) instanceof Date) {
						Date date = (Date) fields[i].get(object);
						jsonObject.put(fields[i].getName(), FORMAT.format(date));
					}else {
						jsonObject.put(fields[i].getName(), fields[i].get(object).toString());
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return jsonObject.toString();
	}

	public static <T> String toJsonArrayString(List<T> list) {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			Object object = list.get(i);
			JSONArray array = new JSONArray();
			if (object instanceof Object[]) {
				Object[] arr = (Object[]) object;
				for (int j = 0; j < arr.length; j++) {
					array.add(arr[j].toString());
				}
				jsonArray.add(array);
			} else {
				jsonArray.add(toJsonObjectString(object));
			}
		}
		return jsonArray.toString();
	}

	public static <T> String toJsonArray(LinkedBlockingQueue<T> list) {
		JSONArray jsonArray = new JSONArray();
		Iterator<T> iterator = list.iterator();
		while (iterator.hasNext()) {
			jsonArray.add(toJsonObjectString(iterator.next()));
		}
		return jsonArray.toString();
	}

	public static JSONObject fromBean(Object object) {
		JSONObject jsonObject = new JSONObject();
		if (object == null) {
			return jsonObject;
		}
		Field[] fields = object.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			String key = fields[i].getName();
			try {
				if (fields[i].get(object) != null) {
					jsonObject.put(key, fields[i].get(object).toString());
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return jsonObject;
	}

	/**
	 * 不转成string
	 * @param object
	 * @return
	 */
	public static JSONObject fromOriginalBean(Object object) {
		JSONObject jsonObject = new JSONObject();
		if (object == null) {
			return jsonObject;
		}
		Field[] fields = object.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			String key = fields[i].getName();
			try {
				if (fields[i].get(object) != null) {
					jsonObject.put(key, fields[i].get(object));
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return jsonObject;
	}

	public static JSONObject fromBean(Object object, int len) {
		JSONObject jsonObject = new JSONObject();
		if (object == null) {
			return jsonObject;
		}
		Field[] fields = object.getClass().getDeclaredFields();
		for (int i = 0; i <= len; i++) {
			fields[i].setAccessible(true);
			String key = fields[i].getName();
			try {
				if (fields[i].get(object) != null) {
					jsonObject.put(key, fields[i].get(object).toString());
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return jsonObject;
	}
}
