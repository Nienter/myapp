package com.xbcx.im.extention.roster;

import com.xbcx.core.NameObject;

public class IMContact extends NameObject{
	
	private static final long serialVersionUID = 1L;

	public IMContact(String strId,String strName){
		super(strId);
		setName(strName);
	}
}
