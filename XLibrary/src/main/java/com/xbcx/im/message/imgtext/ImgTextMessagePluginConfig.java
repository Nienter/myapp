package com.xbcx.im.message.imgtext;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Body;

import com.xbcx.core.ActivityBasePlugin;
import com.xbcx.im.MessageTypeProcessor;
import com.xbcx.im.XMessage;
import com.xbcx.im.MessagePlugin.PluginConfig;
import com.xbcx.im.ui.ChatActivity;
import com.xbcx.im.ui.ChatActivity.AddMessageViewProviderPlugin;
import com.xbcx.im.ui.IMMessageAdapter;

public class ImgTextMessagePluginConfig extends PluginConfig implements
											AddMessageViewProviderPlugin,
											MessageTypeProcessor{

	public ImgTextMessagePluginConfig() {
		super(XMessage.TYPE_IMGTEXT);
		setBodyType("imgtextlink");
	}

	@Override
	public void onBuildSendXmlAttribute(Message message, XMessage xm, Body body) {
	}

	@Override
	public void onParseReceiveAttribute(XMessage xm, Message m, Body body) {
		xm.setImgTextSubType(body.attributes.getAttributeValue("subtype"));
		xm.setImgTextValue(body.attributes.getAttributeValue("eventvalue"));
		xm.setUrl(body.attributes.getAttributeValue("pic"));
	}

	@Override
	public void onAddMessageViewProvider(IMMessageAdapter adapter) {
		adapter.addIMMessageViewProvider(new ImgTextViewLeftProvider());
		adapter.addIMMessageViewProvider(new ImgTextViewRightProvider());
	}

	@Override
	public MessageTypeProcessor createMessageTypeProcessor() {
		return this;
	}

	@Override
	public ActivityBasePlugin createChatActivityPlugin(ChatActivity activity) {
		return this;
	}

}
