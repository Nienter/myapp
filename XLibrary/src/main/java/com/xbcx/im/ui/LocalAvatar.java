package com.xbcx.im.ui;

import java.util.HashMap;

import android.util.SparseIntArray;

public class LocalAvatar {

	private static SparseIntArray mapActivityTypeToResId = new SparseIntArray();
	
	private static HashMap<String, Integer> mapIdToResId;
	
	public static int 	getAvatarResId(int activityType){
		return mapActivityTypeToResId.get(activityType);
	}
	
	public static void 	registerAvatarResId(int activityType,int resId){
		mapActivityTypeToResId.put(activityType, resId);
	}
	
	public static void 	registerAvatarResId(String id,int resId){
		if(mapIdToResId == null){
			mapIdToResId = new HashMap<String, Integer>();
		}
		mapIdToResId.put(id, resId);
	}
	
	public static int 	getAvatarResId(String id){
		if(mapIdToResId == null){
			return 0;
		}
		Integer i = mapIdToResId.get(id);
		return i == null ? 0 : i.intValue();
	}
}
