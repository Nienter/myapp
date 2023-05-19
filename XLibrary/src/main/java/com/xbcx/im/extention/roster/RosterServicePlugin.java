package com.xbcx.im.extention.roster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.MessageEvent;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.util.StringUtils;

import android.text.TextUtils;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.NameObject;
import com.xbcx.im.IMKernel;
import com.xbcx.im.IMLocalID;
import com.xbcx.im.IMSystem;
import com.xbcx.im.XMessage;
import com.xbcx.im.IMSystem.IMEventRunner;
import com.xbcx.im.IMSystem.OnLoginGetPlugin;
import com.xbcx.im.IMSystem.OnMessageEventPlugin;
import com.xbcx.im.extention.blacklist.BlackListServicePlugin.AddBlackListPlugin;
import com.xbcx.im.vcard.VCardProvider;
import com.xbcx.im.ServicePlugin;
import com.xbcx.library.R;

public class RosterServicePlugin implements ServicePlugin<IMSystem>,
									OnLoginGetPlugin,
									RosterListener,
									OnMessageEventPlugin,
									RosterInterface,
									AddBlackListPlugin{
	
	/**
	 * @param userId
	 * @param userName
	 *	</br>
	 *	or
	 * @param List(NameObject)
     */
	public static final int IM_AddFriendApply			= EventCode.generateEventCode();
	
	/**
	 * @param userId
	 * @param Message
     */
	public static final int IM_AddFriendVerify			= EventCode.generateEventCode();
	
	/**
	 * @param userId
     */
	public static final int IM_AddFriendConfirm			= EventCode.generateEventCode();
	
	/**
	 * @param userId
     */
	public static final int IM_DeleteFriend				= EventCode.generateEventCode();
	
	/**
	 * @return Collection(IMContact)
     */
	public static final int IM_GetFriendList			= EventCode.generateEventCode();
	
	/**
	 * @param userId
     */
	public static final int IM_CheckIsFriend			= EventCode.generateEventCode();
	
	/**
	 * @param Collection(IMContact)
     */
	public static final int IM_FriendListChanged		= EventCode.generateEventCode();
	
	/**
	 * @return Collection(IMGroup)
     */
	public static final int IM_GetGroupChatList			= EventCode.generateEventCode();
	
	/**
	 * @param groupId
	 * @return IMGroup
     */
	public static final int IM_GetGroup					= EventCode.generateEventCode();
	
	/**
	 * @param id
	 * @return IMContact
     */
	public static final int IM_GetContact				= EventCode.generateEventCode();
	
	/**
	 * @param Collection(IMGroup)
     */
	public static final int IM_GroupChatListChanged		= EventCode.generateEventCode();
	
	/**
	 * @param name(可为空，为空时第二个参数必须是NameObject)
	 * @param Collection(userId)或Collection(NameObject)
	 * @return groupId,name
     */
	public static final int IM_CreateGroupChat			= EventCode.generateEventCode();
	
	/**
	 * @param groupId
     */
	public static final int IM_DeleteGroupChat			= EventCode.generateEventCode();
	
	/**
	 * @param groupId
     */
	public static final int IM_QuitGroupChat			= EventCode.generateEventCode();
	
	/**
	 * @param groupId
	 * @param name
     */
	public static final int IM_ChangeGroupChatName		= EventCode.generateEventCode();
	
	/**
	 * @param groupId
	 * @param Collection(userId)
     */
	public static final int IM_AddGroupChatMember		= EventCode.generateEventCode();
	
	/**
	 * @param groupId
	 * @param Collection(userId)
     */
	public static final int IM_DeleteGroupChatMember	= EventCode.generateEventCode();

	public static RosterInterface getInterface(){
		if(interf == null){
			interf = new EmptyInterface();
		}
		return interf;
	}
	
	private static RosterInterface interf;
	
	protected IMSystem					mIMService;
	
	protected AndroidEventManager		mEventManager = AndroidEventManager.getInstance();
	
	protected Roster					mRoster;
	
	protected Map<String,IMContact> 	mMapIdToContact = new ConcurrentHashMap<String, IMContact>();
	protected Map<String,IMGroup>		mMapIdToGroup	= new ConcurrentHashMap<String, IMGroup>();
	
	protected boolean					mIsAddFriendTwoDirection= false;
	protected boolean					mIsAddFriendConfirm;//添加好友确认后是否加对方为好友
	protected boolean					mIsDeleteGroupNotify;
	
	@Override
	public void onAttachService(IMSystem service) {
		mIMService = service;
		interf = this;
		service.managerRegisterRunner(IM_AddFriendApply, new AddFriendApplyRunner());
		service.managerRegisterRunner(IM_AddFriendVerify, new AddFriendVerifyRunner());
		service.managerRegisterRunner(IM_AddFriendConfirm, new AddFriendConfirmRunner());
		service.managerRegisterRunner(IM_DeleteFriend, new DeleteFriendRunner());
		service.managerRegisterRunner(IM_CreateGroupChat, new CreateGroupChatRunner());
		service.managerRegisterRunner(IM_DeleteGroupChat, new DeleteGroupChatRunner());
		service.managerRegisterRunner(IM_QuitGroupChat, new QuitGroupChatRunner());
		service.managerRegisterRunner(IM_AddGroupChatMember, new AddGroupChatMemberRunner());
		service.managerRegisterRunner(IM_ChangeGroupChatName, new ChangeGroupChatNameRunner());
		service.managerRegisterRunner(IM_DeleteGroupChatMember, new DeleteGroupChatMemberRunner());
	}

	@Override
	public void onServiceDestory() {
		interf = null;
	}

	@Override
	public void onLoginGet() throws Exception {
		mRoster = mIMService.getConnection().getRoster();
		mRoster.addRosterListener(this);
		
		Collection<RosterEntry> listEntry = mRoster.getEntries();
		synchronized (mMapIdToContact) {
			mMapIdToContact.clear();
			mMapIdToGroup.clear();
			for(RosterEntry entry : listEntry){
				onAddRosterEntry(entry);
			}
		}
		onFriendListChanged();
		onGroupChatListChanged();
	}
	
	@Override
	public void onUsersAddBlackList(Collection<String> users) {
		List<String> listDelete = new ArrayList<String>();
		for(String strUserId : users){
			if(RosterServicePlugin.getInterface().isFriend(strUserId)){
				listDelete.add(strUserId);
			}
		}
		if(listDelete.size() > 0){
			entriesDeleted(listDelete);
		}
	}

	@Override
	public Collection<IMContact> getFriends() {
		return Collections.unmodifiableCollection(mMapIdToContact.values());
	}

	@Override
	public Collection<IMGroup> getGroups() {
		return Collections.unmodifiableCollection(mMapIdToGroup.values());
	}

	@Override
	public IMGroup getGroup(String groupId) {
		return mMapIdToGroup.get(groupId);
	}

	@Override
	public IMContact getContact(String userId) {
		return mMapIdToContact.get(userId);
	}

	protected void onAddRosterEntry(RosterEntry entry){
		if(entry.isGroupChat()){
			IMGroup group = new IMGroup(mIMService.removeSuffix(entry.getUser()),entry.getName());
			for(RosterEntry child : entry.getChilds()){
				final String childId = mIMService.removeSuffix(child.getUser());
				group.addMember(new IMContact(childId, child.getName()));
				group.setRole(childId, child.getGroupAdmin());
			}
			mMapIdToGroup.put(group.getId(), group);
		}else{
			if(!IMKernel.isLocalUser(mIMService.removeSuffix(entry.getUser()))){
				IMContact contact = onCreateContactByRosterEntry(entry);
				mMapIdToContact.put(contact.getId(), contact);
			}
		}
	}
	
	@Override
	public boolean isFriend(String strIMUser){
		return mMapIdToContact.containsKey(strIMUser);
	}
	
	@Override
	public boolean isSelfInGroup(String groupId){
		return mMapIdToGroup.containsKey(groupId);
	}
	
	@Override
	public void entriesAdded(Collection<String> addresses) {
		for(String strJid : addresses){
			RosterEntry entry = mRoster.getEntry(strJid);
			onAddRosterEntry(entry);
		}
		onFriendListChanged();
		onGroupChatListChanged();
	}

	@Override
	public void entriesUpdated(Collection<String> addresses) {
		synchronized (mMapIdToContact) {
			for(String strJid : addresses){
				RosterEntry entry = mRoster.getEntry(strJid);
				if(entry.isGroupChat()){
					IMGroup group = new IMGroup(mIMService.removeSuffix(entry.getUser()),entry.getName());
					for(RosterEntry child : entry.getChilds()){
						final String childId = mIMService.removeSuffix(child.getUser());
						group.addMember(new IMContact(childId, child.getName()));
						group.setRole(childId, child.getGroupAdmin());
					}
					mMapIdToGroup.put(group.getId(), group);
				}else{
					IMContact contact = onCreateContactByRosterEntry(entry);
					if(!TextUtils.isEmpty(entry.getName()) ||
							!mMapIdToContact.containsKey(contact.getId())){
						mMapIdToContact.put(contact.getId(), contact);
					}
				}
			}
		}
		onFriendListChanged();
		onGroupChatListChanged();
	}

	@Override
	public void entriesDeleted(Collection<String> addresses) {
		synchronized (mMapIdToContact) {
			for(String jid : addresses){
				if(jid.contains(IMSystem.GROUP_FLAG)){
					mMapIdToGroup.remove(mIMService.removeSuffix(jid));
				}else{
					mMapIdToContact.remove(mIMService.removeSuffix(jid));
				}
			}
		}
		onFriendListChanged();
		onGroupChatListChanged();
	}
	
	protected void onFriendListChanged(){
		mEventManager.runEvent(IM_FriendListChanged, 
				Collections.unmodifiableCollection(mMapIdToContact.values()));
	}
	
	protected void onGroupChatListChanged(){
		mEventManager.runEvent(IM_GroupChatListChanged, 
				Collections.unmodifiableCollection(mMapIdToGroup.values()));
	}

	protected IMContact onCreateContactByRosterEntry(RosterEntry entry){
		IMContact contact = new IMContact(mIMService.removeSuffix(entry.getUser()), entry.getName());
		return contact;
	}

	@Override
	public void presenceChanged(Presence arg0) {
	}

	@Override
	public void onHandleMessageEvent(Chat chat, Message message,
			MessageEvent event) {
		final String strKind = event.mAttris.getAttributeValue("kind");
		if("addfriendask".equals(strKind)){
			final String strUserId = mIMService.removeSuffix(chat.getParticipant());
		
			XMessage xm = mIMService.onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_TEXT);
			xm.setFromType(XMessage.FROMTYPE_GROUP);
			xm.setGroupId(IMLocalID.ID_FriendVerify);
			xm.setUserId(strUserId);
			xm.setUserName(message.attributes.getAttributeValue("nick"));
			xm.setFromSelf(false);
			xm.setContent(event.getContent());
			xm.setSendTime(mIMService.parseMessageSendTime(message));
			mIMService.onReceiveMessage(xm);
		}else if("addfriendconfirm".equals(strKind)){
			onProcessAddFriendConfirmKindMessage(chat, message, event);
		}else if("addfriend".equals(strKind)){
			onProcessAddFriendKindMessage(chat, message, event);
		}else{
			XMessage xm = null;
			if ("creategroup".equals(strKind)) {
				xm = onProcessCreateGroupMessage(chat, message, event);
			} else if ("removegroup".equals(strKind)) {
				final String strName = event.mAttris.getAttributeValue("name");
				xm = mIMService.onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
				xm.setContent(mIMService.getString(R.string.group_prompt_removed_group,strName));
			} else if ("quitgroup".equals(strKind)) {
				xm = onProcessQuitGroupMessage(chat, message, event);
			} else if ("rename".equals(strKind)) {
				final String strJidModifier = event.mAttris.getAttributeValue("sponsor");
				final String strIdModifier = StringUtils.parseName(strJidModifier);
				xm = mIMService.onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
				if(IMKernel.isLocalUser(strIdModifier)){
					xm.setContent(mIMService.getString(R.string.group_prompt_you_changed_group_name));
				}else{
					xm.setContent(event.mAttris.getAttributeValue("name") + 
							mIMService.getString(R.string.group_prompt_changed_group_name));
				}
				xm.setReaded(true);
			} else if ("addmember".equals(strKind)) {
				final String strUserJidInviter = event.mAttris.getAttributeValue("sponsor");
				final String strUserIdInviter = StringUtils.parseName(strUserJidInviter);
				boolean bNotify = false;
				StringBuffer sb = new StringBuffer();
				if (IMKernel.isLocalUser(strUserIdInviter)) {
					sb.append(mIMService.getString(R.string.group_prompt_you_invite));
					boolean bFirst = true;
					for (MessageEvent.Member member : event.getMembers()) {
						final String strJid = member.mAttris.getAttributeValue("jid");
						if("1".equals(member.mAttris.getAttributeValue("new"))){
							if (!IMKernel.isLocalUser(StringUtils.parseName(strJid))) {
								if(bFirst){
									sb.append(member.mAttris.getAttributeValue("name"));
									bFirst = false;
								}else{
									sb.append("、").append(member.mAttris.getAttributeValue("name"));
								}
							}
						}
					}
				} else {
					int nAddPos = 0;
					boolean bFirst = true;
					boolean bFoundInviter = false;
					for (MessageEvent.Member member : event.getMembers()) {
						final String strJid = member.mAttris.getAttributeValue("jid");
						if (strUserJidInviter.equals(strJid)) {
							String strInviterName = member.mAttris.getAttributeValue("name");
							if(strInviterName == null){
								strInviterName = message.attributes.getAttributeValue("nick");
								if(strInviterName == null){
									strInviterName = "";
								}
							}
							bFoundInviter = true;
							sb.insert(0, strInviterName + " " + mIMService.getString(R.string.invite));
							nAddPos = sb.length();
						}else if("1".equals(member.mAttris.getAttributeValue("new"))){
							if (IMKernel.isLocalUser(StringUtils.parseName(strJid))) {
								bNotify = true;
								final int nPos = bFoundInviter ? nAddPos : 0;
								if(bFirst){
									sb.insert(nPos, mIMService.getString(R.string.you));
									bFirst = false;
								}else{
									sb.insert(nPos, mIMService.getString(R.string.you) + "、");
								}
							} else{
								if(bFirst){
									sb.append(member.mAttris.getAttributeValue("name"));
									bFirst = false;
								}else{
									sb.append("、").append(member.mAttris.getAttributeValue("name"));
								}
							}
						}
					}
				}
				sb.append(" " + mIMService.getString(R.string.group_prompt_added_group));
				xm = mIMService.onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
				xm.setContent(sb.toString());
				xm.setReaded(!bNotify);
			} else if ("kicked".equals(strKind)) {
				final String strName = event.mAttris.getAttributeValue("name");
				xm = mIMService.onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
				xm.setContent(mIMService.getString(R.string.group_prompt_you_been_removed,
						strName));
			} else if ("removemember".equals(strKind)) {
				final String strUserJidInviter = event.mAttris.getAttributeValue("sponsor");
				StringBuffer sb = new StringBuffer();
				if (IMKernel.isLocalUser(mIMService.removeSuffix(strUserJidInviter))) {
					sb.append(mIMService.getString(R.string.group_prompt_you_had));
					boolean bFirst = true;
					for (MessageEvent.Member member : event.getMembers()) {
						if(bFirst){
							sb.append(member.mAttris.getAttributeValue("name"));
							bFirst = false;
						}else{
							sb.append("、").append(member.mAttris.getAttributeValue("name"));
						}
					}
				} else {
					sb.append(event.mAttris.getAttributeValue("name"))
					.append(" " + mIMService.getString(R.string.group_prompt_had));
					boolean bFirst = true;
					for (MessageEvent.Member member : event.getMembers()) {
						if(bFirst){
							sb.append(member.mAttris.getAttributeValue("name"));
							bFirst = false;
						}else{
							sb.append("、").append(member.mAttris.getAttributeValue("name"));
						}
					}
				}
				
				sb.append(" " + mIMService.getString(R.string.group_prompt_removed_member));
				xm = mIMService.onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
				xm.setContent(sb.toString());
				xm.setReaded(true);
			} 
			
			if(xm != null){
				xm.setFromType(XMessage.FROMTYPE_GROUP);
				xm.setGroupId(mIMService.removeSuffix(chat.getParticipant()));
				xm.setGroupName(message.attributes.getAttributeValue("nick"));
				xm.setSendTime(mIMService.parseMessageSendTime(message));
				
				mIMService.onReceiveMessage(xm);
			}
		}
	}
	
	protected void onProcessAddFriendConfirmKindMessage(Chat chat,Message message,MessageEvent event){
		final String strUserId = mIMService.removeSuffix(chat.getParticipant());
		String name = message.attributes.getAttributeValue("nick");
		if(TextUtils.isEmpty(name)){
			name = VCardProvider.getInstance().getCacheName(strUserId);
		}
		
		if(!isFriend(strUserId)){
			try{
				doAddFriendOneDirection(strUserId, name);
			}catch(Exception e){
				e.printStackTrace();
			}
			//mRoster.reload();
		}
		
		XMessage xm = mIMService.onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
		xm.setUserId(strUserId);
		xm.setUserName(name);
		xm.setFromType(XMessage.FROMTYPE_SINGLE);
		xm.setSendTime(mIMService.parseMessageSendTime(message));
		
		xm.setContent(mIMService.getString(R.string.add_friend_confirm,xm.getUserName()));
		
		mIMService.onReceiveMessage(xm);
	}
	
	protected void onProcessAddFriendKindMessage(Chat chat,Message message,MessageEvent event){
		final String strUserId = mIMService.removeSuffix(chat.getParticipant());
		
		String name = message.attributes.getAttributeValue("nick");
		if(TextUtils.isEmpty(name)){
			name = VCardProvider.getInstance().getCacheName(strUserId);
		}
		
		if(mIsAddFriendTwoDirection){
			try{
				doAddFriendOneDirection(strUserId, name);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		XMessage xm = mIMService.onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
		xm.setUserId(strUserId);
		xm.setUserName(name);
		xm.setFromType(XMessage.FROMTYPE_SINGLE);
		xm.setSendTime(mIMService.parseMessageSendTime(message));
		
		xm.setContent(mIMService.getString(R.string.add_you_friend,xm.getUserName()));
		
		mIMService.onReceiveMessage(xm);
	}

	protected void doAddFriendOneDirection(String id, String name) throws XMPPException{
		final String jid = mIMService.addSuffixUserJid(id);
		RosterPacket rosterPacket = new RosterPacket();
		rosterPacket.setType(IQ.Type.SET);
		RosterPacket.Item item = new RosterPacket.Item(jid, name);
		rosterPacket.addRosterItem(item);
		
		PacketCollector collector = mIMService.getConnection().createPacketCollector(new PacketIDFilter(rosterPacket.getPacketID()));
		mIMService.getConnection().sendPacket(rosterPacket);
		IQ response = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
		collector.cancel();
		mIMService.checkResultIQ(response);
	}
	
	protected XMessage onProcessCreateGroupMessage(Chat chat,Message message,MessageEvent event){
		final String strUserJidInviter = event.mAttris.getAttributeValue("sponsor");
		final String strUserIdInviter = StringUtils.parseName(strUserJidInviter);
		
		StringBuffer sb = new StringBuffer();
		boolean bLocal = false;
		if (IMKernel.isLocalUser(strUserIdInviter)) {
			sb.append(mIMService.getString(R.string.group_prompt_you_invite));
			boolean bFirst = true;
			for (MessageEvent.Member member : event.getMembers()) {
				final String strId = mIMService.removeSuffix(member.mAttris.getAttributeValue("jid"));
				if (!IMKernel.isLocalUser(strId)) {
					if(bFirst){
						sb.append(member.mAttris.getAttributeValue("name"));
						bFirst = false;
					}else{
						sb.append("、").append(member.mAttris.getAttributeValue("name"));
					}
				}
			}
			bLocal = true;
		} else {
			for (MessageEvent.Member member : event.getMembers()) {
				final String strJid = member.mAttris.getAttributeValue("jid");
				if (strUserJidInviter.equals(strJid)) {
					sb.insert(0, member.mAttris.getAttributeValue("name") + 
							" " + mIMService.getString(R.string.group_prompt_invite_you));
				} else if (!IMKernel.isLocalUser(mIMService.removeSuffix(strJid))) {
					sb.append("、").append(member.mAttris.getAttributeValue("name"));
				}
			}
		}
		
		sb.append(" " + mIMService.getString(R.string.group_prompt_added_group));
		XMessage im = mIMService.onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
		im.setContent(sb.toString());
		if(bLocal){
			im.setReaded(true);
		}
		return im;
	}
	
	protected XMessage onProcessQuitGroupMessage(Chat chat,Message message,MessageEvent event){
		final String strNick = event.mAttris.getAttributeValue("name");
		XMessage im = mIMService.onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
		im.setContent(mIMService.getString(R.string.group_prompt_quited_group,strNick));
		im.setReaded(true);
		return im;
	}
	
	private class AddFriendApplyRunner extends IMEventRunner{
		public AddFriendApplyRunner() {
			mIMService.super();
		}
		@SuppressWarnings("unchecked")
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final Object param = event.getParamAtIndex(0);
			if(param instanceof List){
				List<NameObject> nos = (List<NameObject>)param;
				RosterPacket rosterPacket = new RosterPacket();
				rosterPacket.setType(IQ.Type.SET);
				rosterPacket.setFrom(mIMService.getConnection().getUser());
				
				for(NameObject no : nos){
					RosterPacket.Item item = new RosterPacket.Item(mIMService.addSuffixUserJid(no.getId()), no.getName());
					rosterPacket.addRosterItem(item);
				}
				
				PacketCollector collector = mIMService.getConnection()
						.createPacketCollector(new PacketIDFilter(rosterPacket
								.getPacketID()));
				managePacketCollector(collector);
				mIMService.getConnection().sendPacket(rosterPacket);
				IQ response = (IQ) collector.nextResult(SmackConfiguration
						.getPacketReplyTimeout());
				mIMService.checkResultIQ(response);
				return true;
			}else{
				final String id = (String)event.getParamAtIndex(0);
				final String name = (String)event.getParamAtIndex(1);
				RosterPacket rosterPacket = new RosterPacket();
				rosterPacket.setType(IQ.Type.SET);
				rosterPacket.setFrom(mIMService.getConnection().getUser());
				RosterPacket.Item item = new RosterPacket.Item(mIMService.addSuffixUserJid(id), name);
				
				rosterPacket.addRosterItem(item);
				PacketCollector collector = mIMService.getConnection()
						.createPacketCollector(new PacketIDFilter(rosterPacket
								.getPacketID()));
				managePacketCollector(collector);
				mIMService.getConnection().sendPacket(rosterPacket);
				IQ response = (IQ) collector.nextResult(SmackConfiguration
						.getPacketReplyTimeout());
				mIMService.checkResultIQ(response);
				return true;
			}
		}
	}
	
	private class AddFriendVerifyRunner extends IMEventRunner{
		public AddFriendVerifyRunner() {
			mIMService.super();
		}
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String id = (String)event.getParamAtIndex(0);
			Chat chat = mIMService.getOrCreateChat(id,XMessage.FROMTYPE_SINGLE);
			
			final String strVerifyText = (String)event.getParamAtIndex(1);
			Message message = new Message();
			final MessageEvent me = new MessageEvent();
			me.mAttris.addAttribute("kind", "addfriendask");
			me.setContent(strVerifyText);
			message.addExtension(me);
			message.attributes.addAttribute("nick", mIMService.getLoginNick());
			chat.sendMessage(message);
			
			return true;
		}
	}
	
	private class AddFriendConfirmRunner extends IMEventRunner{
		public AddFriendConfirmRunner() {
			mIMService.super();
		}
		
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String id = (String)event.getParamAtIndex(0);
			if(TextUtils.isEmpty(id)){
				return false;
			}
			Chat chat = mIMService.getOrCreateChat(id,XMessage.FROMTYPE_SINGLE);
			Message message = new Message();
			final MessageEvent me = new MessageEvent();
			me.mAttris.addAttribute("kind", "addfriendconfirm");
			message.addExtension(me);
			message.attributes.addAttribute("nick", mIMService.getLoginNick());
			chat.sendMessage(message);
			
			if(mIsAddFriendConfirm){
				try{
					doAddFriendOneDirection(id, 
							VCardProvider.getInstance().getCacheName(id));
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			return true;
		}
	}
	
	private class DeleteFriendRunner extends IMEventRunner{
		
		public DeleteFriendRunner() {
			mIMService.super();
		}
		
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String id = (String)event.getParamAtIndex(0);
			mRoster.removeEntry(mRoster.getEntry(mIMService.addSuffixUserJid(id)));
			return true;
		}
	}
	
	private class CreateGroupChatRunner extends IMEventRunner{
		
		public CreateGroupChatRunner() {
			mIMService.super();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected boolean onExecute(Event event) throws Exception {
			String name = (String)event.getParamAtIndex(0);
			final Collection<Object> obs = (Collection<Object>)event.getParamAtIndex(1);
			final List<String> jids = new ArrayList<String>();
			final List<NameObject> nos = new ArrayList<NameObject>();
			boolean bGenerateName = TextUtils.isEmpty(name);
			for(Object obj : obs){
				if(obj instanceof String){
					jids.add(mIMService.addSuffixUserJid((String)obj));
				}else if(obj instanceof NameObject){
					final NameObject no = (NameObject)obj;
					jids.add(mIMService.addSuffixUserJid(no.getId()));
					nos.add(no);
				}
			}
			final String localJid = mIMService.addSuffixUserJid(IMKernel.getLocalUser());
			if(!jids.contains(localJid)){
				jids.add(localJid);
				nos.add(new NameObject(IMKernel.getLocalUser(), mIMService.getLoginNick()));
			}
			if(bGenerateName){
				name = IMKernel.generateGroupName(nos);
			}
			event.addReturnParam(StringUtils.parseName(
					mRoster.createGroupChat(name, jids, SmackConfiguration.getPacketReplyTimeout())));
			event.addReturnParam(name);
			return true;
		}
	}
	
	private class DeleteGroupChatRunner extends IMEventRunner{
		
		public DeleteGroupChatRunner() {
			mIMService.super();
		}
		
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String groupId = (String)event.getParamAtIndex(0);
			IMGroup group = mMapIdToGroup.get(groupId);
			final String groupName = group == null ? "" : group.getName();
			mRoster.deleteGroupChat(mIMService.addSuffixGroupChatJid(groupId), SmackConfiguration.getPacketReplyTimeout());
			
			if(mIsDeleteGroupNotify){
				try{
					XMessage xm = mIMService.onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
					xm.setContent(mIMService.getString(R.string.group_prompt_removed_group,
							mIMService.getString(R.string.you)));
					xm.setFromType(XMessage.FROMTYPE_GROUP);
					xm.setGroupId(groupId);
					xm.setGroupName(groupName);
					xm.setSendTime(System.currentTimeMillis());
					mIMService.onReceiveMessage(xm);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			return true;
		}
	}
	
	private class QuitGroupChatRunner extends IMEventRunner{
		
		public QuitGroupChatRunner() {
			mIMService.super();
		}
		
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String groupId = (String)event.getParamAtIndex(0);
			IMGroup group = mMapIdToGroup.get(groupId);
			final String groupName = group == null ? "" : group.getName();
			mRoster.quitGroupChat(mIMService.addSuffixGroupChatJid(groupId), SmackConfiguration.getPacketReplyTimeout());
			
			if(mIsDeleteGroupNotify){
				try{
					XMessage xm = mIMService.onCreateXMessage(XMessage.buildMessageId(), XMessage.TYPE_PROMPT);
					xm.setContent(mIMService.getString(R.string.group_prompt_quited_group,mIMService.getString(R.string.you)));
					xm.setFromType(XMessage.FROMTYPE_GROUP);
					xm.setGroupId(groupId);
					xm.setGroupName(groupName);
					xm.setSendTime(System.currentTimeMillis());
					mIMService.onReceiveMessage(xm);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			return true;
		}
	}
	
	private class ChangeGroupChatNameRunner extends IMEventRunner{
		
		public ChangeGroupChatNameRunner() {
			mIMService.super();
		}
		
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String groupId = (String)event.getParamAtIndex(0);
			final String name = (String)event.getParamAtIndex(1);
			RosterEntry re = mRoster.getEntry(mIMService.addSuffixGroupChatJid(groupId));
			if(re != null && re.isGroupChat()){
				re.changeGroupName(name, SmackConfiguration.getPacketReplyTimeout());
				return true;
			}
			return false;
		}
	}
	
	private class AddGroupChatMemberRunner extends IMEventRunner{
		
		public AddGroupChatMemberRunner() {
			mIMService.super();
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String groupId = (String)event.getParamAtIndex(0);
			final Collection<String> ids = (Collection<String>)event.getParamAtIndex(1);
			RosterEntry re = mRoster.getEntry(mIMService.addSuffixGroupChatJid(groupId));
			if(re != null && re.isGroupChat()){
				Collection<String> jids = new ArrayList<String>();
				for(String id : ids){
					jids.add(mIMService.addSuffixUserJid(id));
				}
				re.addChilds(jids, SmackConfiguration.getPacketReplyTimeout());
				return true;
			}
			return false;
		}
	}
	
	private class DeleteGroupChatMemberRunner extends IMEventRunner{
		
		public DeleteGroupChatMemberRunner() {
			mIMService.super();
		}
		@SuppressWarnings("unchecked")
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String groupId = (String)event.getParamAtIndex(0);
			final Collection<String> ids = (Collection<String>)event.getParamAtIndex(1);
			RosterEntry re = mRoster.getEntry(mIMService.addSuffixGroupChatJid(groupId));
			if(re != null && re.isGroupChat()){
				final Collection<String> jids = new ArrayList<String>();
				for(String id : ids){
					jids.add(mIMService.addSuffixUserJid(id));
				}
				re.deleteChilds(jids, SmackConfiguration.getPacketReplyTimeout());
				return true;
			}
			return false;
		}
	}
	
	private static class EmptyInterface implements RosterInterface{

		@Override
		public boolean isSelfInGroup(String groupId) {
			return false;
		}

		@Override
		public Collection<IMContact> getFriends() {
			return Collections.emptySet();
		}

		@Override
		public Collection<IMGroup> getGroups() {
			return Collections.emptySet();
		}

		@Override
		public boolean isFriend(String userId) {
			return false;
		}

		@Override
		public IMGroup getGroup(String groupId) {
			return null;
		}

		@Override
		public IMContact getContact(String userId) {
			return null;
		}
	}
}
