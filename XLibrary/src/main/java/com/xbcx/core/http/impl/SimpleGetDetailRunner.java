package com.xbcx.core.http.impl;

import java.util.HashMap;

import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.xbcx.core.Event;
import com.xbcx.utils.JsonParseUtils;

public class SimpleGetDetailRunner extends SimpleItemBaseRunner {

	protected String	mIdHttpKey = "id";
	
	public SimpleGetDetailRunner(String url, Class<?> itemClass) {
		super(url, itemClass);
	}
	
	public SimpleGetDetailRunner setIdHttpKey(String httpKey){
		mIdHttpKey = httpKey;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEventRun(Event event) throws Exception {
		final Object param = event.getParamAtIndex(0);
		RequestParams params = null;
		JSONObject jo = null;
		if(param != null && param instanceof String){
			final String id = (String)param;
			params = new RequestParams(event.findParam(HashMap.class));
			params.add(mIdHttpKey, id);
			jo = doPost(event, mUrl, params);
			if(!jo.has(mIdHttpKey)){
				jo.put(mIdHttpKey, id);
			}
		}else{
			params = new RequestParams(event.findParam(HashMap.class));
			jo = doPost(event, mUrl, params);
			if(!jo.has(mIdHttpKey)){
				jo.put(mIdHttpKey, params.getUrlParams(mIdHttpKey));
			}
		}
		event.addReturnParam(JsonParseUtils.buildObject(mItemClass, jo));
		handleExtendItems(event, jo);
		event.addReturnParam(jo);
		event.setSuccess(true);
	}

}
