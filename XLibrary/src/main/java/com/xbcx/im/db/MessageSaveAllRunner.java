package com.xbcx.im.db;

import java.util.Collection;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.xbcx.core.Event;
import com.xbcx.im.XMessage;

public class MessageSaveAllRunner extends MessageSaveRunner {

	@SuppressWarnings("unchecked")
	@Override
	protected void onExecute(SQLiteDatabase db, Event event) {
		final String id = (String)event.getParamAtIndex(0);
		final Collection<XMessage> xms = event.findParam(Collection.class);
		final String strTableName = getTableName(id);
		if(!tabbleIsExist(strTableName, db)){
			db.execSQL(createTableSql(strTableName));
		}
		db.beginTransaction();
		try{
			for(XMessage xm : xms){
				final ContentValues cv = xm.getSaveContentValues();
				try{
					db.insertOrThrow(strTableName, null, cv);
				}catch(Exception e){
				}
			}
			db.setTransactionSuccessful();
		}finally{
			db.endTransaction();
		}
	}
}
