package com.xbcx.core;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;


public class SimpleActivityContentStatusViewProvider extends SimpleContentStatusViewProvider {

	protected Activity			mActivity;
	
	public SimpleActivityContentStatusViewProvider(Activity activity) {
		super(activity);
		mActivity = activity;
	}

	@Override
	public void addContentView(View v, LayoutParams lp) {
		mActivity.addContentView(v, lp);
	}
	
	@Override
	public int getTopMargin(View statusView) {
		if(mTopMarginProvider == null){
			if(mActivity instanceof BaseActivity){
				final BaseActivity ba = (BaseActivity)mActivity;
				if(ba.getBaseScreen().hasTitle()){
					return ba.getBaseScreen().getViewTitle().getHeight();
				}
			}
		}
		return super.getTopMargin(statusView);
	}
}
