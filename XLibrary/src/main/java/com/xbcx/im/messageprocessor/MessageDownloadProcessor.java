package com.xbcx.im.messageprocessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.util.SparseArray;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventDelegateCanceller;
import com.xbcx.core.EventProgressDelegate;
import com.xbcx.core.http.XHttpRunner;
import com.xbcx.im.XMessage;

public class MessageDownloadProcessor{
	
	static{
		AndroidEventManager.getInstance().registerEventRunner(EventCode.DownloadMessageFile, 
				new MessageDownloadRunner());
	}
	
	private static SparseArray<MessageDownloadProcessor> mMapMsgTypeToDownloadProcessor = new SparseArray<MessageDownloadProcessor>();
	
	public static void registerMessageDownloadProcessor(int msgType,MessageDownloadProcessor processor){
		if(processor != null){
			mMapMsgTypeToDownloadProcessor.put(msgType, processor);
		}
	}
	
	public static MessageDownloadProcessor getMessageDownloadProcessor(int msgType){
		return mMapMsgTypeToDownloadProcessor.get(msgType);
	}
	
	protected static AndroidEventManager mEventManager = AndroidEventManager.getInstance();
	
	protected Map<String, DownloadInfo> mMapIdToDownloadInfo = new ConcurrentHashMap<String,DownloadInfo>();
	protected Map<String, DownloadInfo> mMapIdToThumbDownloadInfo = new ConcurrentHashMap<String,DownloadInfo>();
	
	public void requestDownload(XMessage m){
		requestDownload(m, false);
	}
	
	public void requestDownload(XMessage m,boolean bThumb){
		if(bThumb){
			if(hasThumb()){
				if(!isThumbDownloading(m)){
					DownloadInfo di = new DownloadInfo(m, bThumb, 
							mEventManager.pushEvent(EventCode.DownloadMessageFile, m,bThumb,this));
					mMapIdToThumbDownloadInfo.put(m.getId(), di);
				}
			}
		}else{
			if(!isDownloading(m)){
				DownloadInfo di = new DownloadInfo(m, bThumb, 
						mEventManager.pushEvent(EventCode.DownloadMessageFile, m,bThumb,this));
				mMapIdToDownloadInfo.put(m.getId(), di);
			}
		}
	}
	
	public boolean hasThumb(){
		return false;
	}
	
	public void stopDownload(XMessage m,boolean bThumb){
		if(bThumb){
			DownloadInfo di = mMapIdToThumbDownloadInfo.get(m.getId());
			if(di != null){
				mEventManager.cancelEvent(di.mEvent);
			}
		}else{
			DownloadInfo di = mMapIdToDownloadInfo.get(m.getId());
			if(di != null){
				mEventManager.cancelEvent(di.mEvent);
			}
		}
	}
	
	public void stopAllDownload(){
		for(DownloadInfo di : mMapIdToDownloadInfo.values()){
			mEventManager.cancelEvent(di.mEvent);
		}
		for(DownloadInfo di : mMapIdToThumbDownloadInfo.values()){
			mEventManager.cancelEvent(di.mEvent);
		}
	}
	
	public boolean isThumbDownloading(XMessage m){
		final DownloadInfo di = mMapIdToThumbDownloadInfo.get(m.getId());
		return di != null && mEventManager.isEventRunning(di.mEvent);
	}
	
	public boolean isDownloading(XMessage m){
		final DownloadInfo di = mMapIdToDownloadInfo.get(m.getId());
		return di != null && mEventManager.isEventRunning(di.mEvent);
	}
	
	public int	getThumbDownloadPercentage(XMessage m){
		DownloadInfo di = mMapIdToThumbDownloadInfo.get(m.getId());
		if(di != null){
			return di.mEvent.getProgress();
		}
		return -1;
	}
	
	public int getDownloadPercentage(XMessage m){
		DownloadInfo di = mMapIdToDownloadInfo.get(m.getId());
		if(di != null){
			return di.mEvent.getProgress();
		}
		return -1;
	}
	
	protected boolean customDownload(Event event,XMessage xm,boolean bThumb){
		return false;
	}
	
	protected static class MessageDownloadRunner extends XHttpRunner{
		@Override
		public void onEventRun(Event event) throws Exception {
			final XMessage xm = (XMessage)event.getParamAtIndex(0);
			final boolean bThumb = (Boolean)event.getParamAtIndex(1);
			final MessageDownloadProcessor processor = event.findParam(MessageDownloadProcessor.class);
			try{
				EventDelegateCanceller canceller = new EventDelegateCanceller(event);
				if(!processor.customDownload(event, xm, bThumb)){
					if(bThumb){
						Event e = mEventManager.runEventEx(EventCode.HTTP_Download, 
									canceller,
									new EventProgressDelegate(event),
									xm.getThumbUrl(), xm.getThumbFilePath());
						event.setSuccess(e.isSuccess());
					}else{
						Event e = mEventManager.runEventEx(EventCode.HTTP_Download, 
									canceller,
									new EventProgressDelegate(event),
									xm.getUrl(), xm.getFilePath());
						event.setSuccess(e.isSuccess());
					}
				}
			}finally{
				if(bThumb){
					processor.mMapIdToThumbDownloadInfo.remove(xm.getId());
				}else{
					processor.mMapIdToDownloadInfo.remove(xm.getId());
				}
			}
			if(!event.isCancel()){
				xm.setDownloaded();
				xm.updateDB();
			}
		}
	}
	
	protected class DownloadInfo{
		
		public XMessage 			mMessage;
		public boolean				mIsDownloadThumb;
		public Event				mEvent;
		
		protected DownloadInfo(XMessage m,boolean bDownloadThumb,Event e){
			mMessage = m;
			mIsDownloadThumb = bDownloadThumb;
			
			mEvent = e;
		}
	}
}
