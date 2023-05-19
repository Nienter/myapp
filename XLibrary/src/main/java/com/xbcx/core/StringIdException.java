package com.xbcx.core;

public class StringIdException extends XException {

	private static final long serialVersionUID = 1L;

	private int	mStringId;
	
	public StringIdException(int stringId){
		mStringId = stringId;
	}
	
	public int getStringId(){
		return mStringId;
	}
	
	@Override
	public String getMessage() {
		return XApplication.getApplication().getString(mStringId);
	}
}
