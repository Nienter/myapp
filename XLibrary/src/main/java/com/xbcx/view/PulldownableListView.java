package com.xbcx.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListView;
import android.widget.Scroller;

public abstract class PulldownableListView extends ListView{

	protected 	View mPullDownView;
	
	protected   float 	mDisYStartPullDown;
	protected   boolean mCanRun = true;
	protected 	OnPullDownListener mOnPullDownListener;
	
	protected 	boolean mIsRunning;
	
	protected int mPullDownViewPaddingTop;
	protected int mPullDownViewPaddingTopMax;
	protected int mPullDownViewPaddingTopMin;
	
	protected boolean mIsVerticalScrollBarEnabled;
	
	protected boolean mIsTouching;
	protected boolean mCanHandlePullDown;
	protected boolean mIsStartPullDown;
	protected float	  mTouchLastY;
	protected float   mTouchDisYTotal;
	
	protected Scroller mScroller;
	
	protected Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			if(mScroller.computeScrollOffset()){
				setPullDownViewPaddingTop(mScroller.getCurrX());
				post(this);
			}
		}
	};
	
	public PulldownableListView(Context context) {
		super(context);
		init();
	}
	
	public PulldownableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init(){
		mScroller = new Scroller(getContext());
		
		mPullDownView = onCreatePullDownView();
		
		addHeaderView(mPullDownView);
		
		measurePullDownView(mPullDownView);
		
		mPullDownViewPaddingTop = mPullDownView.getPaddingTop();
		
		final int viewHeight = mPullDownView.getMeasuredHeight();
		final int viewHeightMax = viewHeight + getPullDownBeyondHeight();
		mPullDownViewPaddingTopMax = mPullDownViewPaddingTop + viewHeightMax - viewHeight;
		mPullDownViewPaddingTopMin = mPullDownViewPaddingTop - viewHeight;
		
		setPullDownViewPaddingTop(mPullDownViewPaddingTopMin);
		
		setHeaderDividersEnabled(false);
	}
	
	public void setCanRun(boolean bCan){
		mCanRun = bCan;
		if(bCan){
			mPullDownView.setVisibility(View.VISIBLE);
		}else{
			mPullDownView.setVisibility(View.GONE);
		}
	}
	
	public void setOnPullDownListener(OnPullDownListener listener){
		mOnPullDownListener = listener;
	}
	
	public void startRun(){
		if(!mIsRunning){
			startChangePullDownViewPaddingTop(mPullDownViewPaddingTop);
		}
	}
	
	public void endRun(){
		if(mIsRunning){
			mIsRunning = false;
			hidePullDownView();
			onEndRun();
		}
	}
	
	protected abstract View onCreatePullDownView();
	
	protected abstract int	getPullDownBeyondHeight();
	
	protected abstract void onStartRun();
	
	protected abstract void onEndRun();
	
	public int getPullDownViewHeight(){
		return mPullDownViewPaddingTop - mPullDownViewPaddingTopMin;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(!mScroller.isFinished()){
			return super.onInterceptTouchEvent(ev);
		}
		if(!mIsRunning && ev.getAction() == MotionEvent.ACTION_DOWN){
			if(getFirstVisiblePosition() == 0 && !mCanHandlePullDown){
				mCanHandlePullDown = true;
				mTouchLastY = ev.getY();
			}
		}else if(ev.getAction() == MotionEvent.ACTION_CANCEL ||
				ev.getAction() == MotionEvent.ACTION_UP ||
				ev.getAction() == MotionEvent.ACTION_OUTSIDE){
			mIsTouching = false;
			
			mCanHandlePullDown = false;
			mTouchDisYTotal = 0;
			if(mIsStartPullDown){
				mIsStartPullDown = false;
				setVerticalScrollBarEnabled(mIsVerticalScrollBarEnabled);
				if(ev.getAction() == MotionEvent.ACTION_UP){
					if(mCanRun){
						if(mPullDownView.getPaddingTop() <= mPullDownViewPaddingTop){
							hidePullDownView();
						}else{
							startRun();
						}
					}else{
						hidePullDownView();
					}
				}else{
					setPullDownViewPaddingTop(mPullDownViewPaddingTopMin);
				}
				
				return true;
			}
		}
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final float fTouchCurY = ev.getY();
		final int nAction = ev.getAction();
		if(!mScroller.isFinished()){
			return super.onTouchEvent(ev);
		}
		if(nAction == MotionEvent.ACTION_MOVE){
			mIsTouching = true;
			if(mCanHandlePullDown && !mIsStartPullDown){
				float fDisY = (fTouchCurY - mTouchLastY) / 2.0f;
				mTouchLastY = fTouchCurY;
				if(fDisY > 0){
					mTouchDisYTotal += fDisY;
					if(mTouchDisYTotal > mDisYStartPullDown){
						mIsVerticalScrollBarEnabled = isVerticalScrollBarEnabled();
						setVerticalScrollBarEnabled(false);
						ViewParent viewParent = getParent();
						if(viewParent != null){
							viewParent.requestDisallowInterceptTouchEvent(true);
						}
						
						ev.setAction(MotionEvent.ACTION_CANCEL);
						super.onTouchEvent(ev);
						mIsStartPullDown = true;
						return true;
					}else{
						return true;
					}
				}else if(fDisY < 0){
					mTouchDisYTotal = 0;
					mCanHandlePullDown = false;
				}
			}
			if(mIsStartPullDown){
				float fDisY = (fTouchCurY - mTouchLastY) / 2.0f;
				mTouchLastY = fTouchCurY;
				
				int nPaddingTopLast = mPullDownView.getPaddingTop();
				int nPaddingTop = (int)(nPaddingTopLast + fDisY);
				if(fDisY > 0){
					if(nPaddingTopLast < mPullDownViewPaddingTopMax){
						setPullDownViewPaddingTop(nPaddingTop);
					}
				}else if(fDisY < 0){
					if(nPaddingTopLast > mPullDownViewPaddingTopMin){
						setPullDownViewPaddingTop(nPaddingTop);
					}
				}
				
				return true;
			}
		}else if(nAction == MotionEvent.ACTION_CANCEL ||
				nAction == MotionEvent.ACTION_UP ||
				nAction == MotionEvent.ACTION_OUTSIDE){
			mIsTouching = false;
			
			mCanHandlePullDown = false;
			mTouchDisYTotal = 0;
			if(mIsStartPullDown){
				mIsStartPullDown = false;
				setVerticalScrollBarEnabled(mIsVerticalScrollBarEnabled);
				if(nAction == MotionEvent.ACTION_UP){
					if(mCanRun){
						if(mPullDownView.getPaddingTop() <= mPullDownViewPaddingTop){
							hidePullDownView();
						}else{
							startRun();
						}
					}else{
						hidePullDownView();
					}
				}else{
					/*mPullDownView.setPadding(mPullDownView.getPaddingLeft(),
							mPullDownViewPaddingTopMin,
							mPullDownView.getPaddingRight(), 
							mPullDownView.getPaddingBottom());*/
					setPullDownViewPaddingTop(mPullDownViewPaddingTopMin);
				}
				
				return true;
			}
		}
		return super.onTouchEvent(ev);
	}
	
	protected void startChangePullDownViewPaddingTop(int nPaddingTopDst){
		int nPaddingTopCur = mPullDownView.getPaddingTop();
		mScroller.startScroll(nPaddingTopCur, 0, nPaddingTopDst - nPaddingTopCur, 0);
		post(mRunnable);
	}
	
	protected void hidePullDownView(){
		startChangePullDownViewPaddingTop(mPullDownViewPaddingTopMin);
	}

	protected void setPullDownViewPaddingTop(int nPadding){
		if(nPadding > mPullDownViewPaddingTopMax){
			nPadding = mPullDownViewPaddingTopMax;
		}else if(nPadding < mPullDownViewPaddingTopMin){
			nPadding = mPullDownViewPaddingTopMin;
		}
		int nPaddingTopCur = mPullDownView.getPaddingTop();
		if(nPaddingTopCur != nPadding){
			mPullDownView.setPadding(mPullDownView.getPaddingLeft(),
					nPadding, 
					mPullDownView.getPaddingRight(), 
					mPullDownView.getPaddingBottom());
			
			if(!mIsTouching){
				if(mPullDownView.getPaddingTop() == mPullDownViewPaddingTop){
					mIsRunning = true;
					onStartRun();
					if(mOnPullDownListener != null){
						mOnPullDownListener.onStartRun(this);
					}
				}
			}
			
			onPullDownHeightChanged(nPadding, nPaddingTopCur);
		}
	}
	
	protected void onPullDownHeightChanged(int nNewHeight,int nOldHeight){
	}
	
	public static void measurePullDownView(View view) {
        ViewGroup.LayoutParams p = view.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
       
        int nWidthSpec = ViewGroup.getChildMeasureSpec(0,
                0 + 0, p.width);
        int nHeight = p.height;
       
        int nHeightSpec;
        if (nHeight > 0) {
            nHeightSpec = MeasureSpec.makeMeasureSpec(nHeight, MeasureSpec.EXACTLY); 
        } else {
            nHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        view.measure(nWidthSpec, nHeightSpec);
    }
	
	public static interface OnPullDownListener{
		public void onStartRun(PulldownableListView view);
	}
}
