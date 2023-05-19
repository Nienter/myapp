package com.xbcx.im.ui;

import android.app.Activity;
import android.content.Intent;

public abstract class SendPlugin{
	
	public static final int	SENDTYPE_PHOTO_ALL		 	= 1;
	public static final int	SENDTYPE_PHOTO_ALBUMS 		= 2;
	public static final int	SENDTYPE_PHOTO_CAMERA 		= 3;
	
	private final 	String	mId;
	private 		String	mName;
	private 		int		mIcon;
	
	private 		int		mRequestCode;
	
	private			int		mSortKey;
	
	public SendPlugin(String id,int icon){
		this(id,id,icon);
	}
	
	public SendPlugin(String id,String name,int icon){
		mId = id;
		mName = name;
		mIcon = icon;
	}
	
	public String getId(){
		return mId;
	}
	
	public String getName(){
		return mName;
	}
	
	public int getIcon(){
		return mIcon;
	}
	
	public SendPlugin setSortKey(int sortKey){
		mSortKey = sortKey;
		return this;
	}
	
	public int getSortKey(){
		return mSortKey;
	}
	
	public abstract void onSend(ChatActivity activity);
	
	public void activityResult(ChatActivity activity,int requestCode, int resultCode, Intent data){
		if(resultCode == Activity.RESULT_OK){
			onActivityResult(activity,data);
		}
	}
	
	protected void onActivityResult(ChatActivity activity,Intent data){
		
	}
	
	public boolean	useActivityResult(){
		return false;
	}
	
	public void 	setRequestCode(int code){
		mRequestCode = code;
	}
	
	public int		getRequestCode(){
		return mRequestCode;
	}
	
	public int		getSendType(){
		return 0;
	}
}
