package com.xbcx.core;

import java.io.File;

import android.text.TextUtils;

import com.xbcx.core.XApplication;
import com.xbcx.utils.Encrypter;
import com.xbcx.utils.SystemUtils;

public class FilePaths {
	
	public static String getCameraSaveFilePath(){
		return SystemUtils.getExternalCachePath(XApplication.getApplication()) + 
				File.separator + "camera.jpg";
	}
	
	public static String getCameraTempFolderPath(){
		return SystemUtils.getExternalCachePath(XApplication.getApplication()) + 
				File.separator + "camera" + File.separator;
	}
	
	public static String getPictureChooseFilePath(){
		return SystemUtils.getExternalCachePath(XApplication.getApplication()) + 
				File.separator + "choose.jpg";
	}
	
	public static String getUrlFileCachePath(String strUrl){
		if(TextUtils.isEmpty(strUrl)){
			return SystemUtils.getExternalCachePath(XApplication.getApplication()) + 
					File.separator + "urlfile";
		}
		return SystemUtils.getExternalCachePath(XApplication.getApplication()) + 
				File.separator + "urlfile" + File.separator + Encrypter.encryptBySHA1(strUrl);
	}
	
	public static String getQrcodeSavePath(String strIMUser){
		if(TextUtils.isEmpty(strIMUser)){
			return SystemUtils.getExternalCachePath(XApplication.getApplication()) + 
					File.separator + "qrcode";
		}
		return SystemUtils.getExternalCachePath(XApplication.getApplication()) + 
				File.separator + "qrcode" + File.separator + strIMUser;
	}
	
	public static String getImportFolderPath(){
		return SystemUtils.getExternalCachePath(XApplication.getApplication()) +
				File.separator + "importfile" + File.separator;
	}
	
	public static String getCameraVideoFolderPath(){
		return SystemUtils.getExternalCachePath(XApplication.getApplication()) +
				File.separator + "videos" + File.separator;
	}
}
