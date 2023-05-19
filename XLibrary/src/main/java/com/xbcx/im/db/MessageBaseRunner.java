package com.xbcx.im.db;

import android.text.TextUtils;


public abstract class MessageBaseRunner extends DBBaseRunner {
	
	@Override
	protected String createTableSql() {
		return null;
	}

	protected String createTableSql(String strTableName){
		return "CREATE TABLE " + "[" + strTableName + "] (" + 
				DBColumns.Message.COLUMN_AUTOID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				DBColumns.Message.COLUMN_ID + " TEXT UNIQUE, " +
				DBColumns.Message.COLUMN_TYPE + " INTEGER, " + 
				DBColumns.Message.COLUMN_USERID + " TEXT, " +
				DBColumns.Message.COLUMN_USERNAME + " TEXT, " +
				DBColumns.Message.COLUMN_CONTENT + " TEXT, " +
				DBColumns.Message.COLUMN_FROMSELF + " INTEGER, " +
				DBColumns.Message.COLUMN_SENDTIME + " INTEGER, " +
				DBColumns.Message.COLUMN_EXTENSION + " INTEGER, " +
				DBColumns.Message.COLUMN_URL + " TEXT, " +
				DBColumns.Message.COLUMN_SIZE + " INTEGER, " +
				DBColumns.Message.COLUMN_BUBBLEID + " TEXT, " +
				DBColumns.Message.COLUMN_DISPLAY + " TEXT, " +
				DBColumns.Message.COLUMN_EXTSTRING + " TEXT, " +
				DBColumns.Message.COLUMN_EXTOBJ + " BLOB);";
	}
	
	public static String getTableName(String fromId){
		if(TextUtils.isEmpty(fromId)){
			return null;
		}
		return "msg" + fromId;
	}
	
	public static String parseIdByTableName(String name){
		if(TextUtils.isEmpty(name)){
			return "";
		}
		final int pos = name.indexOf("msg");
		if(pos >= 0){
			return name.substring(pos + 3);
		}
		return "";
	}
	
	protected boolean useIMDatabase(){
		return true;
	}
}
