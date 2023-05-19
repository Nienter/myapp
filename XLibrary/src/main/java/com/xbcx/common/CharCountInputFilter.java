package com.xbcx.common;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;

public class CharCountInputFilter implements InputFilter {
	
	private EditText	mEditText;
	private int			mLength;
	
	public CharCountInputFilter(EditText et,int length){
		mEditText = et;
		mLength = length;
	}

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
		try{
			char c[] = source.toString().toCharArray();
			final int oldCharCount = calculateCharCount(mEditText.getText().toString());
			final int srcCharCount = calculateCharCount(source.toString());
			int maxCharCount = Math.min(srcCharCount, mLength - oldCharCount);
			int index = 0;
			int indexCount = 0;
			while(indexCount < maxCharCount){
				if(c[index++] > 128){
					indexCount += 2;
				}else{
					++indexCount;
				}
			}
			
			return source.subSequence(0, index);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}

	private int calculateCharCount(String s){
		char cs[] = s.toString().toCharArray();
		int count = 0;
		for(char c : cs){
			if(c > 128){
				count += 2;
			}else{
				++count;
			}
		}
		return count;
	}
}
