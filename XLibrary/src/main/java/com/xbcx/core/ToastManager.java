package com.xbcx.core;

import com.xbcx.library.R;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;

public class ToastManager {

	private static ToastManager sInstance;
	
	private ToastCreator	mToastCreator;
	
	private long			mNetworkErrorTipLastTime;
	
	private Toast 			sToastLast;
	private int 			sResIdLast;
	private String			sStringLast;
	private long 			sShowTimeLast;
	
	private Context 		mContext;
	private final Handler 	mHandler;
	
	public static ToastManager getInstance(Context context){
		if(sInstance == null){
			sInstance = new ToastManager();
			sInstance.mContext = context.getApplicationContext();
		}
		return sInstance;
	}
	
	private ToastManager(){
		mHandler = new Handler(Looper.getMainLooper());
	}
	
	public void setToastCreator(ToastCreator creator){
		mToastCreator = creator;
	}
	
	private Runnable mRunnable = new Runnable() {
		public void run() {
			sToastLast = null;
			if(mToastCreator != null){
				sToastLast = mToastCreator.createToast(mContext, mContext.getString(sResIdLast), Toast.LENGTH_SHORT);
			}
			if(sToastLast == null){
				sToastLast = Toast.makeText(mContext, sResIdLast, Toast.LENGTH_SHORT);
			}
			sToastLast.show();
			sShowTimeLast = System.currentTimeMillis();
		}
	};
	
	private Runnable mRunnableString = new Runnable() {
		@Override
		public void run() {
			sToastLast = null;
			if(mToastCreator != null){
				sToastLast = mToastCreator.createToast(mContext, sStringLast, Toast.LENGTH_SHORT);
			}
			if(sToastLast == null){
				sToastLast = Toast.makeText(mContext, sStringLast, Toast.LENGTH_SHORT);
			}
			sToastLast.show();
			sShowTimeLast = System.currentTimeMillis();
		}
	};
	
	private Runnable mRunnableNetworkErrorTipRunnable = new Runnable() {
		@Override
		public void run() {
			final long time = SystemClock.elapsedRealtime();
			if(time - mNetworkErrorTipLastTime > 2000){
				Toast t = new Toast(mContext);
				t.setDuration(2000);
				t.setGravity(Gravity.CENTER, 0, 0);
				ImageView iv = new ImageView(mContext);
				iv.setImageResource(R.drawable.tip_error_network);
				t.setView(iv);
				t.show();
				mNetworkErrorTipLastTime = time;
			}
		}
	};
	
	public void show(int nResId){
		if(nResId == sResIdLast){
			if(System.currentTimeMillis() - sShowTimeLast < 2000){
				return;
			}
		}
//		if(sToastLast != null){
//			sToastLast.cancel();
//		}
		
		sResIdLast = nResId;
		mHandler.removeCallbacks(mRunnable);
		mHandler.post(mRunnable);
	}
	
	public void show(final String strText){
		if(TextUtils.isEmpty(strText)){
			return;
		}
		if(strText.equals(sStringLast)){
			if(System.currentTimeMillis() - sShowTimeLast < 2000){
				return;
			}
		}
		
//		if(sToastLast != null){
//			sToastLast.cancel();
//		}
		
		sStringLast = strText;
		mHandler.removeCallbacks(mRunnableString);
		mHandler.post(mRunnableString);
	}
	
	public void showNetworkErrorTip(){
		mHandler.removeCallbacks(mRunnableNetworkErrorTipRunnable);
		mHandler.post(mRunnableNetworkErrorTipRunnable);
	}
	
	public static interface ToastCreator{
		public Toast createToast(Context context, CharSequence text, int duration);
	}
}
