package com.xbcx.im.message.video;

import android.app.Activity;
import android.content.Intent;

import com.xbcx.common.choose.ChooseVideoProvider;
import com.xbcx.im.ui.IMGlobalSetting;

public class IMChooseVideoProvider extends ChooseVideoProvider{

	public IMChooseVideoProvider(int requestCode) {
		super(requestCode);
	}
	
	public IMChooseVideoProvider(int requestCode, int requestCodeCamera) {
		super(requestCode, requestCodeCamera);
	}
	
	@Override
	public void launchVideoCapture(Activity activity) {
		if(IMGlobalSetting.videoCaptureActivityClass == null){
			super.launchVideoCapture(activity);
		}else{
			try{
				Intent intent = new Intent(activity, IMGlobalSetting.videoCaptureActivityClass);
				activity.startActivityForResult(intent, mRequestCodeCamera);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
