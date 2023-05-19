package com.xbcx.im.message.video;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Body;

import android.content.Context;

import com.xbcx.core.ActivityBasePlugin;
import com.xbcx.core.BaseActivity;
import com.xbcx.im.MessageTypeProcessor;
import com.xbcx.im.XMessage;
import com.xbcx.im.MessagePlugin.PluginConfig;
import com.xbcx.im.messageprocessor.MessageDownloadProcessor;
import com.xbcx.im.messageprocessor.MessageUploadProcessor;
import com.xbcx.im.recentchat.ContentProvider;
import com.xbcx.im.ui.ChatActivity.AddMessageViewProviderPlugin;
import com.xbcx.im.ui.ChatActivity.ChatActivityInitFinishPlugin;
import com.xbcx.im.ui.ChatActivity.MessageOpener;
import com.xbcx.im.ui.ChatActivity;
import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.im.ui.IMMessageAdapter;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

public class VideoMessagePluginConfig extends PluginConfig implements
									ContentProvider,
									AddMessageViewProviderPlugin,
									ChatActivityInitFinishPlugin,
									MessageTypeProcessor{

	protected int				mChooseType;
	
	public VideoMessagePluginConfig() {
		super(XMessage.TYPE_VIDEO);
		setBodyType("videolink");
		IMGlobalSetting.setMessageTypeForwardable(mMessageType);
	}
	
	public VideoMessagePluginConfig setChooseType(int chooseType){
		mChooseType = chooseType;
		return this;
	}

	@Override
	public void onBuildSendXmlAttribute(Message message, XMessage xm,Body body) {
		body.attributes.addAttribute("size", String.valueOf(xm.getVideoSeconds()));
		body.attributes.addAttribute("length", String.valueOf(xm.getVideoSeconds()));
		body.attributes.addAttribute("originurl", xm.getVideoDownloadUrl());
		body.attributes.addAttribute("displayname", xm.getDisplayName());
	}

	@Override
	public void onParseReceiveAttribute(XMessage xm, Message m, Body body) {
		xm.setVideoDownloadUrl(body.attributes.getAttributeValue("originurl"));
		xm.setVideoSeconds(SystemUtils.safeParseInt(body.attributes.getAttributeValue("length")));
	}

	@Override
	public void onAddMessageViewProvider(IMMessageAdapter adapter) {
		adapter.addIMMessageViewProvider(new VideoViewLeftProvider());
		adapter.addIMMessageViewProvider(new VideoViewRightProvider());
	}

	@Override
	public String getContent(Context context, XMessage xm) {
		return context.getString(R.string.video);
	}
	
	@Override
	public void onChatActivityInitFinish(ChatActivity activity) {
		activity.registerMessageOpener(mMessageType, new VideoMessageOpener());
	}
	
	@Override
	public MessageTypeProcessor createMessageTypeProcessor() {
		return this;
	}
	
	@Override
	public ContentProvider createRecentChatContentProvider() {
		return this;
	}
	
	@Override
	public MessageDownloadProcessor createMessageDownloadProcessor() {
		return new VideoMessageDownloadProcessor();
	}
	
	@Override
	public MessageUploadProcessor createMessageUploadProcessor() {
		return new MessageUploadProcessor();
	}

	@Override
	public ActivityBasePlugin createChatActivityPlugin(ChatActivity activity) {
		activity.registerChooseProvider(new IMChooseVideoProvider(BaseActivity.RequestCode_LaunchChooseVideo)
		.setTitle(activity.getString(R.string.video))
		.setChooseType(mChooseType));
		return this;
	}
	
	public static class VideoMessageOpener implements MessageOpener{

		@Override
		public void onOpenMessage(XMessage xm, ChatActivity activity) {
			if(!xm.isDownloading()){
				if(xm.isFileExists()){
					activity.viewVideo(xm);
				}else{
					MessageDownloadProcessor.getMessageDownloadProcessor(XMessage.TYPE_VIDEO)
						.requestDownload(xm, false);
				}
			}
		}
	}

}
