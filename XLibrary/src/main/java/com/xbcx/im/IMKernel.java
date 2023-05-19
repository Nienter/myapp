package com.xbcx.im;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.SparseArray;

import com.xbcx.common.voice.SoundPlayManager;
import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.core.NameObject;
import com.xbcx.core.SharedPreferenceDefine;
import com.xbcx.core.XApplication;
import com.xbcx.core.module.UserInitialListener;
import com.xbcx.core.module.UserReleaseListener;
import com.xbcx.im.db.IMDatabaseManager;
import com.xbcx.im.messageprocessor.MessageUploadProcessor;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.im.ui.simpleimpl.ConflictActivity;
import com.xbcx.im.ui.simpleimpl.LoginPwdErrorActivity;
import com.xbcx.utils.FileHelper;
import com.xbcx.utils.SystemUtils;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.Collection;
import java.util.List;

public class IMKernel implements OnEventListener{

	public static IMKernel getInstance(){
		if(sInstance == null){
			sInstance = new IMKernel();
		}
		return sInstance;
	}
	
	private static IMKernel sInstance;
	
	protected AndroidEventManager		mEventManager = AndroidEventManager.getInstance();
	
	@SuppressWarnings("rawtypes")
	protected Class						mIMSystemClass;
	
	protected Context					mContext;
	protected boolean					mIsInitial;
	
	protected String					mUserId;
	protected boolean					mIsConflict;
	protected boolean					mIsLogin;
	
	protected BidiMap<Integer, String> 			mMapMessageTypeToBodyType = new DualHashBidiMap<Integer, String>();
	protected SparseArray<MessageTypeProcessor> mMapMessageTypeToProcessor = new SparseArray<MessageTypeProcessor>();
	
	protected IMKernel(){
		XApplication.addManager(IMFilePathManager.getInstance());
		registerIMSystem(IMSystem.class);
		
		IMGlobalSetting.setMessageTypeForwardable(XMessage.TYPE_TEXT);
		
		ActivityType.registerActivityClass(ActivityType.ConflictActivity, ConflictActivity.class);
		ActivityType.registerActivityClass(ActivityType.PwdErrorActivity, LoginPwdErrorActivity.class);
		
		mEventManager.addEventListener(EventCode.IM_Conflict, this);
		mEventManager.addEventListener(EventCode.IM_LoginPwdError, this);
		mEventManager.addEventListener(EventCode.IM_LoginFailure, this);
	}
	
	public void initial(Context context){
		if(mIsInitial){
			return;
		}
		mIsInitial = true;
		mContext = context.getApplicationContext();
	}
	
	@SuppressWarnings("rawtypes")
	public void registerIMSystem(Class imClass){
		mIMSystemClass = imClass;
	}
	
	public void	addBodyType(int messageType,String bodyType){
		if(!TextUtils.isEmpty(bodyType)){
			mMapMessageTypeToBodyType.put(messageType, bodyType);
		}
	}
	
	public void	registerMessageTypeProcessor(int type,MessageTypeProcessor processor){
		if(processor != null){
			mMapMessageTypeToProcessor.put(type, processor);
		}
	}
	
	public static boolean canLogin(){
		return canLogin(null);
	}
	
	public static boolean canLogin(String[] userPwd){
		final SharedPreferences sp = getInstance().getContext()
				.getSharedPreferences(SharedPreferenceDefine.SP_IM, 0);
		String user = sp.getString(SharedPreferenceDefine.KEY_USER, null);
		if(!SystemUtils.isTrimEmpty(user)){
			String pwd = sp.getString(SharedPreferenceDefine.KEY_PWD, null);
			if(!SystemUtils.isTrimEmpty(pwd)){
				if(userPwd != null && userPwd.length > 1){
					userPwd[0] = user;
					userPwd[1] = pwd;
				}
				return true;
			}
		}
		return false;
	}
	
	public static void login(String imUser, String imPwd) {
		if(!TextUtils.isEmpty(imUser) &&
				!TextUtils.isEmpty(imPwd)){
			SharedPreferences sp = XApplication.getApplication().getSharedPreferences(
					SharedPreferenceDefine.SP_IM, 0);
			sp.edit()
			.putString(SharedPreferenceDefine.KEY_USER, imUser)
			.putString(SharedPreferenceDefine.KEY_PWD, imPwd)
			.apply();
			IMLoginInfo li = XApplication.getApplication().createIMLoginInfo(imUser, imPwd);
			IMKernel.getInstance().loginUserId(li, true,false);
		}
	}
	
	public static boolean isIMConnectionAvailable(){
		IMStatus status = new IMStatus();
		getInstance().mEventManager.runEvent(EventCode.IM_StatusQuery,status);
		return status.mIsLoginSuccess;
	}
	
	public static boolean isSendingMessage(String msgId){
		return false;
	}
	
