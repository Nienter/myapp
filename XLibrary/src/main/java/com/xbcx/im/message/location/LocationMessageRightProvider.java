package com.xbcx.im.message.location;

import com.xbcx.im.XMessage;

public class LocationMessageRightProvider extends LocationMessageLeftProvider {

	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.isFromSelf()){
			return message.getType() == XMessage.TYPE_LOCATION;
		}
		return false;
	}
}
