package com.xbcx.im.vcard;

import android.widget.ImageView;

import com.xbcx.core.PicUrlObject;
import com.xbcx.core.XApplication;
import com.xbcx.library.R;

public class SimpleVCardAvatarLoader extends VCardAvatarLoader<PicUrlObject> {

	@Override
	public void onUpdateView(ImageView holder, String item, PicUrlObject result) {
		XApplication.setBitmap(holder, result.getPicUrl(), R.drawable.avatar_user);
	}

}
