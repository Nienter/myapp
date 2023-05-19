package com.xbcx.common;

import android.text.TextUtils;

import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.core.Event;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.BaseActivity.ActivityEventHandler;

public class DeleteItemActivityEventHandler implements ActivityEventHandler{

	protected SetBaseAdapter<?> mAdapter;
	protected boolean			mDeleteAllWhenIdEmpty;
	
	public DeleteItemActivityEventHandler(SetBaseAdapter<?> adapter){
		mAdapter = adapter;
	}
	
	public DeleteItemActivityEventHandler setDeleteAllWhenIdEmpty(boolean bDeleteAll){
		mDeleteAllWhenIdEmpty = bDeleteAll;
		return this;
	}
	
	@Override
	public void onHandleEventEnd(Event event, BaseActivity activity) {
		if(event.isSuccess()){
			final String id = event.findParam(String.class);
			if(!TextUtils.isEmpty(id)){
				mAdapter.removeItemById(id);
			}else{
				if(mDeleteAllWhenIdEmpty){
					mAdapter.clear();
				}
			}
		}
	}

}
