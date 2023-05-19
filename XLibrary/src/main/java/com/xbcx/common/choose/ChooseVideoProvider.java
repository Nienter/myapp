package com.xbcx.common.choose;

import java.util.ArrayList;
import java.util.List;

import com.xbcx.common.menu.Menu;
import com.xbcx.common.menu.MenuFactory;
import com.xbcx.common.menu.SimpleMenuDialogCreator;
import com.xbcx.library.R;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class ChooseVideoProvider extends ChooseProvider<ChooseVideoProvider.ChooseVideoCallback> {

	public static final int ChooseType_All 		= 0;
	public static final int ChooseType_Capture 	= 1;
	public static final int ChooseType_Albums 	= 2;
	
	protected final int MENUID_PHOTO_CAMERA	= 1;
	protected final int MENUID_PHOTO_FILE	= 2;
	
	protected int				mRequestCodeCamera;
	
	protected int				mChooseType;
	protected CharSequence		mTitle;
	
	public ChooseVideoProvider(int requestCode) {
		super(requestCode);
		mRequestCodeCamera = requestCode * 2;
	}
	
	public ChooseVideoProvider(int requestCode,int requestCodeCamera) {
		super(requestCode);
		mRequestCodeCamera = requestCodeCamera;
	}
	
	public ChooseVideoProvider setChooseType(int type){
		mChooseType = type;
		return this;
	}
	
	public ChooseVideoProvider setTitle(CharSequence title){
		mTitle = title;
		return this;
	}
	
	@Override
	public boolean acceptRequestCode(int requestCode) {
		return super.acceptRequestCode(requestCode) || requestCode == mRequestCodeCamera;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onChooseResult(Activity activity, int requestCode,Intent data) {
		if(requestCode == mRequestCodeCamera){
			if(data.getData() == null){
				Cursor cursor = activity.managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
						new String[]{MediaStore.Video.Media.DATA,
						MediaStore.Video.Media.DURATION}, 
						null, null, MediaStore.Video.VideoColumns.DATE_ADDED + " DESC");
				if(cursor != null && cursor.moveToFirst()){
					final String videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
					final long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
					onVideoChoose(videoPath,duration);
				}
			}else{
				onVideoChooseResult(data);
			}
		}else{
			onVideoChooseResult(data);
		}
	}
	
	@SuppressWarnings("deprecation")
	protected void onVideoChooseResult(Intent data){
		final String url = data.getDataString();
		if(url.contains("content")){
			try{
				Uri videoUri = data.getData();
				Cursor cursor = mActivity.managedQuery(videoUri, null, null, null, null);
				if(cursor != null && cursor.moveToFirst()){
					final String videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
					final long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
					onVideoChoose(videoPath,duration);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			long duration = 0;
			Cursor cursor = mActivity.managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
					new String[]{MediaStore.Video.Media.DURATION},
					MediaStore.Video.Media.DATA + "='" + url + "'", 
					null, null);
			if(cursor != null && cursor.moveToFirst()){
				duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
			}
			onVideoChoose(url, duration);
		}
	}

	@Override
	protected void onChoose(Activity activity) {
		if(mChooseType == ChooseType_All){
			final Context context = mActivity.getDialogContext();
			List<Menu> menus = new ArrayList<Menu>();
			menus.add(new Menu(MENUID_PHOTO_CAMERA, R.string.shoot_video));
			menus.add(new Menu(MENUID_PHOTO_FILE, R.string.choose_from_albums));
			
			MenuFactory.getInstance().showMenu(context, 
					menus, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(which == 0){
								launchVideoCapture(mActivity);
							}else if(which == 1){
								launchVideoChoose(mActivity);
							}
						}
					}, 
					new SimpleMenuDialogCreator(mTitle));
		}
	}
	
	protected void onVideoChoose(String path,long duration){
		if(mCallBack != null){
			mCallBack.onVideoChoosed(mActivity,path, duration);
		}
	}
	
	public void launchVideoCapture(Activity activity){
		try{
			Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
			activity.startActivityForResult(intent, mRequestCodeCamera);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void launchVideoChoose(Activity activity){
		try{
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("video/*");
			activity.startActivityForResult(intent,mRequestCode);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static interface ChooseVideoCallback extends ChooseCallbackInterface{
		public void onVideoChoosed(Activity activity,String videoPath,long duration);
	}
}
