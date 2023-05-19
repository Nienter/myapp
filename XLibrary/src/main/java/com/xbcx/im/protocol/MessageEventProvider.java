package com.xbcx.im.protocol;

import org.jivesoftware.smack.packet.MessageEvent;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.MessageEvent.Member;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class MessageEventProvider implements PacketExtensionProvider {

	@Override
	public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
		MessageEvent messageEvent = new MessageEvent();
		messageEvent.mAttris.parserAttribute(parser);
		boolean done = false;
		boolean setContent = true;
        while (!done) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
            	setContent = false;
                String elementName = parser.getName();
                if (elementName.equals("member")) {
                	final Member m = new Member();
                	m.mAttris.parserAttribute(parser);
                	messageEvent.addMember(m);
                }
            }
            else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("event")) {
                    done = true;
                }
            }else if(eventType == XmlPullParser.TEXT){
            	if(setContent){
            		messageEvent.setContent(parser.getText());
            	}
            }
        }
		return messageEvent;
	}
}
