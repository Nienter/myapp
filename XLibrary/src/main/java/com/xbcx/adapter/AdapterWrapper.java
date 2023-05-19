package com.xbcx.adapter;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

public abstract class AdapterWrapper extends BaseAdapter implements AnimatableAdapter{
	
	protected ListAdapter		mWrappedAdapter;
	protected boolean			mIsRegistered;
	
	private AnimatableAdapter	mAnimatableAdapter;
	
	public AdapterWrapper(ListAdapter wrapped){
		mWrappedAdapter = wrapped;
	}

	@Override
	public int getCount() {
		return mWrappedAdapter.getCount();
	}

	@Override
	public Object getItem(int position) {
		return mWrappedAdapter.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return mWrappedAdapter.getItemId(position);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return mWrappedAdapter.areAllItemsEnabled();
	}
	
	@Override
	public boolean isEnabled(int position) {
		return mWrappedAdapter.isEnabled(position);
	}

	@Override
	public int getItemViewType(int position) {
		return mWrappedAdapter.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		return mWrappedAdapter.getViewTypeCount();
	}
	
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		super.registerDataSetObserver(observer);
		mIsRegistered = true;
		mWrappedAdapter.registerDataSetObserver(observer);
	}
	
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		super.unregisterDataSetObserver(observer);
		mIsRegistered = false;
		mWrappedAdapter.unregisterDataSetObserver(observer);
	}
	
	public void superRegisterDataSetObserver(DataSetObserver observer){
		super.registerDataSetObserver(observer);
	}
	
	public void superUnregisterDataSetObserver(DataSetObserver observer){
		super.unregisterDataSetObserver(observer);
	}
	
	public boolean isRegisteredDataSetObserver(){
		return mIsRegistered;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return mWrappedAdapter.getView(position, convertView, parent);
	}
	
	@Override
	public boolean isEmpty() {
		return getWrappedAdapter().isEmpty();
	}
	
	public ListAdapter	getWrappedAdapter(){
		return mWrappedAdapter;
	}

	@Override
	public void playAddAnimation(int pos, BaseAdapter adapter) {
		if(mAnimatableAdapter != null){
			if(checkPlayAnimation(pos)){
				mAnimatableAdapter.playAddAnimation(pos, this);
			}
		}
	}

	@Override
	public void playRemoveAnimation(int pos, BaseAdapter adapter) {
		if(mAnimatableAdapter != null){
			if(checkPlayAnimation(pos)){
				mAnimatableAdapter.playRemoveAnimation(pos, this);
			}
		}
	}
	
	protected boolean checkPlayAnimation(int pos){
		final Object realItem = getItem(pos);
		final Object wrapItem = mWrappedAdapter.getItem(pos);
		if(realItem != null && wrapItem != null){
			return realItem == wrapItem;
		}
		return false;
	}

	@Override
	public void setAnimatableAdapter(AnimatableAdapter adapter) {
		mAnimatableAdapter = adapter;
		if(mWrappedAdapter instanceof AnimatableAdapter){
			((AnimatableAdapter)mWrappedAdapter).setAnimatableAdapter(this);
		}
	}

	@Override
	public void setAbsListView(AbsListView listView) {
	}
}
