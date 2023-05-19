package com.xbcx.im.message.photo;

import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventDelegateCanceller;
import com.xbcx.core.EventProgressDelegate;
import com.xbcx.im.XMessage;
import com.xbcx.im.messageprocessor.MessageUploadProcessor;

import android.text.TextUtils;

public class PhotoMessageUploadProcessor extends MessageUploadProcessor{
	
	@Override
	protected boolean onUpload(Event event, XMessage xm, UploadInfo ui) throws Exception {
		final String type = getUploadType(xm);
		if(!TextUtils.isEmpty(type)){
			EventDelegateCanceller canceller = new EventDelegateCanceller(event);
			Event e = mEventManager.runEventEx(
					EventCode.HTTP_PostFile,canceller,
					new EventProgressDelegate(event),
					type,xm.getFilePath());
			if(e.isSuccess()){
				xm.setUrl((String)e.getReturnParamAtIndex(0));
				xm.setThumbUrl((String)e.getReturnParamAtIndex(1));
				return true;
			}
		}
		return false;
	}
}
