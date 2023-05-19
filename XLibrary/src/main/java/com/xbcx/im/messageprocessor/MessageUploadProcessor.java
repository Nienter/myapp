package com.xbcx.im.messageprocessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.text.TextUtils;
import android.util.SparseArray;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventDelegateCanceller;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventProgressDelegate;
import com.xbcx.core.http.XHttpRunner;
import com.xbcx.im.XMessage;

public class MessageUploadProcessor{
	
	protected static AndroidEventManager mEventManager;
	
	static{
		mEventManager = AndroidEventManager.getInstance();
		mEventManager.registerEventRunner(EventCode.UploadMessageFile, new UploadMessageRunner());
	}
	
	private static SparseArray<MessageUploadProcessor> mMapMsgTypeToDownloadProcessor = new SparseArray<MessageUploadProcessor>();
	
	public static void registerMessageUploadProcessor(int msgType,MessageUploadProcessor processor){
		if(processor != null){
			mMapMsgTypeToDownloadProcessor.put(msgType, processor);
		}
	}
	
	public static MessageUploadProcessor getMessageUploadProcessor(int msgType){
		return mMapMsgTypeToDownloadProcessor.get(msgType);
	}
	
	protected Map<String, UploadInfo> 	mMapIdToUploadInfo = new ConcurrentHashMap<String,UploadInfo>();
	
	protected String					mUploadType;
	protected String					mUploadThumbType;
	
	public void requestUpload(XMessage m){
		if(!isUploading(m)){
			UploadInfo ui = new UploadInfo(m,
					mEventManager.pushEvent(EventCode.UploadMessageFile, m,this));
			mMapIdToUploadInfo.put(m.getId(), ui);
		}
	}
	public void 	stopUpload(XMessage m){
		UploadInfo ui = mMapIdToUploadInfo.get(m.getId());
		if(ui != null){
			mEventManager.cancelEvent(ui.mEvent);
		}
	}
	
	public void 	stopAllUpload(){
		for(UploadInfo ui : mMapIdToUploadInfo.values()){
			mEventManager.cancelEvent(ui.mEvent);
		}
	}
	
	public boolean 	isUploading(XMessage m){
		final UploadInfo info = mMapIdToUploadInfo.get(m.getId());
		return info != null && mEventManager.isEventRunning(info.mEvent);
	}
	
	public int		getUploadPercentage(XMessage m){
		UploadInfo ui = mMapIdToUploadInfo.get(m.getId());
		if(ui != null){
			return ui.mEvent.getProgress();
		}
		return -1;
	}
	
	protected void doUpload(Event event) throws Exception{
		final XMessage xm = (XMessage)event.getParamAtIndex(0);
		final UploadInfo ui = mMapIdToUploadInfo.get(xm.getId());
		if(ui != null){
			try{
				if(onUpload(event,xm, ui)){
					if(!ui.mEvent.isCancel()){
						xm.setUploadSuccess(true);
						xm.updateDB();
						event.setSuccess(true);
						
						mEventManager.pushEvent(EventCode.IM_SendMessage, xm);
					}
				}
			}finally{
				mMapIdToUploadInfo.remove(xm.getId());
			}
		}
	}
	
	protected boolean 	onUpload(Event event,XMessage xm,UploadInfo ui) throws Exception{
		final String thumbType = getUploadThumbType(xm);
		EventDelegateCanceller canceller = new EventDelegateCanceller(event);
		if(TextUtils.isEmpty(thumbType)){
			final String type = getUploadType(xm);
			if(!TextUtils.isEmpty(type)){
				Event e = mEventManager.runEventEx(
						EventCode.HTTP_PostFile,canceller,
						new EventProgressDelegate(event),
						type,xm.getFilePath());
				if(e.isSuccess()){
					xm.setUrl((String)e.getReturnParamAtIndex(0));
					return true;
				}
			}
		}else{
			Event e = mEventManager.runEventEx(
					EventCode.HTTP_PostFile,canceller,
					new EventProgressDelegate(event),
					thumbType,xm.getThumbFilePath());
			if(e.isSuccess()){
				xm.setThumbUrl((String)e.getReturnParamAtIndex(0));
				final String type = getUploadType(xm);
				if(!TextUtils.isEmpty(type)){
					e = mEventManager.runEventEx(
							EventCode.HTTP_PostFile,
							canceller,
							new EventProgressDelegate(event),
							type,xm.getFilePath());
					if(e.isSuccess()){
						xm.setUrl((String)e.getReturnParamAtIndex(0));
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public String 	getUploadType(XMessage xm){
		return mUploadType;
	}
	
	public String	getUploadThumbType(XMessage xm){
		return mUploadThumbType;
	}
	
	public void	setUploadType(String type){
		mUploadType = type;
	}
	
	public void setUploadThumbType(String type){
		mUploadThumbType = type;
	}
	
	protected static class UploadMessageRunner extends XHttpRunner{
		@Override
		public void onEventRun(Event event) throws Exception {
			final MessageUploadProcessor processor = event.findParam(MessageUploadProcessor.class);
			processor.doUpload(event);
		}
	}
	
	protected class UploadInfo{
		public XMessage			mMessage;
		public Event			mEvent;
		
		public UploadInfo(XMessage m,Event e){
			mMessage = m;
			
			mEvent = e;
		}
	}
}
