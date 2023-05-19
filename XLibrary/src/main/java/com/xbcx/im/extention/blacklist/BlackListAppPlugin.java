package com.xbcx.im.extention.blacklist;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.core.module.IMServicePlugin;
import com.xbcx.im.IMBasePlugin;
import com.xbcx.im.recentchat.RecentChatManager;

public class BlackListAppPlugin implements IMServicePlugin,
												OnEventListener{

	public static BlackListAppPlugin getInstance(){
		return instance;
	}
	
	static{
		instance = new BlackListAppPlugin();
	}
	
	private static BlackListAppPlugin instance;
	
	protected BlackListAppPlugin() {
	}
	
	@Override
	public IMBasePlugin createIMPlugin() {
		return new BlackListServicePlugin();
	}
	
	public BlackListAppPlugin setDeleteMessageWhenAddBlack(boolean b){
		if(b){
			AndroidEventManager.getInstance().addEventListener(
					BlackListServicePlugin.IM_AddBlackList, this);
		}else{
			AndroidEventManager.getInstance().removeEventListener(
					BlackListServicePlugin.IM_AddBlackList, this);
		}
		return this;
	}

	@Override
	public void onEventRunEnd(Event event) {
		final int code = event.getEventCode();
		if(code == BlackListServicePlugin.IM_AddBlackList){
			if(event.isSuccess()){
				final String id = (String)event.getParamAtIndex(0);
				AndroidEventManager.getInstance().pushEvent(EventCode.DB_DeleteMessage, id);
				RecentChatManager.getInstance().deleteRecentChat(id);
			}
		}
	}
	
}
