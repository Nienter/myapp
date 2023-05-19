package com.xbcx.im.db;

import android.database.Cursor;

import com.xbcx.core.db.XDBObjectCreator;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.IMGlobalSetting;

public class MessageCreator implements XDBObjectCreator<XMessage>{
	
	private String	mOtherSideId;
	private int		mFromType;
	
	public MessageCreator(String otherSideId,int fromType){
		mOtherSideId = otherSideId;
		mFromType = fromType;
	}

	@Override
	public XMessage createObject(Cursor c) {
		XMessage m = IMGlobalSetting.msgFactory.createXMessage(c);
		m.setFromType(mFromType);
		if(mFromType != XMessage.FROMTYPE_SINGLE){
			m.setGroupId(mOtherSideId);
		}
		return m;
	}
}
