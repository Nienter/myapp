package com.xbcx.adapter;

import android.view.View;
import android.view.ViewGroup;

public interface StickyHeader {
	public boolean	isItemViewTypeSticky(int viewType);
	
	public View		getStickyHeaderView(View convertView,int viewType,int index,ViewGroup parent);
}
