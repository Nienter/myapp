package com.xbcx.im.ui.simpleimpl;

import android.os.Bundle;

import com.xbcx.im.ui.BaseChatActivity;
import com.xbcx.im.ui.IMMessageAdapter.OnCheckListener;

public abstract class BaseChooseMessageActivity extends BaseChatActivity  implements 
														OnCheckListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mIsShowTime = false;
		super.onCreate(savedInstanceState);
		
		mMessageAdapter.setIsCheck(true);
		mMessageAdapter.setOnCheckListener(this);
		
		onInitCheckItem();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	protected void onInitCheckItem(){
		final int checkCount = getInitCheckItemCount();
		final int count = mMessageAdapter.getCount();
		int end = count - checkCount;
		if(end < 0){
			end = 0;
		}
		for(int index = count - 1;index >= end;--index){
			mMessageAdapter.setCheckItem(index, true);
		}
	}
	
	protected int getInitCheckItemCount(){
		return 10;
	}
}
