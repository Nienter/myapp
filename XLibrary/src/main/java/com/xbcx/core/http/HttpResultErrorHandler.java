package com.xbcx.core.http;

import com.loopj.android.http.RequestParams;
import com.xbcx.core.Event;
import com.xbcx.core.module.AppBaseListener;

public interface HttpResultErrorHandler extends AppBaseListener{
	public void onHandleHttpResultError(Event event,String url,RequestParams params,String ret,Exception e);
}
