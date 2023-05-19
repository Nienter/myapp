package com.xbcx.adapter;

import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

public interface AnimatableAdapter extends ListAdapter{
	
	public void	playAddAnimation(int pos,BaseAdapter adapter);
	
	public void playRemoveAnimation(int pos,BaseAdapter adapter);
	
	public void setAnimatableAdapter(AnimatableAdapter adapter);
	
	public void setAbsListView(AbsListView listView);
}
