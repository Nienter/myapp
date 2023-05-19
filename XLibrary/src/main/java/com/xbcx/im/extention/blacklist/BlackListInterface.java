package com.xbcx.im.extention.blacklist;

import java.util.Collection;


public interface BlackListInterface {

	public VerifyType getVerifyType();
	
	public Collection<String> getBlackLists();
	
	public boolean isInBlackList(String userId);
}
