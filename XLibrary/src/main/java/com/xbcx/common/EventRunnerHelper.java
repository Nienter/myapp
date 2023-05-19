package com.xbcx.common;

import android.util.SparseArray;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.EventManager.OnEventRunner;

public class EventRunnerHelper {
	
	private SparseArray<OnEventRunner> 	mMapCodeToRunners = new SparseArray<OnEventRunner>();
	
	public void managerRegisterRunner(int eventCode,OnEventRunner runner){
		AndroidEventManager.getInstance().registerEventRunner(eventCode, runner);
		mMapCodeToRunners.put(eventCode, runner);
	}
	
	public void destory(){
		int size = mMapCodeToRunners.size();
		AndroidEventManager eventManager = AndroidEventManager.getInstance();
		for(int index = 0;index < size;++index){
			final int code = mMapCodeToRunners.keyAt(index);
			eventManager.removeEventRunner(code, mMapCodeToRunners.get(code));
		}
	}
}
