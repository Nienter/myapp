package com.xbcx.common;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.xbcx.adapter.CommonPagerAdapter;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.XApplication;
import com.xbcx.core.bmp.XBitmapDisplayer;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

import java.util.ArrayList;
import java.util.List;

public class LookPhotosActivity extends BaseActivity implements 
											ViewPager.OnPageChangeListener,
											ImageLoadingProgressListener,
											ImageLoadingListener{
	
	public static int	DefaultPicResId = 0;
	
	protected View			mViewTitleRight;;
	
	protected ViewPager		mViewPager;
	protected PagerAdapter mAdapter;
	protected List<String> 	mPics;
	protected String		mPicEnter;
	
	private boolean			mIsAnimating;
	
	private GestureDetector	mGestureDetector;
	
	protected DisplayImageOptions mOptions;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mIsShowChatRoomBar = false;
		super.onCreate(savedInstanceState);
		
		final String pic = getIntent().getStringExtra("pic");
		List<String> pics = (ArrayList<String>)getIntent().getSerializableExtra("pics");
		if(pics == null){
			pics = new ArrayList<String>();
			if(!TextUtils.isEmpty(pic)){
				pics.add(pic);
			}
		}
		mPics = pics;
		mPicEnter = pic;
		
		onInitDisplayOptions();
		
		mViewTitleRight = addTextButtonInTitleRight(R.string.save);
		
		mViewPager = (ViewPager)findViewById(R.id.vp);
		mAdapter = onCreatePagerAdapter();
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setAdapter(mAdapter);
		
		if(mAdapter.getCount() > 0){
			onInitPage();
		}
		
		if(getViewTitle() != null){
			ViewCompat.setAlpha(getViewTitle(), 0);
		}
		
		mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				final View viewTitle = getViewTitle();
				if(viewTitle != null){
					if(mIsAnimating){
						return false;
					}
					if(ViewCompat.getAlpha(viewTitle) == 1){
						ObjectAnimator a = ObjectAnimator.ofFloat(viewTitle, "alpha", 1,0);
						a.addListener(new Animator.AnimatorListener() {
							@Override
							public void onAnimationStart(Animator arg0) {
							}
							@Override
							public void onAnimationRepeat(Animator arg0) {
							}
							@Override
							public void onAnimationEnd(Animator arg0) {
								mIsAnimating = false;
							}
							@Override
							public void onAnimationCancel(Animator arg0) {
							}
						});
						a.setDuration(500);
						a.start();
						mIsAnimating = true;
					}else{
						ObjectAnimator a = ObjectAnimator.ofFloat(viewTitle, "alpha", 0,1);
						a.addListener(new Animator.AnimatorListener() {
							@Override
							public void onAnimationStart(Animator arg0) {
							}
							@Override
							public void onAnimationRepeat(Animator arg0) {
							}
							
							@Override
							public void onAnimationEnd(Animator arg0) {
								mIsAnimating = false;
							}
							@Override
							public void onAnimationCancel(Animator arg0) {
							}
						});
						a.setDuration(500);
						a.start();
						mIsAnimating = true;
					}
				}
				return super.onSingleTapUp(e);
			}
		});
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mActivityLayoutId = R.layout.xlibrary_activity_lookphotos;
	}
	
	public static void launch(Activity activity,String pic,ArrayList<String> pics){
		Intent i = new Intent(activity, LookPhotosActivity.class);
		i.putExtra("pic", pic);
		i.putExtra("pics", pics);
		activity.startActivity(i);
	}
	
	protected void onInitDisplayOptions(){
		mOptions = new DisplayImageOptions.Builder()
			.showImageOnLoading(DefaultPicResId)
			.showImageForEmptyUri(DefaultPicResId)
			.showImageOnFail(DefaultPicResId)
			.imageScaleType(ImageScaleType.NONE)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.displayer(new XBitmapDisplayer())
			.resetViewBeforeLoading(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
	}
	
	protected PagerAdapter	onCreatePagerAdapter(){
		MyAdapter adapter = new MyAdapter();
		return adapter;
	}
	
	protected void 			onInitPage(){
		final String pic = mPicEnter;
		if(TextUtils.isEmpty(pic)){
			onPageSelected(0);
		}else{
			final int index = mPics.indexOf(pic);
			if(index > 0){
				mViewPager.setCurrentItem(index);
			}else{
				onPageSelected(0);
			}
		}
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		getTextViewTitle().setText((arg0 + 1) + "/" + mAdapter.getCount());
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		mGestureDetector.onTouchEvent(ev);
		try{
			return super.dispatchTouchEvent(ev);
		}catch(Exception e){
			return false;
		}
	}

	@Override
	protected void onTitleRightButtonClicked(View v) {
		super.onTitleRightButtonClicked(v);
		final String url = mPics.get(mViewPager.getCurrentItem());
		if(!TextUtils.isEmpty(url)){
			XApplication.getImageLoader().loadImage(url, new ImageLoadingListener() {
				@Override
				public void onLoadingStarted(String arg0, View arg1) {
				}
				@Override
				public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
				}
				
				@Override
				public void onLoadingComplete(String arg0, View arg1, final Bitmap bmp) {
					showXProgressDialog();
					new Thread(){
						@Override
						public void run() {
							Media.insertImage(getContentResolver(), bmp, "title", "xiangyu");
							XApplication.getMainThreadHandler().post(new Runnable() {
								@Override
								public void run() {
									mToastManager.show(R.string.save_success);
									dismissXProgressDialog();
								}
							});
						}
					}.start();
				}
				@Override
				public void onLoadingCancelled(String arg0, View arg1) {
				}
			});
		}
	}
	
	@Override
	public void onProgressUpdate(String imageUri, View view, int current, int total) {
		ProgressBar pb = findProgressBar(imageUri);
		if(pb != null){
			pb.setMax(total);
			pb.setProgress(current < 2 ? 2 : current);
		}
	}
	
	@Override
	public void onLoadingStarted(final String imageUri, View view) {
		mViewPager.post(new Runnable() {
			@Override
			public void run() {
				ProgressBar pb = findProgressBar(imageUri);
				if(pb != null){
					pb.setProgress(2);
					pb.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	@Override
	public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
		ProgressBar pb = findProgressBar(imageUri);
		if(pb != null){
			pb.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoadingComplete(final String imageUri, View view, Bitmap loadedImage) {
		mViewPager.post(new Runnable() {
			@Override
			public void run() {
				ProgressBar pb = findProgressBar(imageUri);
				if(pb != null){
					pb.setVisibility(View.GONE);
				}
			}
		});
	}

	@Override
	public void onLoadingCancelled(String imageUri, View view) {
	}
	
	private ProgressBar findProgressBar(String imageUrl){
		int index = mPics.indexOf(imageUrl);
		CommonPagerAdapter adapter = (CommonPagerAdapter)mAdapter;
		View v = adapter.findViewFromPos(index);
		if(v != null){
			ProgressBar pb = (ProgressBar)v.findViewById(R.id.pb);
			return pb;
		}
		return null;
	}
	
	private class MyAdapter extends CommonPagerAdapter{
		
		@Override
		protected View getView(View v, int nPos,ViewGroup parent) {
			v = SystemUtils.inflate(parent.getContext(), R.layout.xlibrary_adapter_imlookphoto_item);
			ImageView iv = (ImageView)v.findViewById(R.id.ivPhoto);
			v.findViewById(R.id.pb).setVisibility(View.GONE);
			final String url = mPics.get(nPos);
			XApplication.getImageLoader().displayImage(url, iv, mOptions,
					LookPhotosActivity.this,LookPhotosActivity.this);
			
			return v;
		}

		@Override
		public int getCount() {
			return mPics.size();
		}
	}
}
