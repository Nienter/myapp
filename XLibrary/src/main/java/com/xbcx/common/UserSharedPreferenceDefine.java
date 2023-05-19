package com.xbcx.common;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.xbcx.core.SharedPreferenceDefine;
import com.xbcx.core.XApplication;
import com.xbcx.im.IMKernel;

public class UserSharedPreferenceDefine {
	
	public static String getUserSharedPreferenceName(){
		String id = IMKernel.getLocalUser();
		if(TextUtils.isEmpty(id)){
			id = XApplication.getApplication().getSharedPreferences(SharedPreferenceDefine.SP_IM, 0)
					.getString(SharedPreferenceDefine.KEY_USER, null);
			if(TextUtils.isEmpty(id)){
				id = "default";
			}
		}
		return id;
	}
	
	/**
	 * 自动登录
	 */
	public static final String KEY_SET_AUTO_LOGIN = "set_auto_login";
	
	public static boolean getBoolValue(String key){
		return getBoolValue(key,false);
	}
	
	public static boolean getBoolValue(String key,boolean defValue){
		SharedPreferences sp = XApplication.getApplication().getSharedPreferences(getUserSharedPreferenceName(), 0);
		return sp.getBoolean(key, defValue);
	}
	
	public static void setBoolValue(String key, boolean value){
		final SharedPreferences sp = XApplication.getApplication()
				.getSharedPreferences(getUserSharedPreferenceName(), 0);
		sp.edit().putBoolean(key, value).commit();
	}
	
	public static boolean reverseBoolValue(String key,boolean defValue){
		final boolean value = !getBoolValue(key, defValue);
		setBoolValue(key, value);
		return value;
	}
	
	public static String getStringValue(String key,String defValue){
		SharedPreferences sp = XApplication.getApplication().getSharedPreferences(getUserSharedPreferenceName(), 0);
		return sp.getString(key, defValue);
	}
	
	public static void setStringValue(String key,String value){
		final SharedPreferences sp = XApplication.getApplication()
				.getSharedPreferences(getUserSharedPreferenceName(), 0);
		sp.edit().putString(key, value).commit();
	}
	
	public static void setStringSetValue(String key,List<String> values){
		final SharedPreferences sp = XApplication.getApplication()
				.getSharedPreferences(getUserSharedPreferenceName(), 0);
		StringBuffer sb = new StringBuffer();
		for(String s : values){
			sb.append(s).append(",");
		}
		sp.edit().putString(key, sb.toString()).commit();
	}
	
	public static List<String> getStringSetValue(String key,List<String> defValues){
		SharedPreferences sp = XApplication.getApplication().getSharedPreferences(getUserSharedPreferenceName(), 0);
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
	
	public static void setIntValue(String key,int value){
		final SharedPreferences sp = XApplication.getApplication()
				.getSharedPreferences(getUserSharedPreferenceName(), 0);
		sp.edit().putInt(key, value).commit();
	}
	
	public static int getIntValue(String key,int defValue){
		SharedPreferences sp = XApplication.getApplication().getSharedPreferences(getUserSharedPreferenceName(), 0);
		return sp.getInt(key, defValue);
	}
}
