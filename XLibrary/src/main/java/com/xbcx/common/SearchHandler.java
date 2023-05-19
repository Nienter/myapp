package com.xbcx.common;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.common.pulltorefresh.PullToRefreshPlugin;
import com.xbcx.core.BaseActivity;
import com.xbcx.utils.SystemUtils;

import java.util.HashMap;

public class SearchHandler implements TextWatcher,Runnable{

	private BaseActivity						mActivity;
	
	private PullToRefreshPlugin<?> 				mPullToRefreshPlugin;
	private SearchInterface						mSearchInterface;
	private SearchHttpValueProvider				mSearchHttpValueProvider;
	private EditText							mSearchEditText;
	
	private boolean								mAutoSearch = true;
	private boolean								mClearWhenEmptyKey = true;
	
	private SetBaseAdapter<?> 					mSearchAdapter;
	
	private OnSearchListener					mOnSearchListener;
	
	public SearchHandler(PullToRefreshPlugin<?> pp,SearchInterface si,EditText et){
		mPullToRefreshPlugin = pp;
		mSearchInterface = si;
		mSearchEditText = et;
		et.addTextChangedListener(this);
	}
	
	public void setActivity(BaseActivity activity){
		mActivity = activity;
	}
	
	public SearchHandler setSearchHttpValueProvider(SearchHttpValueProvider provider){
		mSearchHttpValueProvider = provider;
		return this;
	}
	
	public SearchHandler setOnSearchListener(OnSearchListener listener){
		mOnSearchListener = listener;
		return this;
	}
	
	public SearchHandler setAutoSearch(boolean bAuto){
		mAutoSearch = bAuto;
		return this;
	}
	
	public SearchHandler setClearWhenEmptyKey(boolean b){
		mClearWhenEmptyKey = b;
		return this;
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if(mAutoSearch){
			mSearchEditText.removeCallbacks(this);
			mSearchEditText.postDelayed(this, 500);
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
	}
	
	@Override
	public void run() {
		startSearch();
	}
	
	public void startSearch(){
		if(mSearchEditText != null){
			mPullToRefreshPlugin.clearRefreshEvents();
			mPullToRefreshPlugin.cancelLoadMore();
			if(mPullToRefreshPlugin.getEndlessAdapter() != null){
				mPullToRefreshPlugin.getEndlessAdapter().hideBottomView();
			}
			final String s = SystemUtils.getTrimText(mSearchEditText);
			if(mClearWhenEmptyKey && TextUtils.isEmpty(s)){
				getSearchAdapter().clear();
			}else{
				mPullToRefreshPlugin.addRefreshEvent(
						mActivity.pushEvent(
								mSearchInterface.getSearchEventCode(), 
								buildSearchHttpValue(s)));
			}
			if(mOnSearchListener != null){
				mOnSearchListener.onStartSearch(s);
			}
		}
	}
	
	public SetBaseAdapter<?> getSearchAdapter(){
		if(mSearchAdapter == null){
			mSearchAdapter = mSearchInterface.createSearchAdapter();
		}
		return mSearchAdapter;
	}
	
	public String getSearchEventCode(){
		return mSearchInterface.getSearchEventCode();
	}
	
	public Object buildSearchHttpValue(){
		final String s = SystemUtils.getTrimText(mSearchEditText);
		return buildSearchHttpValue(s);
	}
	
	public Object buildSearchHttpValue(String s){
		return mSearchHttpValueProvider == null ?
				s : mSearchHttpValueProvider.buildSearchHttpValues(s);
	}
	
	public static interface OnSearchListener{
		public void onStartSearch(String searchKey);
	}
	
	public static interface SearchInterface{
		public String 				getSearchEventCode();
		
		public SetBaseAdapter<?> 	createSearchAdapter();
	}
	
	public static interface SearchHttpValueProvider{
		public Object 	buildSearchHttpValues(String searchKey);
	}
	
	public static class SimpleSearchHttpValueProvider implements SearchHttpValueProvider{
		
		protected String	mSearchHttpKey;
		
		public SimpleSearchHttpValueProvider setSearchHttpKey(String key){
			mSearchHttpKey = key;
			return this;
		}
		
		@Override
		public Object buildSearchHttpValues(String searchKey) {
			if(TextUtils.isEmpty(mSearchHttpKey)){
				return searchKey;
			}else{
				HashMap<String, String> values = new HashMap<String, String>();
				values.put(mSearchHttpKey, searchKey);
				return values;
			}
		}
	}
}
