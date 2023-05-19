package com.xbcx.core.bmp;

import java.util.ArrayList;
import java.util.Collection;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;
import com.xbcx.core.XApplication;
import com.xbcx.core.module.ListValueLoaderlListener;
import com.xbcx.core.module.OnLowMemoryListener;
import com.xbcx.utils.SystemUtils;

public class XBitmapProvider implements ListValueLoaderlListener,OnLowMemoryListener{
	
	private static ImageLoader						imageLoader;
	private static SparseArray<DisplayImageOptions> mapDefaultResIdToDisplayOptions = new SparseArray<DisplayImageOptions>();
	
	private static DisplayImageOptionsCreator		displayImageOptionsCreator;
	
	static{
		XApplication.addManager(new XBitmapProvider());
		imageLoader = ImageLoader.getInstance();
		ImageLoaderConfiguration config = XApplication.getApplication().onCreaterImageLoaderConfiguration();
		imageLoader.init(config);
	}
	
	public static ImageLoader getImageLoader(){
		return imageLoader;
	}
	
	public static void pauseImageLoader(){
		imageLoader.pause();
	}
	
	public static void resumeImageLoader(){
		imageLoader.resume();
	}
	
	public static void setDisplayImageOptionsCreator(DisplayImageOptionsCreator creator){
		displayImageOptionsCreator = creator;
	}
	
	public static void clearMemoryCache(boolean bContainHttp){
		if(bContainHttp){
			imageLoader.getMemoryCache().clear();
		}else{
			XApplication.runOnBackground(new Runnable() {
				@Override
				public void run() {
					final Collection<String> keys = imageLoader.getMemoryCache().keys();
					if(keys != null){
						for(String key : new ArrayList<String>(keys)){
							if(!key.startsWith("http")){
								imageLoader.getMemoryCache().remove(key);
							}
						}
					}
				}
			});
		}
	}
	
	public static boolean setBitmapFromFile(final ImageView iv,String filePath){
		final String key = MemoryCacheUtils.generateKey(filePath, new ImageSize(
				XApplication.getScreenWidth() / 3, XApplication.getScreenWidth() / 3));
		Bitmap bmp = imageLoader.getMemoryCache().get(key);
		if(bmp == null){
			bmp = SystemUtils.decodeSampledBitmapFromFilePath(filePath, 
					XApplication.getScreenWidth() / 3, XApplication.getScreenWidth() / 3);
			if(bmp != null){
				imageLoader.getMemoryCache().put(key, bmp);
			}
		}
		if(bmp != null){
			iv.setImageBitmap(bmp);
			return true;
		}
		return false;
	}
	
	/**
     * can use anywhere
     */
	public static void	setBitmap(final ImageView view,String url,int defaultResId){
		DisplayImageOptions options = getDisplayImageOptions(view,url,defaultResId);
		imageLoader.displayImage(url, view, options);
	}
	
	public static void setBitmap(final ImageView view,String url,int maxWidth,int maxHeight,int defaultResId){
		DisplayImageOptions options = getDisplayImageOptions(view,url,defaultResId);
		imageLoader.displayImage(url, new XImageViewAware(view, maxWidth, maxHeight), options);
	}
	
	private static DisplayImageOptions getDisplayImageOptions(ImageView iv,String url,int defaultResId){
		DisplayImageOptions options = mapDefaultResIdToDisplayOptions.get(defaultResId);
		if(options == null){
			if(displayImageOptionsCreator == null){
				displayImageOptionsCreator = new SimpleDisplayImageOptionsCreator();
			}
			options = displayImageOptionsCreator.createDisplayImageOptions(iv, url, defaultResId);
			mapDefaultResIdToDisplayOptions.put(defaultResId, options);
		}
		return options;
	}
	
	public static void setBitmapTransition(final ImageView iv,String url){
		setBitmapTransition(iv, url, 0);
	}
	
	public static void setBitmapTransition(final ImageView iv,String url,int defaultResId){
		Drawable d = iv.getDrawable();
		if(d == null){
			if(defaultResId != 0){
				d = XApplication.getApplication().getResources().getDrawable(defaultResId);
			}
		}
		
		DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showImageOnLoading(d)
			.showImageForEmptyUri(d)
			.showImageOnFail(d)
			.cacheInMemory(true)
			.cacheOnDisk(true)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.displayer(new XBitmapDisplayer())
			.bitmapConfig(Bitmap.Config.ARGB_8888)
			.build();
		imageLoader.displayImage(url, iv, options);
	}

	@Override
	public void onPauseLoader() {
		pauseImageLoader();
	}

	@Override
	public void onResumeLoader() {
		resumeImageLoader();
	}

	@Override
	public void onLowMemory() {
		mapDefaultResIdToDisplayOptions.clear();
	}
	
	public static interface DisplayImageOptionsCreator{
		public DisplayImageOptions createDisplayImageOptions(ImageView view,String url,int defaultResId);
	}
	
	public static class SimpleDisplayImageOptionsCreator implements DisplayImageOptionsCreator{
		@Override
		public DisplayImageOptions createDisplayImageOptions(ImageView view, String url, int defaultResId) {
			return new DisplayImageOptions.Builder()
				.showImageOnLoading(defaultResId)
				.showImageForEmptyUri(defaultResId)
				.showImageOnFail(defaultResId)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.displayer(new XBitmapDisplayer())
				.resetViewBeforeLoading(true)
				.bitmapConfig(Bitmap.Config.ARGB_8888)
				.build();
		}
	}
}
