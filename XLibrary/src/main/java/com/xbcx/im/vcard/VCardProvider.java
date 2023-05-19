package com.xbcx.im.vcard;

import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xbcx.common.valueloader.AdapterViewValueLoader;
import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.core.IDObject;
import com.xbcx.core.PicUrlObject;
import com.xbcx.core.XApplication;
import com.xbcx.core.db.XDB;
import com.xbcx.core.module.HttpLoginListener;
import com.xbcx.core.module.ListValueLoaderlListener;
import com.xbcx.core.module.OnLowMemoryListener;
import com.xbcx.core.module.UserReleaseListener;
import com.xbcx.im.IMKernel;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.im.ui.LocalAvatar;
import com.xbcx.library.R;
import com.xbcx.utils.JsonParseUtils;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class VCardProvider implements OnEventListener,
									ListValueLoaderlListener,
									HttpLoginListener,
									OnLowMemoryListener,
									UserReleaseListener{
	
	public static VCardProvider getInstance(){
		return sInstance;
	}
	
	static{
		sInstance = new VCardProvider();
	}
	
	protected static VCardProvider sInstance;
	
	protected static AndroidEventManager				mEventManager = AndroidEventManager.getInstance();
	
	protected List<WeakReference<AvatarNameManager>>	mAvatarManagers = new ArrayList<WeakReference<AvatarNameManager>>();
	
	protected AvatarNameManager							mDefaultAvatarManager;
	
	protected SparseArray<AutoUpdateInfo>				mMapActivityTypeToAutoUpdateInfo = new SparseArray<VCardProvider.AutoUpdateInfo>();
	
	protected SparseArray<HashMap<String, Object>> 		mMapActivityTypeToResumeLoaderSetValue = new SparseArray<HashMap<String,Object>>();
	
	protected AvatarLoader 		mDefaultAvatarLoader;
	
	protected NameLoader 		mDefaultNameLoader;
	
	protected VCardProvider(){
		XApplication.addManager(this);
		mDefaultAvatarManager = createAvatarManager();
	}
	
	public void setLoadVCardInfo(int eventCode,Class<? extends PicUrlObject> clazz){
		addAutoUpdateInfo(ActivityType.SingleChat, eventCode, clazz);
	}
	
	public void addAutoUpdateInfo(int activityType,int eventCode,Class<?> clazz){
		addAutoUpdateInfo(activityType, new AutoUpdateInfo(eventCode, clazz));
	}
	
	public void addAutoUpdateInfo(int activityType,AutoUpdateInfo ai){
		if(ai.mVCardSaver == null){
			ai.mVCardSaver = new SimpleVCardSaver<>();
		}
		if(ai.mVCardEventProvider == null){
			ai.mVCardEventProvider = new SimpleVCardEventProvider();
		}
		mMapActivityTypeToAutoUpdateInfo.put(activityType, ai);
		mEventManager.addEventListener(ai.mEventCode, this);
	}
	
	@Override
	public void onLowMemory() {
		int length = mMapActivityTypeToAutoUpdateInfo.size();
		for(int index = 0;index < length;++index){
			AutoUpdateInfo ui = mMapActivityTypeToAutoUpdateInfo.valueAt(index);
			ui.mMapIdToVCard.clear();
		}
	}
	
	@Override
	public void onUserRelease(String user) {
		int length = mMapActivityTypeToAutoUpdateInfo.size();
		for(int index = 0;index < length;++index){
			AutoUpdateInfo ui = mMapActivityTypeToAutoUpdateInfo.valueAt(index);
			ui.mMapIdToVCard.clear();
		}
		for(WeakReference<AvatarNameManager> wr : mAvatarManagers){
			final AvatarNameManager am = wr.get();
			if(am != null){
				am.clear();
			}
		}
	}
	
	public void addAvatarNameObserverHandler(AvatarNameObserverHandler handler){
		
	}
	
	public void setAvatar(ImageView iv,String imUser){
		setAvatar(iv, imUser, false);
	}
	
	public void setAvatar(ImageView iv,String imUser,boolean bUpdate){
		mDefaultAvatarManager.setAvatar(iv, imUser, ActivityType.SingleChat);
		if(bUpdate){
			requestVCard(imUser, ActivityType.SingleChat);
		}
	}
	
	public void setName(TextView tv,String imUser){
		setName(tv, imUser, false);
	}
	
	public void setName(TextView tv,String imUser,String defaultName){
		setName(tv, imUser, defaultName,false);
	}
	
	public void setName(TextView tv,String imUser,boolean bUpdate){
		setName(tv, imUser, null, bUpdate);
	}
	
	public void setName(TextView tv,String imUser,String defaultName,boolean bUpdate){
		mDefaultAvatarManager.setName(tv, imUser, defaultName, ActivityType.SingleChat);
		if(bUpdate){
			requestVCard(imUser, ActivityType.SingleChat);
		}
	}
	
	public void setGroupAvatar(ImageView iv,String groupId){
		mDefaultAvatarManager.setAvatar(iv, groupId, ActivityType.GroupChat);
	}
	
	public void setDefaultAvatarLoader(AvatarLoader loader){
		mDefaultAvatarManager.addAvatarLoader(ActivityType.SingleChat, loader);
	}
	public void setDefaultNameLoader(NameLoader loader){
		mDefaultAvatarManager.addNameLoader(ActivityType.SingleChat, loader);
	}
	
	public AvatarNameManager createAvatarManager(){
		AvatarNameManager am = new AvatarNameManager();
		am.addAvatarLoader(ActivityType.SingleChat, new SimpleVCardAvatarLoader());
		am.addNameLoader(ActivityType.SingleChat, new SimpleVCardNameLoader());
		mAvatarManagers.add(new WeakReference<>(am));
		return am;
	}
	
	public String getCacheName(String imUser){
		PicUrlObject vcard = loadVCard(imUser,false);
		if(vcard != null){
			return vcard.getName();
		}
		return "";
	}
	
	public PicUrlObject getCache(String imUser){
		AutoUpdateInfo ui = getAutoUpdateInfo(ActivityType.SingleChat);
		return ui == null ? null : (PicUrlObject)ui.mMapIdToVCard.get(imUser);
	}
	
	public PicUrlObject loadVCard(String userId,boolean bUpdate){
		return (PicUrlObject)loadVCard(userId, ActivityType.SingleChat, bUpdate);
	}
	
	public void saveVCard(final PicUrlObject vcard){
		if(getSingleChatAutoUpdateItemClass().getName().equals(vcard.getClass().getName())){
			XApplication.getMainThreadHandler().post(new Runnable() {
				@Override
				public void run() {
					onLoadVCardSuccess(vcard.getId(), vcard);
				}
			});
		}
	}
	
	public void saveName(final String id,String name,final int activityType){
		if(TextUtils.isEmpty(id)){
			return;
		}
		Object obj = loadVCard(id, activityType, false);
		if(obj != null){
			if(obj instanceof NameProtocol){
				final NameProtocol np = (NameProtocol)obj;
				if(!TextUtils.equals(name, np.getName())){
					np.setName(name);
					XApplication.getMainThreadHandler().post(new Runnable() {
						@Override
						public void run() {
							onLoadVCardSuccess(activityType, id, np);
						}
					});
				}
			}
		}
	}
	
	public Object loadVCard(String id,int activityType,boolean bUpdate){
		if(TextUtils.isEmpty(id)){
			return null;
		}
		AutoUpdateInfo ui = getAutoUpdateInfo(activityType);
		if(ui == null){
			return null;
		}
		boolean update = bUpdate;
		Object item = ui.mMapIdToVCard.get(id);
		if(item == null){
			item = readVCardFromDB(ui, id);
			if(item == null){
				update = true;
			}else{
				ui.mMapIdToVCard.put(id, item);
			}
		}
		
		if(update){
			requestVCard(id, activityType);
		}
		
		return item;
	}
	
	public void requestVCard(String id,int activityType){
		AutoUpdateInfo ui = getAutoUpdateInfo(activityType);
		if(ui != null){
			if(!ui.mVCardEventProvider.isLoading(ui.mEventCode, id)){
				ui.mVCardEventProvider.requestVCard(ui.mEventCode, id);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	protected Object readVCardFromDB(AutoUpdateInfo ai,String id){
		return XDB.getInstance().readById(id, 
				ai.mVCardSaver.getDBObjectClass(ai.mItemClass), 
				false);
	}
	
	protected AutoUpdateInfo getAutoUpdateInfo(int activityType){
		return mMapActivityTypeToAutoUpdateInfo.get(activityType);
	}

	@Override
	public void onEventRunEnd(Event event) {
		if(event.isSuccess()){
			final int code = event.getEventCode();
			int length = mMapActivityTypeToAutoUpdateInfo.size();
			for(int index = 0;index < length;++index){
				AutoUpdateInfo ai = mMapActivityTypeToAutoUpdateInfo.valueAt(index);
				if(ai.mEventCode == code){
					final String id = (String)event.getParamAtIndex(0);
					if(!TextUtils.isEmpty(id)){
						Object item = event.findReturnParam(ai.mItemClass);
						if(item != null){
							final int activityType = mMapActivityTypeToAutoUpdateInfo.keyAt(index);
							onLoadVCardSuccess(activityType, id, item);
						}
					}
					break;
				}
			}
		}
	}
	
	@Override
	public void onPauseLoader() {
	}

	@Override
	public void onResumeLoader() {
		int length = mMapActivityTypeToResumeLoaderSetValue.size();
		for(int index = 0;index < length;++index){
			final int activityType = mMapActivityTypeToResumeLoaderSetValue.keyAt(index);
			final HashMap<String, Object> values = mMapActivityTypeToResumeLoaderSetValue.valueAt(index);
			for(Entry<String, Object> e : values.entrySet()){
				updateName(e.getKey(), e.getValue(), activityType);
			}
		}
		mMapActivityTypeToResumeLoaderSetValue.clear();
	}
	
	protected void onLoadVCardSuccess(String imUser,PicUrlObject vcard){
		onLoadVCardSuccess(ActivityType.SingleChat, imUser, vcard);
	}
	
	protected void onLoadVCardSuccess(int activityType,String imUser,Object vcard){
		vcard = convertVCard(activityType, imUser, vcard);
		cacheAndSaveVCard(imUser,vcard,activityType);
		
		updateName(imUser, vcard,activityType);
		
		updateAvatar(imUser, vcard, activityType);
	}
	
	@SuppressWarnings("unchecked")
	public Object convertVCard(int activityType,String id,Object vcard){
		AutoUpdateInfo ai = getAutoUpdateInfo(activityType);
		if(ai != null){
			if(ai.mItemClass.isAssignableFrom(vcard.getClass())){
				vcard = ai.mVCardSaver.convertDBObject(vcard);
			}
		}
		return vcard;
	}
	
	protected void cacheAndSaveVCard(String id,Object vcard,int activityType){
		AutoUpdateInfo ai = getAutoUpdateInfo(activityType);
		if(ai != null){
			ai.mMapIdToVCard.put(id, vcard);
			if(vcard instanceof IDObject){
				final IDObject dbItem = (IDObject)vcard;
				XApplication.runOnBackground(new Runnable() {
					@Override
					public void run() {
						XDB.getInstance().updateOrInsert(dbItem, false);
					}
				});
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T> void updateName(String id,T name,int activityType){
		if(XApplication.isImageLoaderResume()){
			WeakReference<AvatarNameManager> wr = null;
			AvatarNameManager am = null;
			Iterator<WeakReference<AvatarNameManager>> it = mAvatarManagers.iterator();
			while(it.hasNext()){
				wr = it.next();
				am = wr.get();
				if(am == null){
					it.remove();
				}else{
					NameLoader l = am.getNameLoader(activityType);
					if(l != null){
						if(l.supportVCardChangeHandler()){
							l.onVCardChange(id, name);
						}
					}
				}
			}
		}else{
			HashMap<String, Object> values = mMapActivityTypeToResumeLoaderSetValue.get(activityType);
			if(values == null){
				values = new HashMap<String, Object>();
				mMapActivityTypeToResumeLoaderSetValue.put(activityType, values);
			}
			values.put(id, name);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T> void updateAvatar(String id,T avatar,int activityType){
		WeakReference<AvatarNameManager> wr = null;
		AvatarNameManager am = null;
		Iterator<WeakReference<AvatarNameManager>> it = mAvatarManagers.iterator();
		while(it.hasNext()){
			wr = it.next();
			am = wr.get();
			if(am == null){
				it.remove();
			}else{
				AvatarLoader l = am.getAvatarLoader(activityType);
				if(l != null){
					if(l.supportVCardChangeHandler()){
						l.onVCardChange(id, avatar);
					}
				}
			}
		}
	}
	
	@Override
	public void onHttpLogined(Event event,JSONObject joRet) {
		try{
			PicUrlObject vcard = JsonParseUtils.buildObject(getSingleChatAutoUpdateItemClass(), joRet);
			saveVCard(vcard);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Class<? extends PicUrlObject> getSingleChatAutoUpdateItemClass(){
		AutoUpdateInfo ui = getAutoUpdateInfo(ActivityType.SingleChat);
		return ui == null ? PicUrlObject.class : (Class<? extends PicUrlObject>)ui.mItemClass;
	}
	
	public static interface VCardSaver<T>{
		public IDObject convertDBObject(T item);
		
		public Class<?> getDBObjectClass(Class<T> cls);
	}
	
	public static interface VCardEventProvider{
		public boolean	isLoading(int eventCode,String id);
		public void		requestVCard(int eventCode,String id);
	}
	
	public static class SimpleVCardSaver<T> implements VCardSaver<T>{
		
		@Override
		public IDObject convertDBObject(T item) {
			return (IDObject)item;
		}
		@Override
		public Class<?> getDBObjectClass(Class<T> cls) {
			return cls;
		}
	}
	
	public static class SimpleVCardEventProvider implements VCardEventProvider{
		@Override
		public void requestVCard(int eventCode, String id) {
			mEventManager.pushEvent(eventCode, id);
		}

		@Override
		public boolean isLoading(int eventCode, String id) {
			return mEventManager.isEventRunning(eventCode, id);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static class AutoUpdateInfo{
		int 				mEventCode;
		Class<?> 			mItemClass;
		Map<String, Object> mMapIdToVCard = new ConcurrentHashMap<String, Object>();
		
		VCardSaver			mVCardSaver;
		VCardEventProvider	mVCardEventProvider;
		
		public AutoUpdateInfo(int eventCode,Class<?> cls){
			mEventCode = eventCode;
			mItemClass = cls;
		}
		
		public AutoUpdateInfo setSaver(VCardSaver saver){
			mVCardSaver = saver;
			return this;
		}
		
		public AutoUpdateInfo setEventProvider(VCardEventProvider provider){
			mVCardEventProvider = provider;
			return this;
		}
	}
	
	public abstract static class BaseValueLoader<Holder extends View,Result> extends AdapterViewValueLoader<Holder, String, Result>{
		private int									mActivityType;
		
		private List<VCardChangeHandler<Result>>	mVCardChangeHandlers;
		
		public void setActivityType(int activityType){
			mActivityType = activityType;
		}
		
		public int getActivityType(){
			return mActivityType;
		}
		
		public void addVCardChangeHandler(VCardChangeHandler<Result> handler){
			if(handler == null){
				return;
			}
			if(mVCardChangeHandlers == null){
				mVCardChangeHandlers = new LinkedList<VCardChangeHandler<Result>>();
			}
			mVCardChangeHandlers.add(handler);
		}
		
		public boolean removeVCardChangeHandler(VCardChangeHandler<Result> handler){
			if(mVCardChangeHandlers != null){
				return mVCardChangeHandlers.remove(handler);
			}
			return false;
		}
		
		public boolean supportVCardChangeHandler(){
			return mVCardChangeHandlers != null && mVCardChangeHandlers.size() > 0;
		}
		
		public void onVCardChange(String item,Result result){
			if(mVCardChangeHandlers != null){
				for(VCardChangeHandler<Result> h : mVCardChangeHandlers){
					h.onVCardChanged(item, result);
				}
			}
		}
		
		public boolean isFromVCardChange(){
			return false;
		}
	}
	
	public static interface VCardChangeHandler<Result>{
		public void onVCardChanged(String item,Result result);
	}
	
	public abstract static class NameLoader<Result> extends BaseValueLoader<TextView, Result>{
	}
	
	public static abstract class AvatarLoader<Result> extends BaseValueLoader<ImageView, Result>{
		
		@Override
		public void onUpdateEmpty(ImageView holder, String item) {
			holder.setImageResource(R.drawable.avatar_user);
		}
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static class AvatarNameManager{
		SparseArray<AvatarLoader> 	mapActivityTypeToLoader = new SparseArray<AvatarLoader>();
		
		SparseArray<NameLoader> 	mapActivityTypeToNameLoader = new SparseArray<NameLoader>();
		
		protected AvatarNameManager(){
		}
		
		public void clear(){
			int length = mapActivityTypeToNameLoader.size();
			for(int index = 0;index < length;++index){
				NameLoader loader = mapActivityTypeToNameLoader.valueAt(index);
				loader.clearCache();
			}
			length = mapActivityTypeToLoader.size();
			for(int index = 0;index < length;++index){
				AvatarLoader loader = mapActivityTypeToLoader.valueAt(index);
				loader.clearCache();
			}
		}
		
		public void addAvatarLoader(int activityType,AvatarLoader loader){
			loader.setActivityType(activityType);
			mapActivityTypeToLoader.put(activityType, loader);
		}
		
		public AvatarLoader getAvatarLoader(int activityType){
			return mapActivityTypeToLoader.get(activityType);
		}
		
		public void addNameLoader(int activityType,NameLoader loader){
			loader.setActivityType(activityType);
			mapActivityTypeToNameLoader.put(activityType, loader);
		}
		
		public NameLoader getNameLoader(int activityType){
			return mapActivityTypeToNameLoader.get(activityType);
		}
		
		public void setName(TextView tv,String id,String defaultName,int activityType){
			tv.setText(defaultName);
			if(TextUtils.isEmpty(id)){
				int length = mapActivityTypeToNameLoader.size();
				for(int index = 0;index < length;++index){
					NameLoader loader = mapActivityTypeToNameLoader.valueAt(index);
					loader.removeBindView(tv);
				}
			}else{
				int length = mapActivityTypeToNameLoader.size();
				for(int index = 0;index < length;++index){
					NameLoader loader = mapActivityTypeToNameLoader.valueAt(index);
					if(mapActivityTypeToNameLoader.keyAt(index) == activityType){
						loader.bindView(tv,id);
					}else{
						loader.removeBindView(tv);
					}
				}
			}
		}
		
		public void setAvatar(ImageView iv,String id,int activityType){
			if(TextUtils.isEmpty(id)){
				int length = mapActivityTypeToLoader.size();
				for(int index = 0;index < length;++index){
					AvatarLoader loader = mapActivityTypeToLoader.valueAt(index);
					loader.removeBindView(iv);
				}
				XApplication.getImageLoader().cancelDisplayTask(iv);
			}else{
				int length = mapActivityTypeToLoader.size();
				boolean bHandle = false;
				for(int index = 0;index < length;++index){
					AvatarLoader loader = mapActivityTypeToLoader.valueAt(index);
					if(mapActivityTypeToLoader.keyAt(index) == activityType){
						loader.bindView(iv, id);
						bHandle = true;
					}else{
						loader.removeBindView(iv);
					}
				}
				if(!bHandle){
					int resId = LocalAvatar.getAvatarResId(activityType);
					if(resId == 0){
						resId = LocalAvatar.getAvatarResId(id);
					}
					if(resId != 0){
						try{
							iv.setImageResource(resId);
						}catch(Exception e){
							e.printStackTrace();
							iv.setImageDrawable(null);
						}
					}
				}
			}
		}
	}
	
	public static abstract class AvatarNameObserverHandler{
		
		protected final int mActivityType;
		
		public AvatarNameObserverHandler(int activityType){
			mActivityType = activityType;
		}
	}
	
	public abstract static class ModifyAvatarNameHandler extends AvatarNameObserverHandler implements 
																OnEventListener{

		protected int	mModifyEventCode;
		
		public ModifyAvatarNameHandler(int activityType,int modifyEventCode) {
			super(activityType);
			mModifyEventCode = modifyEventCode;
			mEventManager.addEventListener(modifyEventCode, this);
		}
		
		@Override
		public void onEventRunEnd(Event event) {
			if(event.getEventCode() == mModifyEventCode){
				if(event.isSuccess()){
					AutoUpdateInfo ai = getInstance().getAutoUpdateInfo(mActivityType);
					if(ai != null){
						final String id = event.findParam(String.class);
						final Object item = event.findReturnParam(ai.mItemClass);
						getInstance().onLoadVCardSuccess(mActivityType, id, item);
					}
				}
			}
		}
	}
	
	public static class ModifySelfAvatarHandler extends ModifyAvatarNameHandler{
		
		public ModifySelfAvatarHandler(int modifyEventCode) {
			super(ActivityType.SingleChat,modifyEventCode);
		}

		@Override
		public void onEventRunEnd(Event event) {
			if(event.getEventCode() == mModifyEventCode){
				if(event.isSuccess()){
					String url = event.findParam(String.class);
					PicUrlObject vcard = getInstance().loadVCard(IMKernel.getLocalUser(), false);
					if(vcard != null){
						vcard.setPicUrl(url);
						final String uid = IMKernel.getLocalUser();
						getInstance().onLoadVCardSuccess(mActivityType, uid, vcard);
					}
				}
			}
		}
	}
	
	public static interface NameProtocol{
		public void setName(String name);
		
		public String getName();
	}
}
