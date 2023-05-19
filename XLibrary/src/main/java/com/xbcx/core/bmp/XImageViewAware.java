package com.xbcx.core.bmp;

import android.widget.ImageView;

import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

public class XImageViewAware extends ImageViewAware {

	private int	mMaxWidth;
	private int mMaxHeight;
	
	public XImageViewAware(ImageView imageView,int maxWidth,int maxHeight) {
		super(imageView);
		mMaxWidth = maxWidth;
		mMaxHeight = maxHeight;
	}

	@Override
	public int getWidth() {
		return mMaxWidth;
	}

	@Override
	public int getHeight() {
		return mMaxHeight;
	}
}
