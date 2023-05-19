package com.xbcx.common;

import com.xbcx.core.BaseActivity;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class ListViewScrollPluginListener implements OnScrollListener{
	
	private BaseActivity	mActivity;
	
	public ListViewScrollPluginListener(BaseActivity activity){
		mActivity = activity;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		for(ListViewScrollPlugin p : mActivity.getPlugins(ListViewScrollPlugin.class)){
			p.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		for(ListViewScrollPlugin p : mActivity.getPlugins(ListViewScrollPlugin.class)){
			p.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

}
