package com.xbcx.im.recentchat;

import java.util.Collection;
import java.util.List;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.xbcx.common.CheckNoResultItemObserver;
import com.xbcx.common.pulltorefresh.PullToRefreshActivity;
import com.xbcx.common.pulltorefresh.SimpleAdapterEmptyChecker;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.XApplication;
import com.xbcx.im.XMessage;
import com.xbcx.im.recentchat.RecentChatAdapter.ActivityTypeAdapterViewValueLoader;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.im.ui.TitleEventHandler;
import com.xbcx.im.ui.simpleimpl.OnChildViewClickListener;
import com.xbcx.library.R;

public class RecentChatActivity extends PullToRefreshActivity implements 
												OnItemClickListener,
												OnItemLongClickListener,
												OnChildViewClickListener,
												Runnable{
	protected static final int 	MENUID_DELETE_RECORD = 1;
	
	protected RecentChatAdapter	mAdapter;
	
	private long				mLastUpdateTime;
	private boolean				mNeedUpdate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setIsHideViewFirstLoad(false);
		super.onCreate(savedInstanceState);
		
		new TitleEventHandler(this);
		
		mPullToRefreshPlugin.setOnItemLongClickListener(this);
		mPullToRefreshPlugin.setAdapterEmptyChecker(new SimpleAdapterEmptyChecker(mAdapter));
		mAdapter.registerItemObserver(new CheckNoResultItemObserver(mPullToRefreshPlugin));
		registerForContextMenu(getListView());
		
		disableRefresh();
		
		setNoResultTextId(R.string.no_result_recentchat);
		
		addAndManageEventListener(EventCode.RecentChatChanged);
		addAndManageEventListener(EventCode.UnreadMessageCountChanged);
		
		postDelayed(new Runnable() {
			@Override
			public void run() {
				RecentChatManager.getInstance().asyncLoadDataNotify();
			}
		}, 200);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		removeCallbacks(this);
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mActivityLayoutId = R.layout.activity_simple_pulltorefresh;
	}

	@Override
	public ListAdapter onCreateAdapter() {
		mAdapter = onCreateRecentChatAdapter();
		mAdapter.setOnChildViewClickListener(this);
		return mAdapter;
	}
	
	protected RecentChatAdapter	onCreateRecentChatAdapter(){
		return new RecentChatAdapter(this);
	}
	
	public RecentChatAdapter getRecentChatAdapter(){
		return mAdapter;
	}
	
	public void useReadMessageValueLoader(){
		mAdapter.registerAdapterViewValueLoader(ActivityType.SingleChat, 
				new RecentChatReadMessageValueLoader(XMessage.FROMTYPE_SINGLE));
		mAdapter.registerAdapterViewValueLoader(ActivityType.GroupChat, 
				new RecentChatReadMessageValueLoader(XMessage.FROMTYPE_GROUP));
		mAdapter.registerAdapterViewValueLoader(ActivityType.DiscussionChat, 
				new RecentChatReadMessageValueLoader(XMessage.FROMTYPE_DISCUSSION));
		addAndManageEventListener(EventCode.DB_DeleteMessage);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == EventCode.RecentChatChanged){
			final long time = System.currentTimeMillis();
			if(time - mLastUpdateTime > 500){
				final List<RecentChat> rcs = (List<RecentChat>)event.getParamAtIndex(0);
				mAdapter.replaceAll(onFilterRecentChats(rcs));
			}else{
				if(!mNeedUpdate){
					mNeedUpdate = true;
					postDelayed(this, 500);
				}
			}
			mLastUpdateTime = time;
		}else if(code == EventCode.UnreadMessageCountChanged){
			mAdapter.notifyDataSetChanged();
		}else if(code == EventCode.DB_DeleteMessage){
			ActivityTypeAdapterViewValueLoader<?> loader = mAdapter.getAdapterViewValueLoader(ActivityType.SingleChat);
			if(loader != null){
				loader.clearCache();
			}
			loader = mAdapter.getAdapterViewValueLoader(ActivityType.GroupChat);
			if(loader != null){
				loader.clearCache();
			}
			loader = mAdapter.getAdapterViewValueLoader(ActivityType.DiscussionChat);
			if(loader != null){
				loader.clearCache();
			}
			mAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void run() {
		final List<RecentChat> rcs = RecentChatManager.getInstance().getAllRecentChat();
		mAdapter.replaceAll(onFilterRecentChats(rcs));
		mNeedUpdate = false;
	}
	
	protected Collection<RecentChat> onFilterRecentChats(Collection<RecentChat> rcs){
		return rcs;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Object object = parent.getItemAtPosition(position);
		if(object != null && object instanceof RecentChat){
			RecentChat recentChat = (RecentChat)object;
			setTag(recentChat);
		}
		return false;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, Object item,View view) {
		super.onItemClick(parent, item,view);
		if(item instanceof RecentChat){
			onItemClick((RecentChat)item);
		}
	}
	
	protected void onItemClick(RecentChat rc){
		for(RecentChatLaunchPlugin p : XApplication.getManagers(RecentChatLaunchPlugin.class)){
			if(p.onLaunchRecentChat(this,rc)){
				return;
			}
		}
		ActivityType.launchChatActivity(this, rc.getActivityType(), rc.getId(), rc.getName());
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Object object = getTag();
		if(object != null && object instanceof RecentChat){
			RecentChat recentChat = (RecentChat)object;
			menu.setHeaderTitle(recentChat.getName());
			
			menu.add(0, MENUID_DELETE_RECORD, 0, R.string.delete_record);
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if(item.getItemId() == MENUID_DELETE_RECORD){
			Object tag = getTag();
			if(tag != null && tag instanceof RecentChat){
				final RecentChat recentChat = (RecentChat)tag;
				RecentChatManager.getInstance().deleteRecentChat(recentChat.getId());
				pushEvent(EventCode.DB_DeleteMessage, recentChat.getId());
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onChildViewClicked(BaseAdapter adapter, Object item, int viewId, View v) {
		if(viewId == R.id.ivAvatar){
			if(item != null && item instanceof RecentChat){
				onAvatarClicked((RecentChat)item);
			}
		}
	}
	
	protected void onAvatarClicked(RecentChat rc){
		onItemClick(rc);
	}
}
