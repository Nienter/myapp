package com.xbcx.core.http;

import com.xbcx.core.Event;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.core.XApplication;

import org.apache.http.Header;

import java.io.File;

public class HttpDownloadRunner implements OnEventRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		final String url = (String)event.getParamAtIndex(0);
		final String filePath = (String)event.getParamAtIndex(1);
		XApplication.getLogger().info("download:url" + url + " path:" + filePath);
		final Event e = event;
		XHttpRunner.buildHttpClient().get(url,new XHttpRunner.ResponceHandlerWare(new XFileAsyncHttpResponseHandler(new File(filePath)){
			@Override
			public void onXSuccess(int statusCode, Header[] headers, File file) {
				e.setSuccess(true);
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
				e.setFailException(new Exception(throwable));
			}
			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				e.setProgress((totalSize > 0) ? (int) ((long) bytesWritten * 100L / totalSize) : 0);
			}
		},e));
		XApplication.getLogger().info("download success:" + (e.getFailMessage() == null) + " url" + url + " path:" + filePath);
		if(e.getFailException() != null){
			throw e.getFailException();
		}
	}
}
