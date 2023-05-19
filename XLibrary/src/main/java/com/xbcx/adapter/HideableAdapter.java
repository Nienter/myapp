package com.xbcx.adapter;

import android.widget.BaseAdapter;

public abstract class HideableAdapter extends BaseAdapter implements Hideable{

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
			return 1;
		}
		return 0;
	}
	
	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}
}
