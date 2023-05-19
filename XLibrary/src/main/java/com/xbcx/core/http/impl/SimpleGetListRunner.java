package com.xbcx.core.http.impl;

import java.util.HashMap;

import org.json.JSONObject;

import com.loopj.android.http.RequestParams;
import com.xbcx.core.Event;
import com.xbcx.utils.JsonParseUtils;

public class SimpleGetListRunner extends SimpleItemBaseRunner {

	private String	mJsonListKey = "list";
	
	public SimpleGetListRunner(String url, Class<?> itemClass) {
		super(url, itemClass);
	}
	
	public SimpleGetListRunner jsonListKey(String jsonListKey){
		mJsonListKey = jsonListKey;
		return this;
	}

	@Override
	public void onEventRun(Event event) throws Exception {
		RequestParams params = buildParams(event);
		request(params, event);
	}
	
	@SuppressWarnings("unchecked")
	public RequestParams buildParams(Event event){
		RequestParams params = new RequestParams(event.findParam(HashMap.class));
		addOffset(params, event);
		return params;
	}
	
	public void request(RequestParams params,Event event) throws Exception{
		JSONObject jo = doPost(event, mUrl, params);
		event.setSuccess(onHandleReturnParam(event, jo));
	}

	protected boolean onHandleReturnParam(Event event,JSONObject jo) throws Exception{
		event.addReturnParam(JsonParseUtils.parseArrays(jo, mJsonListKey, mItemClass));
		handleExtendItems(event, jo);
		if(jo.has("hasmore")){
			event.addReturnParam(new HttpPageParam(jo));
		}
		event.addReturnParam(jo);
		handleEventReturnParamProvider(jo, event);
		return true;
	}
}
