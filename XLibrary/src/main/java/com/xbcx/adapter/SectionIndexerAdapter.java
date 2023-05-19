package com.xbcx.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

public class SectionIndexerAdapter extends SectionAdapter implements SectionIndexer{

	protected List<String>				mSections = new ArrayList<String>();
	protected Map<String, BaseAdapter>  mMapSectionKeyToAdapter = new HashMap<String, BaseAdapter>();
	
	public void addSection(String sectionKey,BaseAdapter adapter){
		mMapSectionKeyToAdapter.put(sectionKey, adapter);
		mSections.add(sectionKey);
		addSection(adapter);
	}

	@Override
	public Object[] getSections() {
		return mSections.toArray();
	}

	@Override
	public int getPositionForSection(int section) {
		Object obj[] = getSections();
		final String key = (String)obj[section];
		final BaseAdapter sectionAdapter = mMapSectionKeyToAdapter.get(key);
		int pos = 0;
		for(BaseAdapter adapter : mListAdapter){
			if(adapter == sectionAdapter){
				break;
			}else{
				pos += adapter.getCount();
			}
		}
		return pos;
	}

	@Override
	public int getSectionForPosition(int position) {
		int count = mSections.size();
		for(int index = 0;index < count;++index){
			if(position < getPositionForSection(index)){
				return index;
			}
		}
		return 0;
	}

	@Override
	public void clear() {
		super.clear();
		mSections.clear();
		mMapSectionKeyToAdapter.clear();
	}
}
