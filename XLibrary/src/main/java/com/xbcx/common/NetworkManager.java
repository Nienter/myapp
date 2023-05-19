package com.xbcx.common;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.xbcx.core.XApplication;
import com.xbcx.utils.SystemUtils;

public class NetworkManager {
	
	private static NetworkManager	sInstance;
	
	static{
		sInstance = new NetworkManager();
	}
	public static NetworkManager getInstance(){
		return sInstance;
	}
	
	private List<OnNetworkChangeListener>	mListeners;
	
	private boolean							mLastWifiConnected;
	private boolean 						mLastMobileConnected;
	
	private NetworkManager(){
	}
	
	public void addNetworkListener(OnNetworkChangeListener listener){
		if(mListeners == null){
			mListeners = new LinkedList<NetworkManager.OnNetworkChangeListener>();
		}
		if(mListeners.size() == 0){
			startNetworkMonitor();
		}
		if(!mListeners.contains(listener)){
			mListeners.add(listener);
		}
	}
	
	public void removeNetworkListener(OnNetworkChangeListener listener){
		if(mListeners == null){
			return;
		}
		if(mListeners.remove(listener)){
			if(mListeners.size() == 0){
				stopNetworkMonitor();
			}
		}
	}
	
	private void startNetworkMonitor(){
		final Context context = XApplication.getApplication();
		ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (networkInfo != null)
			mLastWifiConnected = networkInfo.isConnected();

		networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (networkInfo != null)
			mLastMobileConnected = networkInfo.isConnected();
		context.registerReceiver(mBroadcastReceiverNetworkMonitor, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	private void stopNetworkMonitor(){
		final Context context = XApplication.getApplication();
		context.unregisterReceiver(mBroadcastReceiverNetworkMonitor);
	}
	
	private void notifyListenerAvailable(){
		if(mListeners != null){
			for(OnNetworkChangeListener listener : new ArrayList<OnNetworkChangeListener>(mListeners)){
				listener.onNetworkAvailable();
			}
		}
	}
	
	private void notifyListenerChange(){
		if(mListeners != null){
			for(OnNetworkChangeListener listener : new ArrayList<OnNetworkChangeListener>(mListeners)){
				listener.onNetworkChanged();
			}
		}
	}
	
	private void notifyListenerReceive(Context context){
		if(mListeners != null){
			for(OnNetworkChangeListener listener : new ArrayList<OnNetworkChangeListener>(mListeners)){
				if(listener instanceof OnNetworkListener){
					((OnNetworkListener)listener).onReceive(context);
				}
			}
		}
	}
	
	private BroadcastReceiver mBroadcastReceiverNetworkMonitor = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			notifyListenerReceive(context);
			
			if(SystemUtils.isNetworkAvailable(context)){
				notifyListenerAvailable();
			}
			
			boolean isWifiConnected = false;
			boolean isMobileConnected = false;
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (networkInfo != null)
				isWifiConnected = networkInfo.isConnected();

			networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (networkInfo != null)
				isMobileConnected = networkInfo.isConnected();
			
			if(isMobileConnected != mLastMobileConnected ||
					isWifiConnected != mLastWifiConnected){
				mLastMobileConnected = isMobileConnected;
				mLastWifiConnected = isWifiConnected;
				notifyListenerChange();
			}
		}
	};
	
	public static interface OnNetworkChangeListener{
		public void onNetworkAvailable();
		
		public void onNetworkChanged();
	}
	
	public static interface OnNetworkListener extends OnNetworkChangeListener{
		public void onReceive(Context context);
	}
}
