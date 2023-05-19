package com.xbcx.im.vcard;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.Map.Entry;

import android.view.View;

import com.xbcx.common.valueloader.HolderObserver;
import com.xbcx.im.vcard.VCardProvider.BaseValueLoader;
import com.xbcx.im.vcard.VCardProvider.VCardChangeHandler;

public class VCardLoaderHelper<Holder extends View,Result> implements HolderObserver<Holder, String>,
															VCardChangeHandler<Result>{

	private BaseValueLoader<Holder,Result> 	mValueLoader;
	
	private WeakHashMap<Holder, String> 	mMapViewToId = new WeakHashMap<Holder, String>();
	private List<Holder> 					mDeleteCache;
	
	private boolean							mIsFromVCardChange;
	
	public VCardLoaderHelper(BaseValueLoader<Holder, Result> loader){
		mValueLoader = loader;
		loader.setCacheProvider(new VCardValueLoaderCacheProvider<Result>(loader.getActivityType()));
		loader.addHolderObserver(this);
		loader.addVCardChangeHandler(this);
	}
	
	public boolean isFromVCardChange(){
		return mIsFromVCardChange;
	}
	
	@Override
	public void onPutHolder(Holder holder, String item) {
		mMapViewToId.put(holder, item);
	}

	@Override
	public void onRemoveHolder(Holder holder) {
		mMapViewToId.remove(holder);
	}

	@Override
	public void onVCardChanged(String item, Result result) {
		if(mMapViewToId.size() > 0){
			if(mDeleteCache == null){
				mDeleteCache = new ArrayList<Holder>();
			}
			Holder view = null;
			mIsFromVCardChange = true;
			for(Entry<Holder, String> e : mMapViewToId.entrySet()){ 
				view = e.getKey();
				if(view.getWindowToken() == null){
					mDeleteCache.add(view);
				}else{
					if(e.getValue().equals(item)){
						mValueLoader.onUpdateView(view, item, result);
					}	
				}
			}
			mIsFromVCardChange = false;
			for(Holder h : mDeleteCache){
				mMapViewToId.remove(h);
			}
			mDeleteCache.clear();
		}
	}

}
