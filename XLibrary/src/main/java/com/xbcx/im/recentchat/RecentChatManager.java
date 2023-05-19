package com.xbcx.im.recentchat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.EventManager.OnEventRunner;
import com.xbcx.core.IDObject;
import com.xbcx.core.XApplication;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.core.db.XDB;
import com.xbcx.core.module.AppBaseListener;
import com.xbcx.core.module.OnLowMemoryListener;
import com.xbcx.core.module.UserReleaseListener;
import com.xbcx.im.db.DBColumns;
import com.xbcx.im.extention.roster.RosterServicePlugin;
import com.xbcx.im.recentchat.RecentChatProvider;

public class RecentChatManager implements OnEventListener,
										OnLowMemoryListener,
										UserReleaseListener{

	public static RecentChatManager getInstance(){
		return sInstance;
	}
	
	static{
		sInstance = new RecentChatManager();
	}
	
	private static RecentChatManager sInstance;
	
	private AndroidEventManager		mEventManager = AndroidEventManager.getInstance();
	
	private Map<String, RecentChatProvider> mMapClassNameToRecentChatProvider 	= new HashMap<String, RecentChatProvider>();
	private Map<String, String> 			mMapConstantIds;
	
	private List<RecentChat> 		mListRecentChat 	= Collections.synchronizedList(new LinkedList<RecentChat>());
	private Map<String, RecentChat> mMapIdToRecentChat 	= new ConcurrentHashMap<String, RecentChat>();
	
	private Map<String, RecentChat> mMapIdToHasUnreadRecentChat = new ConcurrentHashMap<String, RecentChat>();
	private int						mUnreadMessageTotalCount = 0;
	
	private List<String> 			mTopIds 			= Collections.synchronizedList(new LinkedList<String>());
	
	private	HashMap<String, DefaultRecentChatCreator> mMapDefaultLoadIdToCreator;
	
	private boolean					mDataInited;
	
	private Object					mHandlerSync = new Object();
	private InternalHandler 		mHandler;
	
	private RecentChatManager(){
		XApplication.addManager(this);
		
		mEventManager.registerEventRunner(EventCode.HandleRecentChat, 
				new HandleRecentChatRunner());
		mEventManager.addEventListener(RosterServicePlugin.IM_DeleteFriend, this);
		mEventManager.addEventListener(RosterServicePlugin.IM_DeleteGroupChat, this);
		mEventManager.addEventListener(RosterServicePlugin.IM_QuitGroupChat, this);
	}
	
	@Override
	public void onUserRelease(String user) {
		synchronized (mHandlerSync) {
			if(mHandler != null){
				mHandler.getLooper().quit();
				mHandler = null;
			}
		}
		releaseData();
	}
	
	@Override
	public void onLowMemory() {
	}
	
	protected synchronized void initData(){
		if(!mDataInited){
			mDataInited = true;
			loadFromDB();
		}
	}
	
	protected synchronized void loadFromDB(){
		mEventManager.runEvent(EventCode.DB_ReadRecentChat,
				mListRecentChat);
		for(RecentChat recentChat : mListRecentChat){
			mMapIdToRecentChat.put(recentChat.getId(), recentChat);
			if(recentChat.getUnreadMessageCount() > 0){
				mMapIdToHasUnreadRecentChat.put(recentChat.getId(), recentChat);
				mUnreadMessageTotalCount += recentChat.getUnreadMessageCount();
			}
		}
		
		List<TopId> topids = XDB.getInstance().readAll(TopId.class, true);
		for(TopId id : topids){
			addTopIdInternal(id.getId());
		}
		
		if(mMapDefaultLoadIdToCreator != null){
			for(Entry<String, DefaultRecentChatCreator> e : mMapDefaultLoadIdToCreator.entrySet()){
				final String id = e.getKey();
				if(!mMapIdToRecentChat.containsKey(id)){
					IDObject ido = XDB.getInstance().readById("default_load_ids", id, true);
					if(ido == null){
						RecentChat rc = e.getValue().createDefaultRecentChat(id);
						if(rc != null){
							long time = rc.getTime();
							if(time <= 0){
								time = XApplication.getFixSystemTime();
								rc.setTime(time);
							}
							mEventManager.runEvent(EventCode.DB_SaveRecentChat, rc);
							
							mMapIdToRecentChat.put(rc.getId(), rc);
							int pos = calculateInsertPos(id, rc.getTime());
							mListRecentChat.add(pos, rc);
							if(rc.getUnreadMessageCount() > 0){
								mMapIdToHasUnreadRecentChat.put(rc.getId(), rc);
								mUnreadMessageTotalCount += rc.getUnreadMessageCount();
							}
						}
						XDB.getInstance().updateOrInsert("default_load_ids", new IDObject(id));
					}
				}
			}
		}
	}
	
	protected synchronized void releaseData(){
		mListRecentChat.clear();
		mMapIdToHasUnreadRecentChat.clear();
		mMapIdToRecentChat.clear();
		mUnreadMessageTotalCount = 0;
		mTopIds.clear();
		mDataInited = false;
	}
	
	public void registerRecentChatProvider(Class<?> clazz,RecentChatProvider provider){
		mMapClassNameToRecentChatProvider.put(clazz.getName(), provider);
	}
	
	public RecentChatProvider getRecentChatProvider(Class<?> clazz){
		return mMapClassNameToRecentChatProvider.get(clazz.getName());
	}
	
	public void registerConstantId(String id){
		if(mMapConstantIds == null){
			mMapConstantIds = new HashMap<String, String>();
		}
		mMapConstantIds.put(id, id);
	}
	
	public boolean isConstantId(String id){
		if(mMapConstantIds == null){
			return false;
		}
		return mMapConstantIds.containsKey(id);
	}
	
	public synchronized void registerDefaultLoadRecentChat(String id,DefaultRecentChatCreator creator){
		if(mMapDefaultLoadIdToCreator == null){
			mMapDefaultLoadIdToCreator = new HashMap<String, DefaultRecentChatCreator>();
		}
		mMapDefaultLoadIdToCreator.put(id, creator);
	}
	
	public synchronized void editRecentChat(String id,RecentChatEditCallback callback){
		final RecentChat rc = getRecentChat(id);
		if(rc != null){
			final int oldUnreadCount = rc.getUnreadMessageCount();
			if(callback.onEditRecentChat(rc)){
				if(oldUnreadCount != rc.getUnreadMessageCount()){
					mUnreadMessageTotalCount += rc.getUnreadMessageCount() - oldUnreadCount;
					if(rc.getUnreadMessageCount() > 0){
						mMapIdToHasUnreadRecentChat.put(rc.getId(), rc);
					}
					notifyUnreadMessageCountChanged(rc);
				}
				mEventManager.runEvent(EventCode.DB_SaveRecentChat, rc);
				
				notifyRecentChatChanged();
				
				notifyRecentChatObserverUpdate(rc);
			}
		}
	}
	
	public void checkAndModifyName(String id,final String name){
		final RecentChat rc = getRecentChat(id);
		if(rc != null){
			if(!TextUtils.equals(rc.getName(), name)){
				editRecentChat(id, new RecentChatEditCallback() {
					@Override
					public boolean onEditRecentChat(RecentChat rc) {
						rc.setName(name);
						return true;
					}
				});
			}
		}
	}
	
	public void addTopId(String id){
		addTopIdInternal(id);
		if(!TextUtils.isEmpty(id)){
			final TopId topId = new TopId(id);
			XDB.getInstance().delete(topId.getId(),TopId.class,true);
			XDB.getInstance().updateOrInsert(topId, true);
			Collection<TopIdObserver> os = XApplication.getManagers(TopIdObserver.class);
			if(os != null && !os.isEmpty()){
				final RecentChat rc = getRecentChat(id);
				if(rc != null){
					for(TopIdObserver o : os){
						o.onAddTopId(id,rc);
					}
				}
			}
		}
	}
	
	protected synchronized void addTopIdInternal(String id){
		if(!TextUtils.isEmpty(id)){
			mTopIds.remove(id);
			mTopIds.add(0, id);
			RecentChat rc = getRecentChat(id);
			if(rc != null){
				mListRecentChat.remove(rc);
				mListRecentChat.add(0, rc);
				notifyRecentChatChanged();
			}
		}
	}
	
	public synchronized void removeTopId(String id){
		if(!TextUtils.isEmpty(id)){
			mTopIds.remove(id);
			XDB.getInstance().delete(id, TopId.class,true);
			RecentChat rc = getRecentChat(id);
			if(rc != null){
				mListRecentChat.remove(rc);
				int pos = calculateInsertPos(id, rc.getTime());
				mListRecentChat.add(pos, rc);
				notifyRecentChatChanged();
				
				for(TopIdObserver o : XApplication.getManagers(TopIdObserver.class)){
					o.onRemoveTopId(id,rc);
				}
			}
		}
	}
	
	public void removeAllTopId(boolean bReOrder){
		if(bReOrder){
			final List<String> topIds = new ArrayList<String>(mTopIds);
			for(String id : topIds){
				removeTopId(id);
			}
		}else{
			mTopIds.clear();
			XDB.getInstance().deleteAll(TopId.class, true);
		}
	}
	
	public long	getTopIdTime(String id){
		if(!TextUtils.isEmpty(id)){
			if(mTopIds.contains(id)){
				TopId topId = XDB.getInstance().readById(id, TopId.class, true);
				return topId == null ? 0 : topId.time;
			}
		}
		return 0;
	}
	
	public boolean isTopId(String id){
		return mTopIds.contains(id);
	}
	
	public synchronized void replaceAll(List<RecentChat> rcs){
		mEventManager.runEvent(EventCode.DB_ReplaceAllRecentChat, rcs,mMapIdToHasUnreadRecentChat);
		releaseData();
		loadDataNotify();
	}
	
	protected synchronized void onHandleObject(Object obj){
		if(obj != null){
			RecentChatProvider provider = mMapClassNameToRecentChatProvider.get(
					obj.getClass().getName());
			if(provider != null){
				initData();
				synchronized (provider) {
					final String id = provider.getId(obj);
					if(!TextUtils.isEmpty(id)){
						long time = provider.getTime(obj);
						RecentChat rc = getRecentChat(id);
						if(rc == null){
							rc = new RecentChat(id);
							int index = calculateInsertPos(id,time);
							mListRecentChat.add(index, rc);
							mMapIdToRecentChat.put(id, rc);
						}else{
							mListRecentChat.remove(rc);
							int index = calculateInsertPos(id,time);
							mListRecentChat.add(index, rc);
						}
						rc.setTime(time);
						
						onHandleRecentChat(rc, obj, provider);
					}
				}
			}
		}
	}
	
	protected int calculateInsertPos(String id,long time){
		int index = 0;
		final int topIndex = mTopIds.indexOf(id);
		if(topIndex >= 0){
			for(RecentChat temp : mListRecentChat){
				final int tempTopIndex = mTopIds.indexOf(temp.getId());
				if(tempTopIndex < 0 || topIndex < tempTopIndex){
					break;
				}
				++index;
			}
		}else{
			for(RecentChat temp : mListRecentChat){
				if(mTopIds.contains(temp.getId()) || 
						time < temp.getTime()){
					++index;
				}else{
					break;
				}
			}
		}
		return index;
	}
	
	protected void onHandleRecentChat(RecentChat rc,Object obj,RecentChatProvider provider){
		final int oldUnreadCount = rc.getUnreadMessageCount();

		provider.handleRecentChat(rc, obj);
		
		if(provider.isUnread(obj)){
			rc.addUnreadMessageCount();
			
			mMapIdToHasUnreadRecentChat.put(rc.getId(), rc);
			mUnreadMessageTotalCount += (rc.getUnreadMessageCount() - oldUnreadCount);
		
			notifyUnreadMessageCountChanged(rc);
		}
		
		mEventManager.runEvent(EventCode.DB_SaveRecentChat,rc);
		
		notifyRecentChatChanged();
		
		notifyRecentChatObserverUpdate(rc);
	}
	
	protected void notifyRecentChatObserverUpdate(RecentChat rc){
		for(RecentChatObserver o : XApplication.getManagers(RecentChatObserver.class)){
			o.onUpdateRecentChat(rc);
		}
	}
	
	@Override
	public void onEventRunEnd(Event event) {
		final int code = event.getEventCode();
		if(code == RosterServicePlugin.IM_DeleteFriend){
			if(event.isSuccess()){
				final String id = (String)event.getParamAtIndex(0);
				mEventManager.pushEvent(EventCode.DB_DeleteMessage, id);
				deleteRecentChat(id);
			}
		}else if(code == RosterServicePlugin.IM_DeleteGroupChat ||
				code == RosterServicePlugin.IM_QuitGroupChat){
			if(event.isSuccess()){
				final String id = (String)event.getParamAtIndex(0);
				mEventManager.pushEvent(EventCode.DB_DeleteMessage, id);
				deleteRecentChat(id);
			}
		}
	}
	
	public void clearRecentChat(){
		mEventManager.runEvent(EventCode.DB_DeleteRecentChat);
		releaseData();
		notifyRecentChatChanged();
		notifyUnreadMessageCountChanged(null);
	}
	
	public void clearRecentChatExclude(List<String> ids){
		if(ids != null && ids.size() > 0){
			StringBuffer where = new StringBuffer();
			for(String id : ids){
				if(where.length() > 0){
					where.append(" and ");
				}
				where.append(DBColumns.RecentChatDB.COLUMN_ID + "<>'" + id + "'");
			}
			XDB.getInstance().deleteWhere(DBColumns.RecentChatDB.TABLENAME, 
					where.toString(),
					true);
			releaseData();
			loadDataNotify();
		}
	}
	
	public void deleteRecentChat(String strId){
		if(!TextUtils.isEmpty(strId)){
			mEventManager.runEvent(EventCode.DB_DeleteRecentChat,strId);
		}
		
		RecentChat recentChatRemove = mMapIdToRecentChat.remove(strId);
		if(recentChatRemove != null){
			mListRecentChat.remove(recentChatRemove);
			if(recentChatRemove.getUnreadMessageCount() > 0){
				mMapIdToHasUnreadRecentChat.remove(strId);
				mUnreadMessageTotalCount -= recentChatRemove.getUnreadMessageCount();
			}
			notifyRecentChatChanged();
			notifyUnreadMessageCountChanged(null);
			
			for(RecentChatObserver o : XApplication.getManagers(RecentChatObserver.class)){
				o.onDeleteRecentChat(recentChatRemove);
			}
		}
	}
	
	public void notifyUnreadMessageCountChanged(RecentChat rc){
		mEventManager.runEvent(EventCode.UnreadMessageCountChanged,rc);
	}
	
	public void notifyRecentChatChanged(){
		mEventManager.runEvent(EventCode.RecentChatChanged,
				Collections.unmodifiableList(mListRecentChat));
	}
	
	public void asyncLoadDataNotify(){
		XApplication.runOnBackground(new Runnable() {
			@Override
			public void run() {
				loadDataNotify();
			}
		});
	}
	
	public void loadDataNotify(){
		initData();
		notifyRecentChatChanged();
		notifyUnreadMessageCountChanged(null);
	}
	
	public List<RecentChat> getAllRecentChat(){
		initData();
		return Collections.unmodifiableList(mListRecentChat);
	}
	
	public Collection<RecentChat> getAllHasUnreadRecentChat(){
		initData();
		return Collections.unmodifiableCollection(mMapIdToHasUnreadRecentChat.values());
	}
	
	public synchronized Collection<RecentChat> getHasUnreadRecentChat(int activityType){
		initData();
		Collection<RecentChat> rcs = new ArrayList<RecentChat>();
		for(RecentChat rc : mMapIdToHasUnreadRecentChat.values()){
			if(rc.getActivityType() == activityType){
				rcs.add(rc);
			}
		}
		return rcs;
	}
	
	public int	getUnreadMessageTotalCount(){
		initData();
		return mUnreadMessageTotalCount;
	}
	
	public synchronized int  getUnreadMessageCount(int activityType){
		initData();
		int count = 0;
		for(RecentChat rc : mMapIdToHasUnreadRecentChat.values()){
			if(rc.getActivityType() == activityType){
				count += rc.getUnreadMessageCount();
			}
		}
		return count;
	}
	
	public synchronized int  getUnreadMessageCount(String strId){
		initData();
		if(!TextUtils.isEmpty(strId)){
			RecentChat recentChat = mMapIdToRecentChat.get(strId);
			if(recentChat != null){
				return recentChat.getUnreadMessageCount();
			}
		}
		return 0;
	}
	
	public synchronized void clearUnreadMessageCount(String id){
		mEventManager.pushEvent(EventCode.DB_ClearRecentChatUnread, id);
		if(TextUtils.isEmpty(id)){
			for(RecentChat rc : mMapIdToHasUnreadRecentChat.values()){
				rc.setUnreadMessageCount(0);
			}
			mUnreadMessageTotalCount = 0;
			mMapIdToHasUnreadRecentChat.clear();
			notifyUnreadMessageCountChanged(null);
		}else{
			RecentChat rc = getRecentChat(id);
			if(rc != null && rc.getUnreadMessageCount() > 0){
				mUnreadMessageTotalCount -= rc.getUnreadMessageCount();
				rc.setUnreadMessageCount(0);
				mMapIdToHasUnreadRecentChat.remove(rc.getId());
			
				notifyUnreadMessageCountChanged(rc);
			}
		}
	}
	
	public RecentChat getRecentChat(String strId){
		return mMapIdToRecentChat.get(strId);
	}
	
	public static class TopId extends IDObject{
		private static final long serialVersionUID = 1L;
		
		long	time;

		public TopId(String id) {
			super(id);
			time = XApplication.getFixSystemTime();
		}
		
		public TopId(String id,long time){
			super(id);
			this.time = time;
		}
		
		public long	getTime(){
			return time;
		}
	}
	
	private class HandleRecentChatRunner implements OnEventRunner{
		@Override
		public void onEventRun(Event event) throws Exception {
			Object obj = event.getParamAtIndex(0);
			synchronized (mHandlerSync) {
				if(mHandler == null){
					HandlerThread handleThread = new HandlerThread("processRecentChat");
					handleThread.start();
					mHandler = new InternalHandler(handleThread.getLooper());
				}
			}
			mHandler.sendMessage(mHandler.obtainMessage(1, obj));
		}
	}
	
	private static class InternalHandler extends Handler{
		public InternalHandler(Looper looper){
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			getInstance().onHandleObject(msg.obj);
		}
	}
	
	public static interface TopIdObserver extends AppBaseListener{
		public void onAddTopId(String id,RecentChat rc);
		
		public void onRemoveTopId(String id,RecentChat rc);
	}
	
	public static interface RecentChatObserver extends AppBaseListener{
		public void onUpdateRecentChat(RecentChat rc);
		
		public void onDeleteRecentChat(RecentChat rc);
	}
}
