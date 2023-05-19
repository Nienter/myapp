package com.xbcx.im;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.xbcx.common.voice.VoicePath;
import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.EventCode;
import com.xbcx.core.XApplication;
import com.xbcx.im.db.DBColumns;
import com.xbcx.im.messageprocessor.MessageDownloadProcessor;
import com.xbcx.im.messageprocessor.MessageUploadProcessor;
import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.utils.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

public class XMessage implements VoicePath,Serializable{
	private static final long serialVersionUID = 1L;
	
	public static final int TYPE_UNKNOW		= -1;
	public static final int TYPE_TIME 		= 0;
	public static final int TYPE_TEXT 		= 1;
	public static final int TYPE_VOICE 		= 2;
	public static final int TYPE_PHOTO 		= 3;
	public static final int TYPE_VIDEO		= 4;
	public static final int TYPE_FILE		= 5;
	public static final int TYPE_LOCATION	= 6;
	public static final int TYPE_IMGTEXT	= 7;
	public static final int TYPE_PROMPT		= 10;
	
	public static final int FROMTYPE_SINGLE 	= 1;
	public static final int FROMTYPE_GROUP 		= 2;
	public static final int FROMTYPE_DISCUSSION = 3;
	public static final int FROMTYPE_CHATROOM	= 4;
	
	protected static final int EXTENSION_COUNT = 8;
	
	protected static final int EXTENSION_SENDED 			= 0;
	protected static final int EXTENSION_SENDSUCCESS 		= 1;
	protected static final int EXTENSION_DOWNLOADED 		= 2;
	protected static final int EXTENSION_UPLOADSUCCESS 		= 3;
	protected static final int EXTENSION_PLAYED 			= 4;
	protected static final int EXTENSION_FRIENDASK_HANDLED 	= 5;
	
	protected String 		mId;
	protected int			mType;
	
	protected String		mUserId;
	protected String		mUserName;
	
	protected String		mContent;
	
	protected boolean		mIsFromSelf;
	
	protected int			mFromType;
	
	protected long			mSendTime;
	
	protected String		mGroupId;
	protected String		mGroupName;
	
	protected boolean		mExtension[] = new boolean[EXTENSION_COUNT];
	
	protected String		mUrl;
	protected long			mSize;
	protected String		mBubbleId;
	protected String		mDisplayName;//类型为ImgText的时候代表subType
	
	protected String		mExtString;//类型为ImgText的时候代表value
	protected Object		mExtObj;
	
	protected boolean		mReaded;
	
	protected ContentValues	mContentValues = new ContentValues();
	protected boolean 		mStoraged;

    protected boolean       mDelay;
	
	public XMessage(String strId,int nType){
		mId = strId;
		mType = nType;
		
		if(mId == null){
			throw new IllegalArgumentException("id can't be null");
		}
		
		mContentValues.put(DBColumns.Message.COLUMN_ID, strId);
		mContentValues.put(DBColumns.Message.COLUMN_TYPE, nType);
	}
	
	public XMessage(Cursor cursor){
		mId = cursor.getString(cursor.getColumnIndex(DBColumns.Message.COLUMN_ID));
		mType = cursor.getInt(cursor.getColumnIndex(DBColumns.Message.COLUMN_TYPE));
		mUserId = cursor.getString(cursor.getColumnIndex(DBColumns.Message.COLUMN_USERID));
		mUserName = cursor.getString(cursor.getColumnIndex(DBColumns.Message.COLUMN_USERNAME));
		mContent = cursor.getString(cursor.getColumnIndex(DBColumns.Message.COLUMN_CONTENT));
		mIsFromSelf = SystemUtils.getCursorBoolean(cursor, 
				cursor.getColumnIndex(DBColumns.Message.COLUMN_FROMSELF));
		mSendTime = cursor.getLong(cursor.getColumnIndex(DBColumns.Message.COLUMN_SENDTIME));
		final int nExtension = cursor.getInt(cursor.getColumnIndex(DBColumns.Message.COLUMN_EXTENSION));
		setExtension(nExtension);
		mUrl = cursor.getString(cursor.getColumnIndex(DBColumns.Message.COLUMN_URL));
		mSize = cursor.getLong(cursor.getColumnIndex(DBColumns.Message.COLUMN_SIZE));
		mBubbleId = cursor.getString(cursor.getColumnIndex(DBColumns.Message.COLUMN_BUBBLEID));
		mDisplayName = cursor.getString(cursor.getColumnIndex(DBColumns.Message.COLUMN_DISPLAY));
		mExtString = cursor.getString(cursor.getColumnIndex(DBColumns.Message.COLUMN_EXTSTRING));
		try{
			mExtObj = SystemUtils.byteArrayToObject(
					cursor.getBlob(
							cursor.getColumnIndex(DBColumns.Message.COLUMN_EXTOBJ)));
		}catch(Exception e){
			e.printStackTrace();
		}
		
		setStoraged();
	}
	
