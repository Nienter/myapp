package com.xbcx.view;

import android.content.Context;
import android.util.AttributeSet;

public class RoundAngleSquareImageView extends RoundAngleImageView {
	
	public RoundAngleSquareImageView(Context context) {
		super(context);
	}

	public RoundAngleSquareImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
	}
}
