package com.xbcx.common.choose;

import com.xbcx.core.ActivityBasePlugin;

import android.app.Activity;
import android.content.Intent;

public interface ChooseProviderPlugin<Callback> extends ActivityBasePlugin{

	public boolean acceptRequestCode(int requestCode);
	
	public void choose(Activity activity,Callback callback);
	
	public void onActivityResult(Activity activity,int requestCode, int resultCode, Intent data);
}
