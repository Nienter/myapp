package com.xbcx.im;

import com.xbcx.core.ActivityBasePlugin;
import com.xbcx.core.XApplication;
import com.xbcx.im.messageprocessor.MessageDownloadProcessor;
import com.xbcx.im.messageprocessor.MessageUploadProcessor;
import com.xbcx.im.recentchat.ContentProvider;
import com.xbcx.im.recentchat.XMessageRecentChatProvider;
import com.xbcx.im.ui.ChatActivity;
import com.xbcx.im.ui.ChatActivityCreatePlugin;

public class MessagePlugin implements
								ChatActivityCreatePlugin{
	
	private PluginConfig	mConfig;
	
	public MessagePlugin(PluginConfig pc){
		mConfig = pc;
		final int msgType = pc.mMessageType;
		XMessageRecentChatProvider.registerMessageTypeContentProvider(
				msgType, 
				pc.createRecentChatContentProvider());
		MessageDownloadProcessor.registerMessageDownloadProcessor(
				msgType, 
				pc.createMessageDownloadProcessor());
		MessageUploadProcessor.registerMessageUploadProcessor(
				msgType, 
				pc.createMessageUploadProcessor());
		XApplication.addManager(this);
		
		IMKernel.getInstance().addBodyType(mConfig.mMessageType, mConfig.mBodyType);
		IMKernel.getInstance().registerMessageTypeProcessor(mConfig.mMessageType, mConfig.createMessageTypeProcessor());
	}
	
	@Override
	public void onChatActivityCreated(ChatActivity ca) {
		final ActivityBasePlugin plugin = mConfig.createChatActivityPlugin(ca);
		if(plugin != null){
			ca.registerPlugin(plugin);
		}
	}
	
	public static abstract class PluginConfig{
		public int									mMessageType;
		public String								mBodyType;
		
		public PluginConfig(int msgType){
			mMessageType = msgType;
		}
		
		public PluginConfig setBodyType(String bodyType){
			mBodyType = bodyType;
			return this;
		}
		
		public abstract MessageTypeProcessor createMessageTypeProcessor();
		
		public ContentProvider createRecentChatContentProvider(){
			return null;
		}
		
		public MessageDownloadProcessor createMessageDownloadProcessor(){
			return null;
		}
		
		public MessageUploadProcessor createMessageUploadProcessor(){
			return null;
		}
		
		public abstract ActivityBasePlugin createChatActivityPlugin(ChatActivity activity);
	}
}
