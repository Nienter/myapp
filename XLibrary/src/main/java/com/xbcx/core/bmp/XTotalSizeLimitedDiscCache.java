package com.xbcx.core.bmp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.ext.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;
import com.nostra13.universalimageloader.utils.IoUtils.CopyListener;
import com.xbcx.core.XApplication;
import com.xbcx.utils.FileHelper;

public class XTotalSizeLimitedDiscCache extends LruDiskCache {
	
	public static DiskCache create(File cacheDir, int maxCacheSize){
		try{
			return new XTotalSizeLimitedDiscCache(cacheDir, maxCacheSize);
		}catch(Exception e){
			e.printStackTrace();
		}
		return DefaultConfigurationFactory.createDiskCache(XApplication.getApplication(), 
				new Md5FileNameGenerator(), maxCacheSize, 0);
	}

	public XTotalSizeLimitedDiscCache(File cacheDir, int maxCacheSize) throws IOException{
		super(cacheDir, new Md5FileNameGenerator(),maxCacheSize);
	}

	@Override
	public File get(String key) {
		if(key.startsWith("http")){
			return super.get(key);
		}else{
			File f = new File(key);
			if(f.exists()){
				return f;
			}else{
				return super.get(key);
			}
		}
	}
	
	@Override
	public boolean save(String imageUri, Bitmap bitmap) throws IOException {
		if(imageUri.startsWith("http") || 
				!FileHelper.isFileExists(imageUri)){
			return super.save(imageUri, bitmap);
		}
		return true;
	}
	
	@Override
	public boolean save(String imageUri, InputStream imageStream, CopyListener listener) throws IOException {
		if(imageUri.startsWith("http") || 
				!FileHelper.isFileExists(imageUri)){
			return super.save(imageUri, imageStream, listener);
		}
		return true;
	}
}
