package com.xbcx.im.message.voice;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Body;

import android.content.Context;

import com.xbcx.core.ActivityBasePlugin;
import com.xbcx.im.MessageTypeProcessor;
import com.xbcx.im.XMessage;
import com.xbcx.im.MessagePlugin.PluginConfig;
import com.xbcx.im.messageprocessor.MessageDownloadProcessor;
import com.xbcx.im.messageprocessor.MessageUploadProcessor;
import com.xbcx.im.messageprocessor.VoicePlayProcessor;
import com.xbcx.im.recentchat.ContentProvider;
import com.xbcx.im.ui.ChatActivity;
import com.xbcx.im.ui.IMMessageAdapter;
import com.xbcx.im.ui.ChatActivity.AddMessageViewProviderPlugin;
import com.xbcx.im.ui.ChatActivity.ChatActivityInitFinishPlugin;
import com.xbcx.im.ui.ChatActivity.MessageOpener;
import com.xbcx.library.R;

public class VoiceMessagePluginConfig extends PluginConfig implements
												ContentProvider,
												AddMessageViewProviderPlugin,
												ChatActivityInitFinishPlugin,
												MessageTypeProcessor{

	public VoiceMessagePluginConfig() {
		super(XMessage.TYPE_VOICE);
		setBodyType("vflink");
	}

	@Override
	public void onBuildSendXmlAttribute(Message message, XMessage xm,Body body) {
		body.setMessage(xm.getVoiceDownloadUrl());
		body.attributes.addAttribute("size", String.valueOf(xm.getVoiceFrameCount()));
	}

	@Override
	public void onParseReceiveAttribute(XMessage xm, Message m, Body body) {
		final String strSize = body.attributes.getAttributeValue("size");
		xm.setVoiceFrameCount(Integer.parseInt(strSize));
		xm.setVoiceDownloadUrl(xm.getContent());
	}
	
	@Override
	public void onAddMessageViewProvider(IMMessageAdapter adapter) {
		adapter.addIMMessageViewProvider(new VoiceViewLeftProvider(adapter.getContext()));
		adapter.addIMMessageViewProvider(new VoiceViewRightProvider(adapter.getContext()));
	}
	
	@Override
	public String getContent(Context context, XMessage xm) {
		return context.getString(R.string.voice);
	}

	@Override
	public void onChatActivityInitFinish(ChatActivity activity) {
		activity.registerMessageOpener(mMessageType, new VoiceMessageOpener());
	}

	@Override
	public MessageTypeProcessor createMessageTypeProcessor() {
		return this;
	}
	
	@Override
	public MessageDownloadProcessor createMessageDownloadProcessor() {
		return new MessageDownloadProcessor();
	}
	
	@Override
	public MessageUploadProcessor createMessageUploadProcessor() {
		return new MessageUploadProcessor();
	}
	
	@Override
	public ContentProvider createRecentChatContentProvider() {
		return this;
	}

	@Override
	public ActivityBasePlugin createChatActivityPlugin(ChatActivity activity) {
		return this;
	}
	
	public static class VoiceMessageOpener implements MessageOpener{
		@Override
		public void onOpenMessage(XMessage xm, ChatActivity activity) {
			if(VoicePlayProcessor.getInstance().isPlaying(xm)){
				VoicePlayProcessor.getInstance().stop();
			}else{
				VoicePlayProcessor.getInstance().play(xm);
			}
		}
	}
}
