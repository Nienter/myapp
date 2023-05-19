package com.xbcx.core;

public abstract class XException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public XException() {
	}
	
	public XException(String message){
		super(message);
	}
}
