package com.xbcx.im.message.photo;

import java.io.File;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Body;

import android.content.Context;
import android.graphics.BitmapFactory;

import com.xbcx.core.ActivityBasePlugin;
import com.xbcx.im.MessageTypeProcessor;
import com.xbcx.im.XMessage;
import com.xbcx.im.MessagePlugin.PluginConfig;
import com.xbcx.im.messageprocessor.MessageDownloadProcessor;
import com.xbcx.im.messageprocessor.MessageUploadProcessor;
import com.xbcx.im.recentchat.ContentProvider;
import com.xbcx.im.ui.ChatActivity;
import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.im.ui.ChatActivity.AddMessageViewProviderPlugin;
import com.xbcx.im.ui.ChatActivity.ChatActivityInitFinishPlugin;
import com.xbcx.im.ui.ChatActivity.MessageOpenFilePathChecker;
import com.xbcx.im.ui.ChatActivity.MessageOpener;
import com.xbcx.im.ui.IMMessageAdapter;
import com.xbcx.library.R;
import com.xbcx.utils.FileHelper;

public class PhotoMessagePluginConfig extends PluginConfig implements
												ContentProvider,
												AddMessageViewProviderPlugin,
												ChatActivityInitFinishPlugin,
												MessageTypeProcessor{

	public PhotoMessagePluginConfig() {
		super(XMessage.TYPE_PHOTO);
		setBodyType("pflink");
		IMGlobalSetting.setMessageTypeForwardable(mMessageType);
	}

	@Override
	public void onBuildSendXmlAttribute(Message message, XMessage xm,Body body) {
		body.attributes.addAttribute("displayname", xm.getDisplayName());
		File file = new File(xm.getFilePath());
		body.attributes.addAttribute("size", String.valueOf(file.length()));
		body.attributes.addAttribute("originurl", xm.getPhotoDownloadUrl());
	}

	@Override
	public void onParseReceiveAttribute(XMessage xm, Message m, Body body) {
		xm.setPhotoDownloadUrl(body.attributes.getAttributeValue("originurl"));
	}

	@Override
	public void onAddMessageViewProvider(IMMessageAdapter adapter) {
		adapter.addIMMessageViewProvider(new PhotoViewLeftProvider());
		adapter.addIMMessageViewProvider(new PhotoViewRightProvider());
	}

	@Override
	public String getContent(Context context, XMessage xm) {
		return context.getString(R.string.picture);
	}

	@Override
	public void onChatActivityInitFinish(ChatActivity activity) {
		PhotoMessageOpener opener = new PhotoMessageOpener();
		activity.registerMessageOpener(mMessageType, opener);
		activity.registerMessageOpenFilePathChecker(mMessageType, opener);
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
		return new PhotoMessageDownloadProcessor();
	}
	
	@Override
	public MessageUploadProcessor createMessageUploadProcessor() {
		return new PhotoMessageUploadProcessor();
	}

	@Override
	public ActivityBasePlugin createChatActivityPlugin(ChatActivity activity) {
		return this;
	}
	
	public static class PhotoMessageOpener implements MessageOpener,
													MessageOpenFilePathChecker{
		@Override
		public void onOpenMessage(XMessage m, ChatActivity activity) {
			if(m.isFromSelf()){
				activity.viewDetailPhoto(m);
			}else{
				BitmapFactory.Options op = new BitmapFactory.Options();
				op.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(m.getThumbFilePath(), op);
				if(op.outWidth == 0 || op.outHeight == 0){
					FileHelper.deleteFile(m.getThumbFilePath());
					MessageDownloadProcessor.getMessageDownloadProcessor(XMessage.TYPE_PHOTO)
					.requestDownload(m, true);
				}else{
					activity.viewDetailPhoto(m);
				}
			}
		}

		@Override
		public boolean canOpenMessage(XMessage xm) {
			if(xm.isFromSelf()){
				return xm.isFileExists() || xm.isThumbFileExists();
			}
			return xm.isThumbFileExists();
		}
	}
}
