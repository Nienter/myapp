package com.xbcx.common.choose;

import com.xbcx.core.ActivityPlugin;
import com.xbcx.core.BaseActivity;

import android.app.Activity;
import android.content.Intent;

public abstract class ChooseProvider<Callback extends ChooseCallbackInterface> extends ActivityPlugin<BaseActivity>
											implements ChooseProviderPlugin<Callback>{
	
	protected int		mRequestCode;
	protected Callback	mCallBack;
	
	public ChooseProvider(int requestCode){
		mRequestCode = requestCode;
	}
	
	@Override
	public void choose(Activity activity,Callback cb){
		mCallBack = cb;
		onChoose(activity);
	}
	
	@Override
	public boolean acceptRequestCode(int requestCode) {
		return requestCode == mRequestCode;
	}
	
	@Override
	public void onActivityResult(Activity activity,int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK){
			onChooseResult(activity,requestCode,data);
		}
	}
	
	protected abstract void onChooseResult(Activity activity,int requestCode,Intent data);
	
	protected abstract void onChoose(Activity activity);
}
