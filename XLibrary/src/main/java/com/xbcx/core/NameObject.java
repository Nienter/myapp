package com.xbcx.core;

import com.xbcx.im.vcard.VCardProvider.NameProtocol;

import android.text.TextUtils;

public class NameObject extends IDObject implements 
									Comparable<NameObject>,
									NameProtocol{

	private static final long serialVersionUID = 1L;
	
	private String mName;
	
	public NameObject(String id) {
		super(id);
	}
	
	public NameObject(String id,String name){
		super(id);
		mName = name;
	}

	@Override
	public String getName(){
		return mName == null ? "" : mName;
	}
	
	@Override
	public void	setName(String name){
		mName = name;
	}
	
	@Override
	public int compareTo(NameObject another) {
		final String strNameR = another.getName();
		if(TextUtils.isEmpty(mName)){
			return -1;
		}
		if(TextUtils.isEmpty(strNameR)){
			return 1;
		}
		int nRet = mName.compareTo(strNameR);
		if(nRet == 0){
			nRet = -1;
		}
		return nRet;
	}
}
