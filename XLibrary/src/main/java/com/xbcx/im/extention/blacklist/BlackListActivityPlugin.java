package com.xbcx.im.extention.blacklist;

import java.util.Collection;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.common.CheckNoResultItemObserver;
import com.xbcx.common.pulltorefresh.PullToRefreshPlugin;
import com.xbcx.common.pulltorefresh.SimpleAdapterEmptyChecker;
import com.xbcx.core.ActivityPlugin;
import com.xbcx.core.Event;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.BaseActivity.ActivityEventHandler;
import com.xbcx.im.ui.ActivityType;

public class BlackListActivityPlugin extends ActivityPlugin<BaseActivity> implements
																ActivityEventHandler,
																OnItemClickListener{

	private SetBaseAdapter<String> 	mSetAdapter;
	
	private boolean					mIsClickLaunchUserDetail = true;
	
	public BlackListActivityPlugin(PullToRefreshPlugin<?> prp,SetBaseAdapter<String> adapter){
		prp.disableRefresh();
		prp.setAdapterEmptyChecker(new SimpleAdapterEmptyChecker(adapter))
		.setOnItemClickListener(this);
		adapter.registerItemObserver(new CheckNoResultItemObserver(prp));
		adapter.replaceAll(BlackListServicePlugin.getInterface().getBlackLists());
		mSetAdapter = adapter;
	}
	
	@Override
	protected void onAttachActivity(BaseActivity activity) {
		super.onAttachActivity(activity);
		activity.registerActivityEventHandlerEx(BlackListServicePlugin.IM_BlackListChanged, this);
	}
	
	public BlackListActivityPlugin setIsClickLaunchUserDetail(boolean bLaunch){
		mIsClickLaunchUserDetail = bLaunch;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onHandleEventEnd(Event event, BaseActivity activity) {
		mSetAdapter.replaceAll(event.findParam(Collection.class));
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Object object = parent.getItemAtPosition(position);
		if(object != null && object instanceof String){
			final String strIMUser = (String)object;
			onItemClick(strIMUser);
		}
	}
	
	protected void onItemClick(String userId){
		if(mIsClickLaunchUserDetail){
			ActivityType.launchChatActivity(mActivity, 
					ActivityType.UserDetailActivity, userId, "");
		}
	}
}
