package com.xbcx.common;

import android.widget.AbsListView;

import com.xbcx.core.ActivityBasePlugin;

public interface ListViewScrollPlugin extends ActivityBasePlugin{
	
	public void onScrollStateChanged(AbsListView view, int scrollState);
	
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount);
}
