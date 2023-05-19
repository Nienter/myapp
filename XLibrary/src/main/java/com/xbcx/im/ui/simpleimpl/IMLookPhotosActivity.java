package com.xbcx.im.ui.simpleimpl;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.viewpager.widget.PagerAdapter;

import com.xbcx.adapter.CommonPagerAdapter;
import com.xbcx.common.LookPhotosActivity;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.OnEventProgressListener;
import com.xbcx.core.XApplication;
import com.xbcx.im.XMessage;
import com.xbcx.library.R;
import com.xbcx.utils.FileHelper;
import com.xbcx.utils.SystemUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IMLookPhotosActivity extends LookPhotosActivity implements
												OnEventProgressListener{
	
	private IMPhotoAdapter		mAdapter;
	private List<IMPhotoInfo> 	mPhotoInfos;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mPhotoInfos = (ArrayList<IMPhotoInfo>)getIntent().getSerializableExtra("photos");
		if(mPhotoInfos == null){
			mPhotoInfos = new ArrayList<IMLookPhotosActivity.IMPhotoInfo>();
		}
		super.onCreate(savedInstanceState);
		
		addAndManageEventListener(EventCode.HTTP_Download);
		mEventManager.addEventProgressListener(EventCode.HTTP_Download, this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		mEventManager.removeEventProgressListener(EventCode.HTTP_Download, this);
	}
	
	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
	}

	public static void launch(Activity activity,XMessage xmChoose,List<XMessage> xms){
		Intent i = new Intent(activity, IMLookPhotosActivity.class);
		ArrayList<IMPhotoInfo> pis = new ArrayList<IMPhotoInfo>();
		for(XMessage xm : xms){
			if(xm.getType() == XMessage.TYPE_PHOTO){
				IMPhotoInfo pi = new IMPhotoInfo();
				pi.url = xm.getPhotoDownloadUrl();
				pi.filePath = xm.getFilePath();
				pi.fromSelf = xm.isFromSelf();
				pi.displayName = xm.getDisplayName();
				pi.sendTime = xm.getSendTime();
				pis.add(pi);
			}
		}
		i.putExtra("photos", pis);
		i.putExtra("pic", xmChoose.getPhotoDownloadUrl());
		activity.startActivity(i);
	}

	@Override
	protected PagerAdapter onCreatePagerAdapter() {
		return mAdapter = new IMPhotoAdapter();
	}
	
	@Override
	protected void onInitPage() {
		final String pic = mPicEnter;
		if(TextUtils.isEmpty(pic)){
			onPageSelected(0);
		}else{
			int index = 0;
			for(IMPhotoInfo pi : mPhotoInfos){
				if(pic.equals(pi.url)){
					break;
				}
				++index;
			}
			if(index > 0){
				mViewPager.setCurrentItem(index);
			}else{
				onPageSelected(0);
			}
		}
	}

	@Override
	public void onPageSelected(int arg0) {
		super.onPageSelected(arg0);
		final IMPhotoInfo pi = mPhotoInfos.get(arg0);
		
		if(!FileHelper.isBitmapFile(pi.filePath)){
			if(!mEventManager.isEventRunning(EventCode.HTTP_Download, pi.url,pi.filePath)){
				pushEventNoProgress(EventCode.HTTP_Download,
						pi.url,pi.filePath);
			}
		}
	}
	
	@Override
	protected void onTitleRightButtonClicked(View v) {
		final IMPhotoInfo pi = mPhotoInfos.get(mViewPager.getCurrentItem());
		if(!TextUtils.isEmpty(pi.url)){
			final Bitmap bmp = SystemUtils.decodeSampledBitmapFromFilePath(
					pi.filePath, 
					XApplication.getScreenWidth(), 
					XApplication.getScreenWidth());
			if(bmp != null){
				showXProgressDialog();
				new Thread(){
					@Override
					public void run() {
						Media.insertImage(getContentResolver(), bmp, "title", getString(R.string.app_name));
						XApplication.getMainThreadHandler().post(new Runnable() {
							@Override
							public void run() {
								mToastManager.show(R.string.save_success);
								dismissXProgressDialog();
							}
						});
					}
				}.start();
			}else{
				mToastManager.show(R.string.toast_pic_is_downloading);
			}
		}
	}

	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == EventCode.HTTP_Download){
			final String url = (String)event.getParamAtIndex(0);
			int start = mViewPager.getCurrentItem() - 1;
			if(start < 0){
				start = 0;
			}
			int end = mViewPager.getCurrentItem() + 1;
			if(end > mAdapter.getCount()){
				end = mAdapter.getCount();
			}
			for(int index = start;index < end;++index){
				final IMPhotoInfo pi = mPhotoInfos.get(index);
				if(pi.url.equals(url)){
					final View convertView = mAdapter.findViewFromPos(mViewPager.getCurrentItem());
					if(convertView != null){
						final ProgressBar pb = (ProgressBar)convertView.findViewById(R.id.pb);
						final ImageView iv = (ImageView)convertView.findViewById(R.id.ivPhoto);
						
						pb.setVisibility(View.GONE);
						iv.setVisibility(View.VISIBLE);
						if(event.isSuccess()){
							if(!SystemUtils.safeSetImageBitmap(iv, pi.filePath)){
								iv.setImageResource(R.drawable.chat_img);
							}
						}else{
							iv.setImageResource(R.drawable.chat_img);
						}
					}
					
					break;
				}
			}
		}
	}

	@Override
	public void onEventProgress(Event e, int progress) {
		final String url = (String)e.getParamAtIndex(0);
		int start = mViewPager.getCurrentItem() - 1;
		if(start < 0){
			start = 0;
		}
		int end = mViewPager.getCurrentItem() + 1;
		if(end > mAdapter.getCount()){
			end = mAdapter.getCount();
		}
		for(int index = start;index < end;++index){
			final IMPhotoInfo pi = mPhotoInfos.get(index);
			if(pi.url != null && pi.url.equals(url)){
				final View convertView = mAdapter.findViewFromPos(mViewPager.getCurrentItem());
				if(convertView != null){
					final ProgressBar pb = (ProgressBar)convertView.findViewById(R.id.pb);
					final ImageView iv = (ImageView)convertView.findViewById(R.id.ivPhoto);
					
					final int pro = progress < 2 ? 2 : progress;
					pi.progress = pro;
					pb.setVisibility(View.VISIBLE);
					iv.setVisibility(View.GONE);
					pb.setProgress(pro);
				}
				break;
			}
		}
	}

	private static class IMPhotoInfo implements Serializable{
		private static final long serialVersionUID = 1L;
		
		String	url;
		String	filePath;
		boolean	fromSelf;
		String	displayName;
		long	sendTime;
		
		int		progress = 2;
	}

	private class IMPhotoAdapter extends CommonPagerAdapter{
		@Override
		protected View getView(View v, int nPos,ViewGroup parent) {
			final View convertView = getLayoutInflater().inflate(R.layout.xlibrary_adapter_imlookphoto_item, null);
			final ProgressBar pb = (ProgressBar)convertView.findViewById(R.id.pb);
			final ImageView iv = (ImageView)convertView.findViewById(R.id.ivPhoto);
			pb.setMax(100);
			final IMPhotoInfo pi = mPhotoInfos.get(nPos);
			if(mEventManager.isEventRunning(EventCode.HTTP_Download, 
					pi.url,pi.filePath)){
				pb.setVisibility(View.VISIBLE);
				pb.setProgress(pi.progress);
				iv.setVisibility(View.GONE);
			}else{
				pb.setVisibility(View.GONE);
				if(FileHelper.isFileExists(pi.filePath)){
					if(!SystemUtils.safeSetImageBitmap(iv, pi.filePath,XApplication.getScreenWidth(),XApplication.getScreenHeight())){
						iv.setImageResource(R.drawable.chat_img);
					}
				}else{
					iv.setImageResource(R.drawable.chat_img);
				}
			}
			
			return convertView;
		}

		@Override
		public int getCount() {
			return mPhotoInfos.size();
		}
	}
}
