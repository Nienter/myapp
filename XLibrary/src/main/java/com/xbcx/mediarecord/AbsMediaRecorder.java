package com.xbcx.mediarecord;

public abstract class AbsMediaRecorder {
	
	protected String mFilePathOutput;
	
	public abstract void startRecord();
	
	public abstract void stopRecord();
	
	public abstract void pauseRecord();
	
	public abstract void resumeRecord();
	
	public abstract void release();
	
	public abstract boolean pauseSupport();
	
	public void setFilePathOutput(String strFilePath){
		mFilePathOutput = strFilePath;
	}
	
	public String getFilePathOutput(){
		return mFilePathOutput;
	}
}
