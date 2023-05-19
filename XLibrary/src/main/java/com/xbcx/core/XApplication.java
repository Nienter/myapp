package com.xbcx.core;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.core.bmp.XBitmapProvider;
import com.xbcx.core.bmp.XImageDecoder;
import com.xbcx.core.bmp.XImageDownloader;
import com.xbcx.core.bmp.XTotalSizeLimitedDiscCache;
import com.xbcx.core.http.HttpDownloadRunner;
import com.xbcx.core.module.ActivityCreateListener;
import com.xbcx.core.module.ActivityDestoryListener;
import com.xbcx.core.module.ActivityPauseListener;
import com.xbcx.core.module.ActivityResumeListener;
import com.xbcx.core.module.AppBaseListener;
import com.xbcx.core.module.HttpLoginListener;
import com.xbcx.core.module.ListValueLoaderlListener;
import com.xbcx.core.module.OnLowMemoryListener;
import com.xbcx.im.IMKernel;
import com.xbcx.im.IMLoginInfo;
import com.xbcx.library.R;
import com.xbcx.utils.Encrypter;
import com.xbcx.utils.FileHelper;
import com.xbcx.utils.SystemUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.Collection;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XApplication extends Application implements OnEventListener{

	public static String URL_GetLocationImage = "http://maps.google.com/maps/api/staticmap?" +
			"center=%f,%f&" +
			"zoom=%d&" +
			"size=%dx%d&" +
			"maptype=roadmap&" +
			"markers=color:red%%7Clabel:%%7C%f,%f&" +
			"sensor=false&language=zh-Hans";

	public static XApplication getApplication(){
		return sInstance;
	}

	private static XApplication 	sInstance;

	private static Logger 			sLogger;

	private static Handler			sMainThreadHandler;

	private static int				sScreenWidth;
	private static int				sScreenHeight;
	private static int 				sScreenDpi;

	private static String 			HTTP_KEY = "";

	private static long				timeDifference;

	private static boolean			isStorageChecked;

	private static PluginHelper<AppBaseListener> 	pluginHelper = new SyncPluginHelper<AppBaseListener>();

	private static boolean			isValueLoaderResume = true;


	private static ExecutorService	executorService;

	protected static boolean	isLogEnable;

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;

		TimeZone.setDefault(TimeZone.getTimeZone("GMT+08"));

		CrashHandler.getInstance().init(this);

		sMainThreadHandler = new Handler();

		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		sScreenWidth = dm.widthPixels;
		sScreenHeight = dm.heightPixels;
		sScreenDpi = dm.densityDpi;
		if(sScreenWidth > sScreenHeight){
			int temp = sScreenWidth;
			sScreenWidth = sScreenHeight;
			sScreenHeight = temp;
		}


		executorService = Executors.newCachedThreadPool();

		AndroidEventManager.getInstance().registerEventRunner(EventCode.HTTP_Download, new HttpDownloadRunner());
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		XApplication.getLogger().info("onLowMemory");
		for(OnLowMemoryListener listener : getManagers(OnLowMemoryListener.class)){
			listener.onLowMemory();
		}
	}

	public static void addManager(AppBaseListener manager) {
		pluginHelper.addManager(manager);
	}

	public static <T extends AppBaseListener> Collection<T> getManagers(
			Class<T> cls) {
		return pluginHelper.getManagers(cls);
	}

	public static void removeManager(Object manager){
		pluginHelper.removeManager(manager);
	}

	public static PluginHelper<AppBaseListener> getPluginHelper(){
		return pluginHelper;
	}

	public void requestInitialUserModuel() {
		String[] userPwd = new String[2];
		if(IMKernel.canLogin(userPwd)){
			final String user = userPwd[0];
			IMKernel.getInstance().initialUserModule(user);
		}
	}

	public void notifyHttpLogin(Event event,JSONObject joRet){
		for(HttpLoginListener l : getManagers(HttpLoginListener.class)){
			try{
				l.onHttpLogined(event,joRet);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public static void logout() {
		IMKernel.getInstance().logout();

		NotificationManager nm = (NotificationManager)XApplication.getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancelAll();
	}

	public ImageLoaderConfiguration	onCreaterImageLoaderConfiguration(){
		ImageLoaderConfiguration.Builder b = new ImageLoaderConfiguration.Builder(this);
		onInitImageLoaderConfigurationBuilder(b);
		return b.build();
	}

	public void onInitImageLoaderConfigurationBuilder(ImageLoaderConfiguration.Builder builder){
		builder.taskExecutor(Executors.newCachedThreadPool())
		.denyCacheImageMultipleSizesInMemory()
		.tasksProcessingOrder(QueueProcessingType.LIFO)
		.memoryCache(new WeakMemoryCache())
		.memoryCacheExtraOptions(sScreenWidth * 2 / 3, sScreenWidth * 2 / 3)
		.imageDecoder(new XImageDecoder(true))
		.imageDownloader(new XImageDownloader(this))
		.diskCache(XTotalSizeLimitedDiscCache.create(
				new File(StorageUtils.getCacheDirectory(this), "images"),
				104857600));//100M
	}

	public IMLoginInfo createIMLoginInfo(String imUser,String imPwd){
		return null;
	}

	public static String	getHttpKey(){
		return HTTP_KEY;
	}

	public static void		setHttpKey(String key){
		HTTP_KEY = key;
	}

	public static long		getFixSystemTime(){
		return System.currentTimeMillis() + timeDifference;
	}

	public static long		getTimeDifference(){
		return timeDifference;
	}

	public static String getDeviceUUID(Context context){
		String uuid = SharedPreferenceDefine.getStringValue(SharedPreferenceDefine.KEY_DeviceUUID, null);
		if(TextUtils.isEmpty(uuid)){
			final TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			String strIMEI = tm.getDeviceId();
			if(TextUtils.isEmpty(strIMEI)){
				strIMEI = "1";
			}

			String strMacAddress = SystemUtils.getMacAddress(context);
			if(TextUtils.isEmpty(strMacAddress)){
				strMacAddress = "1";
			}

			uuid = strIMEI + strMacAddress;
			uuid = Encrypter.encryptBySHA1(uuid);
			SharedPreferenceDefine.setStringValue(SharedPreferenceDefine.KEY_DeviceUUID, uuid);
		}
		return uuid;
	}

	/**
	 * 校正服务器与本地时间差
	 * @param serverTime 服务器时间
     */
	public static void		updateServerTimeDifference(long serverTime){
		final long curTime = System.currentTimeMillis();
		timeDifference = serverTime - curTime;
	}

	protected void onActivityCreate(BaseActivity activity){
		for(ActivityCreateListener l : getManagers(ActivityCreateListener.class)){
			l.onActivityCreate(activity);
		}
	}

	protected void onActivityDestory(BaseActivity activity){
		for(ActivityDestoryListener l : getManagers(ActivityDestoryListener.class)){
			l.onActivityDestory(activity);
		}
	}

	protected void onActivityResume(BaseActivity activity){
		IMKernel.getInstance().requestStartIM();
		if(!isStorageChecked){
			isStorageChecked = true;
			runOnBackground(new Runnable() {
				@Override
				public void run() {
					checkExternalStorageAvailable();
				}
			});
		}
		for(ActivityResumeListener l : getManagers(ActivityResumeListener.class)){
			l.onActivityResume(activity);
		}
	}

	protected void onActivityPause(BaseActivity activity){
		for(ActivityPauseListener l : getManagers(ActivityPauseListener.class)){
			l.onActivityPause(activity);
		}
	}

	public static Logger getLogger(){
		if(sLogger == null){
			Level lev;
			if (isLogEnable){
				lev = Level.ALL;
			} else {
				lev = Level.OFF;
			}
			sLogger = Logger.getLogger(sInstance.getPackageName());
			sLogger.setLevel(lev);
			LoggerSystemOutHandler handler = new LoggerSystemOutHandler();
			handler.setLevel(lev);
			sLogger.addHandler(handler);
		}
		return sLogger;
	}

	public static Handler getMainThreadHandler(){
		return sMainThreadHandler;
	}

	public static ImageLoader	getImageLoader(){
		return XBitmapProvider.getImageLoader();
	}

	public static int	getScreenWidth(){
		return sScreenWidth;
	}

	public static int	getScreenHeight(){
		return sScreenHeight;
	}

	public static int 	getScreenDpi(){
		return sScreenDpi;
	}

	public static void runOnBackground(Runnable run){
		executorService.execute(run);
	}

	public static boolean checkExternalStorageAvailable(){
		boolean bAvailable = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
		if(bAvailable){
			String path = SystemUtils.getExternalCachePath(getApplication());
			if(!FileHelper.checkOrCreateDirectory(path)){
				ToastManager.getInstance(sInstance).show(R.string.toast_cannot_create_file_on_sdcard);
				return false;
			}
			StatFs statfs = new StatFs(Environment.getExternalStorageDirectory().getPath());
			if((long)statfs.getAvailableBlocks() * (long)statfs.getBlockSize() < 1048576){
				ToastManager.getInstance(sInstance).show(R.string.prompt_sdcard_full);
				bAvailable = false;
			}
		}else{
			ToastManager.getInstance(sInstance).show(R.string.prompt_sdcard_unavailable);
		}
		return bAvailable;
	}

	public static void pauseImageLoader(){
		isValueLoaderResume = false;
		for(ListValueLoaderlListener l : getManagers(ListValueLoaderlListener.class)){
			l.onPauseLoader();
		}
	}

	public static void resumeImageLoader(){
		isValueLoaderResume = true;
		for(ListValueLoaderlListener l : getManagers(ListValueLoaderlListener.class)){
			l.onResumeLoader();
		}
	}

	public static boolean	isImageLoaderResume(){
		return isValueLoaderResume;
	}

	public static boolean setBitmapFromFile(final ImageView iv,String filePath){
		return XBitmapProvider.setBitmapFromFile(iv, filePath);
	}
	
	/**
     * can use anywhere
     */
	public static void	setBitmap(final ImageView view,String url,int defaultResId){
		XBitmapProvider.setBitmap(view, url, defaultResId);
	}
	
	public static void setBitmap(final ImageView view,String url,int maxWidth,int maxHeight,int defaultResId){
		XBitmapProvider.setBitmap(view, url, maxWidth, maxHeight, defaultResId);
	}

	public static void	setGlideIntoImg(Context mContext, ImageView view,String url,int defaultResId){
		Glide.with(mContext).load(url).error(defaultResId).centerCrop().into(view);
	}

	public static void setGlideIntoImg(Context mContext, ImageView view,String url,int width,int height,int defaultResId){
        Glide.with(mContext).load(url).error(defaultResId).override(width, height).centerCrop().into(view);
	}
	@Override
	public void onEventRunEnd(Event event) {
	}
}
