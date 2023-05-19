package com.xbcx.im;

import java.io.File;

import com.xbcx.core.XApplication;
import com.xbcx.core.module.UserInitialListener;
import com.xbcx.utils.SystemUtils;

import android.text.TextUtils;
import android.util.SparseArray;

public class IMFilePathManager implements UserInitialListener{
	
	public static IMFilePathManager getInstance(){
		return instance;
	}
	
	static{
		instance = new IMFilePathManager();
	}
	
	private static IMFilePathManager instance;
	
	protected String 							mFilePathPrefix;
	
	protected SparseArray<String> 				mMapMsgTypeToFolderName = new SparseArray<String>();
	protected SparseArray<FileNameGenerator> 	mMapMsgTypeToFileNameGenerator = new SparseArray<FileNameGenerator>();
	
	protected IMFilePathManager(){
		registerFileInfo(XMessage.TYPE_FILE, "file",new DefaultFileNameGenerator(""));
		registerFileInfo(XMessage.TYPE_LOCATION, "location",new DefaultFileNameGenerator(""));
		registerFileInfo(XMessage.TYPE_PHOTO, "photo",new DefaultFileNameGenerator(".jpg"));
		registerFileInfo(XMessage.TYPE_VIDEO, "video",new VideoFileNameGenerator());
		registerFileInfo(XMessage.TYPE_VOICE, "voice",new DefaultFileNameGenerator(".amr"));
	}
	
	public void registerFileInfo(int msgType,String folderName,FileNameGenerator generator){
		mMapMsgTypeToFolderName.put(msgType, folderName);
		registerFileNameGenerator(msgType, generator);
	}
	
	public void registerFileNameGenerator(int msgType,FileNameGenerator generator){
		mMapMsgTypeToFileNameGenerator.put(msgType, generator);
	}
	
	@Override
	public void onUserInitial(String user, boolean bAuto) {
		mFilePathPrefix = SystemUtils.getExternalCachePath(XApplication.getApplication()) + 
				File.separator + "users" + File.separator + user;
	}
	
	public String getMessageFilePath(XMessage m){
		return getMessageFilePath(m, false);
	}
	
	public String getMessageFilePath(XMessage m,boolean bThumb){
		final int msgType = m.getType();
		String folderName = mMapMsgTypeToFolderName.get(msgType);
		if(TextUtils.isEmpty(folderName)){
			folderName = "default";
		}
		StringBuffer sb = new StringBuffer(getMessageFolderPath(m.getOtherSideId()))
		.append(File.separator).append(folderName)
		.append(File.separator);
		FileNameGenerator g = mMapMsgTypeToFileNameGenerator.get(msgType);
		if(g == null){
			sb.append(m.getId());
		}else{
			sb.append(g.generateFileName(m, bThumb));
		}
		return sb.toString();
	}
	
	public String getMessageFolderPath(String strId){
		StringBuffer buf = new StringBuffer(mFilePathPrefix);
		buf.append(File.separator);
		if(!TextUtils.isEmpty(strId)){
			buf.append(strId);
		}else{
			throw new IllegalArgumentException("roomId is Empty");
		}
		
		return buf.toString();
	}
	
	public String getMessageFolderPath(){
		return mFilePathPrefix;
	}
	
	public static interface FileNameGenerator{
		public String	generateFileName(XMessage xm,boolean bThumb);
	}
	
	public static class DefaultFileNameGenerator implements FileNameGenerator{

		private String	mSuffix;
		
		public DefaultFileNameGenerator(String suffix){
			mSuffix = suffix;
		}
		
		@Override
		public String generateFileName(XMessage xm, boolean bThumb) {
			if(bThumb){
				return xm.getId() + "thumb" + mSuffix;
			}
			return xm.getId() + mSuffix;
		}
	}
	
	public static class VideoFileNameGenerator implements FileNameGenerator{

		@Override
		public String generateFileName(XMessage xm, boolean bThumb) {
			if(bThumb){
				return "thumb" + File.separator + xm.getId() + ".jpg";
			}
			return xm.getId() + ".mp4";
		}
	}
}
