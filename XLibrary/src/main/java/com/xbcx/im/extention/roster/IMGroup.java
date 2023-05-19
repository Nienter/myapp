package com.xbcx.im.extention.roster;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import com.xbcx.core.NameObject;
import com.xbcx.im.IMKernel;

public class IMGroup extends NameObject {
	
	private static final long serialVersionUID = 1L;
	
	public static final String ROLE_ADMIN 	= "1";
	public static final String ROLE_NORMAL 	= "2";
	
	private HashMap<String, IMContact> 	mMapIdToContact = new HashMap<String, IMContact>();
	private HashMap<String, String> 	mMapIdToRole	= new HashMap<String, String>();

	public IMGroup(String id,String name) {
		super(id);
		setName(name);
	}

	public Collection<IMContact> getMembers(){
		return Collections.unmodifiableCollection(mMapIdToContact.values());
	}
	
	public boolean	hasMember(String userId){
		return mMapIdToContact.containsKey(userId);
	}
	
	public int	getMemberCount(){
		return mMapIdToContact.size();
	}
	
	public String getMemberRole(String id){
		String role = mMapIdToRole.get(id);
		if(role == null){
			role = ROLE_NORMAL;
		}
		return role;
	}
	
	public boolean isSelfAdmin(){
		return ROLE_ADMIN.equals(getMemberRole(IMKernel.getLocalUser()));
	}
	
	void addMember(IMContact contact){
		mMapIdToContact.put(contact.getId(), contact);
	}
	
	void removeMember(String id){
		mMapIdToContact.remove(id);
		mMapIdToRole.remove(id);
	}
	
	void setRole(String id,String role){
		mMapIdToRole.put(id, role);
	}
}
