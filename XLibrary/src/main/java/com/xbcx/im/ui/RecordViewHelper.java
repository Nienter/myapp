package com.xbcx.im.ui;

import com.xbcx.core.XApplication;
import com.xbcx.core.module.RecordCheckListener;
import com.xbcx.library.R;
import com.xbcx.mediarecord.MediaRecordManager;
import com.xbcx.utils.SystemUtils;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

public class RecordViewHelper implements View.OnTouchListener,
											MediaRecordManager.OnRecordListener{
	
	protected View 					mBtnPressTalk;
	protected PopupWindow 			mPopupWindowRecordPrompt;
	protected ImageView 			mImageViewRecordPromt;
	protected ProgressBar 			mProgressBarRecordPrepare;
	
	protected boolean 				mRecordSuccess;
	protected boolean 				mNeedStop;
	protected boolean 				mCancel;
	
	protected MediaRecordManager 	mMediaRecordManager;
	protected boolean				mIsAddEventListener;
	
	private   int					mImageTalkRes1 = R.drawable.image_talkstart_1;
	private   int					mImageTalkRes2 = R.drawable.image_talkstart_2;
	private   int					mImageTalkRes3 = R.drawable.image_talkstart_3;
	private   int	   				mImageTalkRes4 = R.drawable.image_talkstart_4;
	private   int					mImageTalkRes5 = R.drawable.image_talkstart_5;
	
	private   boolean				mDelayHandleDown;
	private   OnRecordListener 		mOnRecordListener;
	private	  CancelHandler			mCancelHandler;
	
	private	  Runnable				mTapCheck;
	
	private   Handler 				mHandler;
	
	private Runnable mRunnableDelayDismissRecordPrompt = new Runnable() {
		public void run() {
			mPopupWindowRecordPrompt.dismiss();
		}
	};
	
	public RecordViewHelper() {
		mMediaRecordManager = MediaRecordManager.getInstance(XApplication.getApplication());
	}
	
	public void onCreate(View btnPressTalk){
		final Context context = btnPressTalk.getContext();
		View contentView = LayoutInflater.from(context).inflate(R.layout.xlibrary_recordprompt, null);
		mImageViewRecordPromt = (ImageView)contentView.findViewById(R.id.imageView);
		mProgressBarRecordPrepare = (ProgressBar)contentView.findViewById(R.id.progressBar);
		
		mBtnPressTalk = btnPressTalk;
		mBtnPressTalk.setOnTouchListener(this);
		
		int nSize = SystemUtils.dipToPixel(context, 150);
		mPopupWindowRecordPrompt = new PopupWindow(contentView,nSize,nSize,false);
		
		mMediaRecordManager.open();
		
		if(mCancelHandler == null){
			mCancelHandler = new ChatActivityCancleHandler();
		}
		
		mHandler = new Handler();
	}
	
	public RecordViewHelper setDelayHandleDown(boolean b){
		mDelayHandleDown = b;
		return this;
	}
	
	public RecordViewHelper setCancelHandler(CancelHandler h){
		mCancelHandler = h;
		return this;
	}
	
	public RecordViewHelper setImageTalkResIds(int res1,int res2,int res3,int res4,int res5){
		mImageTalkRes1 = res1;
		mImageTalkRes2 = res2;
		mImageTalkRes3 = res3;
		mImageTalkRes4 = res4;
		mImageTalkRes5 = res5;
		return this;
	}
	
	public void onDestroy(){
		if(mHandler != null){
			mMediaRecordManager.close();
			mHandler.removeCallbacks(mRunnableDelayDismissRecordPrompt);
			mOnRecordListener = null;
		}
	}
	
	public void onPause(){
		processStopRecord();
		mPopupWindowRecordPrompt.dismiss();
		if(mIsAddEventListener){
			mMediaRecordManager.removeOnRecordListener(this);
			mIsAddEventListener = false;
		}
	}
	
	public void onResume(){
		if(!mIsAddEventListener){
			mMediaRecordManager.addOnRecordListener(this);
		
			mIsAddEventListener = true;
		}
	}
	
	public void setOnRecordListener(OnRecordListener listener){
		mOnRecordListener = listener;
	}
	
	@Override
	public void onStarted(boolean bSuccess) {
		if(bSuccess){
			setRecordPromptDisplayChild(1);
			if(!mPopupWindowRecordPrompt.isShowing()){
				showPopupWindow();
			}
			
			updateRecordWave(0);
			
			if(mOnRecordListener != null){
				mOnRecordListener.onRecordStarted();
			}
		}else{
			onRecordBtnStatusChanged(false);
			
			mRecordSuccess = false;
			
			if(mOnRecordListener != null){
				mOnRecordListener.onRecordFailed();
			}
		}
	}
	
	protected void updateRecordWave(double decibel){
		if(decibel >= 30){
			mImageViewRecordPromt.setImageResource(mImageTalkRes5);
		}else if(decibel >= 28){
			mImageViewRecordPromt.setImageResource(mImageTalkRes4);
		}else if(decibel >= 26){
			mImageViewRecordPromt.setImageResource(mImageTalkRes3);
		}else if(decibel >= 24){
			mImageViewRecordPromt.setImageResource(mImageTalkRes2);
		}else{
			mImageViewRecordPromt.setImageResource(mImageTalkRes1);
		}
	}

	@Override
	public void onStoped(boolean bBeyondMinTime) {
		mNeedStop = false;
		if (mRecordSuccess) {
			if (bBeyondMinTime) {
				mPopupWindowRecordPrompt.dismiss();

				if (mCancel) {
					mCancel = false;
				} else {
					if (mOnRecordListener != null) {
						mOnRecordListener.onRecordEnded(mMediaRecordManager.getRecordFilePath());
					}
				}
			}else{
				onTalkShort();
			}
		}
	}
	
	protected void onTalkShort(){
		mImageViewRecordPromt.setImageResource(R.drawable.image_talkshort);
		mHandler.postDelayed(mRunnableDelayDismissRecordPrompt, 500);
	}

	@Override
	public void onExceedMaxTime() {
		mImageViewRecordPromt.setImageResource(R.drawable.image_talklong);
		mHandler.postDelayed(mRunnableDelayDismissRecordPrompt, 500);
		mNeedStop = false;
		onRecordBtnStatusChanged(false);
	}

	@Override
	public void onInterrupted() {
		processStopRecord();
	}
	
	@Override
	public void onDecibelChanged(double decibel) {
		if(!mCancel){
			updateRecordWave(decibel);
		}
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		int nAction = event.getAction();
		if (nAction == MotionEvent.ACTION_MOVE){
			if (mRecordSuccess) {
				if(mCancelHandler.isCancel(event, v,mImageViewRecordPromt)){
					onChangeCancelStatus();
					mCancel = true;
				}else{
					if(mCancel){
						updateRecordWave(mMediaRecordManager.getCurrentDecibel());
						mCancel = false;
					}
				}
				return true;
			}
		}else if (nAction == MotionEvent.ACTION_DOWN) {
			if (!mPopupWindowRecordPrompt.isShowing()) {
				if(mDelayHandleDown){
					if(mTapCheck == null){
						mTapCheck = new Runnable() {
							@Override
							public void run() {
								SystemUtils.requestDisallowInterceptTouchEvent(mBtnPressTalk, true);
								handleActionDown();
							}
						};
					}
					mHandler.postDelayed(mTapCheck, ViewConfiguration.getTapTimeout());
				}else{
					handleActionDown();
				}
				
				return true;
			}
		} else if(nAction == MotionEvent.ACTION_UP || nAction == MotionEvent.ACTION_CANCEL){
			if(mTapCheck != null){
				mHandler.removeCallbacks(mTapCheck);
			}
			mBtnPressTalk.setPressed(false);
			processStopRecord();
		}
		return false;
	}
	
	protected void handleActionDown(){
		mBtnPressTalk.setPressed(true);
		
		for(RecordCheckListener l : XApplication.getManagers(RecordCheckListener.class)){
			if(!l.onRecordCheck()){
				mRecordSuccess = false;
				return;
			}
		}
		if(mOnRecordListener != null){
			if(mOnRecordListener.onRecordCheck() &&
					XApplication.checkExternalStorageAvailable()){
				setRecordPromptDisplayChild(0);
				showPopupWindow();

				mMediaRecordManager.startRecord();
				mNeedStop = true;
				mRecordSuccess = true;
				
				onRecordBtnStatusChanged(true);
			}else{
				mRecordSuccess = false;
			}
		}else{
			mRecordSuccess = false;
		}
	}
	
	protected void onRecordBtnStatusChanged(boolean bPressDown){
		
	}
	
	protected void onChangeCancelStatus(){
		mImageViewRecordPromt.setImageResource(R.drawable.image_talkcancel);
	}
	
	protected void setRecordPromptDisplayChild(int nWhich){
		if(nWhich == 0){
			mProgressBarRecordPrepare.setVisibility(View.VISIBLE);
			mImageViewRecordPromt.setVisibility(View.GONE);
		}else{
			mProgressBarRecordPrepare.setVisibility(View.GONE);
			mImageViewRecordPromt.setVisibility(View.VISIBLE);
		}
	}
	
	protected void showPopupWindow(){
		mImageViewRecordPromt.setImageBitmap(null);
		mPopupWindowRecordPrompt.showAtLocation(mBtnPressTalk, Gravity.CENTER, 0, 0);
	}
	
	protected void processStopRecord(){
		if (mNeedStop) {
			setRecordPromptDisplayChild(1);
			if (mRecordSuccess) {
				mMediaRecordManager.stopRecord();
			} else {
				mPopupWindowRecordPrompt.dismiss();
			}
			mNeedStop = false;
			
			onRecordBtnStatusChanged(false);
		}
	}
	
	public static interface OnRecordListener{
		public boolean 	onRecordCheck();
		public void 	onRecordStarted();
		
		public void 	onRecordEnded(String strRecordPath);
		
		public void		onRecordFailed();
	}
	
	public static interface CancelHandler{
		public boolean isCancel(MotionEvent ev,View touchView,View tipView);
	}
	
	public static class ChatActivityCancleHandler implements CancelHandler{
		@Override
		public boolean isCancel(MotionEvent ev, View touchView,View tipView) {
			return ev.getRawY() < XApplication.getScreenHeight() * 3 / 4;
		}
	}
	
	public static class SimpleCancleHandler implements CancelHandler{
		
		protected final int 	mLocation[] = new int[2];
		protected final Rect 	mRect = new Rect();
		
		@Override
		public boolean isCancel(MotionEvent event, View touchView,View tipView) {
			tipView.getLocationOnScreen(mLocation);
			tipView.getGlobalVisibleRect(mRect);
			mRect.offsetTo(mLocation[0], mLocation[1]);
			return mRect.contains((int) event.getRawX(), (int) event.getRawY());
		}
	}
}
