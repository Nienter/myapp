package com.xbcx.im.messageprocessor;

import com.xbcx.common.voice.VoicePath;
import com.xbcx.common.voice.VoicePlayer;
import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.EventCode;
import com.xbcx.core.SharedPreferenceDefine;
import com.xbcx.im.XMessage;
import com.xbcx.mediarecord.MediaRecordManager;

import android.media.MediaPlayer;

public class VoicePlayProcessor extends VoicePlayer{
	
	public static VoicePlayProcessor getInstance(){
		if(sInstance == null){
			sInstance = new VoicePlayProcessor();
		}
		return sInstance;
	}
	
	private static VoicePlayProcessor sInstance;
	
	private VoicePath	mRecordStopPlayPath;
	
	private VoicePlayProcessor(){
	}
	
	@Override
	public void release() {
		super.release();
		mRecordStopPlayPath = null;
	}
	
	public boolean isSpeakerOn(){
		return mContext.getSharedPreferences(
				SharedPreferenceDefine.SP_IM,0).getBoolean(
						SharedPreferenceDefine.KEY_SPEAKERON, true);
	}
	
	public void	setSpeakerOn(boolean bOn){
		mContext.getSharedPreferences(
				SharedPreferenceDefine.SP_IM,0).edit().putBoolean(
					SharedPreferenceDefine.KEY_SPEAKERON, bOn).commit();
	}
	
	@Override
	public void play(VoicePath path, boolean bPlayPart) {
		if(MediaRecordManager.getInstance(mContext).isRecording()){
			mRecordStopPlayPath = path;
		}else{
			super.play(path, bPlayPart);
		}
	}
	
	public boolean	hasPausedPath(){
		return mRecordStopPlayPath != null || mPausedPath != null;
	}
	
	@Override
	protected void pause() {
		if(mPlayingPath != null){
			AndroidEventManager.getInstance().runEvent(EventCode.VoicePlayPaused, mPlayingPath);
		}
		super.pause();
	}

	@Override
	public void onStoped(boolean bBeyondMinTime) {
		super.onStoped(bBeyondMinTime);
		if(mRecordStopPlayPath != null){
			play(mRecordStopPlayPath);
			mRecordStopPlayPath = null;
		}
	}
	
	@Override
	protected void onVoicePlayStarted(VoicePath path) {
		XMessage xm = (XMessage)path;
		xm.setPlayed(true);
		xm.updateDB();
		AndroidEventManager.getInstance().runEvent(EventCode.VoicePlayStarted,
				path);
	}

	@Override
	protected void onVoicePlayErrored(VoicePath path) {
		AndroidEventManager.getInstance().runEvent(EventCode.VoicePlayErrored, path);
	}

	@Override
	protected void onVoicePlayStoped(VoicePath path) {
		AndroidEventManager.getInstance().runEvent(
				EventCode.VoicePlayStoped,path);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		AndroidEventManager.getInstance().runEvent(
				EventCode.VoicePlayCompletioned,mPlayingPath);
		super.onCompletion(mp);
	}
}
