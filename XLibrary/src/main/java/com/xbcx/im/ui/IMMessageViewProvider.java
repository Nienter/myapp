package com.xbcx.im.ui;


import java.util.Collection;
import java.util.Collections;

import com.xbcx.im.XMessage;

import android.view.View;
import android.view.ViewGroup;

public abstract class IMMessageViewProvider {
	
	private static IMMessageViewProviderFactory sProviderFactory = new IMMessageViewProviderFactory();
	
	protected IMMessageAdapter	mAdapter;
	
	public Collection<XMessage> getCheckedMessage(){
		return Collections.emptySet();
	}
	
	public boolean isCheckedItem(XMessage xm){
		return mAdapter.isCheckedItem(xm);
	}
	
	public void setIMMessageAdapter(IMMessageAdapter adapter){
		mAdapter = adapter;
	}
	
	public OnViewClickListener getOnViewClickListener(){
		return mAdapter.getOnViewClickListener();
	}
	
	public void notifyDataSetChanged(){
		if(mAdapter != null){
			mAdapter.notifyDataSetChanged();
		}
	}
	
	public abstract boolean acceptHandle(XMessage message);
	
	public abstract View 	getView(XMessage message,View convertView,ViewGroup parent);
	
	public static void setIMMessageViewProviderFactory(IMMessageViewProviderFactory factory){
		sProviderFactory = factory;
	}
	
	public static IMMessageViewProviderFactory getIMMessageViewProviderFactory(){
		return sProviderFactory;
	}

	public static interface OnViewClickListener{
		
		public void 	onViewClicked(XMessage message,int nViewId);
		
		public boolean 	onViewLongClicked(XMessage message,int nViewId);
	}
}
