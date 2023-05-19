package com.xbcx.adapter;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import java.util.LinkedList;
import java.util.List;

public abstract class CommonPagerAdapter extends PagerAdapter {

	private final List<View> mViewRecycle = new LinkedList<View>();
	private final SparseArray<View> mMapPosToView = new SparseArray<View>();
	
	private int mPageCount;
	
	@Override
	public int getCount() {
		return mPageCount;
	}
	
	public View	findViewFromPos(int pos){
		return mMapPosToView.get(pos);
	}
	
	public SparseArray<View> getCacheViews(){
		return mMapPosToView;
	}
	
	public void setPageCount(int nCount){
		mPageCount = nCount;
	}
	
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		final View v = mMapPosToView.get(position);
		mMapPosToView.remove(position);
		
		mViewRecycle.add(v);
		container.removeView(v);
	}
    
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View v = null;
		if(mViewRecycle.size() > 0){
			v = mViewRecycle.get(0);
			mViewRecycle.remove(0);
		}
		
		v = getView(v, position,container);
		
		mMapPosToView.put(position, v);
		
		container.addView(v);
		return v;
	}
	
	protected abstract View getView(View v,int nPos,ViewGroup parent);

}
