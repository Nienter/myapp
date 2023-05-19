package com.xbcx.im.recentchat;

import java.util.HashMap;

import android.text.TextUtils;
import android.util.SparseArray;

import com.xbcx.core.XApplication;
import com.xbcx.core.module.AppBaseListener;
import com.xbcx.im.IMKernel;
import com.xbcx.im.IMLocalID;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.im.vcard.VCardProvider;
import com.xbcx.library.R;

public class XMessageRecentChatProvider implements RecentChatProvider {
	
	private static SparseArray<ContentProvider> 			mMapMsgTypeToContentProvider = new SparseArray<ContentProvider>();
	private static HashMap<String, MessageNotifyProvider> 	mMapToIdToMessageNotifyProvider = null;
	
	public static void registerMessageTypeContentProvider(int msgType,ContentProvider provider){
		if(provider != null){
			mMapMsgTypeToContentProvider.put(msgType, provider);
		}
	}
	
	public static String getContentByProvider(XMessage xm){
		ContentProvider provider = mMapMsgTypeToContentProvider.get(xm.getType());
		return provider == null ? xm.getContent() : provider.getContent(XApplication.getApplication(), xm);
	}
	
	public static void registerMessageNotifyProvider(String toId,MessageNotifyProvider p){
		if(mMapToIdToMessageNotifyProvider == null){
			mMapToIdToMessageNotifyProvider = new HashMap<String, XMessageRecentChatProvider.MessageNotifyProvider>();
		}
		mMapToIdToMessageNotifyProvider.put(toId, p);
	}
	
	public static boolean checkMessageNotifyProvider(XMessage xm){
		if(mMapToIdToMessageNotifyProvider == null){
			return true;
		}
		MessageNotifyProvider p = mMapToIdToMessageNotifyProvider.get(xm.getOtherSideId());
		return p == null ? true : p.isNotify(xm);
	}
	
	public XMessageRecentChatProvider() {
	}
	
	@Override
	public void handleRecentChat(RecentChat rc, Object obj) {
		XMessage xm = (XMessage)obj;
		final String id = xm.getOtherSideId();
		if(IMLocalID.ID_FriendVerify.equals(id)){
			rc.setName(IMKernel.getInstance().getContext().getString(
					R.string.friend_verify_notice));
			rc.setActivityType(ActivityType.FriendVerify);
			rc.setContent(IMKernel.getInstance().getContext().getString(
					R.string.apply_add_you_friend, xm.getUserName()));
		}else{
			if(RecentChatManager.getInstance().isConstantId(id)){
				rc.setName(xm.isFromGroup() ? xm.getGroupName() : xm.getUserName());
				rc.setContent(getContent(xm));
			}else{
				String name = null;
				if(xm.isFromGroup()){
					name = xm.getGroupName();
				}else{
					name = xm.getUserName();
					if(TextUtils.isEmpty(name)){
						name = VCardProvider.getInstance().getCacheName(xm.getUserId());
					}
				}
				if(!TextUtils.isEmpty(name)){
					rc.setName(name);
				}
				
				if(xm.isFromGroup()){
					final String userName = xm.getUserName();
					if(TextUtils.isEmpty(userName) || xm.isFromSelf()){
						rc.setContent(getContent(xm));
					}else{
						rc.setContent("[" + userName + "]" + getContent(xm));
					}
				}else{
					rc.setContent(getContent(xm));
				}
				final int activityType = IMKernel.fromTypeToActivityType(xm.getFromType());
				if(activityType > 0){
					rc.setActivityType(activityType);
				}
			}
		}
	}

	@Override
	public String getId(Object obj) {
		XMessage xm = (XMessage)obj;
		final String id = xm.getOtherSideId();
		for(GetIdPlugin p : XApplication.getManagers(GetIdPlugin.class)){
			p.onGetId(xm);
		}
		return id;
	}
	
	@Override
	public boolean isUnread(Object obj) {
		XMessage xm = (XMessage)obj;
		if(!xm.isFromSelf()){
			boolean bUnread = !xm.isReaded();
			if(bUnread){
				if(checkMessageNotifyProvider(xm)){
					IMKernel.receiveNotify(xm.getOtherSideId());
				}
			}
			return bUnread;
		}
		return false;
	}

	public String getContent(XMessage xm) {
		return getContentByProvider(xm);
	}

	@Override
	public long getTime(Object obj) {
		XMessage xm = (XMessage)obj;
		return xm.getSendTime();
	}
	
	public static interface MessageNotifyProvider{
		public boolean isNotify(XMessage xm);
	}
	
	public static interface GetIdPlugin extends AppBaseListener{
		public void onGetId(XMessage xm);
	}
}
