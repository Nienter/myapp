package com.xbcx.im.vcard;

import android.widget.TextView;

import com.xbcx.core.PicUrlObject;

public class SimpleVCardNameLoader extends VCardNameLoader<PicUrlObject> {

	@Override
	public void onUpdateView(TextView holder, String item, PicUrlObject result) {
		holder.setText(result.getName());
	}
}
