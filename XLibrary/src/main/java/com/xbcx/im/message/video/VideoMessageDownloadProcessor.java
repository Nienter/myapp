package com.xbcx.im.message.video;

import com.xbcx.im.messageprocessor.MessageDownloadProcessor;

public class VideoMessageDownloadProcessor extends MessageDownloadProcessor{

	@Override
	public boolean hasThumb() {
		return true;
	}
}
