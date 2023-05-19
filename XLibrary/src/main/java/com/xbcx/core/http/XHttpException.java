package com.xbcx.core.http;

import org.json.JSONObject;

import com.xbcx.core.XException;
import com.xbcx.utils.JsonParseUtils;

public class XHttpException extends XException {
	private static final long serialVersionUID = 1L;
	
	private int 	errorid;
	private String	error;
	
	public XHttpException(JSONObject jo){
		JsonParseUtils.parse(jo, this,XHttpException.class);
	}
	
	public XHttpException(int errorId,String error){
		super(error);
		this.errorid = errorId;
		this.error = error;
	}
	
	public int getErrorId(){
		return errorid;
	}
	
	@Override
	public String getMessage() {
		return error;
	}
}
