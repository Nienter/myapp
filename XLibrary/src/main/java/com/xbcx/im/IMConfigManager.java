package com.xbcx.im;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import android.app.Notification;

import com.xbcx.core.Event;
import com.xbcx.core.IDObject;
import com.xbcx.core.XApplication;
import com.xbcx.core.db.XDB;
import com.xbcx.core.module.HttpLoginListener;

public class IMConfigManager implements HttpLoginListener{

	public static IMConfigManager getInstance(){
		return sInstance;
	}
	
	static{
		sInstance = new IMConfigManager();
	}
	
	private static IMConfigManager sInstance;
	
	private String		mNotifyStartWithKey 	= "push";
	
	private String		mNotifyMessageKey 		= "pushmsg";
	private String		mNotifyMessageSoundKey 	= "pushsound";
	private String		mNotifyMessageVibrateKey= "pushshake";
	
	private IMConfig	mConfig;
	
	private List<ConfigObserver> mConfigObservers;
	
	protected IMConfigManager(){
		if(sInstance == null){
			XApplication.addManager(this);
		}
		sInstance = this;
	}
	
	public static void initNotificationSoundAndVibrate(Notification n){
		if(getInstance().isReceiveNewMessageSoundNotify()){
			n.defaults |= Notification.DEFAULT_SOUND;
		}
		if(getInstance().isReceiveNewMessageVibrateNotify()){
			n.defaults |= Notification.DEFAULT_VIBRATE;
		}
	}
	
	public void registerConfigObserver(ConfigObserver observer){
		if(observer == null){
			return;
		}
		if(mConfigObservers == null){
			mConfigObservers = new ArrayList<IMConfigManager.ConfigObserver>();
		}
		mConfigObservers.add(observer);
	}
	
	public void unregisterConfigObserver(ConfigObserver observer){
		if(observer == null){
			return;
		}
		if(mConfigObservers != null){
			mConfigObservers.remove(observer);
		}
	}
	
	public IMConfigManager setNotifyStartWithKey(String key){
		mNotifyStartWithKey = key;
		return this;
	}
	
	public IMConfigManager setMessageNotifyKey(String key){
		mNotifyMessageKey = key;
		return this;
	}
	
	public IMConfigManager setMessageSoundNotifyKey(String key){
		mNotifyMessageSoundKey = key;
		return this;
	}
	
	public IMConfigManager setMessageVibrateNotifyKey(String key){
		mNotifyMessageVibrateKey = key;
		return this;
	}
	
	public String getMessageNotifyKey(){
		return mNotifyMessageKey;
	}
	
	public String getMessageSoundNotifykey(){
		return mNotifyMessageSoundKey;
	}
	
	public String getMessageVibrateNotifyKey(){
		return mNotifyMessageVibrateKey;
	}
	
	public boolean isReceiveNewMessageNotify(){
		return isNotify(mNotifyMessageKey, true);
	}
	
	public boolean isReceiveNewMessageSoundNotify(){
		return isNotify(mNotifyMessageSoundKey, true);
	}
	
	public boolean isReceiveNewMessageVibrateNotify(){
		return isNotify(mNotifyMessageVibrateKey, true);
	}
	
	public void	setReceiveNewMessageNotify(boolean bNotify){
		setNotify(mNotifyMessageKey, bNotify);
	}
	
	public void setReceiveNewMessageSoundNotify(boolean bNotify){
		setNotify(mNotifyMessageSoundKey, bNotify);
	}
	
	public void setReceiveNewMessageVibrateNotify(boolean bNotify){
		setNotify(mNotifyMessageVibrateKey, bNotify);
	}
	
	public void setNotify(String key,boolean bNotify){
		checkConfig();
		String value = bNotify ? "1" : "0";
		mConfig.mMapExtras.put(key, value);
		saveConfig();
		if(mConfigObservers != null){
			for(ConfigObserver o : new ArrayList<ConfigObserver>(mConfigObservers)){
				o.onConfigChanged(key, value);
			}
		}
	}
	
	public static boolean toBoolean(String value){
		return "1".equals(value);
	}
	
	public boolean isNotify(String key,boolean defValue){
		checkConfig();
		String value = mConfig.mMapExtras.get(key);
		return value == null ? defValue : "1".equals(value);
	}
	
	public HashMap<String, String> getNotifyExtras(){
		checkConfig();
		return mConfig.mMapExtras;
	}
	
	protected void checkConfig(){
		if(mConfig == null){
			mConfig = XDB.getInstance().readById("imconfig", IMConfig.class, true);
			if(mConfig == null){
				mConfig = new IMConfig();
			}
		}
	}
	
	protected void saveConfig(){
		if(mConfig != null){
			XDB.getInstance().updateOrInsert(mConfig, true);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onHttpLogined(Event event,JSONObject joRet) {
		checkConfig();
		try{
			Iterator<String> it = joRet.keys();
			while(it.hasNext()){
				final String key = it.next();
				if(key.startsWith(mNotifyStartWithKey)){
					mConfig.mMapExtras.put(key, joRet.getString(key));
				}
			}
			saveConfig();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static class IMConfig extends IDObject{
		private static final long serialVersionUID = 1L;
		
		HashMap<String, String> mMapExtras = new HashMap<String, String>();
		
		public IMConfig() {
			super("imconfig");
		}
	}
	
	public static interface ConfigObserver{
		public void onConfigChanged(String key,String value);
	}
}

