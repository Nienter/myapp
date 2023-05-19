package com.xbcx.core;

import java.lang.reflect.Constructor;

import com.xbcx.core.BaseActivity.BaseAttribute;

import android.app.Activity;

public class XScreenFactory {
	
	private static Class<? extends ActivityScreen> sImplClass = ActivityScreen.class;

	public static void registerImplementation(Class<? extends ActivityScreen> implClass){
		sImplClass = implClass;
	}
	
	public static ActivityScreen wrap(Activity activity,BaseAttribute ba){
		ActivityScreen as = null;
		if(sImplClass == null){
			as = new ActivityScreen(activity,ba);
		}else{
			try{
				Constructor<? extends ActivityScreen> constructor = 
						sImplClass.getDeclaredConstructor(Activity.class,BaseAttribute.class);
				as = constructor.newInstance(activity,ba);
			}catch(Exception e){
				e.printStackTrace();
				as = new ActivityScreen(activity,ba);
			}
		}
		return as;
	}
}
