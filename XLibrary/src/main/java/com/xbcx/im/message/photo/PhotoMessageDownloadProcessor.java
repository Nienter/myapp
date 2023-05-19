package com.xbcx.im.message.photo;

import com.xbcx.im.messageprocessor.MessageDownloadProcessor;

public class PhotoMessageDownloadProcessor extends MessageDownloadProcessor{
	
	@Override
	public boolean hasThumb() {
		return true;
	}
}
