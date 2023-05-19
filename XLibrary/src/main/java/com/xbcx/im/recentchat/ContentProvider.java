package com.xbcx.im.recentchat;

import android.content.Context;

import com.xbcx.im.XMessage;

public interface ContentProvider {
	public String	getContent(Context context,XMessage xm);
}
