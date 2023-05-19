package com.xbcx.view;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.xbcx.library.R;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PullToRefreshListView extends PulldownableListView{
	
	private static final SimpleDateFormat DATEFORMAT_YMDHM = new SimpleDateFormat("yyyy-M-d H:mm",Locale.getDefault());
	
	private ProgressBar mProgressBarRefresh;
	private ImageView mImageViewArrow;
	private TextView mTextViewRefresh;
	private TextView mTextViewTime;
	
	private Date mDateRefreshLast;
	
	private RotateAnimation mRotateAnimationFlip;
	private RotateAnimation mRotateAnimationReverseFlip;
	private boolean mPullToRefresh;
	
	public PullToRefreshListView(Context context) {
		super(context);
		init();

	}

	public PullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init(){
		mRotateAnimationFlip = new RotateAnimation(0, -180.0f, 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		mRotateAnimationFlip.setDuration(250);
		mRotateAnimationFlip.setFillAfter(true);
		mRotateAnimationFlip.setFillEnabled(true);
		
		mRotateAnimationReverseFlip = new RotateAnimation(-180.0f, 0,
				Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		mRotateAnimationReverseFlip.setDuration(250);
		mRotateAnimationReverseFlip.setFillAfter(true);
		mRotateAnimationReverseFlip.setFillEnabled(true);
		
		setHorizontalScrollBarEnabled(false);
		setHeaderDividersEnabled(false);
		
		setCacheColorHint(0x00000000);
		setSelector(new ColorDrawable(0x00000000));
	}


	@Override
	protected View onCreatePullDownView() {
		Context context = getContext();
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View refreshView = layoutInflater.inflate(R.layout.xlibrary_refreshview, null);
		
		mProgressBarRefresh = (ProgressBar)refreshView.findViewById(R.id.pbRefresh);
		mImageViewArrow = (ImageView)refreshView.findViewById(R.id.ivPullArrow);
		mTextViewRefresh = (TextView)refreshView.findViewById(R.id.tvRefresh);
		mTextViewTime = (TextView)refreshView.findViewById(R.id.tvTime);
		
		mProgressBarRefresh.setVisibility(View.GONE);
		
		return refreshView;
	}

	@Override
	protected int getPullDownBeyondHeight() {
		return 60;
	}

	@Override
	protected void onStartRun() {
		mProgressBarRefresh.setVisibility(View.VISIBLE);
		mTextViewRefresh.setText(getContext().getString(R.string.pull_refresh_refreshing));
		mImageViewArrow.clearAnimation();
		mImageViewArrow.setVisibility(View.GONE);
	}
	
	@Override
	protected void onEndRun() {
		mProgressBarRefresh.setVisibility(View.GONE);
		mImageViewArrow.setVisibility(View.VISIBLE);
		mPullToRefresh = false;
		
		mDateRefreshLast = new Date();
		mTextViewTime.setText(getContext().getString(R.string.pull_refresh_last_update) + 
				DATEFORMAT_YMDHM.format(mDateRefreshLast));
	}
	
	public void onPullDownHeightChanged(int nPaddingTop,int nPaddingTopOld){
		if(mIsRunning){
			return;
		}
		if(nPaddingTop < mPullDownViewPaddingTop){
			mTextViewRefresh.setText(getContext().getString(R.string.pull_refresh));
			if(mPullToRefresh){
				mImageViewArrow.clearAnimation();
				mImageViewArrow.startAnimation(mRotateAnimationReverseFlip);
				mPullToRefresh = false;
			}
		}else{
			mTextViewRefresh.setText(getContext().getString(R.string.pull_refresh_release_refresh));
			if(!mPullToRefresh){
				mPullToRefresh = true;
				mImageViewArrow.clearAnimation();
				mImageViewArrow.startAnimation(mRotateAnimationFlip);
			}
		}
	}
}
