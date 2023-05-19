package com.xbcx.common;

import android.app.Activity;
import android.text.TextUtils;

import com.xbcx.common.pulltorefresh.PullToRefreshActivity;
import com.xbcx.common.pulltorefresh.PullToRefreshPlugin.PullToRefreshListener;
import com.xbcx.core.ActivityPlugin;
import com.xbcx.core.Event;
import com.xbcx.core.PicUrlObject;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.BaseActivity.ActivityEventHandler;
import com.xbcx.im.IMKernel;
import com.xbcx.im.vcard.VCardProvider;

@SuppressWarnings("rawtypes")
public class UserInfoActivityPlugin extends ActivityPlugin<PullToRefreshActivity> implements
												ActivityEventHandler,
												PullToRefreshListener{

	private String				mId;
	private int					mLoadEventCode;
	private UpdateListener 		mUpdateListener;
	
	public UserInfoActivityPlugin(String id,int loadEventCode){
		mId = id;
		mLoadEventCode = loadEventCode;
	}
	
	public static String getUserId(Activity activity){
		String id = activity.getIntent().getStringExtra("id");
		if(TextUtils.isEmpty(id)){
			id = IMKernel.getLocalUser();
		}
		return id;
	}
	
	@Override
	protected void onAttachActivity(PullToRefreshActivity activity) {
		super.onAttachActivity(activity);
		
		activity.getPullToRefreshPlugin().setPullToRefreshListener(this);
		PicUrlObject ud = VCardProvider.getInstance().loadVCard(mId, true);
		if(ud == null){
			activity.setIsHideViewFirstLoad(true);
		}else{
			activity.setIsCreateRefresh(false);
		}
		
		updateUI(ud);
		
		activity.registerActivityEventHandlerEx(mLoadEventCode, this);
	}
	

	@Override
	public void onPullDownToRefresh() {
		mActivity.pushEventRefresh(mLoadEventCode, mId);
	}

	@Override
	public void onPullUpToRefresh() {
	}
	
	public UserInfoActivityPlugin setUpdateListener(UpdateListener listener){
		mUpdateListener = listener;
		return this;
	}
	
	@Override
	public void onHandleEventEnd(Event event, BaseActivity activity) {
		if(event.getEventCode() == mLoadEventCode){
			if(event.isSuccess()){
				PicUrlObject item = event.findReturnParam(PicUrlObject.class);
				if(mId.equals(item)){
					updateUI(item);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void updateUI(PicUrlObject item){
		if(item != null){
			if(mUpdateListener != null){
				mUpdateListener.onUpdateUI(item);
			}
		}
	}
	
	public static interface UpdateListener<T>{
		public void onUpdateUI(T item);
	}
}
