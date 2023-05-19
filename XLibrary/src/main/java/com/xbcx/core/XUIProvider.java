package com.xbcx.core;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater.Factory;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Orientation;
import com.handmark.pulltorefresh.library.internal.FlipLoadingLayout;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;
import com.xbcx.common.pulltorefresh.PullToRefreshPlugin;
import com.xbcx.common.pulltorefresh.XPullToRefreshPlugin;

public class XUIProvider {

	public static XUIProvider getInstance(){
		if(instance == null){
			instance = new XUIProvider();
		}
		return instance;
	}
	
	public static XUIProvider instance;
	
	public static void setXUIProvider(XUIProvider provider){
		instance = provider;
	}
	
	public boolean isHideViewFirstLoad(){
		return false;
	}
	
	public boolean useDefaultAnimation(){
		return true;
	}
	
	public PullToRefreshPlugin<BaseActivity>	createPullToRefreshPlugin(){
		return new XPullToRefreshPlugin<BaseActivity>();
	}
	
	public LoadingLayout createCustomLoadingLayout(Context context, Mode mode, Orientation scrollDirection, TypedArray attrs){
		return new FlipLoadingLayout(context, mode, scrollDirection, attrs);
	}
	
	public Resources createResources(Resources res){
		return res;
	}
	
	public Factory createLayoutInflateFactory(Activity activity){
		return null;
	}
	
	public void launchCameraPhoto(Activity activity,String path,int requestCode){
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, 
				Uri.fromFile(new File(path)));
		activity.startActivityForResult(intent, requestCode);
	}
}
