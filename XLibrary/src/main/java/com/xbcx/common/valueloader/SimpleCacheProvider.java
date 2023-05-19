package com.xbcx.common.valueloader;

import java.util.HashMap;

public class SimpleCacheProvider<Item, Result> implements CacheProvider<Item, Result>{

	private HashMap<Item,Result> mMapItemToResultCache = new HashMap<Item, Result>();
	
	public void		addCache(Item item,Result result){
		mMapItemToResultCache.put(item, result);
	}
	
	public Result	getCache(Item item){
		return mMapItemToResultCache.get(item);
	}
	
	public void		clearCache(){
		mMapItemToResultCache.clear();
	}
	
	public Result 	removeCache(Item item){
		return mMapItemToResultCache.remove(item);
	}

}
