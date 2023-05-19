package com.xbcx.core.http;

import com.loopj.android.http.RequestParams;
import com.xbcx.core.Event;
import com.xbcx.core.module.AppBaseListener;

public interface HttpCommonParamsIntercepter extends AppBaseListener{

	public String	onInterceptAddCommonParams(Event event,String url,RequestParams rp) throws Exception;
}
