package com.xbcx.core.http.impl;

import java.util.HashMap;

import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.xbcx.core.Event;

public class SimpleRunner extends SimpleBaseRunner {

	public SimpleRunner(String url) {
		super(url);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEventRun(Event event) throws Exception {
		JSONObject jo = doPost(event, mUrl, new RequestParams(event.findParam(HashMap.class)));
		event.addReturnParam(jo);
		event.setSuccess(true);
	}
}
