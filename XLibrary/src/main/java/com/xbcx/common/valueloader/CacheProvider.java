package com.xbcx.common.valueloader;

public interface CacheProvider<Item,Result> {
	
	public void addCache(Item item,Result result);
	
	public Result removeCache(Item item);
	
	public Result getCache(Item item);
	
	public void clearCache();
}
