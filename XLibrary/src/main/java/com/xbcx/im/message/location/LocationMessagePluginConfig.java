package com.xbcx.im.message.location;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Body;

import com.xbcx.core.ActivityBasePlugin;
import com.xbcx.im.MessageTypeProcessor;
import com.xbcx.im.XMessage;
import com.xbcx.im.MessagePlugin.PluginConfig;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.im.ui.ChatActivity;
import com.xbcx.im.ui.ChatActivity.AddMessageViewProviderPlugin;
import com.xbcx.im.ui.ChatActivity.ChatActivityInitFinishPlugin;
import com.xbcx.im.ui.ChatActivity.MessageOpener;
import com.xbcx.im.ui.IMMessageAdapter;

public class LocationMessagePluginConfig extends PluginConfig implements
										AddMessageViewProviderPlugin,
										ChatActivityInitFinishPlugin,
										MessageTypeProcessor{

	public LocationMessagePluginConfig() {
		super(XMessage.TYPE_LOCATION);
		setBodyType("locationlink");
	}

	@Override
	public void onBuildSendXmlAttribute(Message message, XMessage xm,Body body) {
		String s[] = xm.getLocation();
		if(s != null && s.length > 1){
			body.attributes.addAttribute("lat", s[0]);
			body.attributes.addAttribute("lng", s[1]);
		}
	}

	@Override
	public void onParseReceiveAttribute(XMessage xm, Message m, Body body) {
		xm.setLocation(Double.parseDouble(body.attributes.getAttributeValue("lat")), 
				Double.parseDouble(body.attributes.getAttributeValue("lng")));
	}

	@Override
	public void onAddMessageViewProvider(IMMessageAdapter adapter) {
		adapter.addIMMessageViewProvider(new LocationMessageLeftProvider());
		adapter.addIMMessageViewProvider(new LocationMessageRightProvider());
	}

	@Override
	public void onChatActivityInitFinish(ChatActivity activity) {
		activity.registerMessageOpener(mMessageType, new LocationMessageOpener());
	}
	

	@Override
	public MessageTypeProcessor createMessageTypeProcessor() {
		return this;
	}

	@Override
	public ActivityBasePlugin createChatActivityPlugin(ChatActivity activity) {
		return this;
	}

	public static class LocationMessageOpener implements MessageOpener{
		@Override
		public void onOpenMessage(XMessage xm, ChatActivity activity) {
			ActivityType.launchLocationMessageActivity(activity, xm);
		}
	}
}
