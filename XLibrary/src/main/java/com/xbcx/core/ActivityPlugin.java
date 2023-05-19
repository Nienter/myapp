package com.xbcx.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

public abstract class ActivityPlugin<T extends Activity> implements ActivityBasePlugin{
	
	protected T	mActivity;
	
	public final void setActivity(T activity){
		if(mActivity == null){
			mActivity = activity;
			onAttachActivity(activity);
		}else if(mActivity instanceof BaseActivity){
			/**
			 * 用于ActivityGroup情况下，ActivityPlugin只会注入其中一个Activity
			 * ActivityPlugin内部有插件时，能够传递到ActivityGroup的其他子Activity中
			 */
//			if(activity instanceof BaseActivity){
//				if(mChildActivitys == null){
//					@SuppressWarnings("rawtypes")
//					final Collection plugins = new ArrayList();
//					final BaseActivity old = (BaseActivity)mActivity;
//					old.setPluginDelegate(new GetPluginDelegate() {
//						@SuppressWarnings("unchecked")
//						@Override
//						public <E extends ActivityBasePlugin> Collection<E> getPlugins(Class<E> cls) {
//							plugins.clear();
//							if(old.getPluginHelper() != null){
//								plugins.add(old.getPluginHelper().getManagers(cls));
//							}
//							for(BaseActivity a : mChildActivitys){
//								if(a.getPluginHelper() != null){
//									plugins.add(a.getPluginHelper().getManagers(cls));
//								}
//							}
//							return plugins;
//						}
//					});
//					mChildActivitys = new ArrayList<BaseActivity>();
//				}
//				mChildActivitys.add((BaseActivity)activity);
//			}
		}
	}
	
	protected void onAttachActivity(T activity){
		
	}
	
	protected void onPostCreate(Bundle savedInstanceState){
	}
	
	protected void onResume() {
	}
	
	protected void onPause() {
	}
	
	protected void onDestroy() {
	}
	
	protected void onSaveInstanceState(Bundle outState) {
	}
	
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	}
	
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return false;
	}
	
	public boolean onBackPressed() {
		return false;
	}
}
