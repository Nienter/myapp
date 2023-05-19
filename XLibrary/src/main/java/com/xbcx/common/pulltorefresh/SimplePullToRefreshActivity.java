package com.xbcx.common.pulltorefresh;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.widget.ListAdapter;

import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.core.Event;
import com.xbcx.utils.SystemUtils;

public abstract class SimplePullToRefreshActivity<T> extends PullToRefreshActivity implements
																	AdapterEmptyChecker{

	protected SetBaseAdapter<T>		mSetAdapter;
	protected ArrayList<T> 			mItems = new ArrayList<T>();
	
	private   SparseIntArray		mMapTriggerDeleteEventCode;
	private   SparseIntArray		mMapTriggerModifyEventCode;
	private   SparseBooleanArray	mMapTriggerAddEventCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPullToRefreshPlugin.setAdapterEmptyChecker(this);
	}

	@Override
	public ListAdapter onCreateAdapter() {
		mSetAdapter = onCreateSetAdapter();
		return mSetAdapter;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean onCheckAdapterEmpty(PullToRefreshPlugin prp) {
		return mSetAdapter.getCount() <= 0;
	}
	
	@Override
	public void onOneRefreshEventEnd(Event event) {
		super.onOneRefreshEventEnd(event);
		if(event.isSuccess()){
			@SuppressWarnings("unchecked")
			List<T> items = (List<T>)event.findReturnParam(List.class);
			onReplaceItems(items);
		}
	}
	
	public void onReplaceItems(Collection<T> items){
		if(items != null){
			mItems.clear();
			mItems.addAll(items);
			mSetAdapter.replaceAll(onFilterItems(items));
		}
	}

	@Override
	public void onBottomLoadEventEnd(Event event) {
		super.onBottomLoadEventEnd(event);
		if(event.isSuccess()){
			@SuppressWarnings("unchecked")
			List<T> items = (List<T>)event.findReturnParam(List.class);
			onAddItems(items);
		}
	}
	
	@Override
	public void onPullUpLoadEventEnd(Event event) {
		super.onPullUpLoadEventEnd(event);
		if(event.isSuccess()){
			@SuppressWarnings("unchecked")
			List<T> items = (List<T>)event.findReturnParam(List.class);
			onAddItems(items);
		}
	}

	protected void onAddItems(Collection<T> items){
		if(items != null){
			mItems.addAll(items);
			mSetAdapter.addAll(onFilterItems(items));
		}
	}
	
	protected Collection<T> onFilterItems(Collection<T> items){
		return items;
	}
	
	protected void	updateFilterItems(){
		mSetAdapter.replaceAll(onFilterItems(mItems));
	}
	
	public SetBaseAdapter<T> getSetAdapter(){
		return mSetAdapter;
	}

	protected abstract SetBaseAdapter<T> onCreateSetAdapter();
	
	protected void	bindTriggerDeleteEventCode(int code){
		if(mMapTriggerDeleteEventCode == null){
			mMapTriggerDeleteEventCode = new SparseIntArray();
		}
		mMapTriggerDeleteEventCode.put(code, code);
		addAndManageEventListener(code);
	}
	
	protected void	bindTriggerModifyEventCode(int code){
		if(mMapTriggerModifyEventCode == null){
			mMapTriggerModifyEventCode = new SparseIntArray();
		}
		mMapTriggerModifyEventCode.put(code, code);
		addAndManageEventListener(code);
	}
	
	protected void 	bindTriggerAddEventCode(int code,boolean addTop){
		if(mMapTriggerAddEventCode == null){
			mMapTriggerAddEventCode = new SparseBooleanArray();
		}
		mMapTriggerAddEventCode.put(code, addTop);
		addAndManageEventListener(code);
	}

	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(mMapTriggerDeleteEventCode != null && 
				mMapTriggerDeleteEventCode.get(code,-1) != -1){
			if(event.isSuccess()){
				final String id = event.findParam(String.class);
				if(!TextUtils.isEmpty(id)){
					mSetAdapter.removeItemById(id);
					checkNoResult();
				}
			}
		}
		if(mMapTriggerModifyEventCode != null &&
				mMapTriggerModifyEventCode.get(code, -1) != -1){
			if(event.isSuccess()){
				final T item = findReturnItem(event);
				if(item != null){
					mSetAdapter.updateItem(item);
					checkNoResult();
				}
			}
		}
		if(mMapTriggerAddEventCode != null &&
				mMapTriggerAddEventCode.indexOfKey(code) >= 0){
			if(event.isSuccess()){
				onTriggerAddEvent(event);
			}
		}
	}
	
	protected void onTriggerAddEvent(Event event){
		final T item = findReturnItem(event);
		if(item != null){
			if(mMapTriggerAddEventCode.get(event.getEventCode())){
				mSetAdapter.addItem(0, item);
			}else{
				mSetAdapter.addItem(item);
			}
			checkNoResult();
		}
	}
	
	public T findReturnItem(Event event){
		Class<T> cls = getGenericClass();
		if(cls != null){
			return event.findReturnParam(cls);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Class<T> getGenericClass(){
		return (Class<T>)SystemUtils.getSingleGenericClass(getClass(), SimplePullToRefreshActivity.class);
	}
}
