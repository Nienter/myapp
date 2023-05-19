package com.xbcx.common.pulltorefresh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.xbcx.adapter.AnimatableAdapter;
import com.xbcx.core.ActivityPlugin;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.Event;
import com.xbcx.core.ContentStatusViewProvider.TopMarginProvider;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.core.ActivityBasePlugin;
import com.xbcx.core.ContentStatusViewProvider;
import com.xbcx.core.SimpleActivityContentStatusViewProvider;
import com.xbcx.core.XApplication;
import com.xbcx.core.XEndlessAdapter;
import com.xbcx.core.XUIProvider;
import com.xbcx.core.XEndlessAdapter.OnLoadMoreListener;
import com.xbcx.core.XException;
import com.xbcx.core.http.XHttpPagination;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

@SuppressWarnings("rawtypes")
public abstract class PullToRefreshPlugin<T extends BaseActivity> extends ActivityPlugin<T> implements
													OnEventListener,
													XEndlessAdapter.OnLoadMoreListener,
													TopMarginProvider{
	public static long		CreateRefreshDelayTime_Default = 200;
	
	protected 	XEndlessAdapter				mEndlessAdapter;
	
	protected	AnimatableAdapter			mAnimationAdapter;
	
	protected 	XHttpPaginationManager		mXHttpPaginationManager = new SingleXHttpPaginationManager();
	
	protected 	boolean						mIsCreateRefresh = true;
	protected 	boolean						mIsHideViewFirstLoad = XUIProvider.getInstance().isHideViewFirstLoad();
	protected 	boolean						mIsScrollToFirstItemWhenClickTitle = true;
	protected 	boolean						mIsUseDefaultAnimation = XUIProvider.getInstance().useDefaultAnimation();
	protected	long						mCreateRefreshDelayTime = CreateRefreshDelayTime_Default;

	private 	List<Event>					mCurrentRefreshEvents = new ArrayList<Event>();
	private		Event						mCurrentLoadNewEvent;
	private 	Event						mCurrentLoadEvent;
	private 	Event						mCurrentPullUpLoadEvent;
	
	protected 	View						mViewFooter;
	
	protected	ContentStatusViewProvider	mContentStatusViewProvider;
	
	private 	boolean						mIsRequestPullToRefresh;
	private 	boolean						mIsRequestLoadMore;
	
	protected	AdapterCreator						mAdapterCreator;
	protected	OnLoadMoreListener					mOnLoadMoreListener;
	protected	OnItemClickListener					mOnItemClickListener;
	private		PullToRefreshListener				mPullToRefreshListener;
	protected	List<PullToRefeshStatusListener>	mStatusListeners;
	protected	List<PullToRefreshLoadNewListener> 	mLoadNewStatusListeners;
	protected	AdapterEmptyChecker					mAdapterEmptyChecker = new DefaultAdapterEmptyChecker();
	
	public PullToRefreshPlugin<T> setAdapterCreator(AdapterCreator creator){
		mAdapterCreator = creator;
		return this;
	}
	
	public PullToRefreshPlugin<T> setContentStatusViewProvider(ContentStatusViewProvider provider){
		mContentStatusViewProvider = provider;
		return this;
	}
	
	@Override
	protected void onAttachActivity(T activity) {
		super.onAttachActivity(activity);
		
		initContentStatusViewProvider();
		
		createAdapter();
		
		if(mIsScrollToFirstItemWhenClickTitle){
			final TextView tvTitle = mActivity.getBaseScreen().getTextViewTitle();
			if(tvTitle != null){
				tvTitle.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onScrollToFirstItem();
					}
				});
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		XApplication.resumeImageLoader();
	}
	
	protected final void initContentStatusViewProvider(){
		if(mContentStatusViewProvider == null){
			mContentStatusViewProvider = onCreateContentStatusViewProvider();
			mContentStatusViewProvider.setTopMarginProvider(this);
		}
	}
	
	protected ContentStatusViewProvider onCreateContentStatusViewProvider(){
		return new SimpleActivityContentStatusViewProvider(mActivity);
	}
	
	protected void createAdapter(){
		if(mAdapterCreator != null){
			ListAdapter adapter = mAdapterCreator.onCreateAdapter();
			for(AdapterCreatorPlugin p : mActivity.getPlugins(AdapterCreatorPlugin.class)){
				adapter = p.onWrapAdapter(this, adapter);
			}
			mEndlessAdapter = mAdapterCreator.onCreateEndlessAdapter(adapter);
			mEndlessAdapter.setOnLoadMoreListener(this);
			mEndlessAdapter.hideBottomView();
			
			mAnimationAdapter = mAdapterCreator.onCreateAnimationAdapter(mEndlessAdapter);
			mEndlessAdapter.setAnimatableAdapter(mAnimationAdapter);
			
			if(mIsHideViewFirstLoad){
				mEndlessAdapter.setVisible(false);
			}
		}
	}
	
	@Override
	public int getTopMargin(View statusView) {
		final View refreshView = getRefreshView();
		if(statusView.getParent() != refreshView.getParent()){
			Rect r = new Rect();
			refreshView.getGlobalVisibleRect(r);
			Rect rDector = new Rect();
			((ViewGroup)statusView.getParent()).getGlobalVisibleRect(rDector);
			return r.top - rDector.top - (rDector.bottom - r.bottom);
		}
		return 0;
	}
	
	public PullToRefreshPlugin<T> setIsScrollToFirstItemWhenClickTitle(boolean b){
		mIsScrollToFirstItemWhenClickTitle = b;
		return this;
	}
	
	public PullToRefreshPlugin<T> setPullToRefreshStatusListener(PullToRefeshStatusListener listener){
		addPullToRefreshStatusListener(listener);
		return this;
	}
	
	public PullToRefreshPlugin<T> addPullToRefreshStatusListener(PullToRefeshStatusListener listener){
		if(mStatusListeners == null){
			mStatusListeners = new ArrayList<PullToRefreshPlugin.PullToRefeshStatusListener>();
		}
		mStatusListeners.add(listener);
		return this;
	}
	
	public PullToRefreshPlugin<T> addPullToRefreshLoadNewListener(PullToRefreshLoadNewListener listener){
		if(mLoadNewStatusListeners == null){
			mLoadNewStatusListeners = new ArrayList<PullToRefreshPlugin.PullToRefreshLoadNewListener>();
		}
		mLoadNewStatusListeners.add(listener);
		return this;
	}
	
	public PullToRefreshPlugin<T> setPullToRefreshListener(PullToRefreshListener listener){
		mPullToRefreshListener = listener;
		return this;
	}
	
	public PullToRefreshPlugin<T> setOnLoadMoreListener(OnLoadMoreListener listener){
		mOnLoadMoreListener = listener;
		return this;
	}
	
	public void setOnItemLongClickListener(OnItemLongClickListener listener){
		
	}
	
	
	public PullToRefreshPlugin<T> setOnItemClickListener(OnItemClickListener listener){
		mOnItemClickListener = listener;
		return this;
	}
	
	public PullToRefreshPlugin<T> setXHttpPaginationManager(XHttpPaginationManager m){
		mXHttpPaginationManager = m;
		return this;
	}
	
	public XHttpPaginationManager getXHttpPaginationManager(){
		return mXHttpPaginationManager;
	}
	
	public XEndlessAdapter getEndlessAdapter(){
		return mEndlessAdapter;
	}
	
	public void invalidateViews(){
		final ListView lv = getListView();
		if(lv != null){
			lv.invalidateViews();
		}
	}
	
	public void setIsCreateRefresh(boolean bCreateRefresh){
		mIsCreateRefresh = bCreateRefresh;
	}
	
	public void setIsHideViewFirstLoad(boolean bHide){
		mIsHideViewFirstLoad = bHide;
		if(mEndlessAdapter != null){
			mEndlessAdapter.setVisible(!bHide);
		}
	}
	
	public void setUseDefaultAnimation(boolean bUse){
		mIsUseDefaultAnimation = bUse;
	}
	
	public void smoothSetSelection(int pos){
		
	}
	
	public void setSelection(int pos){
		getListView().setSelection(pos);
	}
	
	public void setAdapter(ListAdapter adapter){
		getListView().setAdapter(adapter);
	}
	
	public void resetAdapter(){
		getListView().setAdapter(mEndlessAdapter);
	}
	
	public abstract ListView getListView();
	
	public View getRefreshView(){
		return getListView();
	}
	
	public void setXHttpPagination(XHttpPagination p){
		mXHttpPaginationManager.setXHttpPagination(p, null);
	}
	
	public XHttpPagination getXHttpPagination(){
		return mXHttpPaginationManager.getXHttpPagination(null);
	}
	
	@Override
	public void onStartLoadMore(XEndlessAdapter adapter) {
		mIsRequestLoadMore = true;
		try{
			if(mOnLoadMoreListener != null){
				mOnLoadMoreListener.onStartLoadMore(adapter);
			}
		}finally{
			mIsRequestLoadMore = false;
		}
	}
	
	public void post(Runnable run){
		getRefreshView().post(run);
	}
	
	public void postDelayed(Runnable run,long delayMillis){
		getRefreshView().postDelayed(run, delayMillis);
	}
	
	public void removeCallbacks(Runnable run){
		getRefreshView().removeCallbacks(run);
	}
	
	protected void checkOrCreateViewFooter(){
		if(mViewFooter == null){
			mViewFooter = SystemUtils.inflate(mActivity, R.layout.xlibrary_footer_pulltorefresh);
			for(OnViewFooterHandlePlugin p : mActivity.getPlugins(OnViewFooterHandlePlugin.class)){
				p.onViewFooterCreated(mViewFooter);
			}
		}
	}
	
	protected void onScrollToFirstItem(){
	}
	
	public void startRefresh(){
		startRefreshCancelPre();
	}
	
	public void startRefreshCancelPre(){
		if(!isRefreshDisabled()){
			if(isRefreshing()){
				clearRefreshEvents();
				mIsRequestPullToRefresh = true;
				try{
					callPullDownToRefresh();
				}finally{
					mIsRequestPullToRefresh = false;
				}
			}else{
				onStartRefreshImpl();
			}
			cancelLoadMore();
		}
	}
	
	protected void callPullDownToRefresh(){
		if(mPullToRefreshListener != null){
			PullToRefreshListener listener = mPullToRefreshListener;
			for(PullDownToRefreshPlugin p : mActivity.getPlugins(PullDownToRefreshPlugin.class)){
				listener = p.onWrapPullDownToRefresh(this, listener);
			}
			listener.onPullDownToRefresh();
		}
	}
	
	protected void callPullUpToRefresh(){
		if(mPullToRefreshListener != null){
			mPullToRefreshListener.onPullUpToRefresh();
		}
	}
	
	public boolean isCurrentRequestPullToRefresh(){
		return mIsRequestPullToRefresh;
	}
	
	public boolean isCurrentRequestLoadMore(){
		return mIsRequestLoadMore;
	}
	
	public abstract boolean isRefreshing();
	
	public abstract boolean isRefreshDisabled();
	
	public abstract void 	onStartRefreshImpl();
	
	public abstract void	completeRefresh();
	
	public abstract void	disableRefresh();
	
	public abstract void	enableRefresh();
	
	public void cancelRefresh(){
		clearRefreshEvents();
		completeRefresh();
	}
	
	public void clearRefreshEvents(){
		mCurrentRefreshEvents.clear();
	}
	
	public void cancelLoadNewEvent(){
		if(mCurrentLoadNewEvent != null){
			mCurrentLoadNewEvent = null;
			completeRefresh();
		}
	}
	
	public void cancelLoadMore(){
		if(mCurrentLoadEvent != null){
			mCurrentLoadEvent = null;
			mEndlessAdapter.endLoad();
		}
	}

	public void pushEventRefresh(int eventCode,Object...params){
		addRefreshEvent(mActivity.pushEventNoProgress(eventCode,params));
	}
	
	public void pushEventRefresh(String code,Object...params){
		addRefreshEvent(mActivity.pushEventNoProgress(code,params));
	}
	
	public void addRefreshEvent(Event event){
		event.addEventListener(0,this);
		mCurrentRefreshEvents.add(event);
	}
	
	public void pushEventLoadNew(int eventCode,Object...params){
		mCurrentLoadNewEvent = mActivity.pushEventNoProgress(eventCode, params);
		mCurrentLoadNewEvent.addEventListener(0, this);
	}
	
	public void pushEventLoad(int eventCode,Object...params){
		pushEventLoad(String.valueOf(eventCode), params);
	}
	
	public void pushEventLoad(String code,Object...params){
		final XHttpPagination p = mXHttpPaginationManager.getXHttpPagination(code);
		if(params == null){
			mCurrentLoadEvent = mActivity.pushEventNoProgress(code, p.getOffset(),p);
		}else{
			final int length = params.length + 2;
			Object newParams[] = new Object[length];
			for(int index = 0;index < length - 2;++index){
				newParams[index] = params[index];
			}
			newParams[length - 2] = p.getOffset();
			newParams[length - 1] = p;
			mCurrentLoadEvent = mActivity.pushEventNoProgress(code, newParams);
		}
		mCurrentLoadEvent.addEventListener(0,this);
	}
	
	public void pushEventPullUpLoad(int eventCode,Object...params){
		mCurrentPullUpLoadEvent = mActivity.pushEventNoProgress(eventCode, params);
		mCurrentPullUpLoadEvent.addEventListener(0, this);
	}
	
	public void pushEventWrap(String code,final OnEventListener listener,Object...params){
		Event event = mActivity.pushEventNoProgress(code, params);
		event.addEventListener(0, new OnEventListener() {
			@Override
			public void onEventRunEnd(Event event) {
				if(event.isSuccess()){
					listener.onEventRunEnd(event);
				}else{
					onRefreshFail(event);
					completeRefresh();
				}
			}
		});
	}
	
	public void bindRefreshEventCode(int code){
		mActivity.registerActivityEventHandlerEx(code, 
				new RefreshActivityEventHandler(this));
	}

	@Override
	public void onEventRunEnd(Event event) {
		if(mCurrentRefreshEvents.remove(event)){
			onOneRefreshEventEnd(event);
			if(isAllRefreshEventsFinished()){
				refreshEventEnd(event);
			}
		}else if(isLoadEvent(event)){
			onBottomLoadEventEnd(event);
		}else if(isPullUpLoadEvent(event)){
			onPullUpLoadEventEnd(event);
		}else if(isLoadNewEvent(event)){
			onLoadNewEventEnd(event);
		}
	}
	
	public boolean isAllRefreshEventsFinished(){
		return mCurrentRefreshEvents.size() == 0;
	}
	
	public boolean 	isRefreshEvent(Event event){
		return mCurrentRefreshEvents.contains(event);
	}
	
	public boolean	isLoadNewEvent(Event event){
		return event.equals(mCurrentLoadNewEvent);
	}
	
	public boolean	isLoadEvent(Event event){
		return mCurrentLoadEvent != null && event.equals(mCurrentLoadEvent);
	}
	
	public boolean	isPullUpLoadEvent(Event event){
		return mCurrentPullUpLoadEvent != null && event.equals(mCurrentPullUpLoadEvent);
	}

	protected void onOneRefreshEventEnd(Event event){
		if(event.isSuccess()){
			if(mIsHideViewFirstLoad){
				onFirstLoadSuccess();
				mIsHideViewFirstLoad = false;
			}
			hideFailView();
			XHttpPagination p = event.findReturnParam(XHttpPagination.class);
			if(p != null){
				mXHttpPaginationManager.setXHttpPagination(p, event.getStringCode());
				mEndlessAdapter.setHasMore(p.hasMore());
			}else{
				mEndlessAdapter.hideBottomView();
			}
		}else{
			onRefreshFail(event);
		}
		if(mStatusListeners != null){
			for(PullToRefeshStatusListener l : mStatusListeners){
				l.onOneRefreshEventEnd(event);
			}
		}
	}
	
	public void onFirstLoadSuccess(){
		if(!mEndlessAdapter.isVisible()){
			mEndlessAdapter.setVisible(true);
		}
	}
	
	protected final void refreshEventEnd(Event event){
		completeRefresh();
		if(mStatusListeners != null){
			for(PullToRefeshStatusListener l : mStatusListeners){
				l.onRefreshEventEnd(event);
			}
		}
		onRefreshEventEnd(event);
		if(event.isSuccess()){
			checkNoResult();
		}
	}
	
	protected void onRefreshEventEnd(Event event){
		
	}
	
	protected void onLoadNewEventEnd(Event event){
		completeRefresh();
		if(mLoadNewStatusListeners != null){
			for(PullToRefreshLoadNewListener l : mLoadNewStatusListeners){
				l.onLoadNewEventEnd(event);
			}
		}
	}
	
	protected void onBottomLoadEventEnd(Event event){
		mEndlessAdapter.endLoad();
		mCurrentLoadEvent = null;
		if(event.isSuccess()){
			XHttpPagination p = event.findReturnParam(XHttpPagination.class);
			if(p != null){
				mXHttpPaginationManager.setXHttpPagination(p, event.getStringCode());
				mEndlessAdapter.setHasMore(p.hasMore());
			}else{
				mEndlessAdapter.hideBottomView();
			}
		}else{
			mEndlessAdapter.setLoadFail();
		}
		if(mStatusListeners != null){
			for(PullToRefeshStatusListener l : mStatusListeners){
				l.onBottomLoadEventEnd(event);
			}
		}
	}
	
	protected void onPullUpLoadEventEnd(Event event){
		mCurrentPullUpLoadEvent = null;
		if(event.isSuccess()){
			XHttpPagination p = event.findReturnParam(XHttpPagination.class);
			if(p != null){
				mXHttpPaginationManager.setXHttpPagination(p, event.getStringCode());
				if(!mContentStatusViewProvider.isNoResultViewVisible() &&
						!mContentStatusViewProvider.isFailViewVisible()){
					mEndlessAdapter.setHasMore(p.hasMore());
				}
			}else{
				mEndlessAdapter.hideBottomView();
			}
		}else{
			if(!mContentStatusViewProvider.isNoResultViewVisible() &&
					!mContentStatusViewProvider.isFailViewVisible()){
				mEndlessAdapter.setLoadFail();
			}
		}
		if(mStatusListeners != null){
			for(PullToRefeshStatusListener l : mStatusListeners){
				l.onPullUpLoadEventEnd(event);
			}
		}
	}
	
	public void onRefreshFail(Event e){
		if(mEndlessAdapter.isRegisteredDataSetObserver()){
			if(checkAdapterIsEmpty()){
				mContentStatusViewProvider.showFailView();
			}
		}else{
			mContentStatusViewProvider.showFailView();
		}
		final Exception ex = e.getFailException();
		if(ex != null && ex instanceof XException){
			final XException exception = (XException)ex;
			if(mActivity.isDisconnectException(exception)){
				if(isRefreshDisabled()){
					setFailText(mActivity.getString(R.string.load_fail));
				}else{
					setFailText(mActivity.getString(R.string.pull_to_refresh_fail));
				}
			}else{
				final String fail = exception.getMessage();
				setFailText(fail);
			}
		}
	}
	
	public void setFailText(String text){
		mContentStatusViewProvider.setFailText(text);
	}
	
	public void hideFailView(){
		mContentStatusViewProvider.hideFailView();
	}
	
	public PullToRefreshPlugin<T> setAdapterEmptyChecker(AdapterEmptyChecker checker){
		mAdapterEmptyChecker = checker;
		return this;
	}
	
	public void checkNoResult(){
		if(checkAdapterIsEmpty()){
			showNoResultView();
		}else{
			hideNoResultView();
		}
	}
	
	public ContentStatusViewProvider getContentStatusViewProvider(){
		return mContentStatusViewProvider;
	}
	
	public void showNoResultView(){
		mEndlessAdapter.hideBottomView();
		mContentStatusViewProvider.showNoResultView();
	}
	
	public void hideNoResultView(){
		mContentStatusViewProvider.hideNoResultView();
	}
	
	public boolean checkAdapterIsEmpty(){
		if(mAdapterEmptyChecker != null){
			return mAdapterEmptyChecker.onCheckAdapterEmpty(this);
		}else{
			return false;
		}
	}
	
	public static class DefaultAdapterEmptyChecker implements AdapterEmptyChecker{
		@Override
		public boolean onCheckAdapterEmpty(PullToRefreshPlugin prp) {
			return prp.mEndlessAdapter.getCount() <= 1;
		}
	}
	
	public static interface PullToRefeshStatusListener{
		public void onOneRefreshEventEnd(Event event);
		
		public void onRefreshEventEnd(Event event);
		
		public void onBottomLoadEventEnd(Event event);
		
		public void onPullUpLoadEventEnd(Event event);
	}
	
	public static interface PullToRefreshLoadNewListener{
		public void onLoadNewEventEnd(Event event);
	}
	
	public static interface PullToRefreshListener{
		public void onPullDownToRefresh();

		public void onPullUpToRefresh();
	}
	
	public static interface XHttpPaginationManager{
		public void setXHttpPagination(XHttpPagination p,String code);
		
		public XHttpPagination getXHttpPagination(String code);
	}
	
	public static class SingleXHttpPaginationManager implements XHttpPaginationManager{

		protected XHttpPagination	mXHttpPagination;

		@Override
		public void setXHttpPagination(XHttpPagination p, String code) {
			mXHttpPagination = p;
		}

		@Override
		public XHttpPagination getXHttpPagination(String code) {
			return mXHttpPagination;
		}
	}
	
	public static class MultiXHttpPaginationManager implements XHttpPaginationManager{
		
		HashMap<String, XHttpPagination> mMapCodeToHttpPagination = new HashMap<String, XHttpPagination>();
		
		@Override
		public void setXHttpPagination(XHttpPagination p, String code) {
			if(!TextUtils.isEmpty(code)){
				mMapCodeToHttpPagination.put(code, p);
			}
		}

		@Override
		public XHttpPagination getXHttpPagination(String code) {
			return mMapCodeToHttpPagination.get(code);
		}
	}
	
	public static interface OnViewFooterHandlePlugin extends ActivityBasePlugin{
		public void onViewFooterCreated(View v);
	}
	
	public static interface AdapterCreatorPlugin extends ActivityBasePlugin{
		public ListAdapter onWrapAdapter(PullToRefreshPlugin<?> pp,ListAdapter adapter);
	}
	
	public static interface PullDownToRefreshPlugin extends ActivityBasePlugin{
		public PullToRefreshListener onWrapPullDownToRefresh(PullToRefreshPlugin<?> pp,PullToRefreshListener listener);
	}
}
