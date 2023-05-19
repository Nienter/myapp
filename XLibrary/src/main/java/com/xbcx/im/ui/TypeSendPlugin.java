package com.xbcx.im.ui;

public class TypeSendPlugin extends SendPlugin {
	
	protected int	mType;
	
	public TypeSendPlugin(String id,int icon,int type){
		super(id,icon);
		mType = type;
	}

	@Override
	public int getSendType() {
		return mType;
	}

	@Override
	public void onSend(ChatActivity activity) {
	}
}
