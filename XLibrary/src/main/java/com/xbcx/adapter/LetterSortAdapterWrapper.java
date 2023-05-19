package com.xbcx.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections4.map.MultiValueMap;

import android.database.DataSetObserver;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.xbcx.adapter.FilterAdapterWrapper.ItemFilter;
import com.xbcx.utils.PinyinUtils;
import com.xbcx.utils.SystemUtils;

public class LetterSortAdapterWrapper extends BaseAdapter {
	
	private SetBaseAdapter<String>		mSectionAdapter = new DefaultSectionAdapter();
	
	private List<LetterSortInterface> 	mAdapters = new ArrayList<LetterSortInterface>();
	
	private List<LetterItem> 			mItemInfos = new ArrayList<LetterItem>();
	private HashMap<String, Integer> 	mMapLetterToPos = new HashMap<String, Integer>();
	
	private String						mTopKey = "↑";
	private String						mTopName = "↑";
	private OnLettersChangeListener		mOnLettersChangeListener;
	
	private List<ItemFilter> 			mItemFilters;
	private String						mFilterKey;
	
	private boolean						mDirty = true;
	
	private DataSetObserver				mObserver = new DataSetObserver() {
		
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
	
	public LetterSortAdapterWrapper setOnLettersChangeListener(OnLettersChangeListener listener){
		mOnLettersChangeListener = listener;
		return this;
	}
	
	public LetterSortAdapterWrapper addAdapter(LetterSortInterface adapter){
		mAdapters.add(adapter);
		return this;
	}
	
	public LetterSortAdapterWrapper addItemFilter(ItemFilter filter){
		if(mItemFilters == null){
			mItemFilters = new ArrayList<FilterAdapterWrapper.ItemFilter>();
		}
		mItemFilters.add(filter);
		notifyDataSetChanged();
		return this;
	}
	
	public LetterSortAdapterWrapper setSectionAdapter(SetBaseAdapter<String> sectionAdapter){
		mSectionAdapter = sectionAdapter;
		return this;
	}
	
	public LetterSortAdapterWrapper setFilterKey(String filter){
		mFilterKey = filter;
		notifyDataSetChanged();
		return this;
	}
	
	public LetterSortAdapterWrapper setTopName(String name){
		mTopName = name;
		return this;
	}
	
	@Override
	public int getCount() {
		if(mDirty){
			mDirty = false;
			boolean filterEmpty = TextUtils.isEmpty(mFilterKey);
			MultiValueMap<String, LetterItem> mapItemInfos = new MultiValueMap<String, LetterItem>();
			boolean hasTop = false;
			for(LetterSortInterface ba : mAdapters){
				int count = ba.getCount();
				for(int index = 0;index < count;++index){
					if(mItemFilters != null){
						Object item = ba.getItem(index);
						if(filterItem(item)){
							continue;
						}
					}
					String name = ba.getItemName(index);
					boolean isTop = ba.isTop(index);
					if(filterEmpty || SystemUtils.nameFilter(name, mFilterKey)){
						String firstSpell = null;
						if(isTop){
							hasTop = true;
							firstSpell = mTopKey;
						}else{
							firstSpell = PinyinUtils.getFirstSpell(name);
							if(firstSpell.length() == 0){
								firstSpell = "#";
							}else{
								if(!Character.isLetter(firstSpell.charAt(0))){
									firstSpell = "#";
								}
							}
						}
						mapItemInfos.put(firstSpell, new LetterItem(firstSpell,name,ba, index));
					}
				}
			}
			List<String> letters = new ArrayList<String>(mapItemInfos.keySet());
			if(hasTop){
				letters.remove(mTopKey);
				Collections.sort(letters);
				letters.add(0,mTopName);
				mSectionAdapter.replaceAll(letters);
				letters.remove(0);
				letters.add(0, mTopKey);
			}else{
				Collections.sort(letters);
				mSectionAdapter.replaceAll(letters);
			}
			
			mItemInfos.clear();
			int index = 0;
			List<LetterItem> iis = new ArrayList<LetterItem>();
			for(String letter : letters){
				mMapLetterToPos.put(letter, mItemInfos.size());
				mItemInfos.add(new LetterItem(letter, letter,mSectionAdapter, index++));
				iis.clear();
				iis.addAll(mapItemInfos.getCollection(letter));
				Collections.sort(iis);
				mItemInfos.addAll(iis);
			}
			
			if(mOnLettersChangeListener != null){
				mOnLettersChangeListener.onLettersChanged(letters);
			}
		}
		
		return mItemInfos.size();
	}
	
	protected boolean filterItem(Object item){
		for(ItemFilter filter : mItemFilters){
			if(filter.onFilter(item)){
				return true;
			}
		}
		return false;
	}
	
	public int getLetterSectionPos(String letter){
		Integer i = mMapLetterToPos.get(letter);
		return i == null ? 0 : i.intValue();
	}
	
	public int getLetterSectionPos(int sectionIndex){
		Integer i = mMapLetterToPos.get(mSectionAdapter.getItem(sectionIndex));
		return i == null ? 0 : i.intValue();
	}
	
	@Override
	public void notifyDataSetChanged() {
		mDirty = true;
		super.notifyDataSetChanged();
	}
	
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		super.registerDataSetObserver(observer);
		for(ListAdapter adapter : mAdapters){
			adapter.registerDataSetObserver(mObserver);
		}
	}
	
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		super.unregisterDataSetObserver(observer);
		for(ListAdapter adapter : mAdapters){
			adapter.unregisterDataSetObserver(mObserver);
		}
	}

	@Override
	public Object getItem(int position) {
		if(position >= 0 && position < mItemInfos.size()){
			LetterItem ii = mItemInfos.get(position);
			return ii.mAdapter.getItem(ii.mIndex);
		}
		return null;
	}
	
	@Override
	public int getViewTypeCount() {
		int typeCount = 0;
		for(ListAdapter adapter : mAdapters){
			typeCount += adapter.getViewTypeCount();
		}
		return typeCount + 1;
	}
	
	@Override
	public int getItemViewType(int position) {
		LetterItem ii = mItemInfos.get(position);
		int subType = ii.mAdapter.getItemViewType(ii.mIndex);
		int type = 0;
		for(ListAdapter adapter : mAdapters){
			if(adapter == ii.mAdapter){
				return type + subType;
			}
			type += adapter.getViewTypeCount();
		}
		return type;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LetterItem ii = mItemInfos.get(position);
		return ii.mAdapter.getView(ii.mIndex, convertView, parent);
	}
	
	protected static class LetterItem implements Comparable<LetterItem>{
		String				mLetter;
		String				mName;
		ListAdapter			mAdapter;
		int					mIndex;
		
		public LetterItem(String letter,String name,ListAdapter adapter,int index){
			mLetter = letter;
			mName = name;
			mAdapter = adapter;
			mIndex = index;
		}

		@Override
		public int compareTo(LetterItem another) {
			return this.mName.compareTo(another.mName);
		}
	}
	
	public static interface LetterSortInterface extends ListAdapter{
		public String getItemName(int position);
		
		public boolean isTop(int position);
	}
	
	public static class DefaultSectionAdapter extends SetBaseAdapter<String> implements StickyHeader{

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				TextView tv = new TextView(parent.getContext());
				tv.setMinHeight(SystemUtils.dipToPixel(parent.getContext(), 22));
				tv.setTextColor(Color.WHITE);
				tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
				SystemUtils.setPaddingLeft(tv, SystemUtils.dipToPixel(parent.getContext(), 10));
				convertView = tv;
			}
			TextView tv = (TextView)convertView;
			tv.setText((String)getItem(position));
			return convertView;
		}

		@Override
		public boolean isItemViewTypeSticky(int viewType) {
			return true;
		}

		@Override
		public View getStickyHeaderView(View convertView, int viewType, int index, ViewGroup parent) {
			return getView(index, convertView, parent);
		}
	
		@Override
		public <T extends String> void replaceAll(Collection<T> collection) {
			mListObject.clear();
			
			if(collection != null){
				mListObject.addAll(collection);
			}
		}
	}
	
	public static interface OnLettersChangeListener{
		public void onLettersChanged(List<String> letters);
	}
}
