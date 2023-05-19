package com.xbcx.im.extention.roster;

import java.util.Collection;


public interface RosterInterface {

	public boolean isSelfInGroup(String groupId);
	
	public Collection<IMContact> getFriends();
	
	public Collection<IMGroup> getGroups();
	
	public boolean isFriend(String userId);
	
	public IMGroup getGroup(String groupId);
	
	public IMContact getContact(String userId);
}
