package com.xbcx.utils;

import java.io.File;
import java.util.Locale;

import com.xbcx.core.ToastManager;
import com.xbcx.library.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

public class OpenFileUtils {
	// android获取一个用于打开HTML文件的intent
	public static Intent getHtmlFileIntent(String path) {
		Uri uri = Uri.parse(path).buildUpon()
				.encodedAuthority("com.android.htmlfileprovider")
				.scheme("content").encodedPath(path).build();
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.setDataAndType(uri, "text/html");
		return intent;
	}

	// android获取一个用于打开图片文件的intent
	public static Intent getImageFileIntent(String path) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(path));
		intent.setDataAndType(uri, "image/*");
		return intent;
	}

	// android获取一个用于打开PDF文件的intent
	public static Intent getPdfFileIntent(String path) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(path));
		intent.setDataAndType(uri, "application/pdf");
		return intent;
	}

	// android获取一个用于打开文本文件的intent
	public static Intent getTextFileIntent(String path) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(path));
		intent.setDataAndType(uri, "text/plain");
		return intent;
	}

	// android获取一个用于打开音频文件的intent
	public static Intent getAudioFileIntent(String path) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = Uri.fromFile(new File(path));
		intent.setDataAndType(uri, "audio/*");
		return intent;
	}

	// android获取一个用于打开视频文件的intent
	public static Intent getVideoFileIntent(String path) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = Uri.fromFile(new File(path));
		intent.setDataAndType(uri, "video/*");
		return intent;
	}

	// android获取一个用于打开CHM文件的intent
	public static Intent getChmFileIntent(String path) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(path));
		intent.setDataAndType(uri, "application/x-chm");
		return intent;
	}

	// android获取一个用于打开Word文件的intent
	public static Intent getWordFileIntent(String path) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(path));
		intent.setDataAndType(uri, "application/msword");
		return intent;
	}

	// android获取一个用于打开Excel文件的intent
	public static Intent getExcelFileIntent(String path) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(path));
		intent.setDataAndType(uri, "application/vnd.ms-excel");
		return intent;
	}

	// android获取一个用于打开PPT文件的intent
	public static Intent getPPTFileIntent(String path) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(path));
		intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
		return intent;
	}

	// android获取一个用于打开apk文件的intent
	public static Intent getApkFileIntent(String path) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(path)),
				"application/vnd.android.package-archive");
		return intent;
	}

	public static void 	openFile(Activity activity,String filePath,String ext){
		if(ext == null){
			ext = FileHelper.getFileExt(filePath, "");
		}
		if(!TextUtils.isEmpty(ext)){
			ext = "." + ext.toLowerCase(Locale.getDefault());
			try{
				String arrays[] = activity.getResources().getStringArray(R.array.fileEndingAudio);
				if(SystemUtils.isArrayContain(arrays, ext)){
					activity.startActivity(OpenFileUtils.getAudioFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						activity.getResources().getStringArray(R.array.fileEndingExcel), ext)){
					activity.startActivity(OpenFileUtils.getExcelFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						activity.getResources().getStringArray(R.array.fileEndingImage), ext)){
					activity.startActivity(OpenFileUtils.getImageFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						activity.getResources().getStringArray(R.array.fileEndingPackage), ext)){
					activity.startActivity(OpenFileUtils.getApkFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						activity.getResources().getStringArray(R.array.fileEndingPdf), ext)){
					activity.startActivity(OpenFileUtils.getPdfFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						activity.getResources().getStringArray(R.array.fileEndingPPT), ext)){
					activity.startActivity(OpenFileUtils.getPPTFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						activity.getResources().getStringArray(R.array.fileEndingText), ext)){
					activity.startActivity(OpenFileUtils.getTextFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						activity.getResources().getStringArray(R.array.fileEndingVideo), ext)){
					activity.startActivity(OpenFileUtils.getVideoFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						activity.getResources().getStringArray(R.array.fileEndingWebText), ext)){
					activity.startActivity(OpenFileUtils.getHtmlFileIntent(filePath));
				}else if(SystemUtils.isArrayContain(
						activity.getResources().getStringArray(R.array.fileEndingWord), ext)){
					activity.startActivity(OpenFileUtils.getWordFileIntent(filePath));
				}else{
					ToastManager.getInstance(activity).show(R.string.toast_cannot_open_file);
				}
			}catch(Exception e){
				e.printStackTrace();
				ToastManager.getInstance(activity).show(R.string.toast_cannot_open_file);
			}
		}else{
			ToastManager.getInstance(activity).show(R.string.toast_cannot_open_file);
		}
	}
}
