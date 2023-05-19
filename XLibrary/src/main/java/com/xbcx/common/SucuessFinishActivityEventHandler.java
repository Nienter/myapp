package com.xbcx.common;

import com.xbcx.core.Event;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.BaseActivity.ActivityEventHandler;

public class SucuessFinishActivityEventHandler implements ActivityEventHandler{
	@Override
	public void onHandleEventEnd(Event event, BaseActivity activity) {
		if(event.isSuccess()){
			activity.finish();
		}
	}
}
