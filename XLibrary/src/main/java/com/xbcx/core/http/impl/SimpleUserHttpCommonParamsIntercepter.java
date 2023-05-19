package com.xbcx.core.http.impl;

import android.text.TextUtils;

import com.loopj.android.http.RequestParams;
import com.xbcx.core.Event;
import com.xbcx.core.http.HttpCommonParamsIntercepter;
import com.xbcx.im.IMKernel;

public class SimpleUserHttpCommonParamsIntercepter implements HttpCommonParamsIntercepter{

	private String	mHttpKey = "user";
	
	public SimpleUserHttpCommonParamsIntercepter setHttpKey(String key){
		mHttpKey = key;
		return this;
	}
	
	@Override
	public String onInterceptAddCommonParams(Event event, String url, RequestParams rp)  throws Exception{
		final String user = IMKernel.getLocalUser();
		if(!TextUtils.isEmpty(user)){
			rp.add(mHttpKey, user);
		}
		return url;
	}
}
