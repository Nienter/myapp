package com.xbcx.core.http.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.xbcx.core.Event;
import com.xbcx.core.http.XHttpRunner;

public abstract class SimpleBaseRunner extends XHttpRunner {
	
	protected final String	mUrl;
	
	private List<EventReturnParamProvider> mEventReturnParamProviders;
	
	public SimpleBaseRunner(String url){
		mUrl = url;
	}
	
	public SimpleBaseRunner addEventReturnParamProvider(EventReturnParamProvider provider){
		if(provider != null){
			if(mEventReturnParamProviders == null){
				mEventReturnParamProviders = new ArrayList<SimpleBaseRunner.EventReturnParamProvider>();
			}
			mEventReturnParamProviders.add(provider);
		}
		return this;
	}
	
	public void handleEventReturnParamProvider(JSONObject jo,Event event){
		if(mEventReturnParamProviders != null){
			for(EventReturnParamProvider p : mEventReturnParamProviders){
				Object param = p.onBuildReturnParam(jo, event);
				if(param != null){
					event.addReturnParam(param);
				}
			}
		}
	}
	
	public static interface EventReturnParamProvider{
		public Object onBuildReturnParam(JSONObject jo,Event event);
	}
}
