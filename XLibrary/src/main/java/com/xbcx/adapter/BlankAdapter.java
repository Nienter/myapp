package com.xbcx.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.xbcx.adapter.HideableAdapter;

public class BlankAdapter extends HideableAdapter {
	
	private int	mHeight;
	
	private int	mBackgroundColor = -1;
	
	public BlankAdapter(int height){
		mHeight = height;
	}
	
	public void setBackgroundColor(int color){
		mBackgroundColor = color;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = new View(parent.getContext());
			convertView.setMinimumHeight(mHeight);
			if(mBackgroundColor != -1){
				convertView.setBackgroundColor(mBackgroundColor);
			}
		}
		return convertView;
	}

}
