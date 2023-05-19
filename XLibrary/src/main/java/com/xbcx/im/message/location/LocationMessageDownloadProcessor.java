package com.xbcx.im.message.location;

import java.util.Locale;

import com.xbcx.core.Event;
import com.xbcx.core.EventDelegateCanceller;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventProgressDelegate;
import com.xbcx.core.XApplication;
import com.xbcx.im.IMKernel;
import com.xbcx.im.XMessage;
import com.xbcx.im.messageprocessor.MessageDownloadProcessor;
import com.xbcx.utils.SystemUtils;

public class LocationMessageDownloadProcessor extends MessageDownloadProcessor{
	
	private int		mImageWidth;
	private int		mImageHeight;

	public LocationMessageDownloadProcessor(){
		mImageWidth = SystemUtils.dipToPixel(IMKernel.getInstance().getContext(), 150);
		mImageHeight = SystemUtils.dipToPixel(IMKernel.getInstance().getContext(), 100);
	}
	
	public int getImageWidth(){
		return mImageWidth;
	}
	
	public int getImageHeight(){
		return mImageHeight;
	}

	@Override
	protected boolean customDownload(Event event, XMessage xm, boolean bThumb) {
		final String location[] = xm.getLocation();
		final String url = String.format(Locale.getDefault(),
				XApplication.URL_GetLocationImage, 
				Double.parseDouble(location[0]),
				Double.parseDouble(location[1]),
				12,
				mImageWidth,
				mImageHeight,
				Double.parseDouble(location[0]),
				Double.parseDouble(location[1]));
		Event e = mEventManager.runEventEx(EventCode.HTTP_Download,
				new EventDelegateCanceller(event),
				new EventProgressDelegate(event),
				url,xm.getFilePath());
		event.setSuccess(e.isSuccess());
		return true;
	}

}
