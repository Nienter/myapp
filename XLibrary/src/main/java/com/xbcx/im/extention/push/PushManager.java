package com.xbcx.im.extention.push;

import org.jivesoftware.smack.packet.Presence;

import android.text.TextUtils;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.XApplication;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.core.module.IMServicePlugin;
import com.xbcx.core.module.UserInitialListener;
import com.xbcx.core.module.UserReleaseListener;
import com.xbcx.im.IMBasePlugin;
import com.xbcx.im.IMKernel;
import com.xbcx.im.IMSystem;
import com.xbcx.im.ServicePlugin;
import com.xbcx.im.IMSystem.OnInterceptLoginPresencePlugin;

public abstract class PushManager implements OnEventListener,
							UserInitialListener,
							UserReleaseListener,
							IMServicePlugin,
							ServicePlugin<IMSystem>,
							OnInterceptLoginPresencePlugin{
	
	public static PushManager getInstance(){
		return instance;
	}
	
	private static PushManager instance;
	
	protected static long				StartPushDelayTime	= 180000;
	
	protected IMSystem					mIMSystem;
	
	protected boolean					mNeedStartIMWhenForceground;
	protected Runnable 					mCheckBackgroundRunnable;
	protected String					mPushToken;

	protected PushManager(){
		instance = this;
		AndroidEventManager.getInstance().addEventListener(EventCode.AppBackground, this);
		AndroidEventManager.getInstance().addEventListener(EventCode.AppForceground, this);
		AndroidEventManager.getInstance().addEventListener(EventCode.IM_Login, this);
	}
	
	public static void setStartPushDelayTime(long time){
		StartPushDelayTime = time;
	}
	
	public void setPushToken(String token){
		mPushToken = token;
		sendTokenPacket();
	}
	
	public void sendTokenPacket(){
		final IMSystem s = mIMSystem;
		if(s != null){
			if(s.isConnectionAvailable()){
				Presence p = new Presence(Presence.Type.available);
				p.attr.addAttribute("token", mPushToken);
				s.sendPacket(p);
			}
		}
	}
	
	@Override
	public void onAttachService(IMSystem service) {
		mIMSystem = service;
	}
	
	@Override
	public void onServiceDestory() {
		mIMSystem = null;
	}

	@Override
	public void onEventRunEnd(Event event) {
		int code = event.getEventCode();
		if(code == EventCode.AppBackground){
			if(IMKernel.getInstance().isLogin()){
				mCheckBackgroundRunnable = new Runnable() {
					@Override
					public void run() {
						if(IMKernel.isInBackground(XApplication.getApplication())){
							if(onStartPush()){
								mNeedStartIMWhenForceground = true;
								IMKernel.getInstance().stopIMService();
							}else{
								IMKernel.getInstance().requestStartIM();
							};
						}
					}
				};
				XApplication.getMainThreadHandler().postDelayed(
						mCheckBackgroundRunnable, StartPushDelayTime);
			}
		}else if(code == EventCode.AppForceground){
			XApplication.getMainThreadHandler().removeCallbacks(mCheckBackgroundRunnable);
			if(mNeedStartIMWhenForceground){
				mNeedStartIMWhenForceground = false;
				IMKernel.getInstance().requestStartIM();
			}
		}else if(code == EventCode.IM_Login){
			if(event.isSuccess()){
				sendTokenPacket();
			}
		}
	}

	@Override
	public void onUserInitial(String user, boolean bAuto) {
		onStartPush();
	}

	@Override
	public void onUserRelease(String user) {
		onStopPush();
		mPushToken = null;
	}

	@Override
	public IMBasePlugin createIMPlugin() {
		return this;
	}

	@Override
	public void onInterceptLoginPresence(Presence p) {
		if(!TextUtils.isEmpty(mPushToken)){
			p.attr.addAttribute("token", mPushToken);
		}
	}
	
	protected abstract boolean onStartPush();
	
	protected abstract void onStopPush();
}