	public static void requestSendMessage(XMessage xm,String id,String name,int fromType){
		xm.setFromType(fromType);
		if(fromType == XMessage.FROMTYPE_SINGLE){
			xm.setUserId(id);
			xm.setUserName(name);
		}else{
			xm.setGroupId(id);
			xm.setGroupName(name);
		}
		sendMessage(xm);
	}
	
	public static void	forwardMessage(XMessage src,int fromType,String toId,String toName){
		XMessage copy = src.copy();
		copy.setFromType(fromType);
		if(fromType == XMessage.FROMTYPE_SINGLE){
			copy.setUserId(toId);
			copy.setUserName(toName);
		}else{
			copy.setGroupId(toId);
			copy.setGroupName(toName);
		}
		if(!TextUtils.isEmpty(src.getUserId())){
			FileHelper.copyFile(copy.getFilePath(), src.getFilePath());
			FileHelper.copyFile(copy.getThumbFilePath(), src.getThumbFilePath());
		}
		
		copy.setUploadSuccess(true);
		
		sendMessage(copy);
	}
	
	public static void	forwardMessage(String picPath,int fromType,String toId,String toName){
		XMessage xm = IMGlobalSetting.msgFactory.createXMessage(
				XMessage.buildMessageId(), 
				XMessage.TYPE_PHOTO);
		xm.setFromType(fromType);
		if(fromType == XMessage.FROMTYPE_SINGLE){
			xm.setUserId(toId);
			xm.setUserName(toName);
		}else{
			xm.setGroupId(toId);
			xm.setGroupName(toName);
		}
		FileHelper.copyFile(xm.getFilePath(), picPath);
		
		sendMessage(xm);
	}
	
	private static void sendMessage(XMessage xm){
		xm.setSendTime(XApplication.getFixSystemTime());
		xm.setFromSelf(true);
		AndroidEventManager.getInstance().runEvent(EventCode.DB_SaveMessage, xm);
		AndroidEventManager.getInstance().pushEvent(EventCode.HandleRecentChat, xm);
		MessageUploadProcessor up = MessageUploadProcessor.getMessageUploadProcessor(xm.getType());
		if (isIMConnectionAvailable()){
			if(up != null){
				if(xm.isUploadSuccess() && !TextUtils.isEmpty(xm.getUrl())){
					AndroidEventManager.getInstance().pushEvent(EventCode.IM_SendMessage, xm);
				}else{
					up.requestUpload(xm);
				}
			}else{
				AndroidEventManager.getInstance().pushEvent(EventCode.IM_SendMessage, xm);
			}
		} else {
			if(!xm.isSended()){
				xm.setSended();
				xm.setSendSuccess(false);
				xm.updateDB();
			}
		}

	}
	
	public static int	fromTypeToActivityType(int fromType){
		if(fromType == XMessage.FROMTYPE_SINGLE){
			return ActivityType.SingleChat;
		}else if(fromType == XMessage.FROMTYPE_GROUP){
			return ActivityType.GroupChat;
		}else if(fromType == XMessage.FROMTYPE_DISCUSSION){
			return ActivityType.DiscussionChat;
		}else if(fromType == XMessage.FROMTYPE_CHATROOM){
			return ActivityType.ChatRoom;
		}else{
			return 0;
		}
	}
	
	public static String addSuffixUserJid(String strUserId,String server){
		final String suffix = "@" + server;
		if(strUserId != null && strUserId.endsWith(suffix)){
			return strUserId;
		}
		return strUserId + suffix;
	}
	
	public static String addSuffixRoomJid(String groupId,String server){
		final String suffix = "@conference." + server;
		if(groupId != null && groupId.endsWith(suffix)){
			return groupId;
		}
		return groupId + suffix;
	}
	
	public static String addSuffixGroupChatJid(String groupId,String server){
		final String suffix = "@" + IMSystem.GROUP_FLAG + "." + server;
		if(groupId != null && groupId.endsWith(suffix)){
			return groupId;
		}
		return groupId + suffix;
	}
	
	public static String	addSuffixDiscussionJid(String id,String server){
		final String suffix = "@" + IMSystem.DISCUSSION_FLAG + "." + server;
		if(id != null && id.endsWith(suffix)){
			return id;
		}
		return id + suffix;
	}
	
	public static void	receiveNotify(String fromId){
		if(IMConfigManager.getInstance().isReceiveNewMessageNotify()){
			if(MessageNotifyManager.getInstance().isNotify(fromId)){
				checkPlaySoundAndVibrate();
			}
		}
	}
	
	public static boolean filter(String fromId,XMessage m){
		return fromId.equals(m.getOtherSideId());
	}
	
	public static void 	checkPlaySoundAndVibrate(){
		if(IMConfigManager.getInstance().isReceiveNewMessageSoundNotify()){
			SoundPlayManager.playSound();
		}
		if(IMConfigManager.getInstance().isReceiveNewMessageVibrateNotify()){
			SoundPlayManager.vibrate();
		}
	}
	
