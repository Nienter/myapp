package com.xbcx.im.db;

import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xbcx.core.Event;
import com.xbcx.im.recentchat.RecentChat;

public class RecentChatReadRunner extends RecentChatSaveRunner {

	private List<RecentChat> mRecentChats;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onEventRun(Event event) throws Exception {
		mRecentChats = (List<RecentChat>)event.getParamAtIndex(0);
		requestExecute(true,event);
	}

	@Override
	protected void onExecute(SQLiteDatabase db,Event event) {
		try{
			Cursor cursor = db.query(DBColumns.RecentChatDB.TABLENAME,
					null, null, null, null, null, 
					DBColumns.RecentChatDB.COLUMN_UPDATETIME + " DESC");
			managerCursor(cursor);
			if(cursor != null && cursor.moveToFirst()){
				do{
					mRecentChats.add(new RecentChat(cursor));
				}while(cursor.moveToNext());
			}
		}catch(Exception e){
		}
	}
}
