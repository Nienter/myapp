package com.xbcx.im.db;

public class DBColumns {
	
	public static class Folder{
		public static final String TABLENAME 		= "folder";
		public static final String COLUMN_PATH 		= "path";
		public static final String COLUMN_NAME 		= "name";
		public static final String COLUMN_FILETYPE	= "filetype";
		public static final String COLUMN_FROMSELF	= "fromself";
		public static final String COLUMN_TIME		= "time";
	}
	
	public static class Message{
		public static final String COLUMN_AUTOID 	= "autoid";
		public static final String COLUMN_ID 		= "messageid";
		public static final String COLUMN_TYPE 		= "messagetype";
		public static final String COLUMN_USERID	= "userid";
		public static final String COLUMN_USERNAME	= "username";
		public static final String COLUMN_CONTENT	= "content";
		public static final String COLUMN_FROMSELF	= "fromself";
		public static final String COLUMN_SENDTIME	= "sendtime";
		public static final String COLUMN_EXTENSION	= "extension";
		public static final String COLUMN_URL		= "url";
		public static final String COLUMN_SIZE		= "size";
		public static final String COLUMN_BUBBLEID	= "bubbleid";
		public static final String COLUMN_DISPLAY 	= "display";
		public static final String COLUMN_EXTSTRING	= "extstring";
		public static final String COLUMN_EXTOBJ	= "extobj";
	}
	
	public static class RecentChatDB{
		public static final String TABLENAME			= "recentchat";
		public static final String COLUMN_ID			= "userid";
		public static final String COLUMN_NAME			= "name";
		public static final String COLUMN_CONTENT		= "content";
		public static final String COLUMN_ACTIVITY_TYPE = "activitytype";
		public static final String COLUMN_UNREADCOUNT	= "unreadcount";
		public static final String COLUMN_EXTRAOBJ		= "extraobj";
		public static final String COLUMN_UPDATETIME	= "updatetime";
	}
}
