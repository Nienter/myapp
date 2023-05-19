package com.xbcx.core.http.impl;

import java.util.HashMap;

import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.xbcx.core.Event;

public class SimpleIdRunner extends SimpleBaseRunner {
	
	protected String	mIdHttpKey = "id";

	public SimpleIdRunner(String url) {
		super(url);
	}
	
	public SimpleIdRunner setIdHttpKey(String httpKey){
		mIdHttpKey = httpKey;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEventRun(Event event) throws Exception {
		RequestParams rp = new RequestParams(event.findParam(HashMap.class));
		rp.add(mIdHttpKey, event.findParam(String.class));
		JSONObject jo = doPost(event, mUrl, rp);
		event.addReturnParam(jo);
		event.setSuccess(true);
	}
}