	public static String removeSuffix(String jid){
		if(TextUtils.isEmpty(jid)){
			return "";
		}
		int nIndex = jid.lastIndexOf("@");
		if(nIndex != -1){
			return jid.substring(0, nIndex);
		}
		return jid;
	}
	
	public static <T extends NameObject> String generateGroupName(Collection<T> names){
		StringBuffer buf = new StringBuffer();
		int index = 0;
		for(NameObject no : names){
			if(index == 0){
				buf.append(no.getName());
			}else{
				buf.append(",").append(no.getName());
			}
			++index;
			if(index == 3){
				break;
			}
		}
		return buf.toString();
	}
	
	public static IMStatus getIMStatus(){
		IMStatus status = new IMStatus();
		getInstance().mEventManager.runEvent(EventCode.IM_StatusQuery,status);
		status.mIsConflict = getInstance().mIsConflict;
		return status;
	}
	
	public static String getLocalUser(){
		return getInstance().getUserId();
	}
	
	public static boolean isLocalUser(String user){
		if(user == null){
			return false;
		}
		return user.equals(getLocalUser());
	}
	
	public Context	getContext(){
		return mContext;
	}
	
	public String	getUserId(){
		return mUserId;
	}
	
	public boolean	isUserInitial(){
		return !TextUtils.isEmpty(mUserId);
	}
	
	public boolean	isLogin(){
		return mIsLogin;
	}
	
	public void		requestStartIM(){
		if(!mIsLogin){
			String	ups[] = new String[2];
			if(canLogin(ups)){
				loginUserId(XApplication.getApplication().createIMLoginInfo(
						ups[0], ups[1]), true,true);
			}
		}
	}
	
	public void		loginUserId(IMLoginInfo li,boolean bReconnect,boolean bAuto){
		final String userId = li.getUser();
		if(userId != null){
			mIsLogin = true;
			initialUserModule(userId,bAuto);
			mIsConflict = false;
			Intent intent = new Intent(mContext, mIMSystemClass);
			intent.putExtra("imlogininfo", li);
			intent.putExtra("reconnect", bReconnect);
			intent.putExtra("login", true);
			mContext.startService(intent);
		}
	}
	
	public void		initialUserModule(String userId){
		initialUserModule(userId, true);
	}
	
	public synchronized void initialUserModule(String userId,boolean bAuto){
		if(!userId.equals(mUserId)){
			mUserId = userId;
			/**
			 * 有情况在注销后任然要使用用户数据库
			 */
			IMDatabaseManager.getInstance().initial(userId);
			for(UserInitialListener listener : XApplication.getManagers(UserInitialListener.class)){
				listener.onUserInitial(userId,bAuto);
			}
		}
	}
	
	void setUnLogin(){
		mIsLogin = false;
	}
	
	public void		logout(){
		mContext.getSharedPreferences(SharedPreferenceDefine.SP_IM, 0).edit()
		.remove(SharedPreferenceDefine.KEY_PWD).apply();
		stopIMService();
		boolean bNeedRelease = isUserInitial();
		mUserId = null;
		mEventManager.runEvent(EventCode.IM_LoginOuted);
		if(bNeedRelease){
			for(UserReleaseListener listener : XApplication.getManagers(UserReleaseListener.class)){
				listener.onUserRelease(null);
			}
		}
	}
	
	public void stopIMService(){
		Intent intent = new Intent(mContext, mIMSystemClass);
		mContext.stopService(intent);
		mIsLogin = false;
	}
	
	@Override
	public void onEventRunEnd(Event event) {
		final int code = event.getEventCode();
		if(code == EventCode.IM_Conflict){
			mIsConflict = true;
			logout();
			
			if(!ActivityType.launchConflictActivity(mContext)){
				mEventManager.runEvent(EventCode.LoginActivityLaunched);
			}
		}else if(code == EventCode.IM_LoginPwdError){
			logout();
			
			if(!ActivityType.launchPwdErrorActivity(mContext)){
				mEventManager.runEvent(EventCode.LoginActivityLaunched);
			}
		}else if(code == EventCode.IM_LoginFailure){
			logout();
			
			if(!ActivityType.launchLoginFailureActivity(mContext)){
				mEventManager.runEvent(EventCode.LoginActivityLaunched);
			}
		}
	}
	
	public static boolean isInBackground(Context context){
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		try{
			List<RunningTaskInfo> tasks = am.getRunningTasks(1);  
	        if (!tasks.isEmpty()) {
	        	final RunningTaskInfo ti = tasks.get(0);
	        	if(!ti.baseActivity.getPackageName().equals(context.getPackageName())){
	        		return true;
	        	}
	        }
	        return false;
		}catch(Exception e){
			e.printStackTrace();
		}
		return SystemUtils.isInBackground(context);
	}
}
