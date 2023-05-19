package com.xbcx.common;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.xbcx.core.SimpleContentStatusViewProvider;

public class SimpleFrameLayoutContentStatusViewProvider extends SimpleContentStatusViewProvider {

	private FrameLayout	mParent;
	
	public SimpleFrameLayoutContentStatusViewProvider(FrameLayout parent){
		super(parent.getContext());
		mParent = parent;
	}

	@Override
	public void addContentView(View v, LayoutParams lp) {
		mParent.addView(v, lp);
	}

}
