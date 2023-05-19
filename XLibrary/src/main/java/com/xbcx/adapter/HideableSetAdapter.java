package com.xbcx.adapter;

import com.xbcx.adapter.SetBaseAdapter;

public abstract class HideableSetAdapter<E extends Object> extends SetBaseAdapter<E> implements
																Hideable{
	
	private boolean	mIsShow = true;
	
	@Override
	public void setIsShow(boolean bShow){
		if(mIsShow != bShow){
			mIsShow = bShow;
			notifyDataSetChanged();
		}
	}
	
	@Override
	public boolean isShow(){
		return mIsShow;
	}
	
	@Override
	public int getCount() {
		if(mIsShow){
			return super.getCount();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if(position >= 0 && position < mListObject.size()){
			return super.getItem(position);
		}
		return null;
	}
}
