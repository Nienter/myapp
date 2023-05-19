package com.xbcx.common;

import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.adapter.SetBaseAdapter.ItemObserver;
import com.xbcx.common.pulltorefresh.PullToRefreshPlugin;

public class CheckNoResultItemObserver implements ItemObserver{

	private PullToRefreshPlugin<?> mPullToRefreshPlugin;
	
	public CheckNoResultItemObserver(PullToRefreshPlugin<?> prp){
		mPullToRefreshPlugin = prp;
	}
	
	@Override
	public void onItemCountChanged(SetBaseAdapter<?> adapter) {
		mPullToRefreshPlugin.checkNoResult();
	}

}
