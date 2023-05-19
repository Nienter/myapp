package com.xbcx.im.protocol;

import org.jivesoftware.smack.packet.PacketExtension;

public class MessageDetailExtension implements PacketExtension {

	private final long 		mSize;
	private final String 	mContent;
	
	public MessageDetailExtension(long nSize,String strContent){
		mSize = nSize;
		mContent = strContent;
	}
	
	@Override
	public String getElementName() {
		return "detail";
	}

	@Override
	public String getNamespace() {
		return "jabber:client";
	}
	
	public String getContent(){
		return mContent;
	}
	
	public long	getSize(){
		return mSize;
	}

	@Override
	public String toXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<detail size = \"").append(mSize).append("\">").append(mContent)
		.append("</detail>");
		return buf.toString();
	}

}
