package com.xbcx.im.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.xbcx.core.Event;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.core.db.DBUtils;
import com.xbcx.core.db.DatabaseManager;
import com.xbcx.core.db.PublicDatabaseManager;

public abstract class DBBaseRunner implements OnEventRunner {

	protected List<Cursor> mListCursor;
	
	protected boolean mIsRead = true;
	
	protected void	managerCursor(Cursor cursor){
		if(cursor == null){
			return;
		}
		
		if(mListCursor == null){
			mListCursor = new ArrayList<Cursor>();
		}
		mListCursor.add(cursor);
	}
	
	protected void	requestExecute(boolean bRead,Event event){
		mIsRead = bRead;
		DatabaseManager dm = useIMDatabase() ? IMDatabaseManager.getInstance() :
												PublicDatabaseManager.getInstance();
		if(bRead){
			SQLiteDatabase db = dm.lockReadableDatabase();
			try{
				onExecute(db,event);
			}catch(Exception e){	
			}finally{
				dm.unlockReadableDatabase(db);
				closeCursor();
			}
		}else{
			SQLiteDatabase db = dm.lockWritableDatabase();
			try{
				onExecute(db,event);
			}finally{
				dm.unlockWritableDatabase(db);
				closeCursor();
			}
		}
	}
	
	protected Cursor simpleQuery(SQLiteDatabase db,String tableName,String whereColumn,String whereColumnValue){
		if(TextUtils.isEmpty(whereColumn) || TextUtils.isEmpty(whereColumnValue)){
			return simpleQuery(db, tableName);
		}
		Cursor c = db.query(tableName,null, 
					whereColumn + "='" + whereColumnValue + "'",
					null, null, null, null);
		managerCursor(c);
		return c;
	}
	
	protected Cursor simpleQuery(SQLiteDatabase db,String tableName){
		Cursor c = db.query(tableName,null, null,null, null, null, null);
		managerCursor(c);
		return c;
	}
	
	protected void	safeInsert(SQLiteDatabase db,String strTableName,ContentValues cv){
		long lRet = db.insert(strTableName, null, cv);
		if(lRet == -1){
			if(!tabbleIsExist(strTableName, db)){
				db.execSQL(createTableSql());
				db.insert(strTableName, null, cv);
			}
		}
	}
	
	protected void 	safeUpdate(SQLiteDatabase db,String tableName,ContentValues cv,String whereColumn,String whereColumnValue){
		try{
			int ret = db.update(tableName, cv, 
					whereColumn + "='" + whereColumnValue + "'",null);
			if(ret <= 0){
				cv.put(whereColumn, whereColumnValue);
				safeInsert(db, tableName, cv);
			}
		}catch(Exception e){
			if(!tabbleIsExist(tableName, db)){
				db.execSQL(createTableSql());
				cv.put(whereColumn, whereColumnValue);
				db.insert(tableName, null, cv);
			}
		}
	}
	
	protected abstract String createTableSql();
	
	protected boolean tabbleIsExist(String tableName,SQLiteDatabase db) {
		return DBUtils.tabbleIsExist(tableName, db);
	}
	
	protected void	closeCursor(){
		if(mListCursor != null){
			for(Cursor cursor : mListCursor){
				cursor.close();
			}
			mListCursor.clear();
		}
	}
	
	
	protected boolean useIMDatabase(){
		return false;
	}
	
	protected abstract void 	onExecute(SQLiteDatabase db,Event event);
}
