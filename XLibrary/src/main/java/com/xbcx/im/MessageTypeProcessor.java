package com.xbcx.im;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Body;

public interface MessageTypeProcessor {
	
	public void onBuildSendXmlAttribute(Message message,XMessage xm,Body body);
	
	public void onParseReceiveAttribute(XMessage xm,Message m,Body body);
}
