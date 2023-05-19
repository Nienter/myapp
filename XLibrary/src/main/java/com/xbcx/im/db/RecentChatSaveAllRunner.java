package com.xbcx.im.db;

import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.xbcx.core.Event;
import com.xbcx.core.db.DBUtils;
import com.xbcx.im.recentchat.RecentChat;
import com.xbcx.utils.SystemUtils;

public class RecentChatSaveAllRunner extends RecentChatSaveRunner {
	@SuppressWarnings("unchecked")
	@Override
	protected void onExecute(SQLiteDatabase db, Event event) {
		List<RecentChat> rcs = (List<RecentChat>)event.getParamAtIndex(0);
		if(!DBUtils.tabbleIsExist(DBColumns.RecentChatDB.TABLENAME, db)){
			db.execSQL(createTableSql());
		}
		db.beginTransaction();
		try{
			for(RecentChat rc : rcs){
				final ContentValues cv = new ContentValues();
				cv.put(DBColumns.RecentChatDB.COLUMN_ID, rc.getId());
				cv.put(DBColumns.RecentChatDB.COLUMN_NAME, rc.getName());
				cv.put(DBColumns.RecentChatDB.COLUMN_CONTENT, rc.getContent());
				cv.put(DBColumns.RecentChatDB.COLUMN_ACTIVITY_TYPE, rc.getActivityType());
				cv.put(DBColumns.RecentChatDB.COLUMN_UPDATETIME, rc.getTime());
				if(rc.isExtraObjChanged()){
					if(rc.getExtraObj() != null){
						try{
							cv.put(DBColumns.RecentChatDB.COLUMN_EXTRAOBJ,SystemUtils.objectToByteArray(rc.getExtraObj()));
						}catch(Exception e){
							e.printStackTrace();
						}finally{
							rc.setExtraObjChanged(false);
						}
					}
				}
				db.insert(DBColumns.RecentChatDB.TABLENAME, null, cv);
			}
			db.setTransactionSuccessful();
		}finally{
			db.endTransaction();
		}
	}
}
