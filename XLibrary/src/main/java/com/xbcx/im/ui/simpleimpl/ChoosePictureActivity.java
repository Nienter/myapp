package com.xbcx.im.ui.simpleimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.core.NameObject;
import com.xbcx.core.XApplication;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.bmp.XImageViewAware;
import com.xbcx.library.R;

public class ChoosePictureActivity extends BaseActivity implements 
												AdapterView.OnItemClickListener{
	
	public static final String EXTRA_RETURN_PICS = "pics";
	
	protected GridView				mGridView;
	protected ChoosePictureAdapter 	mAdapter;
	
	protected int					mMaxChooseCount;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mIsShowChatRoomBar = false;
		super.onCreate(savedInstanceState);
		
		mMaxChooseCount = getIntent().getIntExtra("maxchoosecount", -1);
		
		addTextButtonInTitleRight(R.string.complete);
		
		mGridView = (GridView)findViewById(R.id.gv);
		mAdapter = new ChoosePictureAdapter();
		mAdapter.setMultiSelectMode();
		mGridView.setOnItemClickListener(this);
		mGridView.setAdapter(mAdapter);
		showXProgressDialog();
		new Thread(){
			@Override
			public void run() {
				List<NameObject> datas = null;
				Cursor c = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						new String[]{MediaStore.Images.ImageColumns.DATA,MediaStore.Images.ImageColumns.DISPLAY_NAME},
						null, null, MediaStore.Images.ImageColumns.DATE_MODIFIED + " desc");
				if(c != null && c.moveToFirst()){
					datas = new ArrayList<NameObject>();
					do{
						NameObject no = new NameObject(c.getString(
								c.getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
						no.setName(c.getString(
								c.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)));
						datas.add(no);
					}while(c.moveToNext());	
				}
				
				final List<NameObject> result = datas;
				
				XApplication.getMainThreadHandler().post(new Runnable() {
					@Override
					public void run() {
						dismissXProgressDialog();
						if(result != null){
							mAdapter.replaceAll(result);
						}
					}
				});
			}
		}.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mActivityLayoutId = R.layout.xlibrary_activity_choosepicture;
		ba.mTitleTextStringId = R.string.picture;
	}
	
	public static void launchForResult(Activity activity,int maxChooseCount,int requestCode){
		Intent i = new Intent(activity, ChoosePictureActivity.class);
		i.putExtra("maxchoosecount", maxChooseCount);
		activity.startActivityForResult(i, requestCode);
	}
	
	@Override
	protected void onTitleRightButtonClicked(View v) {
		Collection<NameObject> nos = mAdapter.getAllSelectItem();
		if(nos != null && nos.size() > 0){
			Intent data = new Intent();
			data.putExtra(EXTRA_RETURN_PICS, 
					new ArrayList<NameObject>(nos));
			setResult(RESULT_OK, data);
			finish();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final Object item = parent.getItemAtPosition(position);
		if(item != null && item instanceof NameObject){
			final NameObject no = (NameObject)item;
			if(mAdapter.isSelected(no)){
				mAdapter.removeSelectItem(no);
			}else{
				if(mMaxChooseCount == -1 || mAdapter.getAllSelectItem().size() < mMaxChooseCount){
					mAdapter.addSelectItem(no);
				}else{
					mToastManager.show(getString(R.string.toast_choose_pic_max_count, mMaxChooseCount));
				}
			}
		}
	}

	protected static class ChoosePictureAdapter extends SetBaseAdapter<NameObject>{
		
		
		protected DisplayImageOptions mDisplayOptions = new DisplayImageOptions.Builder()
							.cacheInMemory(true)
							.cacheOnDisk(false)
							.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
							.displayer(new SimpleBitmapDisplayer())
							.bitmapConfig(Bitmap.Config.RGB_565)
							.build();
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = LayoutInflater.from(parent.getContext()).inflate(
						R.layout.xlibrary_adapter_choosepicture, null);
			}
			
			final ImageView ivPic = (ImageView)convertView.findViewById(R.id.ivPic);
			final View viewChoose = convertView.findViewById(R.id.ivChoose);
			
			final NameObject no = (NameObject)getItem(position);
			final String uri = no.getId();
			XApplication.getImageLoader().displayImage(uri, 
					new XImageViewAware(ivPic, 
							XApplication.getScreenWidth() / 3, 
							XApplication.getScreenWidth() / 3),
					mDisplayOptions,null);
			if(isSelected(no)){
				viewChoose.setVisibility(View.VISIBLE);
			}else{
				viewChoose.setVisibility(View.GONE);
			}
			
			return convertView;
		}
	}
}
