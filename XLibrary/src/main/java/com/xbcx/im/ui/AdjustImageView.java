package com.xbcx.im.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AdjustImageView extends ImageView {
	
	public AdjustImageView(Context context) {
		super(context);
	}

	public AdjustImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Drawable d = getDrawable();
		if(d != null){
			if(d.getIntrinsicWidth() != 0){
				if(d.getIntrinsicWidth() > getMeasuredWidth()){
					int height = getMeasuredWidth() * d.getIntrinsicHeight() / d.getIntrinsicWidth();
					setMeasuredDimension(getMeasuredWidth(), height);
				}else{
					setMeasuredDimension(d.getIntrinsicWidth(), d.getIntrinsicHeight());
				}
			}
		}
	}
}
