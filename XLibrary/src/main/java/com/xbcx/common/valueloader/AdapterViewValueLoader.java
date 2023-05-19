package com.xbcx.common.valueloader;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import android.util.SparseArray;

import com.xbcx.core.XApplication;
import com.xbcx.core.module.ListValueLoaderlListener;

public abstract class AdapterViewValueLoader<Holder,Item,Result> {
	
	private static ExecutorService	mExecutor = Executors.newCachedThreadPool();
	private static Object			mPauseLock = new Object();
	static{
		XApplication.addManager(new ListScrollManager());
	}
	
	private SparseArray<Item> 			mMapHolderIdToItem = new SparseArray<Item>();
	private Map<Item, ReentrantLock> 	mMapItemToLock = new WeakHashMap<Item, ReentrantLock>();
	
	private CacheProvider<Item, Result> mCacheProvider = new SimpleCacheProvider<Item, Result>();
	
	private List<HolderObserver<Holder, Item>> mHolderObservers;
	
	public void addHolderObserver(HolderObserver<Holder, Item> observer){
		if(observer == null){
			return;
		}
		if(mHolderObservers == null){
			mHolderObservers = new LinkedList<HolderObserver<Holder,Item>>();
		}
		mHolderObservers.add(observer);
	}
	
	public boolean removeHolderObserver(HolderObserver<Holder, Item> observer){
		if(mHolderObservers != null){
			return mHolderObservers.remove(observer);
		}
		return false;
	}
	
	public void setCacheProvider(CacheProvider<Item, Result> provider){
		mCacheProvider = provider;
	}
	
	public void bindView(Holder holder,Item item){
		putHolder(holder, item);
		final Result result = getCache(item);
		if(result == null){
			onUpdateEmpty(holder, item);
			execute(holder,item);
		}else{
			onUpdateView(holder, item, result);
		}
	}
	
	public void removeBindView(Holder holder){
		removeHolder(holder);
	}
	
	public void execute(Holder holder,Item item){
		mExecutor.submit(new LoadTask(holder, item));
	}
	
	protected abstract Result doInBackground(Item item);
	
	public void onUpdateEmpty(Holder holder,Item item){
	}
	
	public void onUpdateLoadFail(Holder holder,Item item){
	}
	
	public abstract void onUpdateView(Holder holder,Item item,Result result);
	
	public void setResult(Holder holder,Item item,Result result){
		if(holder != null){
			onUpdateView(holder, item, result);
		}
	}
	
	public void		addCache(Item item,Result result){
		if(result != null){
			synchronized (mCacheProvider) {
				mCacheProvider.addCache(item, result);
			}
		}
	}
	
	public Result	getCache(Item item){
		synchronized (mCacheProvider) {
			return mCacheProvider.getCache(item);
		}
	}
	
	public void		clearCache(){
		synchronized (mCacheProvider) {
			mCacheProvider.clearCache();
		}
	}
	
	public void 	removeCache(Item item){
		synchronized (mCacheProvider) {
			mCacheProvider.removeCache(item);
		}
	}
	
	protected void putHolder(Holder holder,Item item){
		synchronized (mMapHolderIdToItem) {
			mMapHolderIdToItem.put(holder.hashCode(), item);
		}
		if(mHolderObservers != null){
			for(HolderObserver<Holder, Item> ho : mHolderObservers){
				ho.onPutHolder(holder, item);
			}
		}
	}
	
	protected void removeHolder(Holder holder){
		synchronized (mMapHolderIdToItem) {
			mMapHolderIdToItem.remove(holder.hashCode());
		}
		if(mHolderObservers != null){
			for(HolderObserver<Holder, Item> ho : mHolderObservers){
				ho.onRemoveHolder(holder);
			}
		}
	}
	
	protected ReentrantLock getLock(Item item){
		ReentrantLock lock = mMapItemToLock.get(item);
		if(lock == null){
			lock = new ReentrantLock();
			mMapItemToLock.put(item, lock);
		}
		return lock;
	}
	
	private class LoadTask implements Runnable{
		ReentrantLock 			mLock;
		WeakReference<Holder> 	mHolderRef;
		Item					mItem;
		
		public LoadTask(Holder holder,Item item){
			mLock = getLock(item);
			mHolderRef = new WeakReference<Holder>(holder);
			mItem = item;
		}

		@Override
		public void run() {
			if(!XApplication.isImageLoaderResume()){
				synchronized (mPauseLock) {
					try {
						mPauseLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			mLock.lock();
			try{
				if(isCollected() || isReused()){
					return;
				}
				
				Result r = getCache(mItem);
				if(r == null){
					r = doInBackground(mItem);
					addCache(mItem, r);
				}
				final Result result = r;
				XApplication.getMainThreadHandler().post(new Runnable() {
					@Override
					public void run() {
						final Holder h = mHolderRef.get();
						if(h == null){
							return;
						}
						if(isReused()){
							return;
						}
						synchronized (mMapHolderIdToItem) {
							mMapHolderIdToItem.remove(h.hashCode());
						}
						if(result == null){
							onUpdateLoadFail(h, mItem);
						}else{
							setResult(h,mItem, result);
						}
					}
				});
			}finally{
				mLock.unlock();
			}
		}
		
		protected boolean isCollected(){
			return mHolderRef.get() == null;
		}
		
		protected boolean isReused(){
			final Holder h = mHolderRef.get();
			if(h == null){
				return true;
			}
			synchronized (mMapHolderIdToItem) {
				Item item = mMapHolderIdToItem.get(h.hashCode());
				return !mItem.equals(item);
			}
		}
	}

	private static class ListScrollManager implements ListValueLoaderlListener{
		@Override
		public void onPauseLoader() {
		}

		@Override
		public void onResumeLoader() {
			synchronized (mPauseLock) {
				mPauseLock.notifyAll();
			}
		}
	}
}
