package com.xbcx.common.pulltorefresh;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.xbcx.adapter.AnimatableAdapter;
import com.xbcx.adapter.GridAdapterWrapper;
import com.xbcx.adapter.GridAdapterWrapper.OnGridItemClickListener;
import com.xbcx.adapter.GridAdapterWrapper.OnGridItemLongClickListener;
import com.xbcx.adapter.anim.XSwingBottomInAnimationAdapter;
import com.xbcx.common.pulltorefresh.PullToRefreshPlugin.PullToRefeshStatusListener;
import com.xbcx.common.pulltorefresh.PullToRefreshPlugin.PullToRefreshListener;
import com.xbcx.core.ActivityBasePlugin;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.Event;
import com.xbcx.core.XEndlessAdapter;
import com.xbcx.core.XUIProvider;
import com.xbcx.core.http.XHttpPagination;

public abstract class PullToRefreshActivity extends BaseActivity implements 
								AdapterView.OnItemClickListener, 
								XEndlessAdapter.OnLoadMoreListener, 
								OnGridItemClickListener,
								OnGridItemLongClickListener,
								PullToRefeshStatusListener,
								AdapterCreator,
								PullToRefreshListener{

	protected PullToRefreshPlugin<BaseActivity> mPullToRefreshPlugin = onCreatePullToRefreshPlugin();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPullToRefreshPlugin.setOnItemClickListener(this)
		.setAdapterCreator(this)
		.setPullToRefreshListener(this)
		.setPullToRefreshStatusListener(this)
		.setOnLoadMoreListener(this);
		
		for(InitPullToRefreshPlugin p : getPlugins(InitPullToRefreshPlugin.class)){
			p.onInitPullToRefreshPlugin(mPullToRefreshPlugin);
		}
		onInitPullToRefreshPlugin();
		
		registerPlugin(mPullToRefreshPlugin);
	}
	
	protected void onInitPullToRefreshPlugin(){
		
	}

	protected PullToRefreshPlugin<BaseActivity> onCreatePullToRefreshPlugin() {
		return XUIProvider.getInstance().createPullToRefreshPlugin();
	}

	public PullToRefreshPlugin<BaseActivity> getPullToRefreshPlugin(){
		return mPullToRefreshPlugin;
	}
	
	@Override
	public XEndlessAdapter onCreateEndlessAdapter(ListAdapter wrapped) {
		return new XEndlessAdapter(this, wrapped);
	}

	@Override
	public abstract ListAdapter onCreateAdapter();
	
	@Override
	public AnimatableAdapter onCreateAnimationAdapter(BaseAdapter wrap){
		if(mPullToRefreshPlugin.mIsUseDefaultAnimation){
			return new XSwingBottomInAnimationAdapter(wrap);
		}
		return null;
	}
	
	public final void invalidateViews(){
		mPullToRefreshPlugin.invalidateViews();
	}
	
	public final ListView getListView(){
		return mPullToRefreshPlugin.getListView();
	}
	
	public final View getRefreshView(){
		return mPullToRefreshPlugin.getRefreshView();
	}
	
	public final XEndlessAdapter getEndlessAdapter(){
		return mPullToRefreshPlugin.getEndlessAdapter();
	}
	
	public void setIsCreateRefresh(boolean bCreateRefresh){
		mPullToRefreshPlugin.setIsCreateRefresh(bCreateRefresh);
	}
	
	public void setIsHideViewFirstLoad(boolean bHide){
		mPullToRefreshPlugin.setIsHideViewFirstLoad(bHide);
	}
	
	public final void setXHttpPagination(XHttpPagination p){
		mPullToRefreshPlugin.setXHttpPagination(p);
	}
	
	public void setUseDefaultAnimation(boolean bUse){
		mPullToRefreshPlugin.setUseDefaultAnimation(bUse);
	}
	
	public final void setSelection(int pos){
		mPullToRefreshPlugin.setSelection(pos);
	}
	
	public final void setAdapter(ListAdapter adapter){
		mPullToRefreshPlugin.setAdapter(adapter);
	}
	
	public final void resetAdapter(){
		mPullToRefreshPlugin.resetAdapter();
	}
	
	public final XHttpPagination getXHttpPagination(){
		return mPullToRefreshPlugin.getXHttpPagination();
	}
	
	public final void post(Runnable run){
		mPullToRefreshPlugin.post(run);
	}
	
	public final void postDelayed(Runnable run,long delayMillis){
		mPullToRefreshPlugin.postDelayed(run, delayMillis);
	}
	
	public final void removeCallbacks(Runnable run){
		mPullToRefreshPlugin.removeCallbacks(run);
	}

	public final void startRefreshCancelPre() {
		mPullToRefreshPlugin.startRefreshCancelPre();
	}

	public final boolean isRefreshing() {
		return mPullToRefreshPlugin.isRefreshing();
	}

	public final void startRefresh() {
		mPullToRefreshPlugin.startRefresh();
	}

	public final boolean isRefreshDisabled() {
		return mPullToRefreshPlugin.isRefreshDisabled();
	}

	public final void completeRefresh() {
		mPullToRefreshPlugin.completeRefresh();
	}

	public final void disableRefresh() {
		mPullToRefreshPlugin.disableRefresh();
	}
	
	public final void enableRefresh(){
		mPullToRefreshPlugin.enableRefresh();
	}

	public void onPullDownToRefresh(){
	}

	public void onPullUpToRefresh() {
	}
	
	@Override
	public void onStartLoadMore(XEndlessAdapter adapter) {
	}

	public final void cancelRefresh() {
		mPullToRefreshPlugin.cancelRefresh();
	}

	public final void cancelLoadMore() {
		mPullToRefreshPlugin.cancelLoadMore();
	}

	public void pushEventRefresh(int eventCode, Object... params) {
		mPullToRefreshPlugin.pushEventRefresh(eventCode, params);
	}
	
	public void pushEventRefresh(String code, Object... params) {
		mPullToRefreshPlugin.pushEventRefresh(code, params);
	}

	public void pushEventLoad(int eventCode, Object... params) {
		mPullToRefreshPlugin.pushEventLoad(eventCode, params);
	}
	
	public void pushEventLoad(String code, Object... params) {
		mPullToRefreshPlugin.pushEventLoad(code, params);
	}

	public void pushEventPullUpLoad(int eventCode, Object... params) {
		mPullToRefreshPlugin.pushEventPullUpLoad(eventCode, params);
	}

	public void bindRefreshEventCode(int code) {
		mPullToRefreshPlugin.bindRefreshEventCode(code);
	}

	public final boolean isAllRefreshEventsFinished() {
		return mPullToRefreshPlugin.isAllRefreshEventsFinished();
	}

	public final boolean isLoadEvent(Event event) {
		return mPullToRefreshPlugin.isLoadEvent(event);
	}

	public final boolean isPullUpLoadEvent(Event event) {
		return mPullToRefreshPlugin.isPullUpLoadEvent(event);
	}

	@Override
	public void onOneRefreshEventEnd(Event event) {
	}

	@Override
	public void onRefreshEventEnd(Event event) {
	}

	@Override
	public void onBottomLoadEventEnd(Event event) {
	}

	@Override
	public void onPullUpLoadEventEnd(Event event) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final Object item = parent.getItemAtPosition(position);
		if (item != null) {
			for(OnItemClickPlugin p : getPlugins(OnItemClickPlugin.class)){
				if(p.onItemClicked(parent, item, view)){
					return;
				}
			}
			onItemClick(parent, item,view);
		}
	}

	public void onItemClick(AdapterView<?> parent, Object item,View view) {
		
	}

	@Override
	public void onGridItemClicked(GridAdapterWrapper gaw,View v, int position) {
	}

	@Override
	public boolean onGridItemLongClicked(GridAdapterWrapper gaw,View v, int position) {
		return false;
	}

	protected final void showFailView() {
		mPullToRefreshPlugin.getContentStatusViewProvider().showFailView();
	}

	protected void setFailText(String text) {
		mPullToRefreshPlugin.setFailText(text);
	}

	protected final void hideFailView() {
		mPullToRefreshPlugin.hideFailView();
	}

	public void setNoResultTextId(int textId) {
		setNoResultText(getString(textId));
	}

	public void setNoResultText(String text) {
		mPullToRefreshPlugin.getContentStatusViewProvider().setNoResultText(text);
	}
	
	protected final void checkNoResult(){
		mPullToRefreshPlugin.checkNoResult();
	}

	protected final void showNoResultView() {
		mPullToRefreshPlugin.showNoResultView();
	}

	protected final void hideNoResultView() {
		mPullToRefreshPlugin.hideNoResultView();
	}

	protected final boolean isNoResultViewVisible() {
		return mPullToRefreshPlugin.getContentStatusViewProvider().isNoResultViewVisible();
	}
	
	public static interface InitPullToRefreshPlugin extends ActivityBasePlugin{
		public void onInitPullToRefreshPlugin(PullToRefreshPlugin<?> p);
	}
}
