package com.xbcx.im.ui.simpleimpl;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.core.Event;
import com.xbcx.core.EventManager.OnEventListener;

public abstract class AbsBaseAdapter<E extends Object,T extends AbsBaseAdapter.ViewHolder> extends SetBaseAdapter<E>
												implements View.OnClickListener,
													OnEventListener{

	protected Context mContext;
	
	protected OnChildViewClickListener	mOnChildViewClickListener;
	
	public AbsBaseAdapter(Context context){
		mContext = context;
	}
	
	public void setOnChildViewClickListener(OnChildViewClickListener listener){
		mOnChildViewClickListener = listener;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		T viewHolder;
		if(convertView == null){
			convertView = onCreateConvertView();
			viewHolder = onCreateViewHolder();
			onSetViewHolder(viewHolder, convertView);
			
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (T)convertView.getTag();
		}
		
		final E item = (E)getItem(position);
		
		onSetChildViewTag(viewHolder, item);
		
		onUpdateView(viewHolder, item, position);
		
		return convertView;
	}

	protected abstract View onCreateConvertView();
	
	protected abstract T onCreateViewHolder();
	
	protected abstract void	onSetViewHolder(T viewHolder, View convertView);
	
	protected abstract void	onSetChildViewTag(T viewHolder, Object item);
	
	protected abstract void	onUpdateView(T viewHolder, E item, int position);
	
	@Override
	public void onEventRunEnd(Event event) {
	}
	
	@Override
	public void onClick(View v) {
		if(mOnChildViewClickListener != null){
			mOnChildViewClickListener.onChildViewClicked(this, v.getTag(), v.getId(), v);
		}
	}
	
	public static class ViewHolder{
	}
}
