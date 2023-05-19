package com.xbcx.common;

import com.xbcx.core.Event;
import com.xbcx.core.ToastManager;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.BaseActivity.ActivityEventHandler;

public class ToastActivityEventHandler implements ActivityEventHandler{

	private int	mStringId;
	
	public ToastActivityEventHandler(int stringId){
		mStringId = stringId;
	}
	
	@Override
	public void onHandleEventEnd(Event event, BaseActivity activity) {
		if(event.isSuccess()){
			ToastManager.getInstance(activity).show(mStringId);
		}
	}

}
