package com.xbcx.im.message.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.XApplication;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.core.module.AppBaseListener;
import com.xbcx.im.XMessage;
import com.xbcx.im.db.DBBaseRunner;
import com.xbcx.im.db.DBColumns;

public class FolderManager implements AppBaseListener,OnEventListener{
	
	/**
	 * @param FileItem
     */
	public static final int DB_SaveToFolder;
	
	/**
	 * @param List(FileItem)
     */
	public static final int DB_ReadFolder;
	
	/**
	 * @param Collection(FileItem)
     */
	public static final int DB_DeleteFileItem;
	
	public static FolderManager getInstance(){
		return sInstance;
	}
	
	static{
		DB_SaveToFolder			= EventCode.generateEventCode();
		DB_ReadFolder			= EventCode.generateEventCode();
		DB_DeleteFileItem		= EventCode.generateEventCode();
		sInstance = new FolderManager();
	}
	
	private static FolderManager sInstance;
	
	private static AndroidEventManager mEventManager = AndroidEventManager.getInstance();
	
	protected FolderManager(){
		XApplication.addManager(this);
		
		mEventManager.registerEventRunner(
				DB_SaveToFolder, new SaveToFolderRunner());
		mEventManager.registerEventRunner(
				DB_ReadFolder,new ReadFolderRunner());
		mEventManager.registerEventRunner(
				DB_DeleteFileItem, new DeleteFileItemRunner());
		
		mEventManager.addEventListener(EventCode.DownloadMessageFile, this);
		mEventManager.addEventListener(EventCode.IM_SendMessage, this);
	}
	
	@Override
	public void onEventRunEnd(Event event) {
		final int code = event.getEventCode();
		if(code == EventCode.DownloadMessageFile){
			if(event.isSuccess()){
				final XMessage xm = (XMessage)event.getParamAtIndex(0);
				
				final int type = xm.getType();
				if(type == XMessage.TYPE_FILE){
					mEventManager.pushEvent(DB_SaveToFolder, 
							new FileItem(xm.getFilePath(),xm.getDisplayName(),
									FileItem.FILETYPE_OTHER,false,xm.getSendTime()));
				}else if(type == XMessage.TYPE_PHOTO){
					mEventManager.pushEvent(DB_SaveToFolder, 
							new FileItem(xm.getFilePath(),xm.getDisplayName(),
									FileItem.FILETYPE_PIC,false,xm.getSendTime()));
				}else if(type == XMessage.TYPE_VIDEO){
					mEventManager.pushEvent(DB_SaveToFolder, 
							new FileItem(xm.getVideoFilePath(),xm.getDisplayName(),
									FileItem.FILETYPE_VIDEO,false,xm.getSendTime()));
				}
			}
		}else if(code == EventCode.IM_SendMessage){
			final XMessage xm = (XMessage)event.getParamAtIndex(0);
			final int msgType = xm.getType();
			if(msgType == XMessage.TYPE_FILE){
				AndroidEventManager.getInstance().pushEvent(DB_SaveToFolder, 
						new FileItem(xm.getFilePath(),xm.getDisplayName(),
								FileItem.FILETYPE_OTHER,true,xm.getSendTime()));
			}else if(msgType == XMessage.TYPE_PHOTO){
				AndroidEventManager.getInstance().pushEvent(DB_SaveToFolder, 
						new FileItem(xm.getFilePath(),xm.getDisplayName(),
								FileItem.FILETYPE_PIC,true,xm.getSendTime()));
			}else if(msgType == XMessage.TYPE_VIDEO){
				AndroidEventManager.getInstance().pushEvent(DB_SaveToFolder, 
						new FileItem(xm.getVideoFilePath(),xm.getDisplayName(),
								FileItem.FILETYPE_VIDEO,true,xm.getSendTime()));
			}
		}
	}
	
	protected String getCreateTableSql(){
		return "CREATE TABLE " + DBColumns.Folder.TABLENAME + " (" +
				DBColumns.Folder.COLUMN_PATH + " TEXT PRIMARY KEY, " +
				DBColumns.Folder.COLUMN_NAME + " TEXT, " +
				DBColumns.Folder.COLUMN_FROMSELF + " INTEGER, " +
				DBColumns.Folder.COLUMN_TIME + " INTEGER, " +
				DBColumns.Folder.COLUMN_FILETYPE + " INTEGER);";
	}
	
