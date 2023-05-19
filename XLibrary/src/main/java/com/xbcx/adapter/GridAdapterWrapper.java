package com.xbcx.adapter;

import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

public class GridAdapterWrapper extends AdapterWrapper implements 
													OnClickListener,
													OnLongClickListener{
	
	protected int 							mColumnNum;
	protected int							mHorizontalSpace;
	protected int							mVerticalSpace;
	protected int							mGridLayoutId;
	protected OnGridItemClickListener		mListener;
	protected OnGridItemLongClickListener	mLongClickListener;
	
	protected int							mPadding;
	
	protected HashMap<View, Integer> 		mMapViewToPos = new HashMap<View, Integer>();
	
	public GridAdapterWrapper(ListAdapter wrapped,int columnNum) {
		super(wrapped);
		mColumnNum = columnNum;
	}
	
	public GridAdapterWrapper setHorizontalSpace(int space){
		mHorizontalSpace = space;
		return this;
	}
	
	public GridAdapterWrapper setVerticalSpace(int space){
		mVerticalSpace = space;
		return this;
	}
	
	public GridAdapterWrapper setGridLayoutId(int layoutId){
		mGridLayoutId = layoutId;
		return this;
	}
	
	public GridAdapterWrapper setPadding(int padding){
		mPadding = padding;
		return this;
	}
	
	public GridAdapterWrapper setOnGridItemClickListener(OnGridItemClickListener listener){
		mListener = listener;
		return this;
	}
	
	public GridAdapterWrapper setOnGridItemLongClickListener(OnGridItemLongClickListener listener){
		mLongClickListener = listener;
		return this;
	}

	@Override
	public int getCount() {
		final int realCount = super.getCount();
		return realCount / mColumnNum + (realCount % mColumnNum > 0 ? 1 : 0);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout ll = null;
		if(convertView == null){
			if(mGridLayoutId == 0){
				ll = new LinearLayout(parent.getContext());
				ll.setOrientation(LinearLayout.HORIZONTAL);
			}else{
				ll = (LinearLayout)LayoutInflater.from(parent.getContext()).inflate(mGridLayoutId, null);
			}
			ll.setPadding(mPadding == 0 ? mHorizontalSpace : mPadding, 
					mVerticalSpace, 
					mPadding == 0 ? mHorizontalSpace : mPadding, 
					ll.getPaddingBottom());
		}else{
			ll = (LinearLayout)convertView;
		}
		
		final int start = position * mColumnNum;
		final int realCount = super.getCount();
		for(int index = 0;index < mColumnNum;++index){
			if(start + index < realCount){
				View wrapperConvertView = null;
				if(convertView != null){
					wrapperConvertView = ll.getChildAt(index);
					if(wrapperConvertView != null && wrapperConvertView instanceof EmptyView){
						ll.removeViewAt(index);
						wrapperConvertView = null;
					}
				}
				View v = super.getView(start + index, wrapperConvertView, parent);
				if(wrapperConvertView == null){
					v.setOnClickListener(this);
					v.setOnLongClickListener(this);
					ll.addView(v,index,generateLayoutParams(index));
				}else{
					wrapperConvertView.setVisibility(View.VISIBLE);
				}
				mMapViewToPos.put(v, start + index);
			}else{
				View child = ll.getChildAt(index);
				if(child == null){
					ll.addView(new EmptyView(parent.getContext()), generateLayoutParams(index));
				}else{
					child.setVisibility(View.INVISIBLE);
				}
			}
		}
		return ll;
	}

	@Override
	public void onClick(View v) {
		if(mListener != null){
			mListener.onGridItemClicked(this,v, mMapViewToPos.get(v));
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		if(mLongClickListener != null){
			return mLongClickListener.onGridItemLongClicked(this,v, mMapViewToPos.get(v));
		}
		return false;
	}
	
	protected LinearLayout.LayoutParams generateLayoutParams(int index){
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 
				LinearLayout.LayoutParams.WRAP_CONTENT,1);
		if(index > 0){
			lp.leftMargin = mHorizontalSpace;
		}
		return lp;
	}
	
	private static class EmptyView extends View{
		public EmptyView(Context context) {
			super(context);
		}
	}
	
	public static interface OnGridItemClickListener{
		public void onGridItemClicked(GridAdapterWrapper gaw,View v,int position);
	}
	
	public static interface OnGridItemLongClickListener{
		public boolean onGridItemLongClicked(GridAdapterWrapper gaw,View v,int position);
	}
}
