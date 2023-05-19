package com.xbcx.core.db;

import android.database.Cursor;

public interface XDBObjectCreator<T> {
	public T createObject(Cursor c);
}
