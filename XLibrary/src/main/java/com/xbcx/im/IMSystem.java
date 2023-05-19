package com.xbcx.im;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.text.TextUtils;

import com.xbcx.common.EventRunnerHelper;
import com.xbcx.common.NetworkManager;
import com.xbcx.common.NetworkManager.OnNetworkChangeListener;
import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.core.PicUrlObject;
import com.xbcx.core.PluginHelper;
import com.xbcx.core.StringIdException;
import com.xbcx.core.XApplication;
import com.xbcx.core.module.IMServicePlugin;
import com.xbcx.im.extention.roster.RosterServicePlugin;
import com.xbcx.im.protocol.MessageDetailProvider;
import com.xbcx.im.protocol.MessageEventProvider;
import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.im.vcard.VCardProvider;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Body;
import org.jivesoftware.smack.packet.MessageEvent;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.packet.StreamError;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.sasl.SASLMechanism.Failure;
import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.WeakHashMap;

public class IMSystem extends Service implements ConnectionListener,
												ChatManagerListener,
												OnNetworkChangeListener,
												OnEventListener{
	
	public static final String VCARD_FILED_AVATARURL 	= "DESC";
	public static final String VCARD_FIELD_ADMIN		= "ADMIN";
	
	public static String	GROUP_FLAG 			= "broadcast";
	public static String 	DISCUSSION_FLAG 	= "qz";
	
	protected Context							mContext;
	
	protected String							mServer;
	
	protected XMPPConnection 					mConnection;
	
	protected IMLoginInfo				mLoginInfo;
	
	protected boolean 	mIsReConnect 	   			= false;
	protected int		mReConnectIntervalMillis 	= 3000;
	
	protected boolean	mIsInitiativeDisConnect = false;
	protected boolean	mIsConnectionAvailable	= false;
	protected boolean	mIsConnecting			= false;
	
	protected boolean	mUseMessageId 			= true;
	protected boolean	mIsRegister;
	
	protected boolean	mIsDestroyed			= false;
	
	private WeakHashMap<String, String> mMapUserIdToChatThreadId = new WeakHashMap<String, String>();

	protected AndroidEventManager		mEventManager = AndroidEventManager.getInstance();
	
	private EventRunnerHelper			mEventRunnerHelper = new EventRunnerHelper();
	private	PluginHelper<IMBasePlugin>	mPluginHelper;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		
		SmackConfiguration.setPacketReplyTimeout(20000);
		SmackConfiguration.setKeepAliveInterval(30000);
		
		for(IMServicePlugin p : XApplication.getManagers(IMServicePlugin.class)){
			registerPlugin(p.createIMPlugin());
		}
		
		onInitProviderManager(ProviderManager.getInstance());
		
		mEventManager.addEventListener(EventCode.IM_Login, this);
		mEventManager.addEventListener(EventCode.AppBackground, this);
		mEventManager.addEventListener(EventCode.AppForceground, this);
		
		managerRegisterRunner(EventCode.IM_Login, new LoginRunner());
		managerRegisterRunner(EventCode.IM_StatusQuery, new StatusQueryRunner());
		managerRegisterRunner(EventCode.IM_SendMessage, new SendMessageRunner());
		managerRegisterRunner(EventCode.IM_LoadVCard, new LoadVCardRunner());
		
		NetworkManager.getInstance().addNetworkListener(mNetworkChangeListener);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		IMKernel.getInstance().setUnLogin();
		mIsDestroyed = true;
		
		stopNetworkMonitor();
		if(mIsConnectionAvailable){
			XApplication.runOnBackground(new Runnable() {
				@Override
				public void run() {
					doLoginOut();
				}
			});
		}else{
			final XMPPConnection connection = mConnection;
			if(connection != null){
				new Thread(){
					@Override
					public void run() {
						try{
							connection.removeConnectionListener(IMSystem.this);
							connection.disconnect();
						}catch(Exception e){
							
						}
					}
				}.start();
			}	
		}
		
		mEventRunnerHelper.destory();
		
		NetworkManager.getInstance().removeNetworkListener(mNetworkChangeListener);
		
		mEventManager.removeEventListener(EventCode.IM_Login, this);
		mEventManager.removeEventListener(EventCode.AppBackground, this);
		mEventManager.removeEventListener(EventCode.AppForceground, this);
		
		if(mPluginHelper != null){
			for(ServicePlugin<?> p : getPlugins(ServicePlugin.class)){
				p.onServiceDestory();
			}
			
			mPluginHelper.clear();
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void registerPlugin(IMBasePlugin plugin){
		if(mPluginHelper == null){
			mPluginHelper = new PluginHelper<IMBasePlugin>();
		}
		mPluginHelper.addManager(plugin);
		if(plugin instanceof ServicePlugin){
			((ServicePlugin)plugin).onAttachService(this);
		}
	}
	
	public <T extends IMBasePlugin> Collection<T> getPlugins(Class<T> cls){
		if(mPluginHelper == null){
			return Collections.emptySet();
		}
		return mPluginHelper.getManagers(cls);
	}
	
	public void managerRegisterRunner(int eventCode,OnEventRunner runner){
		mEventRunnerHelper.managerRegisterRunner(eventCode, runner);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(IMKernel.getInstance().isLogin()){
			if(intent != null){
				try{
					if(intent.hasExtra("imlogininfo")){
						IMLoginInfo loginInfo = (IMLoginInfo)intent.getSerializableExtra("imlogininfo");
						if(mLoginInfo != null){
							onStartCommandCheckLoginInfo(mLoginInfo);
						}
						mLoginInfo = loginInfo;
						mServer = mLoginInfo.getServer();
					}
					if(intent.hasExtra("reconnect")){
						mIsReConnect = intent.getBooleanExtra("reconnect", false);
					}
					final boolean bLogin = intent.getBooleanExtra("login", false);
					if(bLogin){
						requestLogin();
					}
				}catch(Exception e){
				}
			}
		}else{
			XApplication.getMainThreadHandler().post(new Runnable() {
				@Override
				public void run() {
					stopSelf();
				}
			});
		}
		return super.onStartCommand(intent, flags, startId);
	}

	protected void onStartCommandCheckLoginInfo(IMLoginInfo li){
		if(!mLoginInfo.equals(li)){
			if(mIsConnectionAvailable){
				doLoginOut();
			}
		}
	}
	
	private OnNetworkChangeListener mNetworkChangeListener = new OnNetworkChangeListener() {
		
		@Override
		public void onNetworkChanged() {
			if(mIsConnectionAvailable){
				XApplication.runOnBackground(new Runnable() {
					@Override
					public void run() {
						try{
							mConnection.disconnect();
						}catch(Exception e){
						}
					}
				});
			}
		}
		
		@Override
		public void onNetworkAvailable() {
		}
	};
	
	@Override
	public void onEventRunEnd(Event event) {
		final int code = event.getEventCode();
		if(code == EventCode.IM_Login){
			handleLoginFinished(event.isSuccess());
		}else if(code == EventCode.AppBackground){
			mReConnectIntervalMillis = 60000;
		}else if(code == EventCode.AppForceground){
			mReConnectIntervalMillis = 3000;
			if(!isConnectionAvailable()){
				if(SystemUtils.isNetworkAvailable(this)){
					requestLogin();
				}
			}
		}
	}
	
	public String getLoginNick(){
		return VCardProvider.getInstance().getCacheName(getUser());
	}
	
	public String getAvatarUrl(){
		return "";
	}
	
	protected void requestLogin(){
		mEventManager.pushEvent(EventCode.IM_Login);
	}
	
	protected boolean isLocalId(String strUserId){
		if(strUserId == null){
			return false;
		}
		if(mLoginInfo != null){
			if(strUserId.equals(mLoginInfo.getUser())){
				return true;
			}
		}
		return false;
	}
	
	protected String	getUser(){
		return mLoginInfo == null ? "" : mLoginInfo.getUser();
	}
	
	public XMPPConnection getConnection(){
		return mConnection;
	}
	
	public boolean isConnectionAvailable(){
		return mIsConnectionAvailable;
	}
	
	protected final void doLogin() throws Exception{
		doLogin(false);
	}
	
	protected final void doLogin(boolean bRegister) throws Exception{
		if(mIsConnectionAvailable){
			return;
		}
		
		if(mIsConnecting){
			return;
		}
		
		if(mIsDestroyed){
			throw new XMPPException("IM Destroyed");
		}
		
		mIsConnecting = true;
		
		try {
			onInitLoginInfo();
			
			ConnectionConfiguration cc = new ConnectionConfiguration(
					mLoginInfo.getIP(), mLoginInfo.getPort(),mLoginInfo.getServer());
			mConnection = new XMPPConnection(cc);
			
			addLogger();
			
			for(OnPreConnectPlugin p : getPlugins(OnPreConnectPlugin.class)){
				p.onPreConnect(mConnection);
			}

			mConnection.getChatManager().addChatListener(this);

			mConnection.connect();
			
			if(mIsDestroyed){
				throw new XMPPException("IM Destroyed");
			}
			
			configConnectionFeatures(mConnection);
			mConnection.addConnectionListener(this);

			onPreLogin();
			
			final String strUsername = mLoginInfo.getUser();
			final String strPassword = mLoginInfo.getPwd();
			try{
				mConnection.login(strUsername, strPassword, "android");
			}catch(XMPPException e){
				if(bRegister){
					try{
						doRegister(strUsername, strPassword);
					}catch(XMPPException e1){
						XMPPError error = e1.getXMPPError();
						if(error != null && error.getCode() == 409){
							mIsReConnect = false;
							onLoginPwdError();
						}
						throw e1;
					}
				}
				throw e;
			}

			Presence presence = new Presence(Presence.Type.available);
			onInterceptLoginPresence(presence);
			for(OnInterceptLoginPresencePlugin p : getPlugins(OnInterceptLoginPresencePlugin.class)){
				p.onInterceptLoginPresence(presence);
			}

			mConnection.sendPacket(presence);
			
			onLoginGet();
			
			if(mIsInitiativeDisConnect){
				mConnection.removeConnectionListener(this);
				mConnection.disconnect();
			}else{
				mIsConnectionAvailable = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if(mConnection != null){
				mConnection.removeConnectionListener(this);
				if(mConnection.isAuthenticated()){
					mConnection.disconnect();
				}
			}
			throw e;
		} finally {
			mIsConnecting = false;
		}
	}
	
	protected void onInitLoginInfo() throws Exception{
		
	}
	
	protected void onPreLogin(){
		for(OnPreLoginPlugin p : getPlugins(OnPreLoginPlugin.class)){
			p.onPreLogin();
		}
	}
	
	protected void addLogger(){
		mConnection.addPacketListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				XApplication.getLogger().info("receive:" + packet.toXML());
				if(packet instanceof Failure){
					Failure f = (Failure)packet;
					if("not-authorized".equals(f.getCondition())){
						mIsReConnect = false;
						onLoginFailure();
					}
				}
			}
		}, AllAcceptPacketFilter.getInstance());
		mConnection.addPacketSendingListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				XApplication.getLogger().info("send:" + packet.toXML());
			}
		}, AllAcceptPacketFilter.getInstance());
	}
	
	protected void onInterceptLoginPresence(Presence presence){
		presence.addExtension(new PacketExtension() {
			public String toXML() {
				return new StringBuffer().append("<ver>")
						.append(SystemUtils.getVersionName(mContext))
						.append("</ver>").toString();
			}

			public String getNamespace() {
				return null;
			}

			public String getElementName() {
				return null;
			}
		});
		presence.addExtension(new PacketExtension() {
			public String toXML() {
				return new StringBuffer("<device>android</device>").toString();
			}

			public String getNamespace() {
				return null;
			}

			public String getElementName() {
				return null;
			}
		});
	}
	
	public void sendPacket(final Packet p){
		if(mIsConnectionAvailable){
			new AsyncTask<Void, Void, Void>(){
				@Override
				protected Void doInBackground(Void... params) {
					if(mIsConnectionAvailable){
						try{
							mConnection.sendPacket(p);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					return null;
				}
			}.execute();
		}
	}
	
	protected void onLoginPwdError(){
		mEventManager.runEvent(EventCode.IM_LoginPwdError);
	}
	
	protected void onLoginFailure(){
		mEventManager.runEvent(EventCode.IM_LoginFailure);
	}
	
	protected void onLoginGet() throws Exception{
		for(OnLoginGetPlugin p : getPlugins(OnLoginGetPlugin.class)){
			p.onLoginGet();
		}
	}
	
	protected void handleLoginFinished(boolean bSuccess){
		if(!bSuccess){
			if(mIsReConnect){
				if(SystemUtils.isNetworkAvailable(mContext)){
					requestReconnect();
				}else{
					startNetworkMonitor();
				}
			}
		}
		for(OnLoginFinishPlugin p : getPlugins(OnLoginFinishPlugin.class)){
			p.onLoginFinished(bSuccess);
		}
	}
	
	protected void doLoginOut(){
		mIsInitiativeDisConnect = true;
		
		mIsConnectionAvailable = false;
		
		mIsReConnect = false;
		
		try{
			mConnection.removeConnectionListener(this);
			mConnection.disconnect();
		}catch(Exception e){
			
		}
	}
	
	protected void doRegister(String user, String pwd) throws XMPPException{
		Registration reg = new Registration();
		reg.setType(IQ.Type.SET);
		reg.setTo(mConnection.getServiceName());
		reg.setUsername(user);
		reg.setPassword(pwd);
		reg.addAttribute("android", "geolo_createUser_android");
		PacketFilter packetFilter = new AndFilter(
				new PacketIDFilter(reg.getPacketID()), 
				new PacketTypeFilter(IQ.class));
		
		PacketCollector packetCollerctor = null;
		IQ result = null;
		try{
			packetCollerctor = mConnection.createPacketCollector(packetFilter);
			mConnection.sendPacket(reg);
			result = (IQ) packetCollerctor.nextResult(SmackConfiguration.getPacketReplyTimeout());
		}finally{
			packetCollerctor.cancel();
		}
		
		checkResultIQ(result);
	}
	
	
	
	protected void requestReconnect(){
		mEventManager.pushEventDelayed(EventCode.IM_Login,mReConnectIntervalMillis);
	}
	
	protected void doSend(XMessage xm) throws Exception{
		for(OnSendMessagePlugin p : getPlugins(OnSendMessagePlugin.class)){
			if(p.onSendMessage(xm)){
				return;
			}
		}
		final int nFromType = xm.getFromType();
		final String toId = xm.getOtherSideId();
		Chat chat = getOrCreateChat(toId,xm.getFromType());
		Message message = new Message();
		onSendInit(message, xm);
		if(nFromType == XMessage.FROMTYPE_SINGLE){
			message.attributes.addAttribute("nick", getLoginNick());
		}else{
			message.attributes.addAttribute("nick", xm.getGroupName());
			message.getMessageBody(null).attributes.addAttribute("name", getLoginNick());
		}
		chat.sendMessage(message);
	}
	
	public void onSendInit(Message message,XMessage xm){
		final int messageType = xm.getType();
		final String content = xm.getContent();
		Body body = message.addBody(null, content == null ? "" : content);
		final MessageTypeProcessor processor = IMKernel.getInstance().
				mMapMessageTypeToProcessor.get(messageType);
		if(processor != null){
			processor.onBuildSendXmlAttribute(message, xm,body);
		}
		
		String bodyType = IMKernel.getInstance().
				mMapMessageTypeToBodyType.get(xm.getType());
		if(bodyType != null){
			body.attributes.addAttribute("type", bodyType);
		}
		if(!TextUtils.isEmpty(xm.getBubbleId())){
			body.attributes.addAttribute("bubbleid", xm.getBubbleId());
		}
		if(mUseMessageId){
			message.setPacketID(xm.getId());
		}
	}
	
	public void checkResultIQ(IQ result) throws XMPPException{
		if (result == null) {
			throw new XMPPException("No response from the server.");
		} else if (result.getType() == IQ.Type.ERROR) {
			throw new XMPPException(result.getError());
		}
	}
	
	protected void configConnectionFeatures(XMPPConnection xmppConnection) {
		ServiceDiscoveryManager.setIdentityName("Android_IM");
		ServiceDiscoveryManager.setIdentityType("phone");
		ServiceDiscoveryManager.setNonCapsCaching(false);
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(xmppConnection);
		if (sdm == null) {
			sdm = new ServiceDiscoveryManager(xmppConnection);
		}
		sdm.addFeature("http://jabber.org/protocol/disco#info");
		sdm.addFeature("http://jabber.org/protocol/caps");
		sdm.addFeature("urn:xmpp:avatar:metadata");
		sdm.addFeature("urn:xmpp:avatar:metadata+notify");
		sdm.addFeature("urn:xmpp:avatar:data");
		sdm.addFeature("http://jabber.org/protocol/nick");
		sdm.addFeature("http://jabber.org/protocol/nick+notify");
		sdm.addFeature("http://jabber.org/protocol/xhtml-im");
		sdm.addFeature("http://jabber.org/protocol/muc");
		sdm.addFeature("http://jabber.org/protocol/commands");
		sdm.addFeature("http://jabber.org/protocol/si/profile/file-transfer");
		sdm.addFeature("http://jabber.org/protocol/si");
		sdm.addFeature("http://jabber.org/protocol/bytestreams");
		sdm.addFeature("http://jabber.org/protocol/ibb");
		sdm.addFeature("http://jabber.org/protocol/feature-neg");
		sdm.addFeature("jabber:iq:privacy");
		sdm.addFeature("vcard-temp");
	}
	
	@Override
	public void connectionClosed() {
		XApplication.getLogger().info("connectionClosed");
		if(!mIsInitiativeDisConnect){
			mIsConnectionAvailable = false;
			onHandleConnectionClosedOnError();
		}
	}

	@Override
	public void connectionClosedOnError(Exception e) {
		//e.printStackTrace();
		if(mIsDestroyed){
			return;
		}
		
		XApplication.getLogger().info("connectionClosedOnError:" + SystemUtils.throwableToString(e));
		mIsConnectionAvailable = false;
		
		boolean bConflict = false;
		if(e instanceof XMPPException){
			XMPPException xe = (XMPPException)e;
			StreamError streamError = xe.getStreamError();
			if(streamError != null){
				if("conflict".equals(streamError.getCode())){
					bConflict = true;
					onConflict();
				}
			}
		}
		
		if(!bConflict){
			onHandleConnectionClosedOnError();
		}
	}
	
	protected void onConflict(){
		mEventManager.runEvent(EventCode.IM_Conflict);
		stopSelf();
	}

	protected void onHandleConnectionClosedOnError(){
		mEventManager.runEvent(EventCode.IM_ConnectionInterrupt, 0);
		
		requestLogin();
		
		for(OnConnectionClosedOnErrorPlugin p : getPlugins(OnConnectionClosedOnErrorPlugin.class)){
			p.onConnectionClosedOnError();
		}
	}
	
	@Override
	public void reconnectingIn(int seconds) {
	}

	@Override
	public void reconnectionSuccessful() {
	}

	@Override
	public void reconnectionFailed(Exception e) {
	}
	
	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		mMapUserIdToChatThreadId.put(removeSuffix(chat.getParticipant()), chat.getThreadID());
		if(!createdLocally){
			chat.addMessageListener(mMessageListenerSingleChat);
		}
	}

	protected void onInitProviderManager(ProviderManager pm){
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#items", new DiscoverItemsProvider());
        pm.addIQProvider("query", "http://jabber.org/protocol/disco#info", new DiscoverInfoProvider());
        pm.addIQProvider("vCard","vcard-temp", new org.jivesoftware.smackx.provider.VCardProvider());
        pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
        
        pm.addExtensionProvider("event", "jabber:client", new MessageEventProvider());
 
        pm.addExtensionProvider("x", "jabber:x:delay", new DelayInformationProvider());
        pm.addExtensionProvider("detail", "jabber:client", new MessageDetailProvider());
        pm.addExtensionProvider("detail", "", new MessageDetailProvider());

        ChatStateExtension.Provider chatState = new ChatStateExtension.Provider();   
        pm.addExtensionProvider("active", "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("composing", "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("paused", "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("inactive", "http://jabber.org/protocol/chatstates", chatState);
        pm.addExtensionProvider("gone", "http://jabber.org/protocol/chatstates", chatState);
        
        for(OnInitProviderManagerPlugin p : getPlugins(OnInitProviderManagerPlugin.class)){
        	p.onInitProviderManager(pm);
        }
	}
	
	public Chat getOrCreateChat(String strUserId,int fromType){
		String strChatThreadId = mMapUserIdToChatThreadId.get(strUserId);
		Chat chat = mConnection.getChatManager().getThreadChat(strChatThreadId);
		if(chat == null){
			chat = mConnection.getChatManager().createChat(
					idToJid(strUserId, fromType),
					mMessageListenerSingleChat);
		}
		return chat;
	}
	
	protected void startNetworkMonitor(){
		NetworkManager.getInstance().addNetworkListener(this);
	}
	
	@Override
	public void onNetworkAvailable() {
		requestLogin();
		stopNetworkMonitor();
	}
	
	@Override
	public void onNetworkChanged() {
	}
	
	protected void stopNetworkMonitor(){
		NetworkManager.getInstance().removeNetworkListener(this);
	}
	
	public final String addSuffixUserJid(String strUserId){
		return IMKernel.addSuffixUserJid(strUserId, mServer);
	}
	
	public final String	addSuffixRoomJid(String groupId){
		return IMKernel.addSuffixRoomJid(groupId, mServer);
	}
	
	public final String addSuffixGroupChatJid(String groupId){
		return IMKernel.addSuffixGroupChatJid(groupId, mServer);
	}
	
	public final String	addSuffixDiscussionJid(String id){
		return IMKernel.addSuffixDiscussionJid(id, mServer);
	}
	
	public String	idToJid(String id,int fromType){
		if(fromType == XMessage.FROMTYPE_SINGLE){
			return addSuffixUserJid(id);
		}else if(fromType == XMessage.FROMTYPE_GROUP){
			return addSuffixGroupChatJid(id);
		}else if(fromType == XMessage.FROMTYPE_DISCUSSION){
			return addSuffixDiscussionJid(id);
		}else if(fromType == XMessage.FROMTYPE_CHATROOM){
			return addSuffixRoomJid(id);
		}
		throw new IllegalArgumentException("unkonw fromType");
	}
	
	public String	removeSuffix(String jid){
		return IMKernel.removeSuffix(jid);
	}
	
	private MessageListener mMessageListenerSingleChat = new MessageListener() {
		@Override
		public void processMessage(Chat chat, Message message) {
			try{
				onProcessSingleChatMessage(chat, message);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	
	protected void onProcessSingleChatMessage(Chat chat,Message message){
		if(message.getType().equals(Message.Type.error)){
			return;
		}
		
		Body body = message.getMessageBody(null);
		if (body != null) {
			onProcessSingleChatBody(chat, message, body);
		}
		
		PacketExtension pe = message.getExtension("event", "jabber:client");
		if(pe != null && pe instanceof MessageEvent){
			final MessageEvent event = (MessageEvent)pe;
			onProcessSingleChatEvent(chat, message, event);
			
			for(OnMessageEventPlugin plugin : getPlugins(OnMessageEventPlugin.class)){
				plugin.onHandleMessageEvent(chat,message,event);
			}
		}
	}
	
	protected void onProcessSingleChatBody(Chat chat,Message message,Body body){
		XMessage xm = onCreateXMessage(onCreateReceiveXMessageId(message), 
				parseMessageType(body));
		boolean bFromSelf = false;
		if(chat.getParticipant().contains(GROUP_FLAG)){
			xm.setFromType(XMessage.FROMTYPE_GROUP);
			xm.setGroupId(removeSuffix(chat.getParticipant()));
			xm.setGroupName(message.attributes.getAttributeValue("nick"));
			xm.setUserId(removeSuffix(body.attributes.getAttributeValue("sponsor")));
			xm.setUserName(body.attributes.getAttributeValue("name"));
			if(isLocalId(xm.getUserId())){
				bFromSelf = true;
			}
		}else if(chat.getParticipant().contains(DISCUSSION_FLAG)){
			xm.setFromType(XMessage.FROMTYPE_DISCUSSION);
			xm.setGroupId(removeSuffix(chat.getParticipant()));
			xm.setGroupName(message.attributes.getAttributeValue("nick"));
			xm.setUserId(removeSuffix(body.attributes.getAttributeValue("sponsor")));
			xm.setUserName(body.attributes.getAttributeValue("name"));
			if(isLocalId(xm.getUserId())){
				bFromSelf = true;
			}
		}else{
			final String strUserId = removeSuffix(chat.getParticipant());
			
			xm.setFromType(XMessage.FROMTYPE_SINGLE);
			if(isLocalId(strUserId)){
				xm.setUserId(removeSuffix(message.getTo()));
				xm.setUserName(VCardProvider.getInstance().getCacheName(xm.getUserId()));
				bFromSelf = true;
			}else{
				xm.setUserId(strUserId);
				xm.setUserName(message.attributes.getAttributeValue("nick"));
			}
		}
		onSetMessageCommonValue(xm, message);
		if(bFromSelf){
			xm.setFromSelf(bFromSelf);
			xm.setSended();
			xm.setSendSuccess(true);
		}else{
			String name = null;
			if(xm.isFromGroup()){
				name = xm.getGroupName();
			}else{
				name = xm.getUserName();
			}
			if(!TextUtils.isEmpty(name)){
				VCardProvider.getInstance().saveName(xm.getOtherSideId(), name, 
						IMKernel.fromTypeToActivityType(xm.getFromType()));
			}
		}
		
		for(MessageFilterPlugin p : XApplication.getManagers(MessageFilterPlugin.class)){
			if(p.isFilterMessage(xm)){
				return;
			}
		}
		
		onReceiveMessage(xm);
	}
	
	protected void onProcessSingleChatEvent(Chat chat,Message message,MessageEvent event){
		
	}
	
	public String	onCreateReceiveXMessageId(Message message){
		if(mUseMessageId){
			final String id = message.getPacketID();
			if(TextUtils.isEmpty(id)){
				return XMessage.buildMessageId();
			}
			return id;
		}
		return XMessage.buildMessageId();
	}
	
	public XMessage onCreateXMessage(String strId,int nMessageType){
		return IMGlobalSetting.msgFactory.createXMessage(strId, nMessageType);
	}
	
	public void	onReceiveMessage(XMessage xm){
		mEventManager.runEvent(EventCode.IM_ReceiveMessage, xm);
		mEventManager.runEvent(EventCode.DB_SaveMessage, xm);
		mEventManager.runEvent(EventCode.HandleRecentChat, xm);
	}
	
	public int parseMessageType(Body body){
		int nType = XMessage.TYPE_TEXT;
		if(body.attributes != null){
			final String strBodyType = body.attributes.getAttributeValue("type");
			Integer type = IMKernel.getInstance().
					mMapMessageTypeToBodyType.getKey(strBodyType);
			if(type == null){
				if(TextUtils.isEmpty(strBodyType)){
					return XMessage.TYPE_TEXT;
				}
				return XMessage.TYPE_UNKNOW;
			}else{
				return type.intValue();
			}
		}
		return nType;
	}
	
	public void onSetMessageCommonValue(XMessage xm,Message m){
		Body body = m.getMessageBody(null);
		xm.setFromSelf(false);
		xm.setContent(body.getMessage());
		xm.setSendTime(parseMessageSendTime(m));
		
		onSetXMessageUrl(xm, m,body);
		
		final String displayname = body.attributes.getAttributeValue("displayname");
		if(!TextUtils.isEmpty(displayname)){
			xm.setDisplayName(displayname);
		}
		xm.setBubbleId(body.attributes.getAttributeValue("bubbleid"));
	}
	
	protected void onSetXMessageUrl(XMessage xm,Message m,Body body){
		final int nType = xm.getType();
		final MessageTypeProcessor processor = 
				IMKernel.getInstance().mMapMessageTypeToProcessor.get(nType);
		if(processor != null){
			processor.onParseReceiveAttribute(xm, m, body);
		}
	}
	
	public long parseMessageSendTime(Message message){
		PacketExtension pe = message.getExtension("x","jabber:x:delay");
		if(pe != null && pe instanceof DelayInformation){
			DelayInformation di = (DelayInformation)pe;
			return di.getStamp().getTime();
		}
		return new Date().getTime();
	}

	public boolean isDelayed(Message message){
        PacketExtension pe = message.getExtension("x","jabber:x:delay");
        if(pe != null && pe instanceof DelayInformation) {
            return true;
        }
        return  false;
    }
	protected XMessage createServerMessage(XmlPullParser parser,String xml,
			String id,int fromType) throws Exception{
		parser.setInput(new StringReader(xml));
		int eventType = parser.getEventType();
		while(eventType != XmlPullParser.END_DOCUMENT){
			if(eventType == XmlPullParser.START_DOCUMENT){
				
			}else if(eventType == XmlPullParser.START_TAG){
				if("message".equals(parser.getName())){
					Message message = (Message) PacketParserUtils.parseMessage(parser);

					if (message.getType().equals(Message.Type.error)) {
						break;
					}

					Body body = message.getMessageBody(null);
					if (body != null) {
						Chat chat = getOrCreateChat(id, fromType);
						XMessage xm = onCreateXMessage(
								onCreateReceiveXMessageId(message), 
								parseMessageType(body));
						if (chat.getParticipant().contains(GROUP_FLAG)) {
							xm.setFromType(XMessage.FROMTYPE_GROUP);
							xm.setGroupId(removeSuffix(chat.getParticipant()));
							//xm.setGroupName(message.attributes.getAttributeValue("nick"));
							xm.setUserId(removeSuffix(message.getFrom()));
							xm.setUserName(message.attributes.getAttributeValue("nick"));
						} else {
							final String strUserId = removeSuffix(chat.getParticipant());

							xm.setFromType(XMessage.FROMTYPE_SINGLE);
							xm.setUserId(strUserId);
							xm.setUserName(message.attributes.getAttributeValue("nick"));
						}
						
						onSetMessageCommonValue(xm, message);
						
						if(isLocalId(removeSuffix(message.getFrom()))){
							xm.setFromSelf(true);
							xm.setUploadSuccess(true);
							xm.setSendSuccess(true);
						}
						
						return xm;
					}
				}
			}
			eventType = parser.next();
		}
		return null;
	}
	
	public abstract class IMEventRunner implements OnEventRunner{
		
		private	  List<PacketCollector> mListPacketCollector;
		
		@Override
		public void onEventRun(Event event) throws Exception{
			if (canExecute()) {
				execute(event);
			}else{
				if(this instanceof Delayable){
					mEventManager.addEventListenerOnce(EventCode.IM_Login, 
							new OnEventListener() {
								@Override
								public void onEventRunEnd(Event event) {
									synchronized (IMEventRunner.this) {
										IMEventRunner.this.notify();
									}
								}
							});
					synchronized (this) {
						wait(SmackConfiguration.getPacketReplyTimeout());
					}
					if(canExecute()){
						execute(event);
					}else{
						throw new StringIdException(R.string.toast_disconnect);
					}
				}else{
					throw new StringIdException(R.string.toast_disconnect);
				}
			}
		}
		
		protected boolean canExecute(){
			return mIsConnectionAvailable;
		}
		
		protected void managePacketCollector(PacketCollector collector){
			if(mListPacketCollector == null){
				mListPacketCollector = new ArrayList<PacketCollector>();
			}
			mListPacketCollector.add(collector);
		}
		
		protected void execute(Event event) throws Exception{
			try {
				XApplication.getLogger().info(getClass().getName() + " execute");
				event.setSuccess(onExecute(event));
			} catch (XMPPException e) {
				if("No response from the server.".equals(e.getMessage())){
					onTimeout();
					throw new StringIdException(R.string.toast_disconnect);
				}
				throw e;
			} finally {
				if(mListPacketCollector != null){
					for(PacketCollector c : mListPacketCollector){
						c.cancel();
					}
				}
				XApplication.getLogger().info(getClass().getName() + " execute:" + event.isSuccess());
			}
		}
		
		protected abstract boolean 	onExecute(Event event) throws Exception;
		
		protected void onTimeout(){
			if(mIsConnectionAvailable){
				mConnection.disconnect();
			}
		}
	}
	
	private class StatusQueryRunner implements OnEventRunner{
		@Override
		public void onEventRun(Event event) throws Exception {
			IMStatus status = (IMStatus)event.getParamAtIndex(0);
			status.mIsLogining = mIsConnecting;
			status.mIsLoginSuccess = mIsConnectionAvailable;
		}
	}
	
	private class LoginRunner extends IMEventRunner{

		@Override
		protected boolean onExecute(Event event) throws Exception {
			mEventManager.runEvent(EventCode.IM_LoginStart);
			doLogin(mIsRegister);
			return true;
		}

		@Override
		protected boolean canExecute() {
			return true;
		}

		@Override
		protected void onTimeout() {
		}
	}
	
	protected class SendMessageRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			XMessage xm = (XMessage)event.getParamAtIndex(0);
			
			boolean bSuccess = false;
			try{
				if(mIsConnectionAvailable){
					if(xm.getFromType() == XMessage.FROMTYPE_GROUP){
						if(RosterServicePlugin.getInterface().isSelfInGroup(xm.getGroupId())){
							doSend(xm);
							bSuccess = true;
						}
					}else{
						doSend(xm);
						bSuccess = true;
					}
				}
			}finally{
				xm.setSended();
				if(!xm.isSendSuccess()){
					xm.setSendSuccess(bSuccess);
				}
				xm.updateDB();
			}
			
			return bSuccess;
		}
		
		@Override
		protected boolean canExecute() {
			return true;
		}
	}
	
	private class LoadVCardRunner extends IMEventRunner{
		@Override
		protected boolean onExecute(Event event) throws Exception {
			final String userId = (String)event.getParamAtIndex(0);
			VCard vcard = new VCard();
			vcard.load(mConnection, addSuffixUserJid(userId));
			PicUrlObject bVcard = new PicUrlObject(userId);
			bVcard.setName(vcard.getNickName());
			bVcard.setPicUrl(vcard.getField(VCARD_FILED_AVATARURL));
			
			if(!TextUtils.isEmpty(bVcard.getName())){
				event.addReturnParam(bVcard);
				return true;
			}
			
			return false;
		}
	}
	
	public static interface OnPreLoginPlugin extends IMBasePlugin{
		public void onPreLogin();
	}
	
	public static interface OnLoginFinishPlugin extends IMBasePlugin{
		public void onLoginFinished(boolean bSuccess);
	}
	
	public static interface OnInitProviderManagerPlugin extends IMBasePlugin{
		public void onInitProviderManager(ProviderManager pm);
	}
	
	public static interface OnPreConnectPlugin extends IMBasePlugin{
		public void onPreConnect(XMPPConnection connection);
	}
	
	public static interface OnLoginGetPlugin extends IMBasePlugin{
		public void onLoginGet() throws Exception;
	}
	
	public static interface OnInterceptLoginPresencePlugin extends IMBasePlugin{
		public void onInterceptLoginPresence(Presence p);
	}
	
	public static interface OnMessageEventPlugin extends IMBasePlugin{
		public void onHandleMessageEvent(Chat chat,Message message,MessageEvent event);
	}
	
	public static interface OnConnectionClosedOnErrorPlugin extends IMBasePlugin{
		public void onConnectionClosedOnError();
	}
	
	public static interface OnSendMessagePlugin extends IMBasePlugin{
		public boolean onSendMessage(XMessage xm) throws Exception; 
	}
}
