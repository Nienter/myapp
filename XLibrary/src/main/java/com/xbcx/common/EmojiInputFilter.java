package com.xbcx.common;

import com.xbcx.core.ToastManager;
import com.xbcx.core.XApplication;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

import android.text.InputFilter;
import android.text.Spanned;

public class EmojiInputFilter implements InputFilter {

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
		if(SystemUtils.hasEmoji(source)){
			ToastManager.getInstance(XApplication.getApplication()).show(R.string.toast_cannot_send_emoji);
			return "";
		}
		return null;

	}

}
