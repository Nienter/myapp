package com.xbcx.core.http;

import android.net.Proxy;
import android.net.Uri;
import android.text.TextUtils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.TextHttpResponseHandler;
import com.xbcx.core.Event;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.core.StringIdException;
import com.xbcx.core.XApplication;
import com.xbcx.library.R;
import com.xbcx.utils.Encrypter;
import com.xbcx.utils.SystemUtils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class XHttpRunner implements OnEventRunner {
	
	public static AsyncHttpClient	buildHttpClient(){
		AsyncHttpClient client = new XSyncHttpClient()
			.setUseOnce(true);
		client.setConnectTimeout(10000);
		client.setResponseTimeout(30000);
		return client;
	}
	
	public static String XHttpKeyEncrypt(String value,String key){
		return value + "-" + Encrypter.encryptBySHA1(value + key);
	}
	
	protected boolean checkRequestSuccess(JSONObject jsonObject){
		try{
			return "true".equals(jsonObject.getString("ok"));
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	protected boolean checkRequestSuccess(String strJson){
		try{
			JSONObject jsonObject = new JSONObject(strJson);
			return "true".equals(jsonObject.getString("ok"));
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	protected void addOffset(RequestParams params,Event event){
		final XHttpPagination p = event.findParam(XHttpPagination.class);
		if(p == null){
			if(TextUtils.isEmpty(params.getUrlParams("offset"))){
				params.add("offset", "0");
			}
		}else{
			params.add("offset", p.getOffset());
		}
	}
	
	public final JSONObject doGet(final Event event,String url,RequestParams rp) throws Exception{
		if(rp == null){
			rp = new RequestParams();
		}
		
		for(HttpInterceptHandler handler : XApplication.getManagers(HttpInterceptHandler.class)){
			final JSONObject jo = handler.onInterceptHandleHttp(this,event, url, rp);
			if(jo != null){
				return jo;
			}
		}
		
		final String fixUrl = addUrlCommonParams(event,url,rp);
		XApplication.getLogger().info(getClass().getName() + " execute url = " + fixUrl);
		final StringBuffer sb = new StringBuffer();
		final AsyncHttpClient client = getAsyncHttpClient(event, url, fixUrl,rp);
		checkAndSetWapProxy(client);
		client.get(fixUrl,rp, new ResponceHandlerWare(new TextHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				sb.append(responseString);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				if(responseString != null){
					sb.append(responseString);
				}
			}
			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				event.setProgress((totalSize > 0) ? (bytesWritten * 100 / totalSize) : 0);
			}
		}, event));
		return onHandleHttpRet(event,fixUrl,rp,sb.toString());
	}
	
	public final JSONObject doPost(final Event event,String url,
			RequestParams params) throws Exception{
		if(params == null){
			params = new RequestParams();
		}
		
		for(HttpInterceptHandler handler : XApplication.getManagers(HttpInterceptHandler.class)){
			final JSONObject jo = handler.onInterceptHandleHttp(this,event, url, params);
			if(jo != null){
				return jo;
			}
		}
		
		return internalPost(event, url, params);
	}
	
	public JSONObject internalPost(final Event event,String url,
			RequestParams params) throws Exception{
		final String fixUrl = addUrlCommonParams(event,url,params);
		XApplication.getLogger().info(getClass().getName() + " execute url = " + fixUrl + " post prams:" + params.toString());
		final StringBuffer sb = new StringBuffer();
		final AsyncHttpClient client = getAsyncHttpClient(event, url,fixUrl, params);
		checkAndSetWapProxy(client);
		client.post(fixUrl,params,new ResponceHandlerWare(new TextHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				sb.append(responseString);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				if(responseString != null){
					sb.append(responseString);
				}
				if(throwable != null){
					XApplication.getLogger().warning(SystemUtils.throwableToString(throwable));
                }
			}
			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				event.setProgress((totalSize > 0) ? (bytesWritten * 100 / totalSize) : 0);
			}
		}, event));
		return onHandleHttpRet(event,fixUrl, params,sb.toString());
	}
	
	protected AsyncHttpClient getAsyncHttpClient(Event event,String url,
			String fixUrl,
			RequestParams params){
		AsyncHttpClient client = null;
		for(HttpClientProvider p : XApplication.getManagers(HttpClientProvider.class)){
			client = p.buildHttpClient(event, url, fixUrl,params);
			break;
		}
		
		if(client == null){
			client = buildHttpClient();
		}
		return client;
	}
	
	@SuppressWarnings("deprecation")
	protected void checkAndSetWapProxy(AsyncHttpClient client){
		if(SystemUtils.isWapNet()){
			final String host = Proxy.getDefaultHost();
			final int port = Proxy.getPort(XApplication.getApplication());
			XApplication.getLogger().info("proxy host:" + host);
			if(TextUtils.isEmpty(host)){
				client.getHttpClient().getParams().removeParameter(ConnRouteParams.DEFAULT_PROXY);
			}else{
				client.setProxy(host, port);
			}
		}else{
			client.getHttpClient().getParams().removeParameter(ConnRouteParams.DEFAULT_PROXY);
		}
	}
	
	protected JSONObject onHandleHttpRet(Event event,String url,RequestParams params,String ret) throws Exception{
		try{
			XApplication.getLogger().info(getClass().getName() + " ret:" + ret);
			if(TextUtils.isEmpty(ret)){
				throw new StringIdException(R.string.toast_disconnect);
			}else{
				JSONObject jo = null;
				try{
					jo = new JSONObject(ret);
				}catch(Exception e){
					throw new StringIdException(R.string.toast_server_error);
				}
				if(jo != null){
					XApplication.updateServerTimeDifference(parseServerTime(jo));
					if(!checkRequestSuccess(jo)){
						event.addReturnParam(jo);
						onHandleXError(jo);
					}
				}
				for(HttpResultHandler handler : XApplication.getManagers(HttpResultHandler.class)){
					handler.onHandleHttpResult(event,url, params, ret, jo);
				}
				return jo;
			}
		}catch(Exception e){
			for(HttpResultErrorHandler handler : XApplication.getManagers(HttpResultErrorHandler.class)){
				handler.onHandleHttpResultError(event,url,params,ret,e);
			}
			throw e;
		}
	}
	
	protected void onHandleXError(JSONObject jo) throws Exception{
		throw new XHttpException(jo);
	}
	
	protected long	parseServerTime(JSONObject jo){
		try{
			return jo.getLong("servertime") * 1000;
		}catch(Exception e){
		}
		return System.currentTimeMillis();
	}

	protected String addUrlCommonParams(Event event,String url,RequestParams params) throws Exception{
		for(HttpCommonParamsIntercepter i : XApplication.getManagers(HttpCommonParamsIntercepter.class)){
			url = i.onInterceptAddCommonParams(event, url, params);
		}
		
		final String httpKey = XApplication.getHttpKey();
		final String strDeviceUUID = XApplication.getDeviceUUID(XApplication.getApplication());
		final long time = XApplication.getFixSystemTime();
		final String strTime = String.valueOf(time / 1000);
		params.add("device", "android");
		params.add("ver", SystemUtils.getVersionName(XApplication.getApplication()));
		params.add("deviceuuid", strDeviceUUID);
		
		params.add("timesign",strTime);
		params.add("width", String.valueOf(XApplication.getScreenWidth()));
		params.add("height", String.valueOf(XApplication.getScreenHeight()));
		params.add("dpi", String.valueOf(XApplication.getScreenDpi()));
		
		List<BasicNameValuePair>	pairs = params.getParamsList();
		pairs.add(new BasicNameValuePair("key", httpKey));
		Collections.sort(pairs, new Comparator<BasicNameValuePair>() {
			@Override
			public int compare(BasicNameValuePair lhs, BasicNameValuePair rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}
		});
		StringBuffer sb = new StringBuffer();
		for(BasicNameValuePair pair : pairs){
			final String value = pair.getValue();
			if(!TextUtils.isEmpty(value)){
				String s = Uri.encode(value);
				while(s.contains("(")){
					s = s.replace("(", "%28");
				}
				while(s.contains(")")){
					s = s.replace(")", "%29");
				}
				while(s.contains("\"")){
					s = s.replace("\"", "%22");
				}
				while(s.contains("!")){
					s = s.replace("!", "%21");
				}
				while(s.contains("'")){
					s = s.replace("'", "%27");
				}
				while(s.contains("*")){
					s = s.replace("*", "%2A");
				}
				sb.append(pair.getName()).append("=")
				.append(s)
				.append("&");
			}
		}
		params.add("sign", Encrypter.encryptByMD5(sb.substring(0, sb.length() - 1)));
		return url;
	}
	
	public static class ResponceHandlerWare implements ResponseHandlerInterface{

		private ResponseHandlerInterface 	mWrap;
				Event 						mEvent;
		
		public ResponceHandlerWare(ResponseHandlerInterface wrap,Event event){
			mWrap = wrap;
			mEvent = event;
		}
		
		@Override
		public void sendResponseMessage(HttpResponse response) throws IOException {
			mWrap.sendResponseMessage(response);
		}

		@Override
		public void sendStartMessage() {
			mWrap.sendStartMessage();
		}

		@Override
		public void sendFinishMessage() {
			mWrap.sendFinishMessage();
		}

		@Override
		public void sendProgressMessage(int bytesWritten, int bytesTotal) {
			mWrap.sendProgressMessage(bytesWritten, bytesTotal);
		}

		@Override
		public void sendCancelMessage() {
			mWrap.sendCancelMessage();
		}

		@Override
		public void sendSuccessMessage(int statusCode, Header[] headers, byte[] responseBody) {
			mWrap.sendSuccessMessage(statusCode, headers, responseBody);
		}

		@Override
		public void sendFailureMessage(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
			mWrap.sendFailureMessage(statusCode, headers, responseBody, error);
		}

		@Override
		public void sendRetryMessage(int retryNo) {
			mWrap.sendRetryMessage(retryNo);
		}

		@Override
		public URI getRequestURI() {
			return mWrap.getRequestURI();
		}

		@Override
		public Header[] getRequestHeaders() {
			return mWrap.getRequestHeaders();
		}

		@Override
		public void setRequestURI(URI requestURI) {
			mWrap.setRequestURI(requestURI);
		}

		@Override
		public void setRequestHeaders(Header[] requestHeaders) {
			mWrap.setRequestHeaders(requestHeaders);
		}

		@Override
		public void setUseSynchronousMode(boolean useSynchronousMode) {
			mWrap.setUseSynchronousMode(useSynchronousMode);
		}

		@Override
		public boolean getUseSynchronousMode() {
			return mWrap.getUseSynchronousMode();
		}
	}
}
