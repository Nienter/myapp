package com.xbcx.core;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.xbcx.adapter.AdapterWrapper;
import com.xbcx.adapter.AnimatableAdapter;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

public class XEndlessAdapter extends AdapterWrapper implements 
											View.OnClickListener,
											AnimatableAdapter{

	protected boolean			mUseLoadMore = true;
	
	protected View 				mPendingView = null;
	protected View				mProgressBar;
	protected TextView			mTextView;
	
	private boolean				mIsAutoLoad = true;
	private boolean				mIsClickLoadEnable = true;
	private boolean				mIsLoading;
	private boolean				mIsLoadFail;
	
	private int					mNoMoreResId = R.string.bottom_load_nomore;
	private int					mLoadMoreResId = R.string.bottom_load_loadmore;
	
	private boolean				mHasMore;
	
	private OnLoadMoreListener	mOnLoadMoreListener;
	
	private AnimatableAdapter	mAnimatableAdapter;
	
	private boolean				mIsVisible = true;
	private boolean				mIsLoadMoreViewEnabled;

	public XEndlessAdapter(Context context,ListAdapter wrapped) {
		this(context,wrapped,R.layout.xlibrary_footer_bottomload);
	}
	
	public XEndlessAdapter(Context context,ListAdapter wrapped,int layoutId) {
		super(wrapped);
		mPendingView = SystemUtils.inflate(context, layoutId);
		mProgressBar = mPendingView.findViewById(R.id.pb);
		mTextView = (TextView)mPendingView.findViewById(R.id.tv);
		mPendingView.setOnClickListener(this);
		mProgressBar.setVisibility(View.GONE);
	}
	
	public void setUseLoadMore(boolean bUse){
		mUseLoadMore = bUse;
		notifyDataSetChanged();
	}
	
	public void setClickLoadEnable(boolean bEnable){
		mIsClickLoadEnable = bEnable;
	}
	
	public void setOnLoadMoreListener(OnLoadMoreListener listener){
		mOnLoadMoreListener = listener;
	}
	
	public XEndlessAdapter setNoMoreResId(int resId){
		mNoMoreResId = resId;
		return this;
	}
	
	public XEndlessAdapter setIsLoadMoreViewEnabled(boolean bEnabled){
		mIsLoadMoreViewEnabled = bEnabled;
		return this;
	}
	
	public XEndlessAdapter setLoadMoreResId(int resId){
		mLoadMoreResId = resId;
		return this;
	}
	
	public void setVisible(boolean bVisible){
		mIsVisible = bVisible;
		notifyDataSetChanged();
	}
	
	public boolean isVisible(){
		return mIsVisible;
	}
	
	public void setHasMore(boolean bHasMore){
		mHasMore = bHasMore;
		mPendingView.setVisibility(View.VISIBLE);
		if(bHasMore){
			mTextView.setText(mLoadMoreResId);
		}else{
			mTextView.setText(mNoMoreResId);
		}
	}
	
	public void endLoad(){
		mIsLoading = false;
		mProgressBar.setVisibility(View.GONE);
	}
	
	public void hideBottomView(){
		mPendingView.setVisibility(View.INVISIBLE);
		mHasMore = false;
	}
	
	public void showBottomView(){
		mPendingView.setVisibility(View.VISIBLE);
	}
	
	public void setText(String text){
		mTextView.setText(text);
	}
	
	public void setLoadFail(){
		mPendingView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
		mTextView.setText(R.string.bottom_load_fail);
		mIsLoadFail = true;
		mIsLoading = false;
	}
	
	public boolean hasMore(){
		return mHasMore;
	}
	
	public void setTextColor(int color){
		mTextView.setTextColor(color);
	}
	
	public void setIsAutoLoad(boolean bAuto){
		mIsAutoLoad = bAuto;
		if(mIsAutoLoad){
			mProgressBar.setVisibility(View.VISIBLE);
			mTextView.setText(mLoadMoreResId);
		}else{
			if(!mIsLoading){
				mProgressBar.setVisibility(View.GONE);
				mTextView.setText(mLoadMoreResId);
			}
		}
	}

	@Override
	public int getCount() {
		if(mIsVisible){
			if(mUseLoadMore){
				return mWrappedAdapter.getCount() + 1;
			}else{
				return mWrappedAdapter.getCount();
			}
		}else{
			return 1;
		}
	}
	
	public int getItemViewType(int position) {
		if (mIsVisible){
			if (position == super.getCount()) {
				return super.getViewTypeCount();
			}

			return super.getItemViewType(position);
		}else{
			return super.getViewTypeCount() + 1;
		}
	}

	public int getViewTypeCount() {
		return super.getViewTypeCount() + 2;
	}

	@Override
	public Object getItem(int position) {
		if (position >= super.getCount()) {
			return (null);
		}
		return super.getItem(position);
	}

	@Override
	public boolean isEnabled(int position) {
		if (position >= super.getCount()) {
			return (mIsLoadMoreViewEnabled);
		}

		return super.isEnabled(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(mIsVisible){
			if (position == super.getCount()) {
				if(mIsAutoLoad && !mIsLoading && mHasMore){
					startLoadMore();
				}
				return (mPendingView);
			}

			return super.getView(position, convertView, parent);
		}else{
			if(convertView == null){
				convertView = new View(parent.getContext());
			}
			return convertView;
		}
	}

	protected void startLoadMore(){
		mIsLoading = true;
		mIsLoadFail = false;
		mProgressBar.setVisibility(View.VISIBLE);
		mTextView.setText(R.string.bottom_load_loading);
		if(mOnLoadMoreListener != null){
			mOnLoadMoreListener.onStartLoadMore(this);
		}
	}
	
	@Override
	public void onClick(View v) {
		if(v == mPendingView){
			if(mIsClickLoadEnable){
				if(!mIsAutoLoad || mIsLoadFail){
					if(!mIsLoading && mHasMore){
						startLoadMore();
					}
				}
			}
		}
	}

	public static interface OnLoadMoreListener{
		public void onStartLoadMore(XEndlessAdapter adapter);
	}

	@Override
	public void playAddAnimation(int pos, BaseAdapter adapter) {
		if(mAnimatableAdapter != null){
			mAnimatableAdapter.playAddAnimation(pos, this);
		}
	}

	@Override
	public void playRemoveAnimation(int pos, BaseAdapter adapter) {
		if(mAnimatableAdapter != null){
			mAnimatableAdapter.playRemoveAnimation(pos, this);
		}
	}

	@Override
	public void setAnimatableAdapter(AnimatableAdapter adapter) {
		mAnimatableAdapter = adapter;
		if(mAnimatableAdapter != null){
			if(mWrappedAdapter instanceof AnimatableAdapter){
				final AnimatableAdapter anim = (AnimatableAdapter)mWrappedAdapter;
				anim.setAnimatableAdapter(this);
			}
		}
	}

	@Override
	public void setAbsListView(AbsListView listView) {
	}
}
