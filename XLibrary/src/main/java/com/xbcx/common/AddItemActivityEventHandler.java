package com.xbcx.common;

import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.core.Event;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.BaseActivity.ActivityEventHandler;
import com.xbcx.utils.SystemUtils;

@SuppressWarnings("rawtypes")
public class AddItemActivityEventHandler implements ActivityEventHandler{
	
	protected SetBaseAdapter mAdapter;
	
	public AddItemActivityEventHandler(SetBaseAdapter adapter){
		mAdapter = adapter;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onHandleEventEnd(Event event, BaseActivity activity) {
		if(event.isSuccess()){
			Class<?> clz = SystemUtils.getSingleGenericClass(mAdapter.getClass(), SetBaseAdapter.class);
			Object item = event.findReturnParam(clz);
			if(item != null){
				mAdapter.addItem(item);
			}
		}
	}
}
