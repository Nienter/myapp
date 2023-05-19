package com.xbcx.im.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.xbcx.core.ActivityBasePlugin;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.NameObject;
import com.xbcx.core.XApplication;
import com.xbcx.im.IMKernel;
import com.xbcx.im.XMessage;
import com.xbcx.im.messageprocessor.MessageDownloadProcessor;
import com.xbcx.im.messageprocessor.MessageUploadProcessor;
import com.xbcx.im.messageprocessor.VoicePlayProcessor;
import com.xbcx.im.ui.messageviewprovider.DefaultTypeViewLeftProvider;
import com.xbcx.im.ui.simpleimpl.ChoosePictureActivity;
import com.xbcx.im.ui.simpleimpl.IMLookPhotosActivity;
import com.xbcx.library.R;
import com.xbcx.parse.AmrParse;
import com.xbcx.utils.FileHelper;
import com.xbcx.utils.SystemUtils;
import com.xbcx.view.PulldownableListView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

public class ChatActivity extends BaseActivity implements 
													XChatEditView.OnEditListener,
													AbsListView.OnScrollListener,
													PulldownableListView.OnPullDownListener,
													IMMessageViewProvider.OnViewClickListener,
													AdapterView.OnItemLongClickListener,
													AdapterView.OnItemClickListener{

	protected static final int MENUID_DELETEMESSAGE = 1;
	protected static final int MENUID_COPYMESSAGE	= 2;
	protected static final int MENUID_FORWARD		= 3;
	
	protected boolean					mIsShowTime = true;
	
	protected String					mId;
	
	protected boolean					mIsReaded;
	
	protected XChatListView				mListView;
	protected IMMessageAdapter 			mMessageAdapter;
	protected XChatEditView 			mEditView;
	protected int						mLastReadPosition;
	
	private   int						mRequestCodePhotoSendPreview = generateRequestCode();
	private   int						mReqeustCodeChoosePicture = generateRequestCode();
	
	private	  SparseArray<SendPlugin>	mMapRequestCodeToSendPlugin;
	
	private   SparseArray<MessageOpener> 				mMapMsgTypeToOpener = new SparseArray<MessageOpener>();
	private   SparseArray<MessageOpenFilePathChecker> 	mMapMsgTypeToFilePathChecker = new SparseArray<MessageOpenFilePathChecker>();
	
	private   int						mRequestCodeInc = 50000;
	
	private	  NotifyConnectionEventHandler	mNotifyConnectionEventHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mIsShowChatRoomBar = false;
		super.onCreate(savedInstanceState);
		mIsChoosePhotoCompression = false;
		mNotifyConnectionEventHandler = new NotifyConnectionEventHandler(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		mListView.setAdapter(null);
		mMessageAdapter.clear();
		mMessageAdapter.clearIMMessageViewProvider();
		
		mEventManager.removeEventProgressListener(EventCode.DownloadMessageFile, this);
		mEventManager.removeEventProgressListener(EventCode.UploadMessageFile, this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mIsReaded = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		mIsReaded = false;
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		
		for(ChatActivityCreatePlugin p : XApplication.getManagers(ChatActivityCreatePlugin.class)){
			p.onChatActivityCreated(this);
		}
		
		ChatBackgroundProvider.setBackground(getWindow().getDecorView());
		
		onInit();
		for(ChatActivityInitFinishPlugin p : getPlugins(ChatActivityInitFinishPlugin.class)){
			p.onChatActivityInitFinish(this);
		}
		
		registerForContextMenu(mListView);
		mListView.setOnItemLongClickListener(this);
	}
	
	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
	}

	protected void onInit(){
		mEditView = (XChatEditView)findViewById(R.id.chatEditView);
		if(mEditView != null){
			onInitEditViewSendPlugin();
			mEditView.setOnEditListener(this);
		}
		
		mListView = (XChatListView)findViewById(R.id.lv);
		mMessageAdapter = new IMMessageAdapter(this)
				.setOnViewClickListener(this);
		onAddMessageViewProvider();
		mMessageAdapter.setDefaultIMMessageViewProvider(new DefaultTypeViewLeftProvider(this));
		mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mListView.setEditView(mEditView);
		mListView.setOnScrollListener(this);
		mListView.setOnPullDownListener(this);
		mListView.setOnItemClickListener(this);
		final MessageAnimationAdapter animatAdapter = new MessageAnimationAdapter(
				mMessageAdapter,mListView);
		mMessageAdapter.setAnimationAdapter(animatAdapter);
		mListView.setAdapter(animatAdapter);
		
		if(!TextUtils.isEmpty(mId)){
			addAndManageEventListener(EventCode.IM_ReceiveMessage);
		}
		
		addAndManageEventListener(EventCode.DB_DeleteMessage);
		addAndManageEventListener(EventCode.DB_SaveAllMessage);
		addAndManageEventListener(EventCode.UploadMessageFile);
		addAndManageEventListener(EventCode.DownloadMessageFile);
		addAndManageEventListener(EventCode.VoicePlayStarted);
		addAndManageEventListener(EventCode.VoicePlayErrored);
		addAndManageEventListener(EventCode.VoicePlayCompletioned);
		addAndManageEventListener(EventCode.VoicePlayStoped);
		addAndManageEventListener(EventCode.VoicePlayPaused);
		addAndManageEventListener(EventCode.IM_SendMessage);
		
		mEventManager.addEventProgressListener(EventCode.UploadMessageFile, this);
		mEventManager.addEventProgressListener(EventCode.DownloadMessageFile, this);
	}
	
	protected void onInitEditViewSendPlugin(){
		List<SendPlugin> sps = new ArrayList<SendPlugin>(IMGlobalSetting.publicSendPlugins);
		
		List<SendPlugin> temps = new ArrayList<SendPlugin>();
		for(AddChatSendPlugin asp : XApplication.getManagers(AddChatSendPlugin.class)){
			SendPlugin sp = asp.onCreateChatSendPlugin(this);
			if(sp != null){
				temps.add(sp);
			}
		}
		
		Collections.sort(temps, new Comparator<SendPlugin>() {
			@Override
			public int compare(SendPlugin lhs, SendPlugin rhs) {
				return lhs.getSortKey() - rhs.getSortKey();
			}
		});
		sps.addAll(temps);
		
		for(SendPlugin sp : sps){
			if(sp.useActivityResult()){
				if(mMapRequestCodeToSendPlugin == null){
					mMapRequestCodeToSendPlugin = new SparseArray<SendPlugin>();
				}
				int code = mRequestCodeInc++;
				sp.setRequestCode(code);
				mMapRequestCodeToSendPlugin.put(code, sp);
			}
		}
		mEditView.addAllSendPlugin(sps);
	}
	
	protected void onAddMessageViewProvider(){
		IMMessageViewProviderFactory factory = IMMessageViewProvider.getIMMessageViewProviderFactory();
		if(factory != null){
			List<IMMessageViewProvider> providers = factory.createIMMessageViewProviders(this);
			if(providers != null){
				for(IMMessageViewProvider provider : providers){
					mMessageAdapter.addIMMessageViewProvider(provider);
				}
			}
		}
		for(AddMessageViewProviderPlugin p : getPlugins(AddMessageViewProviderPlugin.class)){
			p.onAddMessageViewProvider(mMessageAdapter);
		}
	}
	
	public XChatEditView getChatEditView(){
		return mEditView;
	}
	
	public void registerMessageOpener(int msgType,MessageOpener opener){
		mMapMsgTypeToOpener.put(msgType, opener);
	}
	
	public void registerMessageOpenFilePathChecker(int msgType,MessageOpenFilePathChecker checker){
		mMapMsgTypeToFilePathChecker.put(msgType, checker);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
					AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
					AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	protected int loadOnePage(){
		if(mLastReadPosition >= 0){
			List<XMessage> listMessage = onLoadOnePageMessage(mLastReadPosition);
			if(listMessage == null){
				listMessage = Collections.emptyList();
			}
			List<XMessage> listTemp = addGroupTimeMessage(listMessage);
			
			mMessageAdapter.addAllItem(0, listTemp);
			
			onOnePageLoaded(listMessage.size());
			
			return listTemp.size();
		}else{
			mListView.setCanRun(false);
		}
		return 0;
	}
	
	protected List<XMessage> onLoadOnePageMessage(int nPosition){
		return null;
	}
	
	protected List<XMessage> addGroupTimeMessage(Collection<XMessage> listMessage){
		List<XMessage> listTemp = new ArrayList<XMessage>();
		XMessage lastMessage = null;
		for(XMessage m : listMessage){
			XMessage timeMessage = checkOrCreateTimeMessage(m,lastMessage);
			if(timeMessage != null){
				listTemp.add(timeMessage);
			}
			listTemp.add(m);
			lastMessage = m;
		}
		return listTemp;
	}
	
	protected void onOnePageLoaded(int nCount){
		mLastReadPosition -= nCount;
		if(mLastReadPosition < 0){
			mListView.setCanRun(false);
		}
	}
	
	@Override
	public void onRecordFail(boolean bFailByNet) {
		if(bFailByNet){
			if(mNotifyConnectionEventHandler != null){
				mNotifyConnectionEventHandler.startUnConnectionAnimation();
			}
		}else{
			mToastManager.show(R.string.prompt_record_fail);
		}
	}
	
	@Override
	public boolean onSendCheck() {
		if(IMKernel.isIMConnectionAvailable()){
			return true;
		}else{
			if(mNotifyConnectionEventHandler != null){
				mNotifyConnectionEventHandler.startUnConnectionAnimation();
			}
			return false;
		}
	}
	
	@Override
	public void onSendText(CharSequence s) {
		for(MessageTextPlugin p : getPlugins(MessageTextPlugin.class)){
			s = p.onHandleMessageText(s);
		}
		XMessage message = IMGlobalSetting.msgFactory.createXMessage(
				XMessage.buildMessageId(), XMessage.TYPE_TEXT);
		message.setContent(String.valueOf(s));
		onNewMessageEdited(message,true);
		saveAndSendMessage(message);
	}

	@Override
	public void onSendVoice(String strPathName) {
		XMessage message = IMGlobalSetting.msgFactory.createXMessage(
				XMessage.buildMessageId(), XMessage.TYPE_VOICE);
		message.setVoiceFrameCount(AmrParse.parseFrameCount(strPathName));
		
		onNewMessageEdited(message, true);
		
		FileHelper.copyFile(
				message.getVoiceFilePath(),
				strPathName);
		
		saveAndSendMessage(message);
	}
	
	@Override
	public void onSendPlugin(SendPlugin sp) {
		if(sp != null){
			final int sendType = sp.getSendType();
			if(sendType == SendPlugin.SENDTYPE_PHOTO_ALL){
				if(XApplication.checkExternalStorageAvailable()){
					choosePhoto(IMGlobalSetting.photoCrop,getString(R.string.photo));
				}
			}else if(sendType == SendPlugin.SENDTYPE_PHOTO_ALBUMS){
				if(XApplication.checkExternalStorageAvailable()){
					launchPictureChoose(IMGlobalSetting.photoCrop);
				}
			}else if(sendType == SendPlugin.SENDTYPE_PHOTO_CAMERA){
				if(XApplication.checkExternalStorageAvailable()){
					launchCameraPhoto(IMGlobalSetting.photoCrop);
				}
			}else{
				sp.onSend(this);
			}
		}
	}
	
	public String getChatId(){
		return mId;
	}
	
	@Override
	public void launchPictureChoose(boolean bCrop) {
		if(IMGlobalSetting.sendPhotoUseMultiChoose){
			ChoosePictureActivity.launchForResult(this, 10, mReqeustCodeChoosePicture);
		}else{
			super.launchPictureChoose(bCrop);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == mRequestCodePhotoSendPreview){
				Object tag = getTag();
				if(tag != null && tag instanceof NameObject){
					NameObject no = (NameObject)tag;
					sendPhoto(no.getId(), no.getName());
				}
			}else if(requestCode == mReqeustCodeChoosePicture){
				if(data != null){
					final List<NameObject> pics = (ArrayList<NameObject>)data.getSerializableExtra(
							ChoosePictureActivity.EXTRA_RETURN_PICS);
					if(pics != null){
						for(NameObject no : pics){
							sendPhoto(no.getId(), no.getName());
						}
					}
				}
			}else{
				if(mMapRequestCodeToSendPlugin != null){
					final SendPlugin sp = mMapRequestCodeToSendPlugin.get(requestCode);
					if(sp != null){
						sp.activityResult(this,requestCode, resultCode, data);
					}
				}
			}
		}
	}
	
	public void requestSendMessage(XMessage xm,boolean bHidePullUpView){
		onNewMessageEdited(xm, true);
		saveAndSendMessage(xm);
		if(bHidePullUpView){
			mEditView.hideAllPullUpView(true);
		}
	}
	
	@Override
	protected void onPictureChoosed(String filePath, String displayName) {
		super.onPictureChoosed(filePath, displayName);
		if(IMGlobalSetting.photoCrop){
			sendPhoto(filePath, displayName);
		}else{
			if(IMGlobalSetting.photoSendPreview && 
					IMGlobalSetting.photoSendPreviewActivityClass != null){
				setTag(new NameObject(filePath, displayName));
				Intent intent = new Intent(this, IMGlobalSetting.photoSendPreviewActivityClass);
				intent.putExtra("path", filePath);
				startActivityForResult(intent, mRequestCodePhotoSendPreview);
			}else{
				sendPhoto(filePath, displayName);
			}
		}
	}
	
	public void sendPhoto(String filePath,String displayName){
		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, op);
		if(op.outWidth < 0){
			mToastManager.show(R.string.toast_cannot_send_photo);
			return;
		}
		
		XMessage m = IMGlobalSetting.msgFactory.createXMessage(
				XMessage.buildMessageId(), XMessage.TYPE_PHOTO);
		
		if(!TextUtils.isEmpty(displayName)){
			m.setDisplayName(displayName);
		}
		
		onNewMessageEdited(m, true);
		final String choosePicPath = filePath;
		int rotate = SystemUtils.getPictureExifRotateAngle(choosePicPath);
		
		final String strPhotoPath = m.getFilePath();
		if(rotate == 0){
			if(!SystemUtils.compressBitmapFile(strPhotoPath, choosePicPath, 1024, 512)){
				FileHelper.copyFile(strPhotoPath, choosePicPath);
			}
		}else{
			handlePictureExif(choosePicPath, strPhotoPath);
		}
		
		saveAndSendMessage(m);
		
		if(mEditView != null){
			mEditView.hideAllPullUpView(true);
		}
	}
	
	public XMessage sendVideo(String videoPath,long duration){
		XMessage m = IMGlobalSetting.msgFactory.createXMessage(
				XMessage.buildMessageId(), XMessage.TYPE_VIDEO);
		onNewMessageEdited(m, true);
		m.setVideoFilePath(videoPath);
		m.setVideoSeconds((int)duration / 1000);
		
		final Bitmap bmp = SystemUtils.getVideoThumbnail(videoPath);
		if(bmp != null){
			FileHelper.saveBitmapToFile(m.getThumbFilePath(), bmp);
		}
		
		saveAndSendMessage(m);
		
		if(mEditView != null){
			mEditView.hideAllPullUpView(true);
		}
		
		return m;
	}
	
	protected void onNewMessageEdited(XMessage m,boolean bScrollToBottom){
		onInitMessage(m);
		
		if(bScrollToBottom){
			mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		}
		
		XMessage timeMessage = checkOrCreateTimeMessage(m);
		if(timeMessage != null){
			mMessageAdapter.addItem(timeMessage);
		}
		mMessageAdapter.addItem(m);
	}
	
	protected XMessage checkOrCreateTimeMessage(XMessage m){
		final int nItemCount = mMessageAdapter.getCount();
		XMessage lastMessage = nItemCount > 0 ? 
				(XMessage)mMessageAdapter.getItem(nItemCount - 1) : null;
		return checkOrCreateTimeMessage(m, lastMessage);
	}
	
	protected XMessage checkOrCreateTimeMessage(XMessage m,XMessage lastMessage){
		if(mIsShowTime){
			long sendTimeLast = lastMessage == null ? 0 : lastMessage.getSendTime();
			if (m.getSendTime() - sendTimeLast >= 120000) {
				return XMessage.createTimeMessage(m.getSendTime());
			}
			return null;
		}else{
			return null;
		}
	}
	
	protected void saveAndSendMessage(XMessage m){
		mEventManager.runEvent(EventCode.DB_SaveMessage, m);
		mEventManager.pushEvent(EventCode.HandleRecentChat, m);
		
		onSendMessage(m);
	}
	
	protected void onSendMessage(XMessage m){
		if(IMKernel.isIMConnectionAvailable()){
			final int nType = m.getType();
			MessageUploadProcessor up = MessageUploadProcessor.getMessageUploadProcessor(nType);
			if(up != null){
				up.requestUpload(m);
				redrawMessage(m);
			}else{
				mEventManager.pushEvent(EventCode.IM_SendMessage,m);
			}
		}else{
			if(!m.isSended()){
				m.setSended();
				m.updateDB();
			}
		}
	}
	
	protected void onInitMessage(XMessage m){
		m.setFromSelf(true);
		m.setSendTime(XApplication.getFixSystemTime());
		
		if(!IMKernel.isIMConnectionAvailable()){
			m.setSended();
			m.setSendSuccess(false);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int nCode = event.getEventCode();
		if(nCode == EventCode.IM_ReceiveMessage){
			XMessage message = (XMessage)event.getParamAtIndex(0);
			if(IMKernel.filter(mId, message)){
				if(!mMessageAdapter.containsMessage(message.getId())){
					onReceiveMessage(message);
				}
			}
		}else if(nCode == EventCode.DownloadMessageFile ||
				nCode == EventCode.UploadMessageFile){
			final XMessage xm = (XMessage)event.getParamAtIndex(0);
			if(xm != null){
				if(event.isSuccess()){
					final XMessage old = mMessageAdapter.findItem(xm.getId());
					if(old != null){
						old.setUploadSuccess(true);
						old.setUrl(xm.getUrl());
						old.setThumbUrl(xm.getThumbUrl());
					}
				}
				redrawMessage(xm);
			}
		}else if(nCode == EventCode.VoicePlayErrored ||
				nCode == EventCode.VoicePlayCompletioned ||
				nCode == EventCode.VoicePlayStoped ||
				nCode == EventCode.VoicePlayPaused){
			redrawMessage((XMessage)event.getParamAtIndex(0));
		}else if(nCode == EventCode.VoicePlayStarted){
			final XMessage xm = (XMessage)event.getParamAtIndex(0);
			if(xm != null){
				final XMessage old = mMessageAdapter.findItem(xm.getId());
				if(old != null && !old.isPlayed()){
					old.setPlayed(true);
				}
				redrawMessage(xm);
			}
		}else if(nCode == EventCode.IM_SendMessage){
			final XMessage xm = event.findParam(XMessage.class);
			if(mMessageAdapter.containsMessage(xm.getId())){
				if(mId.equals(xm.getOtherSideId())){
					final XMessage old = mMessageAdapter.findItem(xm.getId());
					if(old != null){
						old.setSended();
						old.setUploadSuccess(true);
						old.setSendSuccess(xm.isSendSuccess());
						redrawMessage(old);
					}
				}
			}else{
				if(mId.equals(xm.getOtherSideId())){
					mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
					XMessage timeMessage = checkOrCreateTimeMessage(xm);
					if(timeMessage != null){
						mMessageAdapter.addItem(timeMessage);
					}
					mMessageAdapter.addItem(xm);
				}
			}
			redrawMessage(null);
		}else if(nCode == EventCode.DB_DeleteMessage){
			final String id = (String)event.getParamAtIndex(0);
			if(mId.equals(id)){
				if(event.getParamAtIndex(1) == null){
					mMessageAdapter.clear();
				}
			}
		}else if(nCode == EventCode.DB_SaveAllMessage){
			final String id = (String)event.getParamAtIndex(0);
			if(mId.equals(id)){
				final Collection<XMessage> xms = event.findParam(Collection.class);
				mMessageAdapter.clear();
				mMessageAdapter.addAllItem(addGroupTimeMessage(xms));
			}
		}
	}
	
	@Override
	public void onEventProgress(Event e, int progress) {
		final XMessage xm = (XMessage)e.getParamAtIndex(0);
		redrawMessage(xm);
	}
	
	protected void onReceiveMessage(XMessage m){
		XMessage timeM = checkOrCreateTimeMessage(m);
		if(timeM != null){
			mMessageAdapter.addItem(timeM);
		}
		mMessageAdapter.addItem(m);
		
		if(!m.isReaded()){
			m.setReaded(mIsReaded);
		}
		
		MessageDownloadProcessor dp = MessageDownloadProcessor.getMessageDownloadProcessor(m.getType());
		if(dp != null){
			dp.requestDownload(m, true);
		}
	}
	
	@Override
	public void onStartRun(PulldownableListView view) {
		if(mLastReadPosition >= 0){
			final int nLoadCount = loadOnePage();
			
			mListView.setSelectionFromTop(nLoadCount + mListView.getHeaderViewsCount(),
					mListView.getPullDownViewHeight());
			
			mListView.endRun();
		}
	}
	
	public void openMessage(XMessage m){
		for(MessageOpenCheckPlugin p : getPlugins(MessageOpenCheckPlugin.class)){
			if(!p.onCheckOpenMessage(m)){
				return;
			}
		}
		MessageOpener mo = mMapMsgTypeToOpener.get(m.getType());
		if(mo != null){
			mo.onOpenMessage(m,this);
		}
	}
	
	@Override
	public void onViewClicked(XMessage m, int nViewId) {
		if(!mMessageAdapter.isCheck()){
			if (nViewId == R.id.viewContent) {
				final int nType = m.getType();
				
				if(m.isFromSelf()){
					final MessageUploadProcessor up = MessageUploadProcessor.getMessageUploadProcessor(nType);
					if(up == null){
						if(m.isSendSuccess()){
							openMessage(m);
						}else{
							onWarningViewClicked(m);
						}
					}else{
						if(!up.isUploading(m)){
							if(m.isUploadSuccess()){
								openOrDownloadMessage(m);
							}else{
								onWarningViewClicked(m);
							}
						}
					}
				}else{
					openOrDownloadMessage(m);
				}
			}else if(nViewId == R.id.ivAvatar){
				onAvatarClicked(m);
			}else if(nViewId == R.id.ivWarning){
				onWarningViewClicked(m);
			}else if(nViewId == R.id.btn){
				if(m.isFromSelf()){
					final int type = m.getType();
					final MessageUploadProcessor up = MessageUploadProcessor.getMessageUploadProcessor(type);
					if(up != null){
						if(up.isUploading(m)){
							up.stopUpload(m);
						}else{
							final MessageDownloadProcessor dp = MessageDownloadProcessor.getMessageDownloadProcessor(type);
							if(dp != null && dp.isDownloading(m)){
								dp.stopDownload(m, false);
							}else{
								up.requestUpload(m);
							}
						}
					}
				}else{
					final int type = m.getType();
					final MessageDownloadProcessor dp = MessageDownloadProcessor.getMessageDownloadProcessor(type);
					if(dp != null){
						if(dp.isDownloading(m)){
							dp.stopDownload(m, false);
						}else{
							dp.requestDownload(m, false);
						}
					}
				}
				redrawMessage(m);
			}
		}
	}
	
	private void openOrDownloadMessage(XMessage m){
		final MessageDownloadProcessor dp = MessageDownloadProcessor.getMessageDownloadProcessor(m.getType());
		if(dp == null){
			openMessage(m);
		}else{
			if(dp.hasThumb()){
				if(!dp.isThumbDownloading(m)){
					MessageOpenFilePathChecker c = mMapMsgTypeToFilePathChecker.get(m.getType());
					if(c == null){
						if(m.isThumbFileExists()){
							openMessage(m);
						}else{
							if(XApplication.checkExternalStorageAvailable()){
								dp.requestDownload(m, true);
							}
						}
					}else{
						if(c.canOpenMessage(m)){
							openMessage(m);
						}else{
							if(XApplication.checkExternalStorageAvailable()){
								dp.requestDownload(m, true);
							}
						}
					}
				}
			}else{
				if(!dp.isDownloading(m)){
					if(m.isFileExists()){
						openMessage(m);
					}else{
						if(XApplication.checkExternalStorageAvailable()){
							dp.requestDownload(m, false);
						}
					}
				}
			}
			redrawMessage(m);
		}
	}
	
	protected void onAvatarClicked(XMessage m){
		if(m.isFromSelf()){
			ActivityType.launchChatActivity(this, ActivityType.SelfDetailActivity, 
					IMKernel.getLocalUser(), null);
		}else{
			ActivityType.launchChatActivity(this, ActivityType.UserDetailActivity,
					m.getUserId(), m.getUserName());
		}
	}
	
	protected void onWarningViewClicked(final XMessage m){
		if(m.isFromSelf()){
			if(!m.isSendSuccess()){
				if(IMGlobalSetting.msgReSendDialog){
					DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(which == DialogInterface.BUTTON_POSITIVE){
								onSendMessage(m);
							}
						}
					};
					showYesNoDialog(R.string.ok, R.string.cancel, R.string.dialog_msg_resend, listener);
				}else{
					onSendMessage(m);
				}
			}
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Object object = parent.getItemAtPosition(position);
		if(object != null && object instanceof XMessage){
			final XMessage xm = (XMessage)object;
			if(xm.getType() == XMessage.TYPE_TIME){
				return true;
			}
			setTag(object);
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(mMessageAdapter.isCheck()){
			final Object item = parent.getItemAtPosition(position);
			if(item != null && item instanceof XMessage){
				final XMessage xm = (XMessage)item;
				mMessageAdapter.setCheckItem(xm, !mMessageAdapter.isCheckedItem(xm));
			}
		}
	}

	@Override
	public boolean onViewLongClicked(XMessage message, int nViewId) {
		if(!mMessageAdapter.isCheck()){
			setTag(message);
			openContextMenu(mListView);
			return true;
		}
		return false;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		Object tag = getTag();
		if(tag != null && tag instanceof XMessage){
			XMessage m = (XMessage)tag;
//			menu.setHeaderTitle(getContextMenuTitle(m));
			if(m.getType() != XMessage.TYPE_PROMPT){
				menu.add(0, MENUID_DELETEMESSAGE, 0, R.string.deletemessage);
			}
			if(ActivityType.getActivityClass(ActivityType.ForwardActivity) != null){
				if(IMGlobalSetting.isMessageTypeForwardable(m.getType())){
					menu.add(0, MENUID_FORWARD, 0, R.string.forwardmessage);
				}
			}
			if(m.getType() == XMessage.TYPE_TEXT){
				menu.add(0, MENUID_COPYMESSAGE, 0, R.string.copymessage);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case MENUID_DELETEMESSAGE:
			onDeleteMessage((XMessage)getTag());
			setTag(null);
			break;
		case MENUID_COPYMESSAGE:
			final XMessage xm = (XMessage)getTag();
			SystemUtils.copyToClipBoard(this, xm.getContent());
			setTag(null);
			break;
		case MENUID_FORWARD:
			ActivityType.launchForwardMessageActivity(this, (XMessage)getTag());
		}
		return super.onContextItemSelected(item);
	}
	
	protected void onDeleteMessage(XMessage m){
		final int type = m.getType();
		final MessageUploadProcessor up = MessageUploadProcessor.getMessageUploadProcessor(type);
		if(up != null){
			up.stopUpload(m);
		}
		final MessageDownloadProcessor dp = MessageDownloadProcessor.getMessageDownloadProcessor(type);
		if(dp != null){
			dp.stopDownload(m, true);
			dp.stopDownload(m, false);
			
			if(type == XMessage.TYPE_VOICE){
				if(VoicePlayProcessor.getInstance().isPlaying(m)){
					VoicePlayProcessor.getInstance().stop();
				}
			}
			if(type == XMessage.TYPE_VIDEO){
				if(!m.isFromSelf()){
					FileHelper.deleteFile(m.getVideoFilePath());
				}
			}else{
				FileHelper.deleteFile(m.getFilePath());
			}
			if(dp.hasThumb()){
				FileHelper.deleteFile(m.getThumbFilePath());
			}
		}
		
		int nIndex = mMessageAdapter.indexOf(m);
		XMessage lastM = (XMessage)mMessageAdapter.getItem(nIndex - 1);
		if(lastM.getType() == XMessage.TYPE_TIME){
			boolean bDeleteLast = false;
			if(mMessageAdapter.getCount() > nIndex + 1){
				XMessage nextM = (XMessage)mMessageAdapter.getItem(nIndex + 1);
				if(nextM.getType() == XMessage.TYPE_TIME){
					bDeleteLast = true;
				}
			}else{
				bDeleteLast = true;
			}
			if(bDeleteLast){
				mMessageAdapter.removeItem(nIndex - 1);
				--nIndex;
			}
			
		}
		mMessageAdapter.removeItem(nIndex);
	}
	
	protected String getContextMenuTitle(XMessage message){
		if(getTextViewTitle() != null){
			return getTextViewTitle().getText().toString();
		}
		return "";
	}

	public void viewDetailPhoto(XMessage m){
		IMLookPhotosActivity.launch(this, m, mMessageAdapter.getAllItem());
	}
	
	public void viewVideo(XMessage m){
		try{
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(m.getVideoFilePath())), "video/*");
			startActivity(intent);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void redrawMessage(XMessage m){
		if(mMessageAdapter.isIMMessageViewVisible(m)){
			mMessageAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)&& 
				mListView.getLastVisiblePosition() == mListView.getCount() - 1){
			mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		}else{
			mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
		}
		
		if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
			boolean bHandle = false;
			for(TextMessageImageCoder coder : IMGlobalSetting.textMsgImageCodeces){
				bHandle |= coder.resumeDrawable();
			}
			if(bHandle){
				mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
				redrawMessage(null);
			}
		}else{
			for(TextMessageImageCoder coder : IMGlobalSetting.textMsgImageCodeces){
				coder.pauseDrawable();
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}
	
	public static interface MessageOpenFilePathChecker{
		public boolean canOpenMessage(XMessage xm);
	}
	
	public static interface MessageOpener{
		public void onOpenMessage(XMessage xm,ChatActivity activity);
	}
	
	public static interface MessageOpenCheckPlugin extends ActivityBasePlugin{
		public boolean onCheckOpenMessage(XMessage xm);
	}
	
	public static interface AddMessageViewProviderPlugin extends ActivityBasePlugin{
		public void onAddMessageViewProvider(IMMessageAdapter adapter);
	}
	
	public static interface MessageTextPlugin extends ActivityBasePlugin{
		public CharSequence onHandleMessageText(CharSequence cs);
	}
	
	public static interface ChatActivityInitFinishPlugin extends ActivityBasePlugin{
		public void onChatActivityInitFinish(ChatActivity activity);
	}
}
