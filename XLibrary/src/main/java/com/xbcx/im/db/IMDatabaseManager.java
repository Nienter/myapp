package com.xbcx.im.db;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.EventCode;
import com.xbcx.core.XApplication;
import com.xbcx.core.db.DatabaseManager;
import com.xbcx.im.IMKernel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

public class IMDatabaseManager extends DatabaseManager{
	
	public static IMDatabaseManager getInstance(){
		return sInstance;
	}
	
	static{
		sInstance = new IMDatabaseManager();
	}
	
	private static IMDatabaseManager sInstance;
	
	private IMDatabaseManager(){
		AndroidEventManager eventManager = AndroidEventManager.getInstance();
		eventManager.registerEventRunner(EventCode.DB_SaveMessage, new MessageSaveRunner());
		eventManager.registerEventRunner(EventCode.DB_DeleteMessage, new MessageDeleteRunner());
		eventManager.registerEventRunner(EventCode.DB_ReadRecentChat, new RecentChatReadRunner());
		eventManager.registerEventRunner(EventCode.DB_SaveRecentChat, new RecentChatSaveRunner());
		eventManager.registerEventRunner(EventCode.DB_DeleteRecentChat, new RecentChatDeleteRunner());
		eventManager.registerEventRunner(EventCode.DB_ClearRecentChatUnread, new RecentChatClearUnreadCountRunner());
		eventManager.registerEventRunner(EventCode.DB_SaveAllRecentChat, new RecentChatSaveAllRunner());
		eventManager.registerEventRunner(EventCode.DB_SaveAllMessage, new MessageSaveAllRunner());
		eventManager.registerEventRunner(EventCode.DB_ReplaceAllRecentChat, new RecentChatReplaceAllRunner());
	}
	
	public void initial(String uid){
		release();
	}
	
	@Override
	protected SQLiteOpenHelper onInitDBHelper() {
		final String user = IMKernel.getLocalUser();
		if(TextUtils.isEmpty(user)){
			return null;
		}
		return new DBUserHelper(XApplication.getApplication(), user);
	}
	
	private static class DBUserHelper extends SQLiteOpenHelper{

		private static final int DB_VERSION = 1;
		
		public DBUserHelper(Context context, String name) {
			super(context, name, null, DB_VERSION);
			XApplication.getLogger().info("DB Name:" + name);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}
}
