package com.xbcx.im.ui;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;

import androidx.viewpager.widget.ViewPager;

import com.xbcx.im.ExpressionCoding;

public class EditViewQQExpressionProvider extends BaseEditViewExpressionProvider implements
														ViewPager.OnPageChangeListener,
														AdapterView.OnItemClickListener{
	
	@Override
	public View createTabButton(Context context) {
		return null;
	}

	@Override
	protected TextMessageImageCoder getImageCoder() {
		return ExpressionCoding.getInstance();
	}
}
