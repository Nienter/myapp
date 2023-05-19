package com.xbcx.core.bmp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

import android.content.Context;
import android.net.Proxy;
import android.net.Uri;
import android.text.TextUtils;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.xbcx.core.XApplication;
import com.xbcx.utils.SystemUtils;

public class XImageDownloader extends BaseImageDownloader {

	public XImageDownloader(Context context) {
		super(context);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
		String encodedUrl = Uri.encode(url, ALLOWED_URI_CHARS);
		HttpURLConnection conn = null;
		if(SystemUtils.isWapNet()){
			final String host = Proxy.getDefaultHost();
			final int port = Proxy.getPort(XApplication.getApplication());
			if(TextUtils.isEmpty(host)){
				conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
			}else{
				conn = (HttpURLConnection) new URL(encodedUrl).openConnection(new java.net.Proxy(
						java.net.Proxy.Type.HTTP, new InetSocketAddress(host, port)));
			}
		}else{
			conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
		}
		conn.setConnectTimeout(connectTimeout);
		conn.setReadTimeout(readTimeout);
		return conn;
	}
}
