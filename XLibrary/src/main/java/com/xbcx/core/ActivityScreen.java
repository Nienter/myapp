package com.xbcx.core;

import com.xbcx.core.BaseActivity.BaseAttribute;
import com.xbcx.library.R;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class ActivityScreen extends BaseScreen{
	
	protected Activity			mActivity;
	protected onTitleListener	mOnTitleListener;

	public ActivityScreen(Activity activity, BaseAttribute ba) {
		super(activity, ba);
		mActivity = activity;
	}
	
	public ActivityScreen setOnTitleListener(onTitleListener listener){
		mOnTitleListener = listener;
		return this;
	}
	
	@Override
	public void initTitle() {
		mViewTitle = (XTitleView)mActivity.findViewById(R.id.viewTitle);
		super.initTitle();
		if(mOnTitleListener != null){
			mOnTitleListener.onTitleInited();
		}
	}
	
	@Override
	public void setContentView(int layoutResId) {
		mActivity.setContentView(layoutResId);
		initTitle();
	}
	
	@Override
	public void setContentView(View view, LayoutParams params) {
		mActivity.setContentView(view, params);
		initTitle();
	}
	
	@Override
	public void addContentView(View view, LayoutParams params) {
		mActivity.addContentView(view, params);
	}

	@Override
	public Context getDialogContext() {
		Activity context = mActivity;
		Activity parent = context.getParent();
		while(parent != null){
			context = parent;
			parent = context.getParent();
		}
		return context;
	}

	@Override
	public void onClick(View v) {
		if(v == mButtonBack){
			mActivity.onBackPressed();
		}
	}
	
	public static interface onTitleListener{
		public void onTitleInited();
	}
}
