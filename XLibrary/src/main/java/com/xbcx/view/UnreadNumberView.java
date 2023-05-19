package com.xbcx.view;

import com.xbcx.library.R;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

public class UnreadNumberView extends TextView {
	
	public UnreadNumberView(Context context) {
		super(context);
		init();
		
	}

	public UnreadNumberView(Context context, AttributeSet attrs) {
		super(context,attrs);
		init();
	}
	
	private void init(){
		setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		setTextColor(0xffffffff);
		setBackgroundResource(R.drawable.background_unreadmessagecount);
		setGravity(Gravity.CENTER);
		setSingleLine();
		setTypeface(Typeface.DEFAULT_BOLD);
	}
}
