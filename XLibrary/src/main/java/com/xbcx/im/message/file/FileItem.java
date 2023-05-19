package com.xbcx.im.message.file;

import java.io.File;
import java.io.Serializable;

public class FileItem implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public static final int FILETYPE_OFFICE = 1;
	public static final int FILETYPE_PDF	= 2;
	public static final int FILETYPE_PIC	= 3;
	public static final int FILETYPE_VIDEO	= 4;
	public static final int FILETYPE_OTHER	= 5;

	private final String 	mPath;
	
	private final String 	mName;

	private final int		mFileType;
	
	private final long		mFileSize;
	
	private final boolean	mIsFromSelf;
	
	private final long		mTime;
	
	private boolean			mIsSelect;
	
	public FileItem(String path,String name,int fileType,boolean bFromSelf,long time){
		mName = name;
		mPath = path;
		mFileType = fileType;
		mIsFromSelf = bFromSelf;
		mTime = time;
		
		mFileSize = new File(path).length();
	}

	public String 	getName() {
		return mName;
	}

	public String 	getPath() {
		return mPath;
	}

	public int 		getFileType() {
		return mFileType;
	}
	
	public long 	getFileSize(){
		return mFileSize;
	}
	
	public boolean 	isFromSelf(){
		return mIsFromSelf;
	}
	
	public long		getTime(){
		return mTime;
	}
	
	public void 	setSelect(boolean bSelect){
		mIsSelect = bSelect;
	}
	
	public boolean 	isSelect(){
		return mIsSelect;
	}
}
