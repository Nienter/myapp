package com.xbcx.im.ui;

import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;

public interface TextMessageImageCoder {
	
	public boolean					isSingleDrawable();
	
	public SpannableStringBuilder	spanMessage(SpannableStringBuilder ssb);
	
	public int[]					getResIds();
	
	public Drawable					decode(String code);
	
	public Drawable					decodeSmall(String code);
	
	public String					getCode(int resId);
	
	public boolean					canDecode(String text);
	
	public boolean					pauseDrawable();
	
	public boolean					resumeDrawable();
}
