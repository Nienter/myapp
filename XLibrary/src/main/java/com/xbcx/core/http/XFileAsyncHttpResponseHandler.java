package com.xbcx.core.http;

import java.io.File;

import org.apache.http.Header;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.xbcx.utils.FileHelper;

public abstract class XFileAsyncHttpResponseHandler extends FileAsyncHttpResponseHandler {

	private File	mOriFile;
	
	public XFileAsyncHttpResponseHandler(File file) {
		super(new File(file.getPath() + ".xtemp"));
		mOriFile = file;
		FileHelper.checkOrCreateDirectory(file.getPath());
	}

	@Override
	public final void onSuccess(int statusCode, Header[] headers, File file) {
		if(!file.renameTo(mOriFile)){
			if(mOriFile.exists()){
				mOriFile.delete();
				file.renameTo(mOriFile);
			}
		}
		onXSuccess(statusCode, headers, file);
	}
	
	public abstract void onXSuccess(int statusCode,Header[] headers,File file);
}
