package com.xbcx.common.pulltorefresh;

import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import com.xbcx.adapter.AnimatableAdapter;
import com.xbcx.core.XEndlessAdapter;

public class SimpleAdapterCreator implements AdapterCreator{

	protected Context 		mContext;
	protected ListAdapter	mAdapter;
	
	public SimpleAdapterCreator(Context context,ListAdapter adapter){
		mContext = context;
		mAdapter = adapter;
	}
	
	@Override
	public ListAdapter onCreateAdapter() {
		return mAdapter;
	}

	@Override
	public XEndlessAdapter onCreateEndlessAdapter(ListAdapter wrapped) {
		return new XEndlessAdapter(mContext, wrapped);
	}

	@Override
	public AnimatableAdapter onCreateAnimationAdapter(BaseAdapter wrap) {
		return null;
	}
}
