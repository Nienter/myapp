package com.xbcx.im.db;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.xbcx.core.Event;

public class RecentChatDeleteRunner extends RecentChatSaveRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		requestExecute(false, event);
	}

	@Override
	protected void onExecute(SQLiteDatabase db, Event event) {
		final String id = (String)event.getParamAtIndex(0);
		if(TextUtils.isEmpty(id)){
			db.delete(DBColumns.RecentChatDB.TABLENAME, null, null);
		}else{
			db.delete(DBColumns.RecentChatDB.TABLENAME,
					DBColumns.RecentChatDB.COLUMN_ID + "='" + id + "'", null);
		}
	}

}
