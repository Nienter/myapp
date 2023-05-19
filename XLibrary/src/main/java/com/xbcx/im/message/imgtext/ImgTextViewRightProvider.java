package com.xbcx.im.message.imgtext;

import com.xbcx.im.XMessage;

public class ImgTextViewRightProvider extends ImgTextViewLeftProvider {

	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.isFromSelf() && 
				message.getType() == XMessage.TYPE_IMGTEXT){
			return true;
		}
		return false;
	}
}
