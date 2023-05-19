package com.xbcx.im.ui.messageviewprovider;

import android.app.Activity;

import com.xbcx.im.XMessage;

public class TextViewRightProvider extends TextViewLeftProvider {

	public TextViewRightProvider(Activity activity) {
		super(activity);
	}

	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.getType() == XMessage.TYPE_TEXT){
			XMessage hm = (XMessage)message;
			return hm.isFromSelf();
		}
		return false;
	}
}
