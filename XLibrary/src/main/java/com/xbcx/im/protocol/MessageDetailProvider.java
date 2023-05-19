package com.xbcx.im.protocol;

import java.io.IOException;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class MessageDetailProvider implements PacketExtensionProvider {

	@Override
	public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
		String strSize = parser.getAttributeValue("", "size");

		final String strContent = parseContent(parser);

		return new MessageDetailExtension(Long.parseLong(strSize), strContent);
	}

	private static String parseContent(XmlPullParser parser) throws XmlPullParserException, IOException {
		String content = "";
		int parserDepth = parser.getDepth();
		while (!(parser.next() == XmlPullParser.END_TAG && parser.getDepth() == parserDepth)) {
			content += parser.getText();
		}
		return content;
	}
}
