package com.xbcx.common.pulltorefresh;

import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import com.xbcx.adapter.AnimatableAdapter;
import com.xbcx.core.XEndlessAdapter;

public interface AdapterCreator{
	public ListAdapter 		onCreateAdapter();
	
	public XEndlessAdapter 	onCreateEndlessAdapter(ListAdapter wrapped);
	
	public AnimatableAdapter onCreateAnimationAdapter(BaseAdapter wrap);
}
