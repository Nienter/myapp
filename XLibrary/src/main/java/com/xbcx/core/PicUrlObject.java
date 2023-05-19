package com.xbcx.core;

public class PicUrlObject extends NameObject {

	private static final long serialVersionUID = 1L;
	
	protected String	mPicUrl;
	
	public PicUrlObject(String id) {
		super(id);
	}
	
	public void setPicUrl(String url){
		mPicUrl = url;
	}

	public String getPicUrl(){
		return mPicUrl;
	}
}
