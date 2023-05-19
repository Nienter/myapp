package com.xbcx.common;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.View.MeasureSpec;

public class XTabIndicatorAnimation {
	
	private View	mViewIndicator;
	private int		mTabWidth;
	private int		mCurrentTab;
	
	private int		mOffset;

	public XTabIndicatorAnimation(View viewIndicator,int tabWidth){
		mViewIndicator = viewIndicator;
		mTabWidth = tabWidth;
		mViewIndicator.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		if(mViewIndicator.getMeasuredWidth() < tabWidth){
			mOffset = (tabWidth - mViewIndicator.getMeasuredWidth()) / 2;
		}
	}
	
	public void onTabChanged(int tab){
		ObjectAnimator.ofFloat(mViewIndicator, "translationX", 
				mCurrentTab * mTabWidth + mOffset, tab * mTabWidth + mOffset)
		.setDuration(200).start();
		mCurrentTab = tab;
	}
}
