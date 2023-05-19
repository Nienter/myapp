package com.xbcx.core.http;

import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.xbcx.core.Event;
import com.xbcx.core.module.AppBaseListener;

public interface HttpResultHandler extends AppBaseListener{
	public void onHandleHttpResult(Event event,String url,RequestParams params,String ret,JSONObject jo);
}
