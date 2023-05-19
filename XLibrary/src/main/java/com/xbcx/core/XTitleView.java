package com.xbcx.core;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class XTitleView extends RelativeLayout {
	
	private BaseScreen	mBaseScreen;

	public XTitleView(Context context) {
		this(context, null);
	}
	
	public XTitleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public XTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	public void setBaseScreen(BaseScreen bs){
		mBaseScreen = bs;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(mBaseScreen != null){
			mBaseScreen.updateTitleMargin();
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
