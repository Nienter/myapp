package com.xbcx.im.extention.blacklist;

public final class VerifyType {
	
	public static final VerifyType TYPE_NONE 	= new VerifyType("0");
	public static final VerifyType TYPE_AUTH 	= new VerifyType("1");
	public static final VerifyType TYPE_FORBID 	= new VerifyType("2");
	
	private final String mType;
	
	private VerifyType(String strType){
		mType = strType;
	}
	
	public static VerifyType valueOf(String strType){
		if("0".equals(strType)){
			return TYPE_NONE;
		}else if("1".equals(strType)){
			return TYPE_AUTH;
		}else if("2".equals(strType)){
			return TYPE_FORBID;
		}else{
			return TYPE_NONE;
		}
	}
	
	public String getValue(){
		return mType;
	}
}
