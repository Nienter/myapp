package com.xbcx.core.http.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.xbcx.core.Event;
import com.xbcx.utils.JsonParseUtils;

public abstract class SimpleItemBaseRunner extends SimpleBaseRunner {
	
	protected final Class<?> 					mItemClass;
	
	protected List<Class<?>> 					mItemClasses;
	protected List<String> 						mMapListHttpKeys;
	protected HashMap<String, Class<?>> 		mMapListHttpKeyToItemClass;
	
	public SimpleItemBaseRunner(String url,Class<?> itemClass){
		super(url);
		mItemClass = itemClass;
	}
	
	public SimpleItemBaseRunner addItemClass(Class<?> itemClass){
		if(mItemClasses == null){
			mItemClasses = new ArrayList<Class<?>>();
		}
		mItemClasses.add(itemClass);
		return this;
	}
	
	public SimpleItemBaseRunner addListItemClass(String key,Class<?> cls){
		if(mMapListHttpKeyToItemClass == null){
			mMapListHttpKeys = new ArrayList<String>();
			mMapListHttpKeyToItemClass = new HashMap<String, Class<?>>();
		}
		if(!mMapListHttpKeyToItemClass.containsKey(key)){
			mMapListHttpKeys.add(key);
			mMapListHttpKeyToItemClass.put(key, cls);
		}
		return this;
	}
	
	public void handleExtendItems(Event event,JSONObject jo) throws Exception{
		if(mMapListHttpKeyToItemClass != null){
			for(String key : mMapListHttpKeys){
				event.addReturnParam(JsonParseUtils.parseArrays(jo, key, 
						mMapListHttpKeyToItemClass.get(key)));
			}
		}
		if(mItemClasses != null){
			for(Class<?> c : mItemClasses){
				event.addReturnParam(JsonParseUtils.buildObject(c, jo));
			}
		}
	}
}
