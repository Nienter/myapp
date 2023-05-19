package com.xbcx.adapter;

import java.util.ArrayList;
import java.util.List;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

public class SectionAdapter extends BaseAdapter implements 
													StickyHeader,
													AnimatableAdapter,
													Hideable{
	
	protected List<BaseAdapter> mListAdapter;
	
	protected boolean			mRegisterDataed = false;
	
	protected AnimatableAdapter	mAnimatorAdapter;
	
	private   boolean			mIsShow = true;
	
	public SectionAdapter(){
		mListAdapter = new ArrayList<BaseAdapter>();
	}

	public int getCount() {
		if(mIsShow){
			int nCount = 0;
			int nAdapterCount = mListAdapter.size();
			for(int nIndex = 0;nIndex < nAdapterCount;++nIndex){
				nCount += mListAdapter.get(nIndex).getCount();
			}
			return nCount;
		}else{
			return 0;
		}
	}

	public Object getItem(int position) {
		final int nAdapterCount = mListAdapter.size();
		int nCount = 0;
		for(int nIndex = 0;nIndex < nAdapterCount;++nIndex){
			BaseAdapter adapter = mListAdapter.get(nIndex);
			nCount = adapter.getCount();
			if(position >= nCount){
				position -= nCount;
			}else{
				return adapter.getItem(position);
			}
		}
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		int nAdapterCount = mListAdapter.size();
		int nItemViewType = 0;
		int nCount = 0;
		for(int nIndex = 0;nIndex < nAdapterCount;++nIndex){
			BaseAdapter adapter = mListAdapter.get(nIndex);
			nCount = adapter.getCount();
			if(position >= nCount){
				position -= nCount;
				nItemViewType += adapter.getViewTypeCount();
			}else{
				nItemViewType += adapter.getItemViewType(position);
				break;
			}
		}
		return nItemViewType;
	}

	@Override
	public int getViewTypeCount() {
		int nAdapterCount = mListAdapter.size();
		int nTypeCount = 0;
		for(int nIndex = 0;nIndex < nAdapterCount;++nIndex){
			nTypeCount += mListAdapter.get(nIndex).getViewTypeCount();
		}
		return nTypeCount < 1 ? 1 : nTypeCount;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		mRegisterDataed = true;
		super.registerDataSetObserver(observer);
		for(BaseAdapter adapter : mListAdapter){
			adapter.registerDataSetObserver(observer);
		}
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		mRegisterDataed = false;
		super.unregisterDataSetObserver(observer);
		for(BaseAdapter adapter : mListAdapter){
			adapter.unregisterDataSetObserver(observer);
		}
	}
	
	@Override
	public boolean isEmpty() {
		for(BaseAdapter adapter : mListAdapter){
			if(!adapter.isEmpty()){
				return false;
			}
		}
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int nAdapterCount = mListAdapter.size();
		int nCount = 0;
		for(int nIndex = 0;nIndex < nAdapterCount;++nIndex){
			BaseAdapter adapter = mListAdapter.get(nIndex);
			nCount = adapter.getCount();
			if(position >= nCount){
				position -= nCount;
			}else{
				return adapter.getView(position, convertView, parent);
			}
		}
		return null;
	}

	public int 			getSectionCount(){
		return mListAdapter.size();
	}
	
	public List<BaseAdapter> getAllSectionAdapter(){
		return mListAdapter;
	}
	
	public void 		addSection(BaseAdapter adapter){
		if(mRegisterDataed){
			throw new IllegalArgumentException("viewTypeCount can't change after registerDataSetObserver");
		}
		if(adapter instanceof AnimatableAdapter){
			final AnimatableAdapter animator = (AnimatableAdapter)adapter;
			animator.setAnimatableAdapter(this);
		}
		mListAdapter.add(adapter);
	}
	
	
	public void 		removeSection(BaseAdapter adapter){
		if(mRegisterDataed){
			throw new IllegalArgumentException("viewTypeCount can't change after registerDataSetObserver");
		}
		mListAdapter.remove(adapter);
	}
	
	public BaseAdapter	getAdapter(int position){
		final int nAdapterCount = mListAdapter.size();
		int nCount = 0;
		for(int nIndex = 0;nIndex < nAdapterCount;++nIndex){
			BaseAdapter adapter = mListAdapter.get(nIndex);
			nCount = adapter.getCount();
			if(position >= nCount){
				position -= nCount;
			}else{
				return adapter;
			}
		}
		return null;
	}
	
	public void 		clear(){
		mListAdapter.clear();
	}

	@Override
	public boolean isItemViewTypeSticky(int viewType) {
		int nAdapterCount = mListAdapter.size();
		int nTypeCount = 0;
		for(int nIndex = 0;nIndex < nAdapterCount;++nIndex){
			final BaseAdapter adapter = mListAdapter.get(nIndex);
			nTypeCount += adapter.getViewTypeCount();
			if(viewType < nTypeCount){
				if(adapter instanceof StickyHeader){
					return ((StickyHeader)adapter).isItemViewTypeSticky(
							adapter.getViewTypeCount() - (nTypeCount - viewType));
				}else{
					return false;
				}
			}
		}
		return false;
	}

	@Override
	public View getStickyHeaderView(View convertView,int viewType,int index,ViewGroup parent) {
		int nAdapterCount = mListAdapter.size();
		int nTypeCount = 0;
		int count = 0;
		for(int nIndex = 0;nIndex < nAdapterCount;++nIndex){
			final BaseAdapter adapter = mListAdapter.get(nIndex);
			nTypeCount += adapter.getViewTypeCount();
			if(viewType < nTypeCount){
				if(adapter instanceof StickyHeader){
					return ((StickyHeader)adapter).getStickyHeaderView(
							convertView,
							adapter.getViewTypeCount() - (nTypeCount - viewType),
							index - count,
							parent);
				}else{
					return null;
				}
			}
			count += adapter.getCount();
		}
		return null;
	}

	@Override
	public void playAddAnimation(int pos, BaseAdapter adapter) {
		if(mAnimatorAdapter != null){
			int startIndex = 0;
			for(BaseAdapter section : mListAdapter){
				if(adapter == section){
					mAnimatorAdapter.playAddAnimation(startIndex + pos, this);
					break;
				}
				startIndex += section.getCount();
			}
		}
	}

	@Override
	public void playRemoveAnimation(int pos, BaseAdapter adapter) {
		if(mAnimatorAdapter != null){
			int startIndex = 0;
			for(BaseAdapter section : mListAdapter){
				if(adapter == section){
					mAnimatorAdapter.playRemoveAnimation(startIndex + pos, this);
					break;
				}
				startIndex += section.getCount();
			}
		}
	}

	@Override
	public void setAnimatableAdapter(AnimatableAdapter adapter) {
		mAnimatorAdapter = adapter;
		for(BaseAdapter section : mListAdapter){
			if(section instanceof AnimatableAdapter){
				final AnimatableAdapter animatable = (AnimatableAdapter)section;
				animatable.setAnimatableAdapter(this);
			}
		}
	}

	@Override
	public void setAbsListView(AbsListView listView) {
	}

	@Override
	public void setIsShow(boolean bShow) {
		mIsShow = bShow;
		notifyDataSetChanged();
	}

	@Override
	public boolean isShow() {
		return mIsShow;
	}
}
