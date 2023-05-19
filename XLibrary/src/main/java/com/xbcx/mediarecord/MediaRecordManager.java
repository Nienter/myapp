package com.xbcx.mediarecord;

import java.util.LinkedList;
import java.util.List;


import android.content.Context;

public class MediaRecordManager implements AsyncMediaRecorder.OnMediaRecordListener{
	
	public static MediaRecordManager getInstance(Context context){
		if(sInstance == null){
			sInstance = new MediaRecordManager(context.getApplicationContext());
		}
		return sInstance;
	}
	
	private static MediaRecordManager sInstance;
	
	private Context mContext;
	
	private int 	mOpenTimes;
	
	private AsyncMediaRecorder mMediaRecorder;
	
	private List<OnRecordListener> mRecordListeners = new LinkedList<OnRecordListener>();
	
	private MediaRecordManager(Context context){
		mContext = context;
		
		mOpenTimes = 0;
	}
	
	public void addOnRecordListener(OnRecordListener listener){
		if(!mRecordListeners.contains(listener)){
			mRecordListeners.add(listener);
		}
	}
	
	public boolean removeOnRecordListener(OnRecordListener listener){
		return mRecordListeners.remove(listener);
	}
	
	public String getRecordFilePath(){
		return mMediaRecorder.getFilePathOutput();
	}
	
	public double getCurrentDecibel(){
		return mMediaRecorder.getCurrentDecibel();
	}
	
	public long getRecordTime(){
		return mMediaRecorder.getRecordTime();
	}
	
	public void open(){
		++mOpenTimes;
		if(mOpenTimes == 1){
			mMediaRecorder = new AsyncMediaRecorder(mContext);
			mMediaRecorder.setOnMediaRecordListener(this);
		}
	}
	
	public void close(){
		--mOpenTimes;
		if(mOpenTimes < 0){
			mOpenTimes = 0;
		}
		if(mOpenTimes == 0){
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}
	
	public void startRecord(){
		if(mOpenTimes > 0){
			mMediaRecorder.startRecord();
		}
	}
	
	public void stopRecord(){
		if(mOpenTimes > 0){
			mMediaRecorder.stopRecord();
		}
	}
	
	public boolean	isRecording(){
		if(mMediaRecorder != null){
			return mMediaRecorder.isRecording();
		}
		return false;
	}

	@Override
	public void onStarted(boolean bSuccess) {
		for(OnRecordListener listener : mRecordListeners){
			listener.onStarted(bSuccess);
		}
	}

	@Override
	public void onStoped(boolean bBeyondMinTime) {
		for(OnRecordListener listener : mRecordListeners){
			listener.onStoped(bBeyondMinTime);
		}
	}

	@Override
	public void onExceedMaxTime() {
		for(OnRecordListener listener : mRecordListeners){
			listener.onExceedMaxTime();
		}
	}

	@Override
	public void onInterrupted() {
		for(OnRecordListener listener : mRecordListeners){
			listener.onInterrupted();
		}
	}
	
	@Override
	public void onDecibelChanged(double decibel) {
		for(OnRecordListener listener : mRecordListeners){
			listener.onDecibelChanged(decibel);
		}
	}
	
	public static interface OnRecordListener{
		public void onStarted(boolean bSuccess);
		
		public void onStoped(boolean bBeyondMinTime);
		
		public void	onExceedMaxTime();
		
		public void onInterrupted();
		
		public void onDecibelChanged(double decibel);
	}
}
