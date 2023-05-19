package com.xbcx.core.http;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.xbcx.core.Event;
import com.xbcx.core.module.AppBaseListener;

public interface HttpClientProvider extends AppBaseListener{
	public AsyncHttpClient buildHttpClient(Event event,String url,
			String fixUrl,
			RequestParams params);
}
