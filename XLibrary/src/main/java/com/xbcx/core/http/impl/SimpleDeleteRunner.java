package com.xbcx.core.http.impl;

import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.xbcx.core.Event;

public class SimpleDeleteRunner extends SimpleBaseRunner {
	
	protected String	mDeleteIdHttpKey = "id";
	
	public SimpleDeleteRunner(String url){
		super(url);
	}
	
	public SimpleDeleteRunner setDeleteIdHttpKey(String httpKey){
		mDeleteIdHttpKey = httpKey;
		return this;
	}
	
	@Override
	public void onEventRun(Event event) throws Exception {
		final String id = (String) event.getParamAtIndex(0);
		RequestParams params = new RequestParams();
		params.add(mDeleteIdHttpKey, id);
		JSONObject jo = doPost(event, mUrl, params);
		event.addReturnParam(jo);
		event.setSuccess(true);
	}

}
