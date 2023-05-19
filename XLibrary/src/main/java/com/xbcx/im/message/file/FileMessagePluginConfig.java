package com.xbcx.im.message.file;

import java.util.List;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Body;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.xbcx.core.ActivityBasePlugin;
import com.xbcx.core.BaseActivity;
import com.xbcx.im.MessageTypeProcessor;
import com.xbcx.im.MessagePlugin.PluginConfig;
import com.xbcx.im.XMessage;
import com.xbcx.im.messageprocessor.MessageDownloadProcessor;
import com.xbcx.im.messageprocessor.MessageUploadProcessor;
import com.xbcx.im.recentchat.ContentProvider;
import com.xbcx.im.ui.ChatActivity;
import com.xbcx.im.ui.ChatActivity.ChatActivityInitFinishPlugin;
import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.im.ui.ChatActivity.AddMessageViewProviderPlugin;
import com.xbcx.im.ui.IMMessageAdapter;
import com.xbcx.library.R;
import com.xbcx.utils.FileHelper;

public class FileMessagePluginConfig extends PluginConfig implements
											ContentProvider,
											AddMessageViewProviderPlugin,
											ChatActivityInitFinishPlugin,
											MessageTypeProcessor,
											IMChooseFileProvider.ChooseFileCallback{

	public FileMessagePluginConfig() {
		super(XMessage.TYPE_FILE);
		setBodyType("filelink");
	}

	@Override
	public void onAddMessageViewProvider(IMMessageAdapter adapter) {
		adapter.addIMMessageViewProvider(new FileViewLeftProvider());
		adapter.addIMMessageViewProvider(new FileViewRightProvider());
	}

	@Override
	public void onBuildSendXmlAttribute(Message message, XMessage xm,Body body) {
		body.setMessage(xm.getOfflineFileDownloadUrl());
		body.attributes.addAttribute("size", String.valueOf(xm.getFileSize()));
		body.attributes.addAttribute("displayname", xm.getDisplayName());
	}

	@Override
	public void onParseReceiveAttribute(XMessage xm, Message m, Body body) {
		xm.setOfflineFileDownloadUrl(body.getMessage());
		xm.setFileSize(Long.parseLong(body.attributes.getAttributeValue("size")));
	}

	@Override
	public String getContent(Context context, XMessage xm) {
		return context.getString(R.string.file);
	}

	@Override
	public void onChatActivityInitFinish(ChatActivity activity) {
		activity.registerChooseProvider(
				new IMChooseFileProvider(BaseActivity.RequestCode_LaunchChooseFile));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onFileChoosed(Activity activity,List<FileItem> fis) {
		final ChatActivity ca = (ChatActivity)activity;
		for(FileItem fileItem : fis){
			if(fileItem.getFileType() == FileItem.FILETYPE_PIC){
				XMessage m = IMGlobalSetting.msgFactory.createXMessage(
						XMessage.buildMessageId(), XMessage.TYPE_PHOTO);
				m.setDisplayName(fileItem.getName());
				FileHelper.copyFile(m.getFilePath(), fileItem.getPath());
				ca.requestSendMessage(m, true);
			}else if(fileItem.getFileType() == FileItem.FILETYPE_VIDEO){
				final String videoPath = fileItem.getPath();
				Cursor cursor = ca.managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 
						new String[]{MediaStore.Video.Media.DURATION},
						MediaStore.Video.Media.DATA + "='" + videoPath + "'",
						null, null);
				long duration = 0;
				if(cursor != null && cursor.moveToFirst()){
					duration = cursor.getLong(cursor.getColumnIndex(
							MediaStore.Video.Media.DURATION));
				}
				
				ca.sendVideo(videoPath, duration);
			}else{
				XMessage m = IMGlobalSetting.msgFactory.createXMessage(
						XMessage.buildMessageId(), XMessage.TYPE_FILE);
				m.setDisplayName(fileItem.getName());
				m.setFileSize(fileItem.getFileSize());
				FileHelper.copyFile(m.getFilePath(), fileItem.getPath());
				ca.requestSendMessage(m, true);
			}
		}
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
		return new MessageDownloadProcessor();
	}

	@Override
	public MessageUploadProcessor createMessageUploadProcessor() {
		return new MessageUploadProcessor();
	}

	@Override
	public ActivityBasePlugin createChatActivityPlugin(ChatActivity activity) {
		return this;
	}

}
