package com.xbcx.im.message.file;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;

import com.xbcx.common.choose.ChooseCallbackInterface;
import com.xbcx.common.choose.ChooseProvider;
import com.xbcx.im.ui.ActivityType;

public class IMChooseFileProvider extends ChooseProvider<IMChooseFileProvider.ChooseFileCallback> {

	public IMChooseFileProvider(int requestCode) {
		super(requestCode);
	}

	@Override
	protected void onChoose(Activity activity) {
		Class<?> cls = ActivityType.getActivityClass(
				ActivityType.ChooseFileActivity);
		if(cls != null){
			try{
				Intent intent = new Intent(activity, cls);
				intent.putExtra("choose", true);
				activity.startActivityForResult(intent, mRequestCode);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onChooseResult(Activity activity, int requestCode,Intent data) {
		if(data != null){
			try{
				ArrayList<FileItem> fileItems = (ArrayList<FileItem>)
						data.getSerializableExtra("fileitems");
				if(fileItems != null){
					if(mCallBack != null){
						mCallBack.onFileChoosed(activity,fileItems);
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public static interface ChooseFileCallback extends ChooseCallbackInterface{
		public void onFileChoosed(Activity activity,List<FileItem> fis);
	}
}
