package com.xbcx.im;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.LruCache;
import android.util.SparseArray;

import com.xbcx.core.XApplication;
import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.im.ui.TextMessageImageCoder;
import com.xbcx.library.R;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionCoding implements TextMessageImageCoder{
	
	public static ExpressionCoding getInstance(){
		return sInstance;
	}
	
	private static ExpressionCoding sInstance;
	
	private static int EXPRESSION_CODING[];

	private static final SparseArray<String> mMapResIdToCoding = new SparseArray<String>();
	
	private static final HashMap<String, Integer> mMapCodingToResId = new HashMap<String, Integer>();
	
	private static boolean mInit = false;
	
	private static Pattern mPattern = Pattern.compile("\\[([a-zA-Z0-9\\u4e00-\\u9fa5]+?)\\]");
	
	private static LruCache<String, SpannableStringBuilder> mMapContentToSpannable = new LruCache<String, SpannableStringBuilder>(50);
	
	public static SpannableStringBuilder spanAllExpression(String content){
		if(content == null){
			return new SpannableStringBuilder();
		}
		SpannableStringBuilder ssb = mMapContentToSpannable.get(content);
		if(ssb == null){
			ssb = new SpannableStringBuilder(content);
			mMapContentToSpannable.put(content, ssb);
			
			for(TextMessageImageCoder codec : IMGlobalSetting.textMsgImageCodeces){
				if(!codec.isSingleDrawable()){
					ssb = codec.spanMessage(ssb);
				}
			}
		}
		
		return ssb;
	}
	
	public ExpressionCoding(){
		sInstance = this;
	}
	
	private void init(){
		if(mInit){
			return;
		}
		mInit = true;
		final int nCount = 90;
		EXPRESSION_CODING = new int[nCount];
		final String strPackageName = XApplication.getApplication().getPackageName();
		final Resources res = XApplication.getApplication().getResources();
		for(int nIndex = 0;nIndex < nCount;++nIndex){
			int nResId = res.getIdentifier(String.format("smiley_%d", nIndex), 
					"drawable", strPackageName);
			EXPRESSION_CODING[nIndex] = nResId;
		}
		
		String strCodings[] = XApplication.getApplication().getResources()
				.getStringArray(R.array.expression_coding);
		int nIndex = 0;
		for(int resId : EXPRESSION_CODING){
			mMapCodingToResId.put(strCodings[nIndex], resId);
			mMapResIdToCoding.put(resId, strCodings[nIndex]);
			++nIndex;
		}
	}

	@Override
	public boolean isSingleDrawable() {
		return false;
	}

	@Override
	public SpannableStringBuilder spanMessage(SpannableStringBuilder ssb) {
		init();
		Matcher matcher = mPattern.matcher(ssb.toString());
		while(matcher.find()){
			Drawable d = decode(matcher.group());
			if(d != null){
				d.setBounds(0, 0, (int)(d.getIntrinsicWidth() * 0.6f),
						(int)(d.getIntrinsicHeight() * 0.6f));
				ssb.setSpan(new ImageSpan(d,ImageSpan.ALIGN_BOTTOM), 
								matcher.start(),
								matcher.end(), 
								SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return ssb;
	}

	@Override
	public int[] getResIds() {
		init();
		return EXPRESSION_CODING;
	}

	@Override
	public Drawable decode(String code) {
		init();
		Integer id = mMapCodingToResId.get(code);
		if(id != null){
			return XApplication.getApplication().getResources().getDrawable(
					id);
		}
		return null;
	}

	@Override
	public Drawable decodeSmall(String code) {
		return decode(code);
	}

	@Override
	public String getCode(int resId) {
		init();
		return mMapResIdToCoding.get(resId);
	}

	@Override
	public boolean canDecode(String text) {
		return mMapCodingToResId.containsKey(text);
	}

	@Override
	public boolean pauseDrawable() {
		return false;
	}

	@Override
	public boolean resumeDrawable() {
		return false;
	}
}
