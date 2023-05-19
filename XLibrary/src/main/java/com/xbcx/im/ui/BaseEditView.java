package com.xbcx.im.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

public class BaseEditView extends FrameLayout {

	protected List<PullUpViewWrapper> 	mListPullUpViewWrapper = new ArrayList<PullUpViewWrapper>();
	
	protected InputMethodManager		mInputMethodManager;
	protected boolean					mInputMethodVisible;
	
	protected EditText 					mEditText;
	
	private Runnable mRunnableDelayShowInputMethod = new Runnable() {
		public void run() {
			if(!hasPullUpViewVisible()){
				mEditText.setFocusableInTouchMode(true);
				mEditText.requestFocus();
				mInputMethodManager.showSoftInput(mEditText, 0);
			}
		}
	};
	
	private Runnable mRunnableDelaySetInputMethodVisible = new Runnable() {
		@Override
		public void run() {
			mInputMethodVisible = false;
		}
	};
	
	public BaseEditView(Context context) {
		super(context);
		init();
	}
	
	public BaseEditView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init()	{
		mInputMethodManager = (InputMethodManager)getContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		for(PullUpViewWrapper wrapper : mListPullUpViewWrapper){
			removeCallbacks(wrapper);
		}
//		mListPullUpViewWrapper.clear();
	}
	
	public void destroy(){
		for(PullUpViewWrapper wrapper : mListPullUpViewWrapper){
			removeCallbacks(wrapper);
		}
		mListPullUpViewWrapper.clear();
	}
	
	public void addPullUpView(View view){
		mListPullUpViewWrapper.add(new PullUpViewWrapper(view));
	}
	
	public void hideInputMethod(){
		removeCallbacks(mRunnableDelayShowInputMethod);
		mEditText.setFocusableInTouchMode(false);
		mEditText.clearFocus();
		mInputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
		postDelayed(mRunnableDelaySetInputMethodVisible, 100);
	}
	
	public void showInputMethod(){
		mInputMethodVisible = true;
		removeCallbacks(mRunnableDelaySetInputMethodVisible);
		
		boolean bHasPullUpViewVisible = false;
		for(PullUpViewWrapper wrapper : mListPullUpViewWrapper){
			if(wrapper.mVisible){
				bHasPullUpViewVisible = true;
				wrapper.hide(true);
				postDelayed(mRunnableDelayShowInputMethod, 200);
				break;
			}else{
				removeCallbacks(wrapper.mRunnableDelayShow);
			}
		}
		
		if(!bHasPullUpViewVisible){
			mEditText.setFocusableInTouchMode(true);
			mEditText.requestFocus();
			mInputMethodManager.showSoftInput(mEditText, 0);
		}
	}
	
	public void hideAllPullUpView(boolean bAnim){
		for(PullUpViewWrapper wrapper : mListPullUpViewWrapper){
			wrapper.hide(bAnim);
		}
	}
	
	public void hidePullUpView(View view,boolean bAnim){
		for(PullUpViewWrapper wrapper : mListPullUpViewWrapper){
			if(wrapper.mView == view){
				wrapper.hide(bAnim);
				break;
			}
		}
	}
	
	public void showPullUpview(View view){
		if(mInputMethodVisible){
			hideInputMethod();
			PullUpViewWrapper wrapper = findWrapper(view);
			if(wrapper != null){
				wrapper.show(true);
			}
		}else{
			boolean bDelay = false;
			for(PullUpViewWrapper wrapper : mListPullUpViewWrapper){
				if(wrapper.mVisible && wrapper.mView != view){
					wrapper.hide(true);
					bDelay = true;
				}
			}
			
			PullUpViewWrapper wrapper = findWrapper(view);
			if(wrapper != null){
				wrapper.show(bDelay);
			}
		}
	}
	
	protected PullUpViewWrapper findWrapper(View view){
		for(PullUpViewWrapper wrapper : mListPullUpViewWrapper){
			if(wrapper.mView == view){
				return wrapper;
			}
		}
		return null;
	}
	
	protected boolean	isPullUpViewVisible(View view){
		PullUpViewWrapper wrapper = findWrapper(view);
		return wrapper == null ? false : wrapper.mVisible;
	}
	
	protected boolean	hasPullUpViewVisible(){
		for(PullUpViewWrapper wrapper : mListPullUpViewWrapper){
			if(wrapper.mVisible){
				return true;
			}
		}
		return false;
	}

	protected class PullUpViewWrapper implements Runnable{
		public final View 	mView;
		
		public boolean		mVisible;
		
		public int			mHeight;
		
		public Scroller 	mScroller = new Scroller(getContext());
		
		public Runnable 	mRunnableDelayShow = new Runnable() {
			@Override
			public void run() {
				if(!mInputMethodVisible){
					show(false);
				}
			}
		};
		
		public PullUpViewWrapper(View view){
			mView = view;
			measureView(mView);
			mHeight = mView.getMeasuredHeight();
			mVisible = true;
		}
		
		public void show(boolean bDelay){
			if(bDelay){
				postDelayed(mRunnableDelayShow, 200);
			}else{
				mVisible = true;
				mScroller.startScroll(0, mView.getHeight(), 0, mHeight - mView.getHeight());
				post(this);
			}
		}
		
		public void hide(boolean bAnim){
			removeCallbacks(mRunnableDelayShow);
			if (mVisible) {
				mVisible = false;
				if (bAnim) {
					mScroller.startScroll(0, mView.getHeight(), 0, 0 - mView.getHeight());
					post(this);
				} else {
					removeCallbacks(this);
					ViewGroup.LayoutParams lp = mView.getLayoutParams();
					lp.height = 0;
					mView.setLayoutParams(lp);
				}
			}
		}

		@Override
		public void run() {
			if(mVisible){
				removeCallbacks(mRunnableDelayShowInputMethod);
				mInputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
				mInputMethodVisible = false;
			}
			if(mScroller.computeScrollOffset()){
				ViewGroup.LayoutParams lp = mView.getLayoutParams();
				lp.height = mScroller.getCurrY();
				mView.setLayoutParams(lp);
				post(this);
			}
		}
		
		private void measureView(View v){
			ViewGroup.LayoutParams p = v.getLayoutParams();
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
	        v.measure(nWidthSpec, nHeightSpec);
		}
	}
}
