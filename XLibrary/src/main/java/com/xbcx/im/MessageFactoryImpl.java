package com.xbcx.im;

import android.database.Cursor;

public class MessageFactoryImpl implements XMessageFactory {

	@Override
	public XMessage createXMessage(String id, int type) {
		return new XMessage(id, type);
	}

	@Override
	public XMessage createXMessage(Cursor cursor) {
		return new XMessage(cursor);
	}

}