	private class SaveToFolderRunner extends DBBaseRunner{

		@Override
		public void onEventRun(Event event) throws Exception {
			requestExecute(false, event);
		}

		@Override
		protected String createTableSql() {
			return getCreateTableSql();
		}

		@Override
		protected boolean useIMDatabase() {
			return true;
		}

		@Override
		protected void onExecute(SQLiteDatabase db, Event event) {
			final FileItem fi = (FileItem)event.getParamAtIndex(0);
			ContentValues cv = new ContentValues();
			cv.put(DBColumns.Folder.COLUMN_PATH, fi.getPath());
			cv.put(DBColumns.Folder.COLUMN_NAME, fi.getName());
			cv.put(DBColumns.Folder.COLUMN_FILETYPE, fi.getFileType());
			cv.put(DBColumns.Folder.COLUMN_FROMSELF, fi.isFromSelf());
			cv.put(DBColumns.Folder.COLUMN_TIME, fi.getTime());
			safeInsert(db, DBColumns.Folder.TABLENAME, cv);
		}
	}
	
	private class ReadFolderRunner extends DBBaseRunner{

		@Override
		public void onEventRun(Event event) throws Exception {
			requestExecute(false, event);
		}

		@Override
		protected String createTableSql() {
			return getCreateTableSql();
		}
		
		@Override
		protected boolean useIMDatabase() {
			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onExecute(SQLiteDatabase db, Event event) {
			List<FileItem> fileItems = (List<FileItem>)event.getParamAtIndex(0);
			Cursor cursor = db.query(DBColumns.Folder.TABLENAME, 
					null, null, null, null, null, null);
			managerCursor(cursor);
			if(cursor != null && cursor.moveToFirst()){
				List<String> deletePaths = new ArrayList<String>();
				do{
					final String path = cursor.getString(cursor.getColumnIndex(DBColumns.Folder.COLUMN_PATH));
					File file = new File(path);
					if(file.exists()){
						fileItems.add(new FileItem(
								path,
								cursor.getString(cursor.getColumnIndex(DBColumns.Folder.COLUMN_NAME)),
								cursor.getInt(cursor.getColumnIndex(DBColumns.Folder.COLUMN_FILETYPE)),
								cursor.getInt(cursor.getColumnIndex(DBColumns.Folder.COLUMN_FROMSELF)) != 0,
								cursor.getLong(cursor.getColumnIndex(DBColumns.Folder.COLUMN_TIME))));
					}else{
						deletePaths.add(path);
					}
				}while(cursor.moveToNext());
				if(deletePaths.size() > 0){
					db.beginTransaction();
					try{
						for(String path : deletePaths){
							db.delete(DBColumns.Folder.TABLENAME,
									DBColumns.Folder.COLUMN_PATH + "='" + path + "'" ,
									null);
						}
						db.setTransactionSuccessful();
					}finally{
						db.endTransaction();
					}
				}
			}
		}
	}
	
	private static class DeleteFileItemRunner extends DBBaseRunner{

		@Override
		public void onEventRun(Event event) throws Exception {
			requestExecute(false, event);
		}
		
		@Override
		protected boolean useIMDatabase() {
			return true;
		}

		@Override
		protected String createTableSql() {
			return "CREATE TABLE " + DBColumns.Folder.TABLENAME + " (" +
					DBColumns.Folder.COLUMN_PATH + " TEXT PRIMARY KEY, " +
					DBColumns.Folder.COLUMN_NAME + " TEXT, " +
					DBColumns.Folder.COLUMN_FILETYPE + " INTEGER);";
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onExecute(SQLiteDatabase db, Event event) {
			final Collection<FileItem> fileItems = (Collection<FileItem>)event.getParamAtIndex(0);
			if(fileItems.size() > 0){
				db.beginTransaction();
				try{
					for(FileItem fi : fileItems){
						db.delete(DBColumns.Folder.TABLENAME, 
								DBColumns.Folder.COLUMN_PATH + "='" + fi.getPath() + "'",
								null);
					}
					db.setTransactionSuccessful();
				}finally{
					db.endTransaction();
				}
			}
		} 
	}
}
