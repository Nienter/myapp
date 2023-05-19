package com.xbcx.im.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.xbcx.core.Event;
import com.xbcx.im.IMFilePathManager;
import com.xbcx.utils.FileHelper;

public class MessageDeleteRunner extends MessageBaseRunner {

	@Override
	public void onEventRun(Event event) throws Exception {
		requestExecute(false, event);
	}

	@Override
	protected void onExecute(SQLiteDatabase db, Event event) {
		final String id = (String)event.getParamAtIndex(0);
		final String msgId = (String)event.getParamAtIndex(1);
		if(TextUtils.isEmpty(id)){
			final String sql = "select name from Sqlite_master where type ='table' and name like 'msg%'";
			Cursor cursor = db.rawQuery(sql, null);
			managerCursor(cursor);
			if(cursor != null && cursor.moveToFirst()){
				do{
					final String tableName = cursor.getString(0);
					db.delete(tableName, null, null);
				}while(cursor.moveToNext());
			}
			
			FileHelper.deleteFolder(IMFilePathManager.getInstance().getMessageFolderPath());
		}else{
			if(TextUtils.isEmpty(msgId)){
				db.delete(getTableName(id), null, null);
				FileHelper.deleteFolder(IMFilePathManager.getInstance().getMessageFolderPath(id));
			}else{
				db.delete(getTableName(id),
						DBColumns.Message.COLUMN_ID + "='" + msgId + "'" , 
						null);
			}
		}
	}

}
