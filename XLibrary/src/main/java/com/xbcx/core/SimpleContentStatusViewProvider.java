package com.xbcx.core;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

public abstract class SimpleContentStatusViewProvider implements ContentStatusViewProvider {

	protected	Context					mContext;
	
	protected	View					mViewFail;
	protected 	TextView				mTextViewFail;
	protected 	CharSequence			mFailText;
	
	protected	View					mViewNoResult;
	protected 	TextView				mTextViewNoResult;
	protected 	CharSequence			mNoResultText;
	
	protected	TopMarginProvider		mTopMarginProvider;
	
	protected	NoResultViewProvider	mNoResultViewProvider;
	
	public SimpleContentStatusViewProvider(Context context){
		mContext = context;
	}
	
	@Override
	public void setFailText(CharSequence text){
		mFailText = text;
		if(mTextViewFail != null){
			mTextViewFail.setText(mFailText);
		}
	}
	
	public void setNoResultViewProvider(NoResultViewProvider provider){
		mNoResultViewProvider = provider;
	}
	
	@Override
	public void showFailView(){
		hideNoResultView();
		if(mViewFail == null){
			mViewFail = SystemUtils.inflate(mContext,R.layout.xlibrary_view_fail);
			mTextViewFail = (TextView)mViewFail.findViewById(R.id.tvFail);
			setFailText(mFailText);
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, 
					ViewGroup.LayoutParams.MATCH_PARENT);
			lp.gravity = Gravity.TOP;
			addContentView(mViewFail, lp);
			calculateTopMargin(mViewFail);
		}else{
			mViewFail.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void hideFailView(){
		if(mViewFail != null){
			mViewFail.setVisibility(View.GONE);
		}
	}
	
	@Override
	public boolean isFailViewVisible(){
		if(mViewFail != null){
			return mViewFail.getVisibility() == View.VISIBLE;
		}
		return false;
	}
	
	@Override
	public CharSequence getFailText() {
		return mFailText;
	}
	
	@Override
	public void setNoResultText(CharSequence text){
		mNoResultText = text;
		if(mTextViewNoResult != null){
			mTextViewNoResult.setText(mNoResultText);
		}
	}
	
	@Override
	public boolean hasSetNoResultText(){
		return !TextUtils.isEmpty(mNoResultText);
	}
	
	@Override
	public CharSequence getNoResultText() {
		return mNoResultText;
	}
	
	public void showNoResultView(){
		hideFailView();
		if(mViewNoResult == null){
			if(mNoResultViewProvider == null){
				mNoResultViewProvider = new SimpleNoResultViewProvider();
			}
			mViewNoResult = mNoResultViewProvider.createNoResultView(mContext);
			mTextViewNoResult = (TextView)mViewNoResult.findViewById(R.id.tv);
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, 
					ViewGroup.LayoutParams.MATCH_PARENT);
			lp.gravity = Gravity.BOTTOM;
			mTextViewNoResult.setText(mNoResultText);
			addContentView(mViewNoResult, lp);
			calculateTopMargin(mViewNoResult);
		}else{
			mViewNoResult.setVisibility(View.VISIBLE);
		}
	}
	
	public void calculateTopMargin(View view){
		MarginLayoutParams mlp = (MarginLayoutParams)view.getLayoutParams();
		if(mlp != null){
			mlp.topMargin = getTopMargin(view);
			view.setLayoutParams(mlp);
		}
	}
	
	@Override
	public void setTopMarginProvider(TopMarginProvider provider) {
		mTopMarginProvider = provider;
	}
	
	public int getTopMargin(View statusView){
		if(mTopMarginProvider == null){
			return 0;
		}
		return mTopMarginProvider.getTopMargin(statusView);
	}
	
	public void hideNoResultView(){
		if(mViewNoResult != null){
			mViewNoResult.setVisibility(View.GONE);
		}
	}
	
	public boolean isNoResultViewVisible(){
		return mViewNoResult != null && mViewNoResult.getVisibility() == View.VISIBLE;
	}

	public static interface NoResultViewProvider{
		public View createNoResultView(Context context);
	}
	
	public static class SimpleNoResultViewProvider implements NoResultViewProvider{

		@Override
		public View createNoResultView(Context context) {
			return SystemUtils.inflate(context,R.layout.xlibrary_view_no_search_result);
		}
		
	}
}
