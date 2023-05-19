package com.xbcx.core.http;

import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.xbcx.core.Event;
import com.xbcx.core.module.AppBaseListener;

public interface HttpCachePlugin extends AppBaseListener{
	public JSONObject onReadHttpCache(XHttpRunner runner,Event event,String url,RequestParams rp);
}
