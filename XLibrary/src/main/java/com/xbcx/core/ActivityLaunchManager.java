package com.xbcx.core;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;

public class ActivityLaunchManager {
	static{
		sInstance = new ActivityLaunchManager();
	}
	public static ActivityLaunchManager getInstance(){
		return sInstance;
	}
	
	private static ActivityLaunchManager sInstance;
	
	private List<LaunchIntercepter>	mLaunchIntercepters = new ArrayList<LaunchIntercepter>();
	private List<Activity> 			mRunningActivitys = new ArrayList<Activity>();
	
	private ActivityLaunchManager(){
	}
	
	public void registerLaunchIntercepter(LaunchIntercepter li){
		mLaunchIntercepters.add(li);
	}
	
	public Activity getCurrentActivity(){
		if(mRunningActivitys.size() > 0){
			Activity a = mRunningActivitys.get(mRunningActivitys.size() - 1);
			if(a.isFinishing()){
				mRunningActivitys.remove(mRunningActivitys.size() - 1);
				return getCurrentActivity();
			}
			return a;
		}
		return null;
	}
	
	public void onActivityCreate(Activity activity){
		mRunningActivitys.add(activity);
	}
	
	public void onActivityDestory(Activity activity){
		mRunningActivitys.remove(activity);
	}
	
	public void onStartActivity(Intent intent,Activity fromActivity){
		if(intent.getComponent() == null){
			return;
		}
		for(LaunchIntercepter li : mLaunchIntercepters){
			li.onInterceptLaunchActivity(intent, fromActivity);
		}
	}
	
	public static interface LaunchIntercepter{
		public void onInterceptLaunchActivity(Intent intent,Activity fromActivity);
	}
}
