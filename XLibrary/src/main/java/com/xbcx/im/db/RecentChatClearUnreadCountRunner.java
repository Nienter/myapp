package com.xbcx.im.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.xbcx.core.Event;

public class RecentChatClearUnreadCountRunner extends RecentChatSaveRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		super.onEventRun(event);
	}

	@Override
	protected void onExecute(SQLiteDatabase db, Event event) {
		final String id = (String)event.getParamAtIndex(0);
		ContentValues cv = new ContentValues();
		cv.put(DBColumns.RecentChatDB.COLUMN_UNREADCOUNT, 0);
		if(TextUtils.isEmpty(id)){
			db.update(DBColumns.RecentChatDB.TABLENAME, cv, null, null);
		}else{
			db.update(DBColumns.RecentChatDB.TABLENAME, cv, 
					DBColumns.RecentChatDB.COLUMN_ID + "='" + id + "'", null);
		}
	}
	
}
