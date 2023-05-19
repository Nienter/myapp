package com.xbcx.core;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;

public class SharedPreferenceDefine {
	public static final String SP_IM 			= "im";
	public static final String KEY_USER			= "user";
	public static final String KEY_PWD			= "pwd";
	public static final String KEY_HttpUser		= "httpuser";
	public static final String KEY_HttpPwd		= "httppwd";
	public static final String KEY_SPEAKERON	= "sperkeron";
	public static final String KEY_CHAT_BG		= "keychatbg";
	
	public static final String KEY_DeviceUUID	= "deviceuuid";
	
	public static String getStringValue(String key,String defValue){
		return XApplication.getApplication().getSharedPreferences(SP_IM, 0)
				.getString(key, defValue);
	}
	
	public static void setStringValue(String key,String value){
		XApplication.getApplication().getSharedPreferences(SP_IM, 0)
		.edit().putString(key, value).commit();
	}
	
	public static boolean getBooleanValue(String key,boolean defValue){
		return XApplication.getApplication().getSharedPreferences(SP_IM, 0)
				.getBoolean(key, defValue);
	}
	
	public static void setBooleanValue(String key,boolean value){
		XApplication.getApplication().getSharedPreferences(SP_IM, 0)
		.edit().putBoolean(key, value).commit();
	}
	
	public static void setIntValue(String key,int value){
		XApplication.getApplication().getSharedPreferences(SP_IM, 0)
		.edit().putInt(key, value).commit();
	}
	
	public static int getIntValue(String key,int defValue){
		return XApplication.getApplication().getSharedPreferences(SP_IM, 0)
				.getInt(key, defValue);
	}
	
	public static void setStringSetValue(String key,List<String> values){
		final SharedPreferences sp = XApplication.getApplication()
				.getSharedPreferences(SP_IM, 0);
		StringBuffer sb = new StringBuffer();
		for(String s : values){
			sb.append(s).append(",");
		}
		sp.edit().putString(key, sb.toString()).commit();
	}
	
	public static List<String> getStringSetValue(String key,List<String> defValues){
		SharedPreferences sp = XApplication.getApplication().getSharedPreferences(SP_IM, 0);
		final String s = sp.getString(key, null);
		if(s == null){
			return defValues;
		}
		String[] temps = s.split(",");
		if(temps != null){
			final List<String> values = new ArrayList<String>();
			for(String temp : temps){
				values.add(temp);
			}
			return values;
		}
		return defValues;
	}
}
