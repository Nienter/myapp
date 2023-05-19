package com.xbcx.common.voice;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.os.Build;

import com.xbcx.core.IDObject;
import com.xbcx.core.XApplication;
import com.xbcx.mediarecord.MediaRecordManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VoicePlayer implements OnCompletionListener,
									OnErrorListener,
									OnInfoListener,
									MediaRecordManager.OnRecordListener,
									SensorEventListener{
	
	public static VoicePlayer getInstance(){
		if(sInstance == null){
			sInstance = new VoicePlayer();
		}
		return sInstance;
	}
	
	private static VoicePlayer sInstance;
	
	private int							mInitTimes;
	
	private List<OnVoicePlayListener>	mVoicePlayListeners = null;
	
	protected Context			mContext;
	
	protected MediaPlayer 		mMediaPlayer;
	
	protected AudioManager 		mAudioManager;
	protected SensorManager		mSensorManager;
	protected Sensor			mSensorProximiny;
	protected float				mProximiny;//当前传感器距离
	
	protected VoicePath			mPlayingPath;
	protected VoicePath 		mPausedPath;
	protected boolean			mPaused;
	
	protected VoicePath			mPlayPartPath;
	
	protected boolean			mIsAutoSwitchSpearker = true;
	protected boolean			mIsSpearkOn = true;
	
	protected boolean			mCurSpeakerOn;
	
	protected boolean			mIsChangeSpearker;
	
	OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
					focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				pause();
			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				resume();
			}
		}
	};
	
	protected Runnable mRunnablePlayPartToStop = new Runnable() {
		@Override
		public void run() {
			if(mPlayPartPath != null && 
					mPlayPartPath.equals(mPlayingPath)){
				stop();
				
				mPlayPartPath = null;
			}
		}
	};
	
	protected VoicePlayer(){
		if(Build.BRAND.toLowerCase(Locale.getDefault()).contains("htc")){
			mIsChangeSpearker = true;
		}
		mInitTimes = 0;
	}
	
	public void initial(){
		if(mInitTimes == 0){
			mContext = XApplication.getApplication();
			
			initMediaPlayer();
			
			mPaused = false;
		
			mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
			mSensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
			mSensorProximiny = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			
			mIsSpearkOn = isSpeakerOn();
			
			MediaRecordManager.getInstance(mContext).addOnRecordListener(this);
		}
		++mInitTimes;
	}
	
	public void release(){
		--mInitTimes;
		if(mInitTimes < 0){
			mInitTimes = 0;
		}
		if(mInitTimes == 0){
			mMediaPlayer.release();
			mMediaPlayer = null;
			
			restorePlayMode();
			
			clearListener();
			
			mPlayingPath = null;
			mPausedPath = null;
			
			XApplication.getMainThreadHandler().removeCallbacks(mRunnablePlayPartToStop);
			
			MediaRecordManager.getInstance(mContext).removeOnRecordListener(this);
		}
	}
	
	protected void initMediaPlayer(){
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setLooping(false);
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnErrorListener(this);
		mMediaPlayer.setOnInfoListener(this);
	}
	
	public void addVoicePlayListener(OnVoicePlayListener listener){
		if(mVoicePlayListeners == null){
			mVoicePlayListeners = new ArrayList<VoicePlayer.OnVoicePlayListener>();
		}
		mVoicePlayListeners.add(listener);
	}
	
	public void removeVoicePlayListener(OnVoicePlayListener listener){
		if(mVoicePlayListeners == null){
			return;
		}
		mVoicePlayListeners.remove(listener);
	}
	
	public void setAutoSwitchSpeaker(boolean bAuto){
		mIsAutoSwitchSpearker = bAuto;
	}
	
	public boolean isSpeakerOn(){
		return mIsSpearkOn;
	}
	
	public void	setSpeakerOn(boolean bOn){
		mIsSpearkOn = bOn;
		if(mInitTimes > 0){
			if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
				checkAndSetPlayMode();
			}
		}
	}
	
	protected void checkAndSetPlayMode(){
		if(mIsSpearkOn){
			restorePlayMode();
		}else{
			doSetCallMode();
		}
	}
	
	protected void doSetCallMode(){
		mCurSpeakerOn = false;
		if(mIsChangeSpearker){
			mAudioManager.setSpeakerphoneOn(false);
		}
		mAudioManager.setMode(AudioManager.MODE_IN_CALL);
	}
	
	protected void restorePlayMode(){
		mCurSpeakerOn = true;
		if(mIsChangeSpearker){
			mAudioManager.setSpeakerphoneOn(true);
		}
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
	}
	
	public void play(VoicePath path){
		play(path, false);
	}
	
	public void play(VoicePath path, boolean bPlayPart) {
		if(mPlayingPath != null){
			onVoicePlayStoped(mPlayingPath);
		}
		try {
			checkAndSetPlayMode();

			// mAudioManager.setMode(AudioManager.MODE_IN_CALL);

			mMediaPlayer.reset();

			if (mIsSpearkOn) {
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			} else {
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
			}

			mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, mIsSpearkOn ? AudioManager.STREAM_MUSIC : AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			if (mIsAutoSwitchSpearker) {
				mSensorManager.registerListener(this, mSensorProximiny, SensorManager.SENSOR_DELAY_NORMAL);
			}

			mMediaPlayer.setDataSource(path.getVoiceFilePath());
			mMediaPlayer.prepare();
			mMediaPlayer.start();

			mPlayingPath = path;
			
			onVoicePlayStarted(mPlayingPath);
		} catch (Exception e) {
			e.printStackTrace();
			onVoicePlayErrored(path);
		}
	}
	
	public int	getCurrentPlayPosition(){
		if(mMediaPlayer != null){
			try{
				return mMediaPlayer.getCurrentPosition();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	protected void onVoicePlayStarted(VoicePath path){
		if(mVoicePlayListeners != null){
			for(OnVoicePlayListener listener : mVoicePlayListeners){
				listener.onVoicePlayStarted(path);
			}
		}
	}
	
	protected void onVoicePlayErrored(VoicePath path){
		if(mVoicePlayListeners != null){
			for(OnVoicePlayListener listener : mVoicePlayListeners){
				listener.onVoicePlayErrored(path);
			}
		}
	}
	
	protected void onVoicePlayStoped(VoicePath path){
		if(mVoicePlayListeners != null){
			for(OnVoicePlayListener listener : mVoicePlayListeners){
				listener.onVoicePlayStoped(path);
			}
		}
	}
	
	public void stop(){
		if(mPlayingPath != null){
			mMediaPlayer.stop();
			
			clearListener();
			
			restorePlayMode();
			
			onVoicePlayStoped(mPlayingPath);

			mPlayingPath = null;
		}
	}

	public void stopWithoutCallback(){
		if(mPlayingPath != null){
			mMediaPlayer.stop();

			clearListener();

			restorePlayMode();

			mPlayingPath = null;
		}
	}
	
	public boolean isPlaying(VoicePath path){
		return path != null && path.equals(mPlayingPath);
	}
	
	public boolean isPlaying(){
		return mPlayingPath != null;
	}
	
	protected void resume(){
		if(mPaused){
			if(mPausedPath != null){
				checkAndSetPlayMode();
				mPlayingPath = mPausedPath;
				mPausedPath = null;
				mMediaPlayer.start();
				
				if(mPlayPartPath != null && 
						mPlayPartPath.equals(mPlayingPath)){
					XApplication.getMainThreadHandler().postDelayed(
							mRunnablePlayPartToStop, 
							mMediaPlayer.getDuration() / 2 - mMediaPlayer.getCurrentPosition());
				}
			}
		}
	}
	
	protected void pause(){
		restorePlayMode();
		mPaused = true;
		if(mPlayingPath != null){
			mPausedPath = mPlayingPath;
			mPlayingPath = null;
			mMediaPlayer.pause();
			if(mPlayPartPath != null){
				XApplication.getMainThreadHandler().removeCallbacks(
						mRunnablePlayPartToStop);
			}
		}
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if(mIsSpearkOn){
			mProximiny = event.values[0];
			if(mProximiny >= Math.min(5,mSensorProximiny.getMaximumRange())){
				if(mPlayingPath != null){
					if(!mCurSpeakerOn){
						final int curPosition = mMediaPlayer.getCurrentPosition();
						mMediaPlayer.stop();
						mMediaPlayer.release();
						
						restorePlayMode();
						
						initMediaPlayer();
						mMediaPlayer.reset();
						mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
						try{
							mMediaPlayer.setDataSource(mPlayingPath.getVoiceFilePath());
							mMediaPlayer.prepare();
							mMediaPlayer.start();
							mMediaPlayer.seekTo(curPosition);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			}else{
				if(mPlayingPath != null){
					if(mCurSpeakerOn){
						final int curPosition = mMediaPlayer.getCurrentPosition();
						mMediaPlayer.stop();
						mMediaPlayer.release();
						
						doSetCallMode();
						
						initMediaPlayer();
						mMediaPlayer.reset();
						mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
						try{
							mMediaPlayer.setDataSource(mPlayingPath.getVoiceFilePath());
							mMediaPlayer.prepare();
							mMediaPlayer.start();
							mMediaPlayer.seekTo(curPosition);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		return false;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		clearListener();
		
		restorePlayMode();
		
		onVoicePlayErrored(mPlayingPath);
		
		mPlayingPath = null;
		mPausedPath = null;
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		clearListener();
		
		restorePlayMode();
		
		if(mVoicePlayListeners != null){
			for(OnVoicePlayListener listener : mVoicePlayListeners){
				listener.onVoicePlayCompletioned(mPlayingPath);
			}
		}
		
		mPlayingPath = null;
	}
	
	protected void clearListener(){
		mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onStarted(boolean bSuccess) {
		pause();
	}

	@Override
	public void onStoped(boolean bBeyondMinTime) {
		resume();
	}

	@Override
	public void onExceedMaxTime() {
	}

	@Override
	public void onInterrupted() {
	}

	@Override
	public void onDecibelChanged(double decibel) {
	}
	
	public static class SimpleVoicePath extends IDObject implements VoicePath{
		private static final long serialVersionUID = 1L;
		
		private final String mPath;
		
		public SimpleVoicePath(String path){
			super(path);
			mPath = path;
		}

		@Override
		public String getVoiceFilePath() {
			return mPath;
		}
	}
	
	public static interface OnVoicePlayListener{
		public void onVoicePlayStarted(VoicePath path);
		
		public void	onVoicePlayStoped(VoicePath path);
		
		public void onVoicePlayErrored(VoicePath path);
		
		public void	onVoicePlayCompletioned(VoicePath path);
	}
}
