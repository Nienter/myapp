package com.xbcx.adapter;

import java.util.ArrayList;
import java.util.List;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class FilterAdapterWrapper extends AdapterWrapper {

	private boolean mDirty = true;
	
	private List<ItemFilter> mItemFilters = new ArrayList<ItemFilter>();
	
	private List<FilterItem> mItems = new ArrayList<FilterItem>();
	
	private DataSetObserver	mObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			super.onChanged();
			notifyDataSetChanged();
		}
		
		@Override
		public void onInvalidated() {
			super.onInvalidated();
			notifyDataSetInvalidated();
		}
	};
	
	public FilterAdapterWrapper(ListAdapter wrapped) {
		super(wrapped);
	}
	
	public FilterAdapterWrapper addItemFilter(ItemFilter filter){
		mItemFilters.add(filter);
		notifyDataSetChanged();
		return this;
	}
	
	@Override
	public int getCount() {
		if(mDirty){
			mDirty = false;
			mItems.clear();
			int count = mWrappedAdapter.getCount();
			for(int index = 0;index < count;++index){
				Object item = mWrappedAdapter.getItem(index);
				for(ItemFilter f : mItemFilters){
					if(!f.onFilter(item)){
						mItems.add(new FilterItem(index));
					}
				}
			}
		}
		return mItems.size();
	}
	
	@Override
	public Object getItem(int position) {
		if(position >= 0 && position < mItems.size()){
			FilterItem fi = mItems.get(position);
			return mWrappedAdapter.getItem(fi.mIndex);
		}
		return null;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FilterItem fi = mItems.get(position);
		return mWrappedAdapter.getView(fi.mIndex, convertView, parent);
	}
	
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		superRegisterDataSetObserver(observer);
		mWrappedAdapter.registerDataSetObserver(mObserver);
		mIsRegistered = true;
	}
	
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		superUnregisterDataSetObserver(observer);
		mWrappedAdapter.unregisterDataSetObserver(mObserver);
		mIsRegistered = false;
	}
	
	@Override
	public void notifyDataSetChanged() {
		mDirty = true;
		super.notifyDataSetChanged();
	}
	
	private static class FilterItem{
		int mIndex;
		
		public FilterItem(int index){
			mIndex = index;
		}
	}

	public static interface ItemFilter{
		public boolean onFilter(Object item);
	}
}
