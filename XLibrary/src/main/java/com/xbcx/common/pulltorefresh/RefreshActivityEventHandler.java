package com.xbcx.common.pulltorefresh;

import com.xbcx.core.BaseActivity;
import com.xbcx.core.BaseActivity.ActivityEventHandler;
import com.xbcx.core.Event;

public class RefreshActivityEventHandler implements ActivityEventHandler{

	protected PullToRefreshPlugin<?> mPullToRefreshPlugin;
	
	public RefreshActivityEventHandler(PullToRefreshPlugin<?> pp){
		mPullToRefreshPlugin = pp;
	}
	
	@Override
	public void onHandleEventEnd(Event event, BaseActivity activity) {
		if(event.isSuccess()){
			mPullToRefreshPlugin.startRefresh();
		}
	}

}
