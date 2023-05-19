package com.xbcx.im;

import android.database.Cursor;

public interface XMessageFactory {
	
	public XMessage createXMessage(String id,int type);
	
	public XMessage createXMessage(Cursor cursor);
}
