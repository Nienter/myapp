package com.xbcx.common.choose;

import java.io.File;
import java.net.URI;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

public class ChooseFileProvider extends ChooseProvider<ChooseFileProvider.ChooseFileCallback> {
	
	public ChooseFileProvider(int requestCode) {
		super(requestCode);
	}

	@Override
	protected void onChoose(Activity activity) {
		try{
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("application/*");
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			activity.startActivityForResult(intent, mRequestCode);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onChooseResult(Activity activity,int requestCode,Intent data) {
		try{
			Uri uri = data.getData();
			if(uri != null){
				String path = null;
				if("content".equals(uri.getScheme())){
					Cursor cursor = activity.managedQuery(uri, 
							new String[]{MediaStore.MediaColumns.DATA},
							null, null, null);
					if(cursor != null && cursor.moveToFirst()){
						path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
					}else{
						return;
					}
				}else{
					File file = new File(URI.create(uri.toString()));
					path = file.getAbsolutePath();
				}
				if(!TextUtils.isEmpty(path)){
					if(mCallBack != null){
						mCallBack.onFileChoosed(path);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static interface ChooseFileCallback extends ChooseCallbackInterface{
		public void onFileChoosed(String filePath);
	}
}
