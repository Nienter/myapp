package com.xbcx.im.extention.blacklist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PrivacyList;
import org.jivesoftware.smack.PrivacyListManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Privacy;
import org.jivesoftware.smack.packet.PrivacyItem;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.im.IMBasePlugin;
import com.xbcx.im.IMKernel;
import com.xbcx.im.IMSystem;
import com.xbcx.im.ServicePlugin;
import com.xbcx.im.IMSystem.OnLoginGetPlugin;
import com.xbcx.im.IMSystem.OnPreConnectPlugin;
import com.xbcx.im.IMSystem.IMEventRunner;

public class BlackListServicePlugin implements ServicePlugin<IMSystem>,
										BlackListInterface,
										OnPreConnectPlugin,
										OnLoginGetPlugin{
	
	/**
	 * @param Collection(String)
     */
	public static final int IM_BlackListChanged		= EventCode.generateEventCode();
	
	/**
	 * @param id
     */
	public static final int IM_AddBlackList			= EventCode.generateEventCode();
	
	/**
	 * @param id
     */
	public static final int IM_DeleteBlackList		= EventCode.generateEventCode();
	
	/**
	 * @param VerifyType
     */
	public static final int IM_SetVerifyType		= EventCode.generateEventCode();
	
	private static BlackListInterface 	instance;
	
	public static BlackListInterface getInterface(){
		if(instance == null){
			instance = new EmptyBlackListInterface();
		}
		return instance;
	}
	
	protected IMSystem				mIMService;
	
	protected Map<String,String> 	mMapIdBlackList = new ConcurrentHashMap<String, String>();
	
	protected String				mVerifyType;
	
	@Override
	public void onAttachService(IMSystem service) {
		mIMService = service;
		instance = this;
		service.managerRegisterRunner(IM_SetVerifyType, new SetVerifyTypeRunner());
		service.managerRegisterRunner(IM_AddBlackList, new AddPrivacyRunner());
		service.managerRegisterRunner(IM_DeleteBlackList, new DeletePrivacyRunner());
	}

	@Override
	public void onServiceDestory() {
		instance = null;
	}
	
	@Override
	public Collection<String> getBlackLists(){
		return mMapIdBlackList.keySet();
	}
	
	@Override
	public boolean isInBlackList(String id){
		return mMapIdBlackList.containsKey(id);
	}
	
	@Override
	public VerifyType getVerifyType(){
		return VerifyType.valueOf(mVerifyType);
	}
	
	@Override
	public void onPreConnect(XMPPConnection connection) {
		PrivacyListManager.getInstanceFor(connection);
	}
	
	@Override
	public void onLoginGet() throws Exception {
		loadBlackList();
	}

	protected void loadBlackList() throws Exception {
		XMPPConnection con = mIMService.getConnection();
		PrivacyListManager plm = PrivacyListManager.getInstanceFor(con);
		if (plm != null) {
			String listName = "default";
			Privacy request = new Privacy();
			request.setPrivacyList(listName, new ArrayList<PrivacyItem>());
			Privacy answer = plm.getRequest(request);
			PrivacyList privacyList = new PrivacyList(false, true, listName,
					answer.getPrivacyList(listName));
			for (String strName : answer.getPrivacyListAttributeNames(listName)) {
				privacyList.mAttributeHelper.addAttribute(strName,
						answer.getPrivacyListAttributeValue(listName, strName));
			}

			synchronized (mMapIdBlackList) {
				mMapIdBlackList.clear();
				for (PrivacyItem item : privacyList.getItems()) {
					if (PrivacyItem.Type.jid.equals(item.getType())) {
						final String strId = IMKernel.removeSuffix(item.getValue());
						mMapIdBlackList.put(strId, strId);
					}
				}
			}
			
			onBlackListChanged();
			
			mVerifyType = privacyList.mAttributeHelper.getAttributeValue("type");
		}
	}
	
	protected void doAddBlackList(List<String> listUserId) throws XMPPException{
		List<PrivacyItem> listItem = new ArrayList<PrivacyItem>();
		for (String strUserId : listUserId) {
			PrivacyItem item = new PrivacyItem("jid", false, 0);
			item.setValue(mIMService.addSuffixUserJid(strUserId));
			listItem.add(item);
		}
		final XMPPConnection c = mIMService.getConnection();
		if (listItem.size() > 0) {
			TypePrivacy packet = new TypePrivacy();
			packet.setFrom(c.getUser());
			packet.setType(IQ.Type.SET);
			packet.setPrivacyType("addblack");
			packet.setPrivacyList("default", listItem);

			PacketCollector collector = c.createPacketCollector(
					new PacketIDFilter(packet.getPacketID()));
			try{
				c.sendPacket(packet);
				Privacy result = (Privacy) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
				mIMService.checkResultIQ(result);
			}finally{
				collector.cancel();
			}
			
			synchronized (mMapIdBlackList) {
				for(String strUserId : listUserId){
					mMapIdBlackList.put(strUserId, strUserId);
				}
			}
			
			onBlackListChanged();
			
			for(AddBlackListPlugin ap : mIMService.getPlugins(AddBlackListPlugin.class)){
				ap.onUsersAddBlackList(listUserId);
			}
		}
	}
	
	protected void onBlackListChanged(){
		AndroidEventManager.getInstance().runEvent(IM_BlackListChanged, 
				Collections.unmodifiableCollection(mMapIdBlackList.keySet()));
	}
	
	protected void doDeleteBlackList(List<String> listUserId) throws XMPPException{
		List<PrivacyItem> listItem = new ArrayList<PrivacyItem>();
		for (String strUserId : listUserId) {
			PrivacyItem item = new PrivacyItem("jid", false, 0);
			item.setValue(mIMService.addSuffixUserJid(strUserId));
			listItem.add(item);
		}
		if (listItem.size() > 0) {
			TypePrivacy packet = new TypePrivacy();
			packet.setType(IQ.Type.SET);
			packet.setPrivacyType("delblack");
			packet.setPrivacyList("default", listItem);

			final XMPPConnection c = mIMService.getConnection();
			PacketCollector collector = c.createPacketCollector(
					new PacketIDFilter(packet.getPacketID()));
			try{
				c.sendPacket(packet);
				Privacy result = (Privacy) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
				mIMService.checkResultIQ(result);
			}finally{
				collector.cancel();
			}
			
			synchronized (mMapIdBlackList) {
				for(String strUserId : listUserId){
					mMapIdBlackList.remove(strUserId);
				}
			}
			
			onBlackListChanged();
		}
	}
	
	private class AddPrivacyRunner extends IMEventRunner{
		public AddPrivacyRunner() {
			mIMService.super();
		}
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String id = (String)event.getParamAtIndex(0);
			List<String> ids = new ArrayList<String>();
			ids.add(id);
			doAddBlackList(ids);
			return true;
		}
	}
	
	private class DeletePrivacyRunner extends IMEventRunner{
		public DeletePrivacyRunner() {
			mIMService.super();
		}
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String id = (String)event.getParamAtIndex(0);
			List<String> ids = new ArrayList<String>();
			ids.add(id);
			doDeleteBlackList(ids);
			return true;
		}
	}
	
	private class SetVerifyTypeRunner extends IMEventRunner{
		public SetVerifyTypeRunner() {
			mIMService.super();
		}
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final VerifyType verifyType = (VerifyType)event.getParamAtIndex(0);
			
			final XMPPConnection c = mIMService.getConnection();
			TypePrivacy privacy = new TypePrivacy();
			privacy.setType(IQ.Type.SET);
			privacy.setPrivacyType("auth");
			privacy.setListType(verifyType.getValue());
			privacy.setPrivacyList("default", new ArrayList<PrivacyItem>());
			PacketCollector collector = c.createPacketCollector(
					new PacketIDFilter(privacy.getPacketID()));
			managePacketCollector(collector);
			c.sendPacket(privacy);
			Privacy result = (Privacy)collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
			mIMService.checkResultIQ(result);
			
			mVerifyType = verifyType.getValue();
			
			return true;
		}
	}
	
	public static interface AddBlackListPlugin extends IMBasePlugin{
		public void onUsersAddBlackList(Collection<String> users);
	}
	
	private static class EmptyBlackListInterface implements BlackListInterface{
		@Override
		public VerifyType getVerifyType() {
			return VerifyType.TYPE_NONE;
		}

		@Override
		public Collection<String> getBlackLists() {
			return null;
		}

		@Override
		public boolean isInBlackList(String userId) {
			return false;
		}
	}
}
