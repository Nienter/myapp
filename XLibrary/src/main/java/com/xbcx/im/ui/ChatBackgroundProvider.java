package com.xbcx.im.ui;

import java.util.Collection;
import java.util.HashMap;

import com.xbcx.core.SharedPreferenceDefine;
import com.xbcx.core.XApplication;
import com.xbcx.im.IMKernel;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.SparseArray;
import android.view.View;

public class ChatBackgroundProvider {
	
	private static SparseArray<String> 		mMapResIdToKey = new SparseArray<String>();
	private static HashMap<String, Integer> mMapKeyToResId = new HashMap<String, Integer>();
	
	public static void registerBackgroundResId(String key,int resId){
		mMapKeyToResId.put(key, resId);
		mMapResIdToKey.put(resId, key);
	}
	
	@SuppressWarnings("deprecation")
	public static void setBackground(View v){
		final String chatBg = getSharedPreferences().getString(
						SharedPreferenceDefine.KEY_CHAT_BG, "1");
		final Integer resId = mMapKeyToResId.get(chatBg);
		if(resId == null){
			try{
				final Bitmap bmp = BitmapFactory.decodeFile(chatBg);
				if(bmp == null){
					//v.setBackgroundResource(R.drawable.bg_chat_1);
				}else{
					v.setBackgroundDrawable(new BitmapDrawable(
							XApplication.getApplication().getResources(), bmp));
				}
			}catch(OutOfMemoryError e){
				//v.setBackgroundResource(R.drawable.bg_chat_1);
			}
		}else{
			v.setBackgroundResource(resId.intValue());
		}
	}
	
	public static Collection<Integer> getBackgroundResIds(){
		return mMapKeyToResId.values();
	}
	
	public static int  getCurrentBackgroundResId(){
		final String chatBg = getSharedPreferences().getString(
						SharedPreferenceDefine.KEY_CHAT_BG, "1");
		final Integer resId = mMapKeyToResId.get(chatBg);
		return resId == null ? 0 : resId.intValue();
	}
	
	public static void saveBackground(int resId){
		final String key = mMapResIdToKey.get(resId);
		if(key != null){
			getSharedPreferences()
			.edit()
			.putString(SharedPreferenceDefine.KEY_CHAT_BG, key)
			.commit();
		}
	}
	
	public static void saveBackground(String path){
		getSharedPreferences()
		.edit()
		.putString(SharedPreferenceDefine.KEY_CHAT_BG, path)
		.commit();
	}
	
	protected static SharedPreferences getSharedPreferences(){
		return IMKernel.getInstance().getContext().getSharedPreferences(
				IMKernel.getLocalUser(), 0);
	}
}
