package com.xbcx.common.pulltorefresh;

import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.core.Event;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.BaseActivity.ActivityEventHandler;

public class AddOrModifyActivityEventHandler<T> implements ActivityEventHandler{

	private Class<T> 			mItemClass;
	private SetBaseAdapter<T> 	mAdapter;
	
	public AddOrModifyActivityEventHandler(SetBaseAdapter<T> adapter,Class<T> itemClass){
		mAdapter = adapter;
		mItemClass = itemClass;
	}
	
	@Override
	public void onHandleEventEnd(Event event, BaseActivity activity) {
		if(event.isSuccess()){
			T item = event.findReturnParam(mItemClass);
			if(item != null){
				mAdapter.updateOrInsertItem(0, item);
			}
		}
	}

}
