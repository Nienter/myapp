package com.xbcx.im.messageprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.xbcx.im.XMessage;

public class VoiceMessagePlayProcessor {

	protected final ArrayList<XMessage> mListVoiceMessage = new ArrayList<XMessage>();
	protected final HashMap<String,Integer> mMapMessageIdToPos = new HashMap<String, Integer>(1024);
	
	public void onHandleMessage(XMessage m){
		if(m.getType() == XMessage.TYPE_VOICE){
			addMessage(m);
			
			onNewVoiceMessage(m);
		}
	}
	
	protected void onNewVoiceMessage(XMessage m){
	}
	
	public void onCreate(){
	}
	
	public void onDestroy(){
		mListVoiceMessage.clear();
		mMapMessageIdToPos.clear();
	}
	
	public void onResume(){
	}
	
	public void onPause(){
	}
	
	public void start(){
	}
	
	public void addMessage(XMessage m){
		mMapMessageIdToPos.put(m.getId(), mListVoiceMessage.size());
		mListVoiceMessage.add(m);
	}
	
	public void addAllMessage(int pos,List<XMessage> xms){
		List<XMessage> xmsVoice = new ArrayList<XMessage>();
		for(XMessage xm : xms){
			if(xm.getType() == XMessage.TYPE_VOICE){
				xmsVoice.add(xm);
			}
		}
		mListVoiceMessage.addAll(pos, xmsVoice);
		int nIndex = 0;
		for(XMessage xm : mListVoiceMessage){
			mMapMessageIdToPos.put(xm.getId(), nIndex++);
		}
	}
	
	public void removeMessage(XMessage m){
		mListVoiceMessage.remove(m);
		int nIndex = 0;
		for(XMessage xm : mListVoiceMessage){
			mMapMessageIdToPos.put(xm.getId(),nIndex++);
		}
	}
	
	public void clear(){
		mListVoiceMessage.clear();
		mMapMessageIdToPos.clear();
	}
	
	public int getVoiceCount(){
		return mListVoiceMessage.size();
	}
	
	public XMessage getFirstNotDownloadedMessage(){
		for(XMessage m : mListVoiceMessage){
			if(!m.isFromSelf() && !m.isFileExists()){
				return m;
			}
		}
		return null;
	}
	
	public XMessage getFirstNotPlayedMessage(){
		for(XMessage m : mListVoiceMessage){
			if(!m.isFromSelf() && !m.isPlayed()){
				return m;
			}
		}
		return null;
	}
	
	public XMessage getRecentlyNotPlayedMessage(){
		final int nSize = mListVoiceMessage.size();
		if(nSize > 0){
			XMessage m = mListVoiceMessage.get(nSize - 1);
			if(!m.isFromSelf() && !m.isPlayed()){
				return m;
			}
		}
		return null;
	}

	public XMessage getNextNotDownloadedMessage(String strMessageId){
		Integer pos = mMapMessageIdToPos.get(strMessageId);
		if(pos != null){
			final int nPos = pos.intValue();
			final int nSize = mListVoiceMessage.size();
			if(nSize > nPos + 1){
				XMessage m = null;
				for(int nIndex = nPos + 1;nIndex < nSize;++nIndex){
					m = mListVoiceMessage.get(nIndex);
					if(!m.isFromSelf() && !m.isFileExists()){
						return m;
					}
				}
			}
		}
		return null;
	}
	
	public XMessage getNextDownloadedAndNotPlayedMessage(String strMessageId){
		Integer pos = mMapMessageIdToPos.get(strMessageId);
		if(pos != null){
			final int nPos = pos.intValue();
			final int nSize = mListVoiceMessage.size();
			if(nSize > nPos + 1){
				XMessage m = null;
				for(int nIndex = nPos + 1;nIndex < nSize;++nIndex){
					m = mListVoiceMessage.get(nIndex);
					if(!m.isFromSelf() && !m.isPlayed()){
						if(m.isFileExists()){
							return m;
						}else{
							return null;
						}
					}
				}
			}
		}
		return null;
	}
}
