package com.xbcx.im.ui.messageviewprovider;

import com.xbcx.core.XApplication;
import com.xbcx.im.XMessage;
import com.xbcx.library.R;

import android.app.Activity;

public class DefaultTypeViewLeftProvider extends TextViewLeftProvider {

	public DefaultTypeViewLeftProvider(Activity activity) {
		super(activity);
	}

	@Override
	protected void onUpdateView(TextViewHolder viewHolder, XMessage m) {
		super.onUpdateView(viewHolder, m);
		String tip = "\n" + "[" + XApplication.getApplication().getString(R.string.message_nonrecognition) + "]";
		viewHolder.mTextView.append(tip);
	}
}
