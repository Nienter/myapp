package com.xbcx.im.ui;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.xbcx.core.EventCode;
import com.xbcx.core.db.XDB;
import com.xbcx.im.XMessage;
import com.xbcx.im.db.DBColumns;
import com.xbcx.im.db.MessageBaseRunner;
import com.xbcx.im.db.MessageCreator;
import com.xbcx.im.messageprocessor.MessageDownloadProcessor;
import com.xbcx.im.messageprocessor.VoiceMessageNormalPlayProcessor;
import com.xbcx.im.messageprocessor.VoiceMessagePlayProcessor;
import com.xbcx.im.messageprocessor.VoicePlayProcessor;
import com.xbcx.im.recentchat.RecentChatManager;
import com.xbcx.im.vcard.VCardProvider;

public abstract class BaseChatActivity extends ChatActivity {
	
	protected static final String EXTRA_ID 		= "id";
	protected static final String EXTRA_NAME 	= "name";
	
	protected String mName;
	
	protected int	mInitReadCount = 0;
	
	protected VoiceMessagePlayProcessor	mPlayProcessor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mId = getIntent().getStringExtra(EXTRA_ID);
		mName = getIntent().getStringExtra(EXTRA_NAME);
		if(TextUtils.isEmpty(mId)){
			finish();
		}
		super.onCreate(savedInstanceState);
		
		onInitialVoicePlayer();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPlayProcessor.onDestroy();
		
		onReleaseVoicePlayer();
	}
	
	protected void onInitialVoicePlayer(){
		VoicePlayProcessor.getInstance().initial();
	}
	
	protected void onReleaseVoicePlayer(){
		VoicePlayProcessor.getInstance().stop();
		
		VoicePlayProcessor.getInstance().release();
	}
	
	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mTitleText = mName;
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(!TextUtils.isEmpty(mId)){
			RecentChatManager.getInstance().clearUnreadMessageCount(mId);
		}
		
		mPlayProcessor.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		mPlayProcessor.onPause();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		final String id = intent.getStringExtra("id");
		final String name = intent.getStringExtra("name");
		if(!mId.equals(id)){
			mId = id;
			mName = name;
			getTextViewTitle().setText(mName);
			mMessageAdapter.clear();
			mPlayProcessor.clear();
			initLoad();
			
			mListView.setCanRun(true);
		}
	}
	
	@Override
	protected void onInit() {
		super.onInit();
		
		mPlayProcessor = onCreateVoiceMessagePlayProcessor();
		mPlayProcessor.onCreate();
		
		initLoad();
		
		mPlayProcessor.start();
	}
	
	protected VoiceMessagePlayProcessor onCreateVoiceMessagePlayProcessor(){
		return new VoiceMessageNormalPlayProcessor();
	}

	protected void initLoad(){
		final int nUnreadMessageCount = RecentChatManager.getInstance()
				.getUnreadMessageCount(mId);
		
		final int msgCount = XDB.getInstance().readCount(
				MessageBaseRunner.getTableName(mId), 
				getReadMessageWhere(), 
				true);
		mLastReadPosition = msgCount - 1;

		mInitReadCount = nUnreadMessageCount;
		if(mInitReadCount == 0){
			mInitReadCount = 15;
		}
		
		loadOnePage();
		
		mListView.setSelection(mMessageAdapter.getCount() - 1);
	}
	
	protected abstract int 		getFromType();
	
	protected abstract boolean 	isGroupChat();
	
	protected String			getReadMessageWhere(){
		return null;
	}
	
	@Override
	protected List<XMessage> onLoadOnePageMessage(int nPosition) {
		super.onLoadOnePageMessage(nPosition);
		
		int readCount = 0;
		if(mInitReadCount == 0){
			readCount = 15;
		}else{
			readCount = Math.max(mInitReadCount, 15);
		}
		List<XMessage> messages = XDB.getInstance().readLimitReverse(MessageBaseRunner.getTableName(mId), 
				nPosition, 
				readCount, 
				getReadMessageWhere(), 
				DBColumns.Message.COLUMN_AUTOID + " ASC", 
				new MessageCreator(mId, getFromType()));
		
		if(mInitReadCount != 0){
			for(XMessage m : messages){
				if(m.getType() == XMessage.TYPE_VOICE){
					mPlayProcessor.addMessage(m);
				}else{
					MessageDownloadProcessor dp = MessageDownloadProcessor.getMessageDownloadProcessor(m.getType());
					if(dp != null){
						if(dp.hasThumb()){
							if(!dp.isThumbDownloading(m) &&
									!m.isThumbFileExists()){
								dp.requestDownload(m, true);
							}
						}else{
							if(!dp.isDownloading(m) &&
									!m.isFileExists()){
								dp.requestDownload(m, false);
							}
						}
					}
				}
			}
			mInitReadCount = 0;
		}else{
			mPlayProcessor.addAllMessage(0, messages);
		}
		return messages;
	}
	
	protected void reloadMessageFromDB(){
		final int msgCount = XDB.getInstance().readCount(
				MessageBaseRunner.getTableName(mId), 
				getReadMessageWhere(), 
				true);
		mLastReadPosition = msgCount - 1;
		
		if(mLastReadPosition >= 0){
			List<XMessage> messages = XDB.getInstance().readLimitReverse(MessageBaseRunner.getTableName(mId), 
					mLastReadPosition, 
					mMessageAdapter.getCount(), 
					getReadMessageWhere(), 
					DBColumns.Message.COLUMN_AUTOID + " ASC", 
					new MessageCreator(mId, getFromType()));
			
			mMessageAdapter.clear();
			List<XMessage> xms = addGroupTimeMessage(messages);
			mMessageAdapter.addAllItem(xms);
		}
	}

	@Override
	protected void onReceiveMessage(XMessage m) {
		super.onReceiveMessage(m);
		mPlayProcessor.onHandleMessage(m);
	}

	@Override
	protected void saveAndSendMessage(XMessage m) {
		super.saveAndSendMessage(m);
		mPlayProcessor.onHandleMessage(m);
	}

	@Override
	protected void onDeleteMessage(XMessage m) {
		super.onDeleteMessage(m);
		mEventManager.runEvent(EventCode.DB_DeleteMessage, mId,m.getId());
		
		mPlayProcessor.removeMessage(m);
	}

	@Override
	public boolean onSendCheck() {
		return true;
	}

	@Override
	protected void onInitMessage(XMessage m) {
		super.onInitMessage(m);
		m.setFromType(getFromType());
		if(isGroupChat()){
			m.setGroupId(mId);
			m.setGroupName(mName);
		}else{
			m.setUserId(mId);
			String name = VCardProvider.getInstance().getCacheName(mId);
			if(TextUtils.isEmpty(name)){
				name = mName;
			}
			m.setUserName(name);
		}
	}
}
