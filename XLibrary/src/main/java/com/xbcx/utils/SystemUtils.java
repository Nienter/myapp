package com.xbcx.utils;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.LocalActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore.Images;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.ClipboardManager;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.collection.ArrayMap;

import com.xbcx.core.ActivityBasePlugin;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.NameObject;
import com.xbcx.core.XApplication;

import org.apache.http.HttpHost;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class SystemUtils {
	
	private static float 	sDensity 			= 0;
	
	private static int		sArmArchitecture 	= -1;
	
	public static int getArmArchitecture() {
		if (sArmArchitecture != -1)
			return sArmArchitecture;
		try {
			InputStream is = new FileInputStream("/proc/cpuinfo");
			InputStreamReader ir = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(ir);
			try {
				String name = "CPU architecture";
				while (true) {
					String line = br.readLine();
					String[] pair = line.split(":");
					if (pair.length != 2)
						continue;
					String key = pair[0].trim();
					String val = pair[1].trim();
					if (key.compareToIgnoreCase(name) == 0) {
						String n = val.substring(0, 1);
						sArmArchitecture = Integer.parseInt(n);
						break;
					}
				}
			} finally {
				br.close();
				ir.close();
				is.close();
				if (sArmArchitecture == -1)
					sArmArchitecture = 6;
			}
		} catch (Exception e) {
			sArmArchitecture = 6;
		}
		return sArmArchitecture;
	}
	
	public static boolean isNum(String str){
		return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
	}
	
	public static boolean isWifi(Context context){
		ConnectivityManager cm = (ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo ni = cm.getActiveNetworkInfo();
		if(ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI){
			return true;
		}
		return false;
	}
	
	public static boolean isInBackground(Context context){
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> listAppProcessInfo = am.getRunningAppProcesses();
		if(listAppProcessInfo != null){
			final String strPackageName = context.getPackageName();
			for(RunningAppProcessInfo pi : listAppProcessInfo){
				if(pi.processName.equals(strPackageName)){
					if(pi.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
							pi.importance != RunningAppProcessInfo.IMPORTANCE_VISIBLE){
						return true;
					}else{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public static boolean isNetworkAvailable(Context context){
		ConnectivityManager cm = (ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo ni = cm.getActiveNetworkInfo();
		if(ni != null && ni.getState() == NetworkInfo.State.CONNECTED){
			return true;
		}
		return false;
	}
	
	public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
        	return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
            	apiKey = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {

        }
        return apiKey;
    }
	
	public static void launchHome(Activity activity){
		try{
			Intent intent = new Intent(Intent.ACTION_MAIN);
	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        intent.addCategory(Intent.CATEGORY_HOME);
	        activity.startActivity(intent);
		}catch(Exception e){
			e.printStackTrace();
			activity.finish();
		}
	}
	
	public static int randomRange(int nStart,int nEnd){
		if(nStart >= nEnd){
			return nEnd;
		}
		return nStart + (int)(Math.random() * (nEnd - nStart));
	}
	
	public static boolean isExternalStorageMounted(){
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
	
	public static String getExternalCachePath(Context context){
		return buildExternalCachePath(context);
	}
	
	private static String buildExternalCachePath(Context context){
		return Environment.getExternalStorageDirectory().getPath() + 
				"/Android/data/" + context.getPackageName() + "/cache";
	}
	
	public static String getExternalPath(Context context){
		return buildExternalPath(context);
	}
	
	private static String buildExternalPath(Context context){
		return Environment.getExternalStorageDirectory().getPath() + 
				"/Android/data/" + context.getPackageName();
	}
	
	public static int dipToPixel(Context context,int nDip){
		if(sDensity == 0){
			final WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
			DisplayMetrics dm = new DisplayMetrics();
			wm.getDefaultDisplay().getMetrics(dm);
			sDensity = dm.density;
		}
		return (int)(sDensity * nDip);
	}
	
	public static int dipToPixel(Context context,float dip){
		if(sDensity == 0){
			final WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
			DisplayMetrics dm = new DisplayMetrics();
			wm.getDefaultDisplay().getMetrics(dm);
			sDensity = dm.density;
		}
		return (int)(sDensity * dip);
	}
	
	public static String getMacAddress(Context context){
		final WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		return wm.getConnectionInfo().getMacAddress();
	}
	
	public static void printCallStack(){
		for(StackTraceElement e : new Throwable().getStackTrace()){
			System.out.println(e.toString());
		}
	}
	
	public static void copyToClipBoard(Context context,String strText){
		final ClipboardManager manager = (ClipboardManager)context.getSystemService(
				Context.CLIPBOARD_SERVICE);
		manager.setText(strText);
	}
	
	public static boolean isEmail(String strEmail){
		Pattern pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
		Matcher matcher = pattern.matcher(strEmail);
		return matcher.matches();
	}
	
	public static String getVersionName(Context context){
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static int getVersionCode(Context context){
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
			en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); 
			enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    return inetAddress.getHostAddress().toString();
	                }
	            }
	        }
	    } catch (SocketException ex) {
	    	ex.printStackTrace();
	    }
	    return null;
	}
	
	public static Location getLocation(Context context){
		final LocationManager locationManager = (LocationManager)context
				.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		final String strProvider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(strProvider);
		try{
			if(location == null){
				location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			if(location == null){
				location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
		}catch(Exception e){
			
		}
		return location;
	}
	
	public static void	addEditTextLengthFilter(EditText editText,int nLengthLimit){
		InputFilter filters[] = editText.getFilters();
		if(filters == null){
			editText.getEditableText().setFilters(
					new InputFilter[]{new InputFilter.LengthFilter(nLengthLimit)});
		}else{
			List<InputFilter> newFilters = new ArrayList<InputFilter>();
			for(InputFilter filter : filters){
				if(!(filter instanceof InputFilter.LengthFilter)){
					newFilters.add(filter);
				}
			}
			newFilters.add(new InputFilter.LengthFilter(nLengthLimit));
			InputFilter arrFilters[] = new InputFilter[newFilters.size()];
			newFilters.toArray(arrFilters);
			editText.getEditableText().setFilters(arrFilters);
		}
	}
	
	public static void addEditTextInputFilter(EditText et,InputFilter filter){
		if(filter == null){
			return;
		}
		InputFilter filters[] = et.getFilters();
		if(filters == null){
			et.getEditableText().setFilters(
					new InputFilter[]{filter});
		}else{
			final int nSize = filters.length + 1;
			InputFilter newFilters[] = new InputFilter[nSize];
			int nIndex = 0;
			for(InputFilter f : filters){
				newFilters[nIndex++] = f;
			}
			newFilters[nIndex] = filter;
			et.getEditableText().setFilters(newFilters);
		}
	}
	
	public static boolean getCursorBoolean(Cursor cursor,int nColumnIndex){
		return cursor.getInt(nColumnIndex) == 1 ? true : false;
	}
	
	public static boolean safeSetImageBitmap(ImageView iv,String path){
		return safeSetImageBitmap(iv, path, 512, 512);
	}
	
	public static boolean safeSetImageBitmap(ImageView iv,String path,int reqWidth,int reqHeight){
		final Bitmap bmp = decodeSampledBitmapFromFilePath(path, reqWidth, reqHeight);
		if(bmp != null){
			iv.setImageBitmap(bmp);
			return true;
		}
		return false;
	}
	
	public static Bitmap getVideoThumbnail(String filePath){
		return getVideoThumbnail(filePath, dipToPixel(XApplication.getApplication(), 100));
	}
	
	public static Bitmap getVideoThumbnail(String filePath,int maxSize){
		Bitmap bmp = ThumbnailUtils.createVideoThumbnail(filePath, Images.Thumbnails.MINI_KIND);
		if(bmp != null){
			final int width = bmp.getWidth();
			final int height = bmp.getHeight();
			if(width > maxSize || height > maxSize){
				int fixWidth = 0;
				int fixHeight = 0;
				if(height > width){
					fixHeight = maxSize;
					fixWidth = width * fixHeight / height;
				}else{
					fixWidth = maxSize;
					fixHeight = height * fixWidth / width;
				}
				bmp = Bitmap.createScaledBitmap(bmp, fixWidth, fixHeight, false);
			}
		}
		return bmp;
	}
	
	public static int nextPowerOf2(int n) {
        n -= 1;
        n |= n >>> 16;
        n |= n >>> 8;
        n |= n >>> 4;
        n |= n >>> 2;
        n |= n >>> 1;
        return n + 1;
    }
	
	public static int computeSampleSize(BitmapFactory.Options options,String path,
            int minSideLength, int maxNumOfPixels) {
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		
		if(options.outWidth != -1){
			options.inJustDecodeBounds = false;
			
	        int initialSize = computeInitialSampleSize(options, minSideLength,
	                maxNumOfPixels);

	        int roundedSize;
	        if (initialSize <= 8) {
	            roundedSize = 1;
	            while (roundedSize < initialSize) {
	                roundedSize <<= 1;
	            }
	        } else {
	            roundedSize = (initialSize + 7) / 8 * 8;
	        }
	        
	        options.inSampleSize = roundedSize;

	        return roundedSize;
		}else{
			return -1;
		}
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels < 0) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength < 0) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if (maxNumOfPixels < 0 && minSideLength < 0) {
            return 1;
        } else if (minSideLength < 0) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
    
    public static boolean compressBitmapFile(String dstPath,String srcPath,int reqWidth,int reqHeight){
    	final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(srcPath, options);
        if(options.outWidth > 0){
        	if(options.outWidth > reqWidth || options.outHeight > reqHeight){
        		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;
                try{
                	Bitmap bmp = BitmapFactory.decodeFile(srcPath,options);
                	FileHelper.saveBitmapToFile(dstPath, bmp, 90);
                }catch(OutOfMemoryError e){
                	e.printStackTrace();
                	return false;
                }
        	}else{
        		FileHelper.copyFile(dstPath, srcPath);
        	}
        }else{
        	return false;
        }
        return true;
    }
    
    public static boolean compressBitmap(String dstPath,Bitmap bmp,int reqWidth,int reqHeight){
    	final BitmapFactory.Options options = new BitmapFactory.Options();
    	options.outWidth = bmp.getWidth();
    	options.outHeight = bmp.getHeight();
    	if(options.outWidth > reqWidth || options.outHeight > reqHeight){
    		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            bmp = Bitmap.createScaledBitmap(bmp, options.outWidth / options.inSampleSize, 
            		options.outHeight / options.inSampleSize, true);
            try{
            	FileHelper.saveBitmapToFile(dstPath, bmp, 90);
            	bmp.recycle();
            }catch(OutOfMemoryError e){
            	e.printStackTrace();
            	return false;
            }
    	}else{
    		FileHelper.saveBitmapToFile(dstPath, bmp);
    	}
        return true;
    }
    
    public static Bitmap decodeSampledBitmapFromFilePath(String path,int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;
        BitmapFactory.decodeFile(path, options);
        if(options.outWidth > 0){
        	options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            try{
            	return BitmapFactory.decodeFile(path,options);
            }catch(OutOfMemoryError e){
            	e.printStackTrace();
            	options.inSampleSize = options.inSampleSize * 2;
            	try{
            		return BitmapFactory.decodeFile(path, options);
            	}catch(OutOfMemoryError e1){
            		e1.printStackTrace();
            	}
            }
        }
        return null;
    }
    
    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            final float totalPixels = width * height;

            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }
    
    public static boolean isArrayContain(Object[] objs,Object item){
    	for(Object obj : objs){
    		if(obj.equals(item)){
    			return true;
    		}
    	}
    	return false;
    }
    
    public static String throwableToString(Throwable e){
    	StringBuffer sb = new StringBuffer();
		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		e.printStackTrace(pw);
		Throwable cause = e.getCause();

		while (cause != null) {
			cause.printStackTrace(pw);
			cause = cause.getCause();
		}
		pw.close();
		String result = writer.toString();
		result = result.replaceAll("\n", "\r\n");
		sb.append(result);
		final String ret = sb.toString();
		if(TextUtils.isEmpty(ret)){
			return e.getMessage();
		}
		return ret;
    }
    
    public static byte[] objectToByteArray(Object obj) throws IOException{
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream bos = new ObjectOutputStream(baos);
		bos.writeObject(obj);
		try{
			return baos.toByteArray();
		}finally{
			bos.close();
		}
    }
    
    public static Object byteArrayToObject(byte[] data) throws 
    StreamCorruptedException, IOException, ClassNotFoundException{
    	if(data == null){
    		return null;
    	}
    	ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bais);
		try{
			return ois.readObject();
		}finally{
			ois.close();
		}
    }
    
    public static int getPictureExifRotateAngle(String path){
    	int rotate = 0;
		try{
			ExifInterface ei = new ExifInterface(path);
			int ori = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			if(ori == ExifInterface.ORIENTATION_ROTATE_180){
				rotate = 180;
			}else if(ori == ExifInterface.ORIENTATION_ROTATE_270){
				rotate = 270;
			}else if(ori == ExifInterface.ORIENTATION_ROTATE_90){
				rotate = 90;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return rotate;
    }
    
    public static void filterEnterKey(EditText et){
    	et.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
    	et.setSingleLine();
    }
    
    public static boolean handleJumpIntent(Activity activity,Intent intent){
    	final Bundle b = intent.getBundleExtra("jump");
		if(b != null){
			final String className = b.getString("class_name");
			try{
				Intent i = new Intent(activity, Class.forName(className));
				i.putExtras(b);
				activity.startActivity(i);
				return true;
			}catch(Exception e){
			}
		}
		return false;
    }
    
    public static int safeParseInt(String s){
    	try{
    		return Integer.parseInt(s);
    	}catch(Exception e){
    	}
    	return 0;
    }
    
    public static long safeParseLong(String s){
    	try{
    		return Long.parseLong(s);
    	}catch(Exception e){
    	}
    	return 0;
    }
    
    public static void setPaddingLeft(View v,int p){
    	v.setPadding(p, v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom());
    }
    
    public static void setPaddingBottom(View v,int p){
    	v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), p);
    }

    public static void launchActivity(Activity activity,Class<?> clazz){
    	Intent i = new Intent(activity, clazz);
    	activity.startActivity(i);
    }
    
    public static void launchActivity(Activity activity,Class<?> clazz,Bundle b){
    	Intent i = new Intent(activity, clazz);
    	if(b != null){
    		i.putExtras(b);
    	}
    	activity.startActivity(i);
    }
    
    public static void launchActivityClearTop(Activity activity,Class<?> clazz,Bundle b){
    	Intent i = new Intent(activity, clazz);
    	i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	if(b != null){
    		i.putExtras(b);
    	}
    	activity.startActivity(i);
    }
    
    public static void launchIDActivity(Activity activity,Class<?> clazz,String id){
    	Intent i = new Intent(activity, clazz);
    	i.putExtra("id", id);
    	activity.startActivity(i);
    }
    
    public static void launchActivityForResult(Activity activity,Class<?> clazz,int requestCode){
    	Intent i = new Intent(activity, clazz);
    	activity.startActivityForResult(i, requestCode);
    }
    
    public static void launchActivityForResult(Activity activity,Class<?> clazz,Bundle b,int requestCode){
    	Intent i = new Intent(activity, clazz);
    	if(b != null){
    		i.putExtras(b);
    	}
    	activity.startActivityForResult(i, requestCode);
    }
    
    public static void launchIDActivityForResult(Activity activity,Class<?> clazz,String id,int requestCode){
    	Intent i = new Intent(activity, clazz);
    	i.putExtra("id", id);
    	activity.startActivityForResult(i, requestCode);
    }
    
    public static void launchIDAndNameActivity(Activity activity,Class<?> clazz,NameObject no){
    	launchIDAndNameActivity(activity, clazz, no.getId(), no.getName());
    }
    
    public static void launchIDAndNameActivity(Activity activity,Class<?> clazz,String id,String name){
    	Intent i = new Intent(activity, clazz);
    	i.putExtra("id", id);
    	i.putExtra("name", name);
    	activity.startActivity(i);
    }
    
    public static View	inflate(Context context,int layoutId){
		return LayoutInflater.from(context).inflate(layoutId, null);
	}
    
    public static void	setTextColorResId(TextView tv,int colorResId){
    	tv.setTextColor(tv.getResources().getColor(colorResId));
    }
    
    public static boolean hasEmoji(CharSequence text){
    	try {
			final int nLength = text.length();
			byte byteTemp[] = text.toString().getBytes("UTF-16");
			int nTemp = 0;
			for(int nIndex = 0;nIndex < nLength;++nIndex){
				nTemp = 2 + nIndex * 2;
				final int nCode = ((((int)byteTemp[nTemp + 1]) << 8) & 0xffff) + 
					(((int)byteTemp[nTemp]) & 0xff);
				if(nCode >= 0xd800 && nCode <= 0xf8ff){
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return false;
    }
    
    public static void showPopupWindowByListItem(PopupWindow pw,ListView lv,View itemView){
    	final Rect r = new Rect();
		final int location[] = new int[2];
		itemView.getLocationOnScreen(location);
		itemView.getLocalVisibleRect(r);
		pw.showAtLocation(lv,
				Gravity.TOP | Gravity.LEFT, 
				XApplication.getScreenWidth() / 2 - pw.getWidth() / 2,
				location[1] - pw.getHeight() + SystemUtils.dipToPixel(lv.getContext(), 10));
    }
    
    public static void callPhone(Activity activity,String phone){
    	try{
    		Intent intent = new Intent();
    		intent.setAction(Intent.ACTION_DIAL);
    		intent.setData(Uri.parse("tel:"+phone));
    		activity.startActivity(intent);
    	}catch(Exception e){
    	}
    }
    
    public static boolean setProxy(WebView webview, String host, int port) {
        // 3.2 (HC) or lower
        if (Build.VERSION.SDK_INT <= 13) {
            return setProxyUpToHC(webview, host, port);
        }
        // ICS: 4.0
        else if (Build.VERSION.SDK_INT <= 15) {
            return setProxyICS(webview, host, port);
        }
        // 4.1-4.3 (JB)
        else if (Build.VERSION.SDK_INT <= 18) {
            return setProxyJB(webview, host, port);
        }
        // 4.4 (KK)
        else {
            return setProxyKK(webview, host, port);
        }
    }
    
    private static String LOG_TAG = "xbcx";

    /**
     * Set Proxy for Android 3.2 and below.
     */
    @SuppressWarnings("all")
    private static boolean setProxyUpToHC(WebView webview, String host, int port) {
        HttpHost proxyServer = new HttpHost(host, port);
        // Getting network
        Class networkClass = null;
        Object network = null;
        try {
            networkClass = Class.forName("android.webkit.Network");
            if (networkClass == null) {
                return false;
            }
            Method getInstanceMethod = networkClass.getMethod("getInstance", Context.class);
            if (getInstanceMethod == null) {
                Log.e("xbcx", "failed to get getInstance method");
            }
            network = getInstanceMethod.invoke(networkClass, new Object[]{webview.getContext()});
        } catch (Exception ex) {
            Log.e("xbcx", "error getting network: " + ex);
            return false;
        }
        if (network == null) {
            Log.e("xbcx", "error getting network: network is null");
            return false;
        }
        Object requestQueue = null;
        try {
            Field requestQueueField = networkClass
                    .getDeclaredField("mRequestQueue");
            requestQueue = getFieldValueSafely(requestQueueField, network);
        } catch (Exception ex) {
            Log.e("xbcx", "error getting field value");
            return false;
        }
        if (requestQueue == null) {
            Log.e("xbcx", "Request queue is null");
            return false;
        }
        Field proxyHostField = null;
        try {
            Class requestQueueClass = Class.forName("android.net.http.RequestQueue");
            proxyHostField = requestQueueClass
                    .getDeclaredField("mProxyHost");
        } catch (Exception ex) {
            Log.e("xbcx", "error getting proxy host field");
            return false;
        }

        boolean temp = proxyHostField.isAccessible();
        try {
            proxyHostField.setAccessible(true);
            proxyHostField.set(requestQueue, proxyServer);
        } catch (Exception ex) {
            Log.e(LOG_TAG, "error setting proxy host");
        } finally {
            proxyHostField.setAccessible(temp);
        }

        Log.d(LOG_TAG, "Setting proxy with <= 3.2 API successful!");
        return true;
    }

    @SuppressWarnings("all")
    private static boolean setProxyICS(WebView webview, String host, int port) {
        try
        {
            Log.d(LOG_TAG, "Setting proxy with 4.0 API.");

            Class jwcjb = Class.forName("android.webkit.JWebCoreJavaBridge");
            Class params[] = new Class[1];
            params[0] = Class.forName("android.net.ProxyProperties");
            Method updateProxyInstance = jwcjb.getDeclaredMethod("updateProxy", params);

            Class wv = Class.forName("android.webkit.WebView");
            Field mWebViewCoreField = wv.getDeclaredField("mWebViewCore");
            Object mWebViewCoreFieldInstance = getFieldValueSafely(mWebViewCoreField, webview);

            Class wvc = Class.forName("android.webkit.WebViewCore");
            Field mBrowserFrameField = wvc.getDeclaredField("mBrowserFrame");
            Object mBrowserFrame = getFieldValueSafely(mBrowserFrameField, mWebViewCoreFieldInstance);

            Class bf = Class.forName("android.webkit.BrowserFrame");
            Field sJavaBridgeField = bf.getDeclaredField("sJavaBridge");
            Object sJavaBridge = getFieldValueSafely(sJavaBridgeField, mBrowserFrame);

            Class ppclass = Class.forName("android.net.ProxyProperties");
            Class pparams[] = new Class[3];
            pparams[0] = String.class;
            pparams[1] = int.class;
            pparams[2] = String.class;
            Constructor ppcont = ppclass.getConstructor(pparams);

            updateProxyInstance.invoke(sJavaBridge, ppcont.newInstance(host, port, null));

            Log.d(LOG_TAG, "Setting proxy with 4.0 API successful!");
            return true;
        }
        catch (Exception ex)
        {
            Log.e(LOG_TAG, "failed to set HTTP proxy: " + ex);
            return false;
        }
    }

    /**
     * Set Proxy for Android 4.1 - 4.3.
     */
    @SuppressWarnings("all")
    private static boolean setProxyJB(WebView webview, String host, int port) {
        Log.d(LOG_TAG, "Setting proxy with 4.1 - 4.3 API.");

        try {
            Class wvcClass = Class.forName("android.webkit.WebViewClassic");
            Class wvParams[] = new Class[1];
            wvParams[0] = Class.forName("android.webkit.WebView");
            Method fromWebView = wvcClass.getDeclaredMethod("fromWebView", wvParams);
            Object webViewClassic = fromWebView.invoke(null, webview);

            Class wv = Class.forName("android.webkit.WebViewClassic");
            Field mWebViewCoreField = wv.getDeclaredField("mWebViewCore");
            Object mWebViewCoreFieldInstance = getFieldValueSafely(mWebViewCoreField, webViewClassic);

            Class wvc = Class.forName("android.webkit.WebViewCore");
            Field mBrowserFrameField = wvc.getDeclaredField("mBrowserFrame");
            Object mBrowserFrame = getFieldValueSafely(mBrowserFrameField, mWebViewCoreFieldInstance);

            Class bf = Class.forName("android.webkit.BrowserFrame");
            Field sJavaBridgeField = bf.getDeclaredField("sJavaBridge");
            Object sJavaBridge = getFieldValueSafely(sJavaBridgeField, mBrowserFrame);

            Class ppclass = Class.forName("android.net.ProxyProperties");
            Class pparams[] = new Class[3];
            pparams[0] = String.class;
            pparams[1] = int.class;
            pparams[2] = String.class;
            Constructor ppcont = ppclass.getConstructor(pparams);

            Class jwcjb = Class.forName("android.webkit.JWebCoreJavaBridge");
            Class params[] = new Class[1];
            params[0] = Class.forName("android.net.ProxyProperties");
            Method updateProxyInstance = jwcjb.getDeclaredMethod("updateProxy", params);

            updateProxyInstance.invoke(sJavaBridge, ppcont.newInstance(host, port, null));
        } catch (Exception ex) {
            Log.e(LOG_TAG,"Setting proxy with >= 4.1 API failed with error: " + ex.getMessage());
            return false;
        }

        Log.d(LOG_TAG, "Setting proxy with 4.1 - 4.3 API successful!");
        return true;
    }

    @SuppressWarnings("all")
    private static boolean setProxyKK(WebView webView, String host, int port) {
        Log.d(LOG_TAG, "Setting proxy with >= 4.4 API.");

        Context appContext = webView.getContext().getApplicationContext();
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port + "");
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port + "");
        try {
            Class applictionCls = XApplication.getApplication().getClass();
            Field loadedApkField = applictionCls.getField("mLoadedApk");
            loadedApkField.setAccessible(true);
            Object loadedApk = loadedApkField.get(appContext);
            Class loadedApkCls = Class.forName("android.app.LoadedApk");
            Field receiversField = loadedApkCls.getDeclaredField("mReceivers");
            receiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) receiversField.get(loadedApk);
            for (Object receiverMap : receivers.values()) {
                for (Object rec : ((ArrayMap) receiverMap).keySet()) {
                    Class clazz = rec.getClass();
                    if (clazz.getName().contains("ProxyChangeListener")) {
                        Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);

                        /*********** optional, may be need in future *************/
                        final String CLASS_NAME = "android.net.ProxyProperties";
                        Class cls = Class.forName(CLASS_NAME);
                        Constructor constructor = cls.getConstructor(String.class, Integer.TYPE, String.class);
                        constructor.setAccessible(true);
                        Object proxyProperties = constructor.newInstance(host, port, null);
                        intent.putExtra("proxy", (Parcelable) proxyProperties);
                        /*********** optional, may be need in future *************/

                        onReceiveMethod.invoke(rec, appContext, intent);
                    }
                }
            }

            Log.d(LOG_TAG, "Setting proxy with >= 4.4 API successful!");
            return true;
        } catch (ClassNotFoundException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        } catch (NoSuchFieldException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        } catch (IllegalAccessException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        } catch (IllegalArgumentException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        } catch (NoSuchMethodException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        } catch (InvocationTargetException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        } catch (InstantiationException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(LOG_TAG, e.getMessage());
            Log.v(LOG_TAG, exceptionAsString);
        }
        return false;
    }

    private static Object getFieldValueSafely(Field field, Object classInstance) throws IllegalArgumentException, IllegalAccessException {
        boolean oldAccessibleValue = field.isAccessible();
        field.setAccessible(true);
        Object result = field.get(classInstance);
        field.setAccessible(oldAccessibleValue);
        return result;
    }
    
    public static String getPhoneModel(){
    	if(Build.MODEL.toLowerCase(Locale.getDefault()).contains(
				Build.BRAND.toLowerCase(Locale.getDefault()))){
			return Build.MODEL;
		}else{
			return Build.BRAND + " " + Build.MODEL;
		}
    }
    
    public static String getTrimText(EditText editText){
    	String text = editText.getText().toString().trim();
		return text;
    }
    
    public static boolean isTrimEmpty(String text){
    	if(text != null){
    		text = text.trim();
    	}
    	return TextUtils.isEmpty(text);
    }
    
    public static boolean isWapNet() {  
    	final Context mContext = XApplication.getApplication();
        try {  
            final ConnectivityManager connectivityManager = (ConnectivityManager) mContext  
                    .getSystemService(Context.CONNECTIVITY_SERVICE);  
            final NetworkInfo mobNetInfoActivity = connectivityManager  
                    .getActiveNetworkInfo();  
            if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) {  
            	return false;
            } else {  
                // NetworkInfo不为null开始判断是网络类型  
                int netType = mobNetInfoActivity.getType();  
                if (netType == ConnectivityManager.TYPE_WIFI) {  
                    // wifi net处理  
                    return false;  
                } else if (netType == ConnectivityManager.TYPE_MOBILE) {  
                    // 注意二：  
                    // 判断是否电信wap:  
                    // 不要通过getExtraInfo获取接入点名称来判断类型，  
                    // 因为通过目前电信多种机型测试发现接入点名称大都为#777或者null，  
                    // 电信机器wap接入点中要比移动联通wap接入点多设置一个用户名和密码,  
                    // 所以可以通过这个进行判断！  
  
                    boolean is3G = isFastMobileNetwork(mContext);
                    if(is3G){
                    	return false;
                    }
  
                    final Cursor c = mContext.getContentResolver().query(  
                    		Uri.parse("content://telephony/carriers/preferapn"), null, null, null, null);  
                    if (c != null) {  
                        c.moveToFirst();  
                        final String user = c.getString(c  
                                .getColumnIndex("user"));  
                        if (!TextUtils.isEmpty(user)) {  
                            if (user.startsWith("ctwap")) { 
                            	return true;
                            } 
//                            else if (user.startsWith("ctnet")) {  
//                                return true;
//                            }  
                        }  
                    }  
                    c.close();  
  
                    // 注意三：  
                    // 判断是移动联通wap:  
                    // 其实还有一种方法通过getString(c.getColumnIndex("proxy")获取代理ip  
                    // 来判断接入点，10.0.0.172就是移动联通wap，10.0.0.200就是电信wap，但在  
                    // 实际开发中并不是所有机器都能获取到接入点代理信息，例如魅族M9 （2.2）等...  
                    // 所以采用getExtraInfo获取接入点名字进行判断  
  
                    String netMode = mobNetInfoActivity.getExtraInfo();   
                    if (netMode != null) {  
                        // 通过apn名称判断是否是联通和移动wap  
                        netMode = netMode.toLowerCase(Locale.getDefault());  
                        if (netMode.equals("cmwap") ||
                        		netMode.equals("3gwap") || 
                        		netMode.equals("uniwap")) {  
                            return true;  
                        } 
//                        else if (netMode.equals("cmnet")) {  
//                            return true; 
//                        } else if (netMode.equals("3gnet")  
//                                || netMode.equals("uninet")) {  
//                            return true;
//                        }  
                    }  
                }  
            }  
  
        } catch (Exception ex) {  
            ex.printStackTrace();  
        }  
        return false;  
    }  
  
    private static boolean isFastMobileNetwork(Context context) {  
        TelephonyManager telephonyManager = (TelephonyManager) context  
                .getSystemService(Context.TELEPHONY_SERVICE);  
  
        switch (telephonyManager.getNetworkType()) {  
        case TelephonyManager.NETWORK_TYPE_1xRTT:  
            return false; // ~ 50-100 kbps  
        case TelephonyManager.NETWORK_TYPE_CDMA:  
            return false; // ~ 14-64 kbps  
        case TelephonyManager.NETWORK_TYPE_EDGE:  
            return false; // ~ 50-100 kbps  
        case TelephonyManager.NETWORK_TYPE_EVDO_0:  
            return true; // ~ 400-1000 kbps  
        case TelephonyManager.NETWORK_TYPE_EVDO_A:  
            return true; // ~ 600-1400 kbps  
        case TelephonyManager.NETWORK_TYPE_GPRS:  
            return false; // ~ 100 kbps  
        case TelephonyManager.NETWORK_TYPE_HSDPA:  
            return true; // ~ 2-14 Mbps  
        case TelephonyManager.NETWORK_TYPE_HSPA:  
            return true; // ~ 700-1700 kbps  
        case TelephonyManager.NETWORK_TYPE_HSUPA:  
            return true; // ~ 1-23 Mbps  
        case TelephonyManager.NETWORK_TYPE_UMTS:  
            return true; // ~ 400-7000 kbps  
        case TelephonyManager.NETWORK_TYPE_EHRPD:  
            return true; // ~ 1-2 Mbps  
        case TelephonyManager.NETWORK_TYPE_EVDO_B:  
            return true; // ~ 5 Mbps  
        case TelephonyManager.NETWORK_TYPE_HSPAP:  
            return true; // ~ 10-20 Mbps  
        case TelephonyManager.NETWORK_TYPE_IDEN:  
            return false; // ~25 kbps  
        case TelephonyManager.NETWORK_TYPE_LTE:  
            return true; // ~ 10+ Mbps  
        case TelephonyManager.NETWORK_TYPE_UNKNOWN:  
            return false;  
        default:  
            return false;  
  
        }  
    } 
    
    public static boolean isGPSEnable(Context context){
    	try{
    		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        	return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return false;
    }
    
	public static void launchGPSSetup(Context context) {
		Intent intent = new Intent();
		intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException ex) {
			intent.setAction(Settings.ACTION_SETTINGS);
			try {
				context.startActivity(intent);
			} catch (Exception e) {
			}
		}
	}
	
	public static boolean isAirplaneOn(Context context){
		return (Settings.System.getInt(context.getContentResolver(), 
				Settings.System.AIRPLANE_MODE_ON, 0) == 1 ? true : false); 
	}
	
	public static boolean nameFilter(NameObject no,String key){
		if(no == null){
			return false;
		}
		final String pinyin = PinyinUtils.getPinyin(no.getName()).toLowerCase(Locale.getDefault());
		final String fixKey = key.toLowerCase(Locale.getDefault());
		return pinyin.contains(fixKey) || 
				no.getName().toLowerCase(Locale.getDefault()).contains(fixKey);
	}
	
	public static boolean nameFilter(String name,String key){
		if(TextUtils.isEmpty(name)){
			return false;
		}
		final String pinyin = PinyinUtils.getPinyin(name).toLowerCase(Locale.getDefault());
		final String fixKey = key.toLowerCase(Locale.getDefault());
		return pinyin.contains(fixKey) || 
				name.toLowerCase(Locale.getDefault()).contains(fixKey);
	}
	
	public static Class<?> getSingleGenericClass(Class<?> subClass,Class<?> genericClass){
		for(Class<?> clazz = subClass;clazz != genericClass && clazz != null;clazz = clazz.getSuperclass()){
			int index = 0;
			Type genType = clazz.getGenericSuperclass();
			if (!(genType instanceof ParameterizedType)) {
				continue;
			}
			Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
			if (index >= params.length || index < 0) {
				continue;
			}
			if (!(params[index] instanceof Class)) {
				continue;
			}
			return (Class<?>)params[index];
		}
		return null;
	}
	
	public static Intent createSingleTaskIntent(Context context,Class<?> cls){
		Intent i = new Intent(context, cls);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		return i;
	}
	
	public static boolean isMockGPSSettingOpen(Context context) {
		return Settings.Secure.getInt(context.getContentResolver(),
				Settings.Secure.ALLOW_MOCK_LOCATION, 0) == 1;
	}

	public static boolean isDataOpen(Context context) {
		return Settings.Secure.getInt(context.getContentResolver(),
				"mobile_data", 0) == 1;
	}
	
	public static boolean destroy(ActivityGroup activityGroup, String id) {
		final LocalActivityManager activityManager = activityGroup
				.getLocalActivityManager();
		if (activityManager != null) {
			activityManager.destroyActivity(id, false);
			try {
				final Field mActivitiesField = LocalActivityManager.class
						.getDeclaredField("mActivities");
				if (mActivitiesField != null) {
					mActivitiesField.setAccessible(true);
					@SuppressWarnings("unchecked")
					final Map<String, Object> mActivities = (Map<String, Object>) mActivitiesField
							.get(activityManager);
					if (mActivities != null) {
						mActivities.remove(id);
					}
					final Field mActivityArrayField = LocalActivityManager.class
							.getDeclaredField("mActivityArray");
					if (mActivityArrayField != null) {
						mActivityArrayField.setAccessible(true);
						@SuppressWarnings("unchecked")
						final ArrayList<Object> mActivityArray = (ArrayList<Object>) mActivityArrayField
								.get(activityManager);
						if (mActivityArray != null) {
							for (Object record : mActivityArray) {
								final Field idField = record.getClass()
										.getDeclaredField("id");
								if (idField != null) {
									idField.setAccessible(true);
									final String _id = (String) idField
											.get(record);
									if (id.equals(_id)) {
										mActivityArray.remove(record);
										break;
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
	
	public static void addPluginClassName(Intent intent,Class<? extends ActivityBasePlugin> pluginCls){
		ArrayList<String> clsNames = intent.getStringArrayListExtra(BaseActivity.Extra_InputPluginClassNames);
		if(clsNames == null){
			clsNames = new ArrayList<String>();
		}
		clsNames.add(pluginCls.getName());
		intent.putStringArrayListExtra(BaseActivity.Extra_InputPluginClassNames, clsNames);
	}
	
	public static List<String> getInputPluginClassNames(Activity activity){
		List<String> clsNames = activity.getIntent().getStringArrayListExtra(BaseActivity.Extra_InputPluginClassNames);
		if(clsNames == null){
			return Collections.emptyList();
		}
		return clsNames;
	}
	
	public static void registerInputPlugins(BaseActivity activity){
		for(String s : getInputPluginClassNames(activity)){
			try{
				Class<?> cls = Class.forName(s);
				Constructor<?> c = cls.getDeclaredConstructor();
				c.setAccessible(true);
				activity.registerPlugin((ActivityBasePlugin)c.newInstance());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static Method getMethod(Class<?> cls,String methodName, Class<?>... parameterTypes){
		for(Class<?> c = cls;c != null;c = c.getSuperclass()){
			try{
				Method m = c.getDeclaredMethod(methodName,parameterTypes);
				m.setAccessible(true);
				return m;
			}catch(Exception e){
			}
		}
		return null;
	}
	
	public static void requestDisallowInterceptTouchEvent(View v,boolean bDisallow){
		ViewParent vp = v.getParent();
		if(vp != null){
			vp.requestDisallowInterceptTouchEvent(bDisallow);
		}
	}
}