	public static String buildMessageId(){
		return UUID.randomUUID().toString();
	}
	
	public static XMessage createTimeMessage(long sendTime){
		XMessage m = IMGlobalSetting.msgFactory.createXMessage(
				XMessage.buildMessageId(), TYPE_TIME);
		m.mSendTime = sendTime;
		return m;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o){
			return true;
		}
		if(o != null && o instanceof XMessage){
			return ((XMessage)o).getId().equals(getId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return mId.hashCode();
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(mId);
		out.writeInt(mType);
		out.writeObject(mUserId);
		out.writeObject(mUserName);
		out.writeObject(mContent);
		out.writeBoolean(mIsFromSelf);
		out.writeInt(mFromType);
		out.writeLong(mSendTime);
		out.writeObject(mGroupId);
		out.writeObject(mGroupName);
		out.writeInt(getExtension());
		out.writeObject(mExtObj);
		out.writeObject(mExtString);
		out.writeBoolean(mReaded);
        out.writeObject(mDelay);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		mId = (String)in.readObject();
		mType = in.readInt();
		mContentValues = new ContentValues();
		mContentValues.put(DBColumns.Message.COLUMN_ID, mId);
		mContentValues.put(DBColumns.Message.COLUMN_TYPE, mType);
		setUserId((String)in.readObject());
		setUserName((String)in.readObject());
		setContent((String)in.readObject());
		setFromSelf(in.readBoolean());
		setFromType(in.readInt());
		setSendTime(in.readLong());
		setGroupId((String)in.readObject());
		setGroupName((String)in.readObject());
		mExtension = new boolean[EXTENSION_COUNT];
		setExtension(in.readInt());
		setExtObj(in.readObject());
		setExtString((String)in.readObject());
		setReaded(in.readBoolean());
	}
	
	public XMessage copy(){
		XMessage copy = IMGlobalSetting.msgFactory.createXMessage(XMessage.buildMessageId(), getType());
		copy.setContent(getContent());
		copy.setUserId(getUserId());
		copy.setUserName(getUserName());
		copy.setFromType(getFromType());
		copy.setFromSelf(isFromSelf());
		copy.setSendTime(XApplication.getFixSystemTime());
		copy.setExtension(getExtension());
		copy.setUrl(getUrl());
		copy.setSize(getFileSize());
		copy.setBubbleId(getBubbleId());
		copy.setDisplayName(getDisplayName());
		copy.setExtString(getExtString());
		copy.setExtObj(getExtObj());
		return copy;
	}

	public String 	getId() {
		return mId;
	}

	public int 		getType() {
		return mType;
	}
	
	public String	getUserId(){
		return mUserId;
	}
	
	public String	getUserName(){
		return mUserName;
	}
	
	public String	getContent(){
		return mContent;
	}

	public boolean	isFromSelf(){
		return mIsFromSelf;
	}
	
	public int		getFromType(){
		return mFromType;
	}
	
	public long		getSendTime(){
		return mSendTime;
	}
	
	public String	getGroupId(){
		return mGroupId;
	}
	
	public String	getGroupName(){
		return mGroupName;
	}
	
	public boolean	isFromGroup(){
		return mGroupId != null;
	}
	
	public String	getOtherSideId(){
		if(isFromGroup()){
			return getGroupId();
		}else{
			return getUserId();
		}
	}
	
	public void		setUserId(String strUserId){
		mUserId = strUserId;
		mContentValues.put(DBColumns.Message.COLUMN_USERID, strUserId);
	}
	
	public void 	setUserName(String strUserName){
		mUserName = strUserName;
		mContentValues.put(DBColumns.Message.COLUMN_USERNAME, strUserName);
	}
	
	public void		setContent(String strContent){
		mContent = strContent;
		mContentValues.put(DBColumns.Message.COLUMN_CONTENT, strContent);
	}
	
	public void		setFromSelf(boolean bFromSelf){
		mIsFromSelf = bFromSelf;
		mContentValues.put(DBColumns.Message.COLUMN_FROMSELF, bFromSelf);
	}
	
	public void		setFromType(int nFromType){
		mFromType = nFromType;
	}
	
	public void		setSendTime(long lTime){
		mSendTime = lTime;
		mContentValues.put(DBColumns.Message.COLUMN_SENDTIME, lTime);
	}
	
	public void 	setGroupId(String strGroupId){
		mGroupId = strGroupId;
	}

	public void     setDelay(boolean isDelay){mDelay = isDelay;}
	
	public void		setGroupName(String name){
		mGroupName = name;
	}
	
	public boolean	isSended(){
		return mExtension[EXTENSION_SENDED];
	}

	public boolean  isDelay(){return mDelay;}

	public boolean 	isSendSuccess(){
		return mExtension[EXTENSION_SENDSUCCESS];
	}
	
	public boolean 	isDownloaded(){
		return mExtension[EXTENSION_DOWNLOADED];
	}
	
	public boolean 	isPlayed(){
		return mExtension[EXTENSION_PLAYED];
	}
	
	public boolean 	isUploadSuccess(){
		return mExtension[EXTENSION_UPLOADSUCCESS];
	}
	
	public void setSended(){
		setExtension(EXTENSION_SENDED,true);
	}
	
	public void setSendSuccess(boolean bSuccess) {
		setExtension(EXTENSION_SENDSUCCESS, bSuccess);
	}

	public void setDownloaded() {
		setExtension(EXTENSION_DOWNLOADED, true);
	}

	public void setUploadSuccess(boolean bSuccess) {
		setExtension(EXTENSION_UPLOADSUCCESS, bSuccess);
	}

	public void setPlayed(boolean bPlayed) {
		setExtension(EXTENSION_PLAYED, bPlayed);
	}
	
	public void setAddFriendAskHandled(boolean bHandled){
		setExtension(EXTENSION_FRIENDASK_HANDLED, bHandled);
	}
	
	public boolean isAddFriendAskHandled(){
		return mExtension[EXTENSION_FRIENDASK_HANDLED];
	}
	
	public void setExtension(int nIndex,boolean bValue){
		if(mExtension[nIndex] != bValue){
			mExtension[nIndex] = bValue;
		}
		mContentValues.put(DBColumns.Message.COLUMN_EXTENSION, getExtension());
	}
	
	public boolean getExtention(int index){
		return mExtension[index];
	}
	
	protected void setExtension(int nExtension){
		for(int nIndex = 0;nIndex < EXTENSION_COUNT;++nIndex){
			mExtension[nIndex] = (((nExtension >> nIndex) & 0x01) == 1);
		}
	}
	
	protected int getExtension(){
		int nExtension = 0;
		for(int nIndex = 0;nIndex < EXTENSION_COUNT;++nIndex){
			nExtension = ((mExtension[nIndex] ? 1 : 0) << nIndex) | nExtension;
		}
		return nExtension;
	}
	
	public void setUrl(String url){
		mUrl = url;
		mContentValues.put(DBColumns.Message.COLUMN_URL, url);
	}
	
	public String getUrl(){
		return mUrl;
	}
	
	public String getThumbUrl(){
		if(mType == TYPE_PHOTO){
			return getThumbPhotoDownloadUrl();
		}else if(mType == TYPE_VIDEO){
			return getVideoThumbDownloadUrl();
		}
		return "";
	}
	
	public void setThumbUrl(String url){
		if(mType == TYPE_PHOTO){
			setThumbPhotoDownloadUrl(url);
		}else if(mType == TYPE_VIDEO){
			setVideoThumbDownloadUrl(url);
		}
	}
	
	public void setSize(long size){
		mSize = size;
		mContentValues.put(DBColumns.Message.COLUMN_SIZE, size);
	}
	
	public void setBubbleId(String id){
		mBubbleId = id;
		mContentValues.put(DBColumns.Message.COLUMN_BUBBLEID, id);
	}
	
	public String getBubbleId(){
		return mBubbleId;
	}
	
	public void setExtString(String ext){
		mExtString = ext;
		mContentValues.put(DBColumns.Message.COLUMN_EXTSTRING, mExtString);
	}
	
	public String getExtString(){
		return mExtString;
	}
	
	public void	setExtObj(Object ext){
		mExtObj = ext;
		try{
			mContentValues.put(DBColumns.Message.COLUMN_EXTOBJ, SystemUtils.objectToByteArray(ext));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public Object getExtObj(){
		return mExtObj;
	}
	
	public boolean	isStoraged(){
		return mStoraged;
	}
	
	public boolean 	isReaded(){
		return mReaded;
	}
	
	public void 	setStoraged(){
		mStoraged = true;
		mContentValues.clear();
	}
	
	public ContentValues getSaveContentValues(){
		return mContentValues;
	}
	
	public void 	setReaded(boolean bReaded){
		mReaded = bReaded;
	}
	
	public void updateDB(){
		if(isStoraged()){
			onUpdateDB();
		}
	}
	
	public String getFileName(){
		return mId;
	}
	
	public void setDisplayName(String name){
		mDisplayName = name;
		mContentValues.put(DBColumns.Message.COLUMN_DISPLAY,mDisplayName);
	}
	
	public String getDisplayName(){
		String ret;
		if(mType == XMessage.TYPE_VIDEO){
			ret = getVideoDisplayName();
		}else{
			ret = mDisplayName;
		}
		if(TextUtils.isEmpty(ret)){
			if(mType == XMessage.TYPE_PHOTO){
				return mId + ".jpg";
			}else if(mType == XMessage.TYPE_VOICE){
				return mId + ".amr";
			}else{
				return mId;
			}
		}else{
			return ret;
		}
	}
	
	public void		setPhotoDownloadUrl(String url){
		setUrl(url);
	}
	
	public String	getPhotoDownloadUrl(){
		return mUrl;
	}
	
	public void		setThumbPhotoDownloadUrl(String url){
		setContent(url);
	}
	
	public String	getThumbPhotoDownloadUrl(){
		return getContent();
	}
	
	public void		setVoiceDownloadUrl(String url){
		setUrl(url);
	}
	
	public String	getVoiceDownloadUrl(){
		return getUrl();
	}
	
	public void setVoiceFrameCount(int frameCount){
		setSize(frameCount);
	}
	
	public int getVoiceFrameCount(){
		return (int)mSize;
	}
	
	public int getVoiceSeconds(){
		if(mType == TYPE_VOICE){
			int nFrameCount = getVoiceFrameCount();
			int lSeconds = nFrameCount / 50;
			if(lSeconds <= 0)lSeconds = 1;
			return lSeconds;
		}
		return 0;
	}
	
	public int getVoiceMilliseconds(){
		if(mType == TYPE_VOICE){
			int nFrameCount = getVoiceFrameCount();
			return nFrameCount * 20;
		}
		return 0;
	}
	
	public void	setVideoDownloadUrl(String url){
		setUrl(url);
	}
	
	public String getVideoDownloadUrl(){
		return mUrl;
	}
	
	public void setVideoThumbDownloadUrl(String url){
		setContent(url);
	}
	
	public String getVideoThumbDownloadUrl(){
		return getContent();
	}
	
	public void setVideoFilePath(String filePath){
		setDisplayName(filePath);
	}
	
	public String getVideoFilePath(){
		if(mDisplayName != null && mDisplayName.contains(File.separator)){
			return mDisplayName;
		}else{
			return IMFilePathManager.getInstance().getMessageFilePath(this);
		}
	}
	
	public String getFilePath(){
		if(mType == XMessage.TYPE_VIDEO){
			return getVideoFilePath();
		}
		return IMFilePathManager.getInstance().getMessageFilePath(this);
	}
	
	public String getThumbFilePath(){
		return IMFilePathManager.getInstance().getMessageFilePath(this, true);
	}
	
	protected String getVideoDisplayName(){
		if(mDisplayName != null){
			int pos = mDisplayName.lastIndexOf(File.separator);
			if(pos >= 0){
				return mDisplayName.substring(pos + File.separator.length());
			}
			return mDisplayName;
		}
		return mDisplayName;
	}
	
	public void setVideoSeconds(int seconds){
		setSize(seconds);
	}
	
	public int	getVideoSeconds(){
		return (int)mSize;
	}
	
	public void setOfflineFileDownloadUrl(String url){
		setUrl(url);
	}
	
	public String getOfflineFileDownloadUrl(){
		return mUrl;
	}
	
	public void setFileSize(long size){
		setSize(size);
	}
	
	public long getFileSize(){
		return mSize;
	}
	
	public void setLocation(double lat,double lng){
		setUrl(String.valueOf(lat) + "," + String.valueOf(lng));
	}
	
	/**
	 * String[0]:lat
	 * </br>String[1]:lng
     */
	public String[] getLocation(){
		return mUrl.split(",");
	}
	
	public void setImgTextSubType(String subType){
		setExtString(subType);
	}
	
	public String getImgTextSubType(){
		return getExtString();
	}
	
	public void setImgTextValue(String value){
		setDisplayName(value);
	}
	
	public String getImgTextValue(){
		return getDisplayName();
	}
	
	public boolean 	isFileExists(){
		return new File(getFilePath()).exists();
	}
	
	public boolean 	isThumbFileExists(){
		return new File(getThumbFilePath()).exists();
	}
	
	@Override
	public String getVoiceFilePath() {
		return getFilePath();
	}
	
	protected void onUpdateDB(){
		if(mContentValues.size() > 0){
			AndroidEventManager.getInstance().runEvent(EventCode.DB_SaveMessage, this);
		}
	}
	
	public boolean isDownloading(){
		MessageDownloadProcessor p = MessageDownloadProcessor.getMessageDownloadProcessor(mType);
		return p == null ? false : p.isDownloading(this);
	}
	
	public boolean isThumbDownloading(){
		MessageDownloadProcessor p = MessageDownloadProcessor.getMessageDownloadProcessor(mType);
		return p == null ? false : p.isThumbDownloading(this);
	}
	
	public int getDownloadPercentage(){
		MessageDownloadProcessor p = MessageDownloadProcessor.getMessageDownloadProcessor(mType);
		return p == null ? 0 : p.getDownloadPercentage(this);
	}
	
	public int getThumbDownloadPercentage(){
		MessageDownloadProcessor p = MessageDownloadProcessor.getMessageDownloadProcessor(mType);
		return p == null ? 0 : p.getThumbDownloadPercentage(this);
	}
	
	public boolean isUploading(){
		MessageUploadProcessor p = MessageUploadProcessor.getMessageUploadProcessor(mType);
		return p == null ? false : p.isUploading(this);
	}
	
	public int getUploadPercentage(){
		MessageUploadProcessor p = MessageUploadProcessor.getMessageUploadProcessor(mType);
		return p == null ? 0 : p.getUploadPercentage(this);
	}
}
