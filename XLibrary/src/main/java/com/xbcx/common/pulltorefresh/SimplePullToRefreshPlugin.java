package com.xbcx.common.pulltorefresh;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.common.CheckNoResultItemObserver;
import com.xbcx.common.DeleteItemActivityEventHandler;
import com.xbcx.common.SearchHandler;
import com.xbcx.common.SearchHandler.SearchHttpValueProvider;
import com.xbcx.common.SearchHandler.SearchInterface;
import com.xbcx.common.pulltorefresh.PullToRefreshPlugin.PullToRefeshStatusListener;
import com.xbcx.common.pulltorefresh.PullToRefreshPlugin.PullToRefreshListener;
import com.xbcx.core.ActivityPlugin;
import com.xbcx.core.Event;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.XEndlessAdapter;
import com.xbcx.core.XEndlessAdapter.OnLoadMoreListener;
import com.xbcx.core.http.HttpMapValueBuilder;
import com.xbcx.utils.SystemUtils;

public class SimplePullToRefreshPlugin extends ActivityPlugin<BaseActivity> implements
														PullToRefreshListener,
														PullToRefeshStatusListener,
														OnLoadMoreListener,
														OnTouchListener,
														SearchInterface,
														SearchHttpValueProvider{

	private PullToRefreshPlugin<?> 			mPullToRefreshPlugin;
	
	private SetBaseAdapter<?> 				mSetAdapter;
	private String							mEventCode;
	
	private SearchHandler					mSearchHandler;
	private String							mSearchHttpKey;
	private EditText						mSearchEditText;
	
	private LoadEventParamProvider			mParamProvider;
	
	private SparseIntArray					mMapTriggerDeleteEventCode;
	
	public SimplePullToRefreshPlugin(PullToRefreshPlugin<?> prp,SetBaseAdapter<?> adapter){
		mPullToRefreshPlugin = prp
				.setPullToRefreshListener(this)
				.setPullToRefreshStatusListener(this)
				.setOnLoadMoreListener(this)
				.setAdapterEmptyChecker(new SimpleAdapterEmptyChecker(adapter));
		
		mSetAdapter = adapter;
		mSetAdapter.registerItemObserver(new CheckNoResultItemObserver(prp));
	}
	
	@Override
	protected void onAttachActivity(BaseActivity activity) {
		super.onAttachActivity(activity);
		if(mMapTriggerDeleteEventCode != null){
			int size = mMapTriggerDeleteEventCode.size();
			for(int index = 0;index < size;++index){
				int code = mMapTriggerDeleteEventCode.keyAt(index);
				activity.registerActivityEventHandlerEx(code, 
						new DeleteItemActivityEventHandler(mSetAdapter));
			}
			mMapTriggerDeleteEventCode = null;
		}
		if(mSearchHandler != null){
			mSearchHandler.setActivity(activity);
		}
		if(mSearchEditText != null){
			mPullToRefreshPlugin.getListView().setOnTouchListener(this);
		}
	}
	
	public SimplePullToRefreshPlugin setLoadEventCode(int eventCode){
		return setLoadEventCode(String.valueOf(eventCode));
	}
	
	public SimplePullToRefreshPlugin setLoadEventCode(String code){
		mEventCode = code;
		return this;
	}
	
	public SimplePullToRefreshPlugin setSearchEditText(EditText et){
		mSearchHandler = new SearchHandler(mPullToRefreshPlugin, this, et)
			.setSearchHttpValueProvider(this);
		mSearchEditText = et;
		return this;
	}
	
	public SimplePullToRefreshPlugin setSearchHttpKey(String searchHttpKey){
		mSearchHttpKey = searchHttpKey;
		return this;
	}
	
	public SimplePullToRefreshPlugin setLoadEventParamProvider(LoadEventParamProvider provider){
		mParamProvider = provider;
		return this;
	}
	
	public SimplePullToRefreshPlugin bindTriggerDeleteEventCode(int code){
		if(mActivity == null){
			if(mMapTriggerDeleteEventCode == null){
				mMapTriggerDeleteEventCode = new SparseIntArray();
			}
			mMapTriggerDeleteEventCode.put(code, code);
		}else{
			mActivity.registerActivityEventHandlerEx(code, 
					new DeleteItemActivityEventHandler(mSetAdapter));
		}
		return this;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			if(mSearchEditText != null){
				InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
			}
		}
		return false;
	}

	@Override
	public void onPullDownToRefresh() {
		if(!TextUtils.isEmpty(mEventCode)){
			mPullToRefreshPlugin.pushEventRefresh(mEventCode,buildEventParam());
		}
	}

	@Override
	public void onPullUpToRefresh() {
	}
	
	@Override
	public String getSearchEventCode() {
		return mEventCode;
	}
	
	@Override
	public Object buildSearchHttpValues(String searchKey) {
		return buildEventParam();
	}
	
	@Override
	public SetBaseAdapter<?> createSearchAdapter() {
		return mSetAdapter;
	}
	
	public void startSearch(){
		if(mSearchHandler != null){
			mSearchHandler.startSearch();
		}
	}
	
	public SearchHandler getSearchHandler(){
		return mSearchHandler;
	}
	
	@Override
	public void onStartLoadMore(XEndlessAdapter adapter) {
		if(!TextUtils.isEmpty(mEventCode)){
			mPullToRefreshPlugin.pushEventLoad(mEventCode,buildEventParam());
		}
	}
	
	public Object buildEventParam(){
		if(mParamProvider == null){
			if(mSearchEditText != null){
				if(TextUtils.isEmpty(mSearchHttpKey)){
					return SystemUtils.getTrimText(mSearchEditText);
				}else{
					HashMap<String, String> values = new HashMap<String, String>();
					values.put(mSearchHttpKey, SystemUtils.getTrimText(mSearchEditText));
					return values;
				}
			}
			return null;
		}
		return mParamProvider.buildLoadEventParam(this);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onOneRefreshEventEnd(Event event) {
		if(event.isSuccess()){
			if(mSearchEditText != null){
				mActivity.getBaseScreen().dismissAllXProgressDialog();
			}
			mSetAdapter.replaceAll(event.findReturnParam(List.class));
		}
	}

	@Override
	public void onRefreshEventEnd(Event event) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onBottomLoadEventEnd(Event event) {
		if(event.isSuccess()){
			mSetAdapter.addAll(event.findReturnParam(List.class));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onPullUpLoadEventEnd(Event event) {
		if(event.isSuccess()){
			mSetAdapter.addAll(event.findReturnParam(List.class));
		}
	}
	
	public static interface LoadEventParamProvider{
		public Object buildLoadEventParam(SimplePullToRefreshPlugin p);
	}
	
	public static class IdLoadEventParamProvider implements LoadEventParamProvider{
		
		protected String	mId;
		
		protected String	mIdHttpKey = "id";
		
		public IdLoadEventParamProvider(String id){
			mId = id;
		}
		
		public IdLoadEventParamProvider setIdHttpKey(String key){
			mIdHttpKey = key;
			return this;
		}
		
		@Override
		public Object buildLoadEventParam(SimplePullToRefreshPlugin p) {
			return new HttpMapValueBuilder().put(mIdHttpKey, mId).build();
		}
	}
}
