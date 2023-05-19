package com.xbcx.core;

import java.io.Serializable;

import com.xbcx.utils.JsonImplementation;

@JsonImplementation(idJsonKey="id")
public class IDObject implements Serializable,IDProtocol{
	
	private static final long serialVersionUID = 1L;
	
	protected String mId;
	
	public IDObject(String id){
		mId = id;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this){
			return true;
		}
		if(o != null && getClass().isInstance(o)){
			return getId().equals(((IDObject)o).getId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		final String id = getId();
		return id == null ? super.hashCode() : id.hashCode();
	}

	@Override
	public String getId(){
		return mId;
	}
	
	public void setId(String id){
		mId = id;
	}
}
