package com.xbcx.im.vcard;

import com.xbcx.common.valueloader.CacheProvider;

public class VCardValueLoaderCacheProvider<Result> implements CacheProvider<String, Result>{

	private int	mActivityType;
	
	public VCardValueLoaderCacheProvider(int activityType){
		mActivityType = activityType;
	}
	
	@Override
	public void addCache(String item, Result result) {
	}

	@Override
	public Result removeCache(String item) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Result getCache(String item) {
		return (Result)VCardProvider.getInstance().loadVCard(item, mActivityType, false);
	}

	@Override
	public void clearCache() {
	}

}
