package com.xbcx.common.voice;

import com.xbcx.core.XApplication;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.util.SparseArray;
import android.util.SparseIntArray;

public class SoundPlayManager {
	
	private static SoundPool 		soundPool;
	
	private static Object			mSycn = new Object();
	
	private static int				soundResId;
	private static int				soundId;
	private static long 			soundPlayTimeLast;
	
	private static Vibrator			vibrator;
	private static long				vibratorTimeLast;
	
	private static SoundPlayer		defaultSoundPlayer;
	
	public static void setSoundResId(int resId){
		soundResId = resId;
	}
	
	public static void playSound(){
		synchronized (mSycn) {
			if(soundPool == null){
				if(soundResId == 0){
					return;
				}
				soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
				soundId = soundPool.load(XApplication.getApplication(), soundResId, 1);
			}
			
			final long lTimeNow = System.currentTimeMillis();
			if(lTimeNow - soundPlayTimeLast > 1000){
				simplePlay(soundPool,soundId);
			}
			soundPlayTimeLast = lTimeNow;
		}
	}
	
	public static void playSound(int resId){
		playSound(resId, null);
	}
	
	public static void playSound(final int resId,final PlayType pt){
		synchronized (mSycn) {
			if(defaultSoundPlayer == null){
				if(soundPool == null){
					soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
				}
				defaultSoundPlayer = new SycnSoundPlayer(soundPool,mSycn);
			}
			defaultSoundPlayer.playSound(resId, pt);
		}
	}
	
	private static int simplePlay(SoundPool sp,int soundId){
		return sp.play(soundId, 1, 1, 0, 0, 1);
	}
	
	public static void vibrate(){
		if(vibrator == null){
			vibrator = (Vibrator)XApplication.getApplication().getSystemService(
					Context.VIBRATOR_SERVICE);
		}
		
		final long lTimeNow = System.currentTimeMillis();
		if (lTimeNow - vibratorTimeLast > 1000) {
			vibrator.vibrate(200);
		}
		vibratorTimeLast = lTimeNow;
	}
	
	public static interface PlayType{
		public int play(SoundPool sp,int soundId);
	}
	
	public static class LoopPlayType implements PlayType{
		@Override
		public int play(SoundPool sp, int soundId) {
			return sp.play(soundId, 1, 1, 0, -1, 1);
		}
	}
	
	public static class SoundPlayer implements SoundPool.OnLoadCompleteListener{
		
		private SoundPool				mSoundPool;
		
		private SparseIntArray 			mapResIdToSoundId;
		private SparseIntArray			mapWaitSoundId;
		private SparseArray<PlayType>	mapSoundIdToPlayType;
		private SparseIntArray			loadCompleteSoundIds;
		
		public SoundPlayer(){
			this(new SoundPool(10, AudioManager.STREAM_SYSTEM, 5));
		}
		
		public SoundPlayer(SoundPool sp){
			mSoundPool = sp;
			mSoundPool.setOnLoadCompleteListener(this);
		}
		
		public void playSound(int resId){
			playSound(resId, null);
		}
		
		public void playSound(final int resId,final PlayType pt){
			if(mapResIdToSoundId == null){
				mapResIdToSoundId = new SparseIntArray();
			}
			int si = mapResIdToSoundId.get(resId, -1);
			if(si == -1){
				si = mSoundPool.load(XApplication.getApplication(),resId, 1);
				if(mapWaitSoundId == null){
					mapWaitSoundId = new SparseIntArray();
				}
				mapWaitSoundId.put(si, si);
				if(pt != null){
					if(mapSoundIdToPlayType == null){
						mapSoundIdToPlayType = new SparseArray<SoundPlayManager.PlayType>();
					}
					mapSoundIdToPlayType.put(si, pt);
				}
				mapResIdToSoundId.put(resId, si);
			}else{
				if(isSoundIdsLoadComplete(si)){
					play(si);
				}
			}
		}
		
		public void release(){
			mSoundPool.release();
		}

		@Override
		public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
			if(loadCompleteSoundIds == null){
				loadCompleteSoundIds = new SparseIntArray();
			}
			loadCompleteSoundIds.put(sampleId, sampleId);
			if(mapWaitSoundId != null){
				if(mapWaitSoundId.get(sampleId) != 0){
					mapWaitSoundId.delete(sampleId);
					play(sampleId);
				}
			}
		}
		
		protected void play(int soundId){
			PlayType pt = null;
			if(mapSoundIdToPlayType != null){
				pt = mapSoundIdToPlayType.get(soundId);
			}
			if(pt == null){
				simplePlay(mSoundPool,soundId);
			}else{
				pt.play(mSoundPool,soundId);
			}
		}
		
		protected boolean isSoundIdsLoadComplete(int soundId){
			if(loadCompleteSoundIds != null){
				return loadCompleteSoundIds.get(soundId, -1) != -1;
			}
			return false;
		}
	}
	
	public static class SycnSoundPlayer extends SoundPlayer{
		
		private Object sync;
		
		public SycnSoundPlayer(SoundPool sp,Object sync){
			super(sp);
			this.sync = sync;
		}
		
		@Override
		public void playSound(int resId, PlayType pt) {
			synchronized(sync){
				super.playSound(resId, pt);
			}
		}
		
		@Override
		public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
			synchronized (sync) {
				super.onLoadComplete(soundPool, sampleId, status);
			}
		}
		
		@Override
		protected void play(int soundId) {
			synchronized (sync) {
				super.play(soundId);
			}
		}
		
		@Override
		protected boolean isSoundIdsLoadComplete(int soundId) {
			synchronized (sync) {
				return super.isSoundIdsLoadComplete(soundId);
			}
		}
	}
}
