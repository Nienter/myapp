package com.xbcx.core;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater.Factory;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xbcx.common.DatePickerDialogLauncher;
import com.xbcx.common.choose.ChooseCallbackInterface;
import com.xbcx.common.choose.ChooseFileProvider.ChooseFileCallback;
import com.xbcx.common.choose.ChooseProvider;
import com.xbcx.common.choose.ChooseProviderPlugin;
import com.xbcx.common.choose.ChooseVideoProvider.ChooseVideoCallback;
import com.xbcx.common.menu.Menu;
import com.xbcx.common.menu.MenuFactory;
import com.xbcx.common.menu.SimpleMenuDialogCreator;
import com.xbcx.core.ActivityScreen.onTitleListener;
import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.core.http.XHttpException;
import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.im.vcard.VCardProvider;
import com.xbcx.library.BuildConfig;
import com.xbcx.library.R;
import com.xbcx.utils.FileHelper;
import com.xbcx.utils.SystemUtils;

import net.simonvt.timepicker.TimePickerDialog;

import org.apache.commons.collections4.map.MultiValueMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public abstract class BaseActivity extends AppCompatActivity implements
												onTitleListener,
												OnEventListener,
												OnEventProgressListener{
	
	public static final String EXTRA_HasTitle		= "hastitle";
	public static final String EXTRA_AddBackButton 	= "addbackbutton";
	public static final String Extra_InputPluginClassNames	= "input_plugin_class_names";
	public static final String EXTRA_DestroyWhenLoginActivityLaunch = "destory_when_login_activity_launch";
	
	public static final int RequestCode_LaunchCamera 		= 15000;
	public static final int RequestCode_LaunchChoosePicture = 15001;
	public static final int RequestCode_LaunchChooseVideo	= 15002;
	public static final int RequestCode_LaunchChooseFile	= 15003;
	
	protected static 	ToastManager 			mToastManager = ToastManager.getInstance(XApplication.getApplication());
	
	protected static 	AndroidEventManager		mEventManager = AndroidEventManager.getInstance();

	private   static boolean 			sBackgrounded;
	private   static long				sBackgroundTime;
	
	protected BaseAttribute     		mBaseAttribute;
	protected ActivityScreen			mBaseScreen;
	
	protected RelativeLayout			mRelativeLayoutTitle;
	protected TextView					mTextViewTitle;
	protected TextView					mTextViewSubTitle;
	
	protected boolean mIsShowChatRoomBar = IMGlobalSetting.showChatRoomBar;
	protected boolean mDestroyWhenLoginActivityLaunch 	= true;
	
	private HashMap<String,OnEventListener> 			mMapCodeToListener;
	private	HashMap<String, String>						mMapNotToastCode;
	private HashMap<Event, Event> 						mMapPushEvents;
	private HashMap<Event, Boolean>						mMapEventToProgressBlock;
	private MultiValueMap<String, ActivityEventHandler> mMapCodeToActivityEventHandler;
	private Event										mEventSuccessFinish;
	private int											mToastSuccessId;
	
	private boolean						mIsResume;
	private boolean						mIsForbidPushEvent;
	
	protected boolean					mIsChoosePhotoCrop;
	protected boolean					mIsCropPhotoSquare;
	protected boolean					mIsChoosePhotoCompression = true;
	protected int						mChoosePhotoReqWidth = 1024;
	protected int						mChoosePhotoReqHeight = 1024;
	
	private   Object					mTag;
	private   int						mRequestCodeInc = 300;
	
	private   boolean					mUseEditTextClickOutSideHideIMM = true;
	private   List<View> 				mEditTextesForClickOutSideHideIMM;
	protected boolean					mIsEditTextClickOutSideSwallowTouch = true;
	protected boolean					mIsEditTextClickOutSideHideWhenClick = false;
	private	  GestureDetector			mEditTextClickOutSideDetector;
	
	private PluginHelper<ActivityBasePlugin> mPluginHelper;
	private GetPluginDelegate			mGetPluginDelegate;
	
	private Factory						mLayoutInflateFactory;
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if(BuildConfig.DEBUG){
			System.out.println("finalize:" + getClass().getName());
		}
	}
	
	public BaseActivity() {
		ActivityLaunchManager.getInstance().onActivityCreate(this);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SystemUtils.registerInputPlugins(this);
		mLayoutInflateFactory = XUIProvider.getInstance().createLayoutInflateFactory(this);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		onInitAttribute(mBaseAttribute = new BaseAttribute());
		
		XApplication.getApplication().onActivityCreate(this);
		
		mBaseScreen = onCreateScreen(mBaseAttribute);
		mBaseScreen.setOnTitleListener(this).onCreate();
		
		if(savedInstanceState != null){
			mIsChoosePhotoCrop = savedInstanceState.getBoolean("is_choose_photo_crop", false);
			mIsCropPhotoSquare = savedInstanceState.getBoolean("is_crop_photo_square", false);
		}
		
		onSetParam();
		
		addAndManageEventListener(EventCode.LoginActivityLaunched);
	}
	
	@Override
	public final void onTitleInited(){
		mRelativeLayoutTitle = mBaseScreen.getViewTitle();
		mTextViewTitle = mBaseScreen.getTextViewTitle();
	}
	
	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		if(mLayoutInflateFactory != null){
			View v = mLayoutInflateFactory.onCreateView(name, context, attrs);
			if(v != null)return v;
		}
		return super.onCreateView(name, context, attrs);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBaseScreen.onDestory();
		if(mPluginHelper != null){
			for(ActivityPlugin<?> ap : getPlugins(ActivityPlugin.class)){
				ap.onDestroy();
			}
			mPluginHelper.clear();
		}
		mTag = null;
		
		mIsForbidPushEvent = true;
		
		if(mMapCodeToListener != null){
			for(Entry<String, OnEventListener> e : mMapCodeToListener.entrySet()){
				mEventManager.removeEventListener(e.getKey(), e.getValue());
			}
			mMapCodeToListener.clear();
		}
		
		if(mMapPushEvents != null){
			for(Event e : mMapPushEvents.keySet()){
				mEventManager.removeEventListener(e.getStringCode(), this);
				mEventManager.clearEventListenerEx(e);
			}
			mMapPushEvents.clear();
		}
		
		if(mMapCodeToActivityEventHandler != null){
			mMapCodeToActivityEventHandler.clear();
		}
		
		XApplication.getApplication().onActivityDestory(this);
		ActivityLaunchManager.getInstance().onActivityDestory(this);
	}

	protected void onSetParam(){
		if(getIntent().hasExtra(EXTRA_DestroyWhenLoginActivityLaunch)){
			mDestroyWhenLoginActivityLaunch = getIntent().getBooleanExtra(EXTRA_DestroyWhenLoginActivityLaunch, true);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mIsResume = true;
		for(ActivityPlugin<?> ap : getPlugins(ActivityPlugin.class)){
			ap.onResume();
		}
		XApplication.getApplication().onActivityResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mIsResume = false;
		for(ActivityPlugin<?> ap : getPlugins(ActivityPlugin.class)){
			ap.onPause();
		}
		XApplication.getApplication().onActivityPause(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(sBackgrounded){
			sBackgrounded = false;
			mEventManager.runEvent(EventCode.AppForceground,
					SystemClock.elapsedRealtime() - sBackgroundTime);
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(XApplication.getMainThreadHandler().post(new Runnable() {
			@Override
			public void run() {
				if(!sBackgrounded){
					if(SystemUtils.isInBackground(BaseActivity.this)){
						sBackgrounded = true;
						sBackgroundTime = SystemClock.elapsedRealtime();
						mEventManager.runEvent(EventCode.AppBackground);
					}
				}
			}
		}));
	}
	
	public BaseAttribute getBaseAttribute(){
		return mBaseAttribute;
	}
	
	public boolean isResume(){
		return mIsResume;
	}
	
	@Override
	public Resources getResources() {
		return XUIProvider.getInstance().createResources(super.getResources());
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		for(ActivityPlugin<?> ap : getPlugins(ActivityPlugin.class)){
			ap.onPostCreate(savedInstanceState);
		}
		mBaseScreen.onPostCreate();
	}
	
	protected ActivityScreen	onCreateScreen(BaseAttribute ba){
		return XScreenFactory.wrap(this, ba);
	}
	
	public ActivityScreen getBaseScreen(){
		return mBaseScreen;
	}
	
	public RelativeLayout getViewTitle(){
		return mBaseScreen.getViewTitle();
	}
	
	public TextView getTextViewTitle(){
		return mBaseScreen.getTextViewTitle();
	}
	
	public View	getButtonBack(){
		return mBaseScreen.getButtonBack();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(mIsChoosePhotoCrop){
			outState.putBoolean("is_choose_photo_crop", true);
		}
		if(mIsCropPhotoSquare){
			outState.putBoolean("is_crop_photo_square", true);
		}
		for(ActivityPlugin<?> ap : getPlugins(ActivityPlugin.class)){
			ap.onSaveInstanceState(outState);
		}
	}
	
	public boolean isShowChatRoomBar(){
		return mIsShowChatRoomBar;
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		for(ActivityPlugin<?> ap : getPlugins(ActivityPlugin.class)){
			ap.onRestoreInstanceState(savedInstanceState);
		}
	}
	
	public final void registerChooseProvider(ChooseProviderPlugin<?> provider){
		registerPlugin(provider);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final void registerPlugin(ActivityBasePlugin plugin){
		if(mPluginHelper == null){
			if(isSyncPlugin()){
				mPluginHelper = new SyncPluginHelper<ActivityBasePlugin>();
			}else{
				mPluginHelper = new PluginHelper<ActivityBasePlugin>();
			}
		}
		mPluginHelper.addManager(plugin);
		if(plugin instanceof ActivityPlugin){
			((ActivityPlugin)plugin).setActivity(this);
		}
	}
	
	public boolean isSyncPlugin(){
		return false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final void registerPluginAtHead(ActivityBasePlugin plugin){
		if(mPluginHelper == null){
			mPluginHelper = new PluginHelper();
		}
		mPluginHelper.addManagerAtHead(plugin);
		if(plugin instanceof ActivityPlugin){
			((ActivityPlugin)plugin).setActivity(this);
		}
	}
	
	public final void removePlugin(ActivityBasePlugin plugin){
		if(mPluginHelper != null){
			mPluginHelper.removeManager(plugin);
		}
	}
	
	final void setPluginDelegate(GetPluginDelegate d){
		mGetPluginDelegate = d;
	}
	
	public final <T extends ActivityBasePlugin> Collection<T> getPlugins(Class<T> cls){
//		if(mGetPluginDelegate != null){
//			return mGetPluginDelegate.getPlugins(cls);
//		}
		if(mPluginHelper == null){
			return Collections.emptySet();
		}
		return mPluginHelper.getManagers(cls);
	}
	
	public final PluginHelper<ActivityBasePlugin> getPluginHelper(){
		return mPluginHelper;
	}
	
	public void setIsXProgressFocusable(boolean bFocus){
		mBaseScreen.setIsXProgressFocusable(bFocus);
	}

	@Override
	public void setContentView(int layoutResID) {
		try{
			super.setContentView(layoutResID);
		}catch(OutOfMemoryError e){
			System.gc();
			ToastManager.getInstance(this).show(R.string.toast_out_of_memory);
			finish();
		}
	}
	
	public final void setUseEditTextClickOutSideHideIMM(boolean b){
		mUseEditTextClickOutSideHideIMM = b;
	}

	public final void registerEditTextForClickOutSideHideIMM(View v){
		if(mEditTextesForClickOutSideHideIMM == null){
			mEditTextesForClickOutSideHideIMM = new ArrayList<View>();
		}
		mEditTextesForClickOutSideHideIMM.add(v);
	}
	
	public final void unregisterEditTextForClickOutSideHideIMM(View v){
		if(mEditTextesForClickOutSideHideIMM != null){
			mEditTextesForClickOutSideHideIMM.remove(v);
		}
	}
	
	public final void setIsEditTextClickOutSideSwallowTouch(boolean bSwallow){
		mIsEditTextClickOutSideSwallowTouch = bSwallow;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(mUseEditTextClickOutSideHideIMM && 
				mEditTextesForClickOutSideHideIMM != null &&
				mEditTextesForClickOutSideHideIMM.size() > 0){
			if(mIsEditTextClickOutSideHideWhenClick){
				if(mEditTextClickOutSideDetector == null){
					mEditTextClickOutSideDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
						@Override
						public boolean onSingleTapUp(MotionEvent e) {
							if(handleEditTextOutSide(e)){
								return true;
							}
							return super.onSingleTapUp(e);
						}
					});
				}
				mEditTextClickOutSideDetector.onTouchEvent(ev);
			}else{
				if(ev.getAction() == MotionEvent.ACTION_DOWN){
					if(handleEditTextOutSide(ev)){
						return true;
					}
				}
			}
		}

		try{
			boolean bHandled = false;
			for(ActivityPlugin<?> ap : getPlugins(ActivityPlugin.class)){
				if(ap.dispatchTouchEvent(ev)){
					bHandled = true;
				}
			}
			if(!bHandled){
				return super.dispatchTouchEvent(ev);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean handleEditTextOutSide(MotionEvent ev){
		final int x = (int)ev.getRawX();
		final int y = (int)ev.getRawY();
		Rect r = new Rect();
		int location[] = new int[2];
		
		boolean bHide = true;
		
		for(View et : mEditTextesForClickOutSideHideIMM){
			et.getGlobalVisibleRect(r);
			et.getLocationOnScreen(location);
			r.offsetTo(location[0], location[1]);
			if(r.contains(x, y)){
				bHide = false;
				break;
			}
		}
		
		if(bHide){
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			if(imm.hideSoftInputFromWindow(mEditTextesForClickOutSideHideIMM.get(0).getWindowToken(), 0)){
				if(mIsEditTextClickOutSideSwallowTouch){
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void onBackPressed() {
		try{
			boolean bHandled = false;
			for(ActivityPlugin<?> ap : getPlugins(ActivityPlugin.class)){
				if(ap.onBackPressed()){
					bHandled = true;
				}
			}
			if(!bHandled){
				onXBackPressed();
			}
		}catch(Exception e){
			finish();
		}
	}
	
	public void onXBackPressed(){
		super.onBackPressed();
	}
	
	public int	generateRequestCode(){
		return ++mRequestCodeInc;
	}

	public void choosePhoto(){
		choosePhoto(true);
	}
	
	public void choosePhoto(boolean bCrop){
		choosePhoto(bCrop, null);
	}
	
	public void choosePhoto(boolean bCrop,String title){
		mIsChoosePhotoCrop = bCrop;
		final Context context = getDialogContext();
		List<Menu> menus = new ArrayList<Menu>();
		menus.add(new Menu(1, R.string.photograph));
		menus.add(new Menu(2, R.string.choose_from_albums));
		MenuFactory.getInstance().showMenu(context, menus, 
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == 0){
					launchCameraPhoto(mIsChoosePhotoCrop);
				}else if(which == 1){
					launchPictureChoose(mIsChoosePhotoCrop);
				}
			}
		}, new SimpleMenuDialogCreator(title));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void chooseVideo(ChooseVideoCallback callback){
		ChooseProviderPlugin p = getChooseProvider(RequestCode_LaunchChooseVideo);
		if(p != null){
			p.choose(this, callback);
		}
	}

	public void launchCamera(boolean bVideo){
		launchCameraPhoto(true);
	}
	
	public void launchCameraPhoto(boolean bCrop){
		mIsChoosePhotoCrop = bCrop;
		try{
			final String path = getCameraSaveFilePath();
			if(FileHelper.checkOrCreateDirectory(path)){
				XUIProvider.getInstance().launchCameraPhoto(this, path, RequestCode_LaunchCamera);
			}else{
				if(path.startsWith(SystemUtils.getExternalPath(this))){
					if(SystemUtils.isExternalStorageMounted()){
						ToastManager.getInstance(this).show(R.string.toast_cannot_create_file_on_sdcard_reboot);
					}else{
						ToastManager.getInstance(this).show(R.string.toast_cannot_create_file_on_sdcard);
					}
				}else{
					ToastManager.getInstance(this).show(R.string.toast_cannot_create_file_on_sdcard);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void launchPictureChoose(){
		launchPictureChoose(true);
	}
	
	public void launchPictureChoose(boolean bCrop){
		mIsChoosePhotoCrop = bCrop;
		try{
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			FileHelper.checkOrCreateDirectory(FilePaths.getPictureChooseFilePath());
			FileHelper.deleteFile(FilePaths.getPictureChooseFilePath());
			if(bCrop){
				onSetCropExtra(intent);
			}
			startActivityForResult(intent,RequestCode_LaunchChoosePicture);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void launchFileChoose(ChooseFileCallback cb){
		ChooseProviderPlugin provider = getChooseProvider(RequestCode_LaunchChooseFile);
		if(provider != null){
			provider.choose(this, cb);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ChooseCallbackInterface> void chooseProvider(int requestCode,T callback){
		ChooseProvider<T> provider = (ChooseProvider<T>)getChooseProvider(requestCode);
		if(provider != null){
			provider.choose(this, callback);
		}
	}
	
	public ChooseProviderPlugin<?> getChooseProvider(int requestCode){
		for(ChooseProviderPlugin<?> p : getPlugins(ChooseProviderPlugin.class)){
			if(p.acceptRequestCode(requestCode)){
				return p;
			}
		}
		return null;
	}
	
	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		ActivityLaunchManager.getInstance().onStartActivity(intent, this);
		super.startActivityForResult(intent, requestCode);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == RequestCode_LaunchCamera){
				onCameraResult(data);
			}else if(requestCode == RequestCode_LaunchChoosePicture){
				onPictureChooseResult(data);
			}
		}
		
		ChooseProviderPlugin<?> provider = getChooseProvider(requestCode);
		if(provider != null){
			provider.onActivityResult(this, requestCode, resultCode, data);
		}
		
		for(ActivityPlugin<?> ap : getPlugins(ActivityPlugin.class)){
			ap.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	protected void onCameraResult(Intent data){
		handleCameraPhotoResult(mIsChoosePhotoCrop);
	}
	
	protected void handleCameraPhotoResult(boolean bCrop){
		final String path = getCameraSaveFilePath();
		handlePictureExif(path, path);
		if(bCrop){
			try {
				Intent intent = new Intent("com.android.camera.action.CROP");
				intent.setDataAndType(Uri.fromFile(new File(path)), 
						"image/*");
				FileHelper.deleteFile(FilePaths.getPictureChooseFilePath());
				onSetCropExtra(intent);
				startActivityForResult(intent, RequestCode_LaunchChoosePicture);
			} catch (Exception e) {
				e.printStackTrace();
				handleCameraPhotoResult(false);
			}
		}else{
			if(mIsChoosePhotoCompression){
				SystemUtils.compressBitmapFile(path, path, 
						mChoosePhotoReqWidth,
						mChoosePhotoReqHeight);
			}
			onPictureChoosed(path, null);
		}
	}
	
	protected void handlePictureExif(String srcPath,String savePath){
		handlePictureExif(-1, srcPath, savePath);
	}
	
	protected void handlePictureExif(int rotate,String srcPath,String savePath){
		if(rotate == -1){
			rotate = SystemUtils.getPictureExifRotateAngle(srcPath);
		}
		if(rotate != 0){
			Bitmap bmpOld = SystemUtils.decodeSampledBitmapFromFilePath(srcPath, 
					XApplication.getScreenWidth(), XApplication.getScreenWidth());
			if(bmpOld != null){
				Matrix matrix = new Matrix();
				matrix.preRotate(rotate);
				final Bitmap bmpNew = Bitmap.createBitmap(bmpOld, 
						0, 0, bmpOld.getWidth(), bmpOld.getHeight(), matrix, true);
				FileHelper.saveBitmapToFile(savePath, bmpNew);
				bmpOld.recycle();
				bmpNew.recycle();
			}else{
				FileHelper.copyFile(savePath, srcPath);
			}
		}
	}
	
	protected void onPictureChooseResult(Intent data){
		if(data != null){
			if(mIsChoosePhotoCrop){
				File file = new File(FilePaths.getPictureChooseFilePath());
				if(file.exists()){
					if(mIsChoosePhotoCompression){
						final String path = FilePaths.getPictureChooseFilePath();
						SystemUtils.compressBitmapFile(path, path, 
								mChoosePhotoReqWidth, mChoosePhotoReqHeight);
					}
					onPictureChoosed(FilePaths.getPictureChooseFilePath(), null);
				}else{
					if(!handlePictureChooseResultUri(data)){
						Object obj = data.getParcelableExtra("data");
						if(obj != null && obj instanceof Bitmap){
							final Bitmap bmp = (Bitmap)obj;
							FileHelper.saveBitmapToFile(
									FilePaths.getPictureChooseFilePath(), bmp);
							onPictureChoosed(FilePaths.getPictureChooseFilePath(), null);
						}
					}
				}
			}else{
				if(!handlePictureChooseResultUri(data)){
					File file = new File(FilePaths.getPictureChooseFilePath());
					if(file.exists()){
						if(mIsChoosePhotoCompression){
							SystemUtils.compressBitmapFile(FilePaths.getPictureChooseFilePath(), 
									FilePaths.getPictureChooseFilePath(), 
									mChoosePhotoReqWidth, mChoosePhotoReqHeight);
						}
						onPictureChoosed(FilePaths.getPictureChooseFilePath(), null);
					}else{
						Object obj = data.getParcelableExtra("data");
						if(obj != null && obj instanceof Bitmap){
							final Bitmap bmp = (Bitmap)obj;
							FileHelper.saveBitmapToFile(
									FilePaths.getPictureChooseFilePath(), bmp);
							onPictureChoosed(FilePaths.getPictureChooseFilePath(), null);
						}
					}
				}
			}
		}
	}
	
	protected boolean handlePictureChooseResultUri(Intent data){
		Uri uri = data.getData();
		if(uri != null){
			if(uri.getScheme().startsWith("file")){
				onPictureChoosed(uri.getPath(),new File(uri.getPath()).getName());
				return true;
			}else{
				@SuppressWarnings("deprecation")
				Cursor cursor = managedQuery(uri, 
						new String[]{MediaStore.Images.ImageColumns.DISPLAY_NAME,
									MediaStore.Images.ImageColumns.DATA}, 
						null, null, null);
				if(cursor != null && cursor.moveToFirst()){
					String displayName = cursor.getString(
							cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
					final String srcPath = cursor.getString(
							cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
					if(TextUtils.isEmpty(srcPath)){
						try{
							Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
							if(bmp != null){
								if(mIsChoosePhotoCompression){
									SystemUtils.compressBitmap(
											FilePaths.getPictureChooseFilePath(), 
											bmp, 
											mChoosePhotoReqWidth, mChoosePhotoReqHeight);
								}else{
									FileHelper.saveBitmapToFile(FilePaths.getPictureChooseFilePath(), bmp);
								}
								bmp.recycle();
								onPictureChoosed(FilePaths.getPictureChooseFilePath(), 
										displayName);
								return true;
							}
						}catch(Exception e){
							e.printStackTrace();
						}catch(OutOfMemoryError e){
							e.printStackTrace();
							ToastManager.getInstance(this).show(R.string.toast_out_of_memory);
						}
					}else{
						if(mIsChoosePhotoCompression){
							SystemUtils.compressBitmapFile(
									FilePaths.getPictureChooseFilePath(),
									srcPath, 
									mChoosePhotoReqWidth, mChoosePhotoReqHeight);
							onPictureChoosed(FilePaths.getPictureChooseFilePath(), 
									displayName);
						}else{
							onPictureChoosed(srcPath, 
									displayName);
						}
						return true;
					}
				}
			}
		}
		return false;
	}
	
	protected void onPictureChoosed(String filePath,String displayName){
		for(ChoosePictureEndPlugin p : getPlugins(ChoosePictureEndPlugin.class)){
			p.onPictureChoosed(filePath, displayName);
		}
	}
	
	protected void onSetCropExtra(Intent intent){
		intent.putExtra("crop", "true"); 
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", true);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, 
				Uri.fromFile(new File(FilePaths.getPictureChooseFilePath())));
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		if(mIsCropPhotoSquare){
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
		}
	}
	
	protected String 	getCameraSaveFilePath(){
		return FilePaths.getCameraSaveFilePath();
	}
	
	protected void 		setTag(Object object){
		mTag = object;
	}
	
	protected Object 	getTag(){
		return mTag;
	}
	
	public void showProgressDialog(){
		mBaseScreen.showProgressDialog();
	}
	
	public void showProgressDialog(String strTitle,int nStringId){
		mBaseScreen.showProgressDialog(strTitle, nStringId);
	}
	
	public void showProgressDialog(String strTitle,String strMessage){
		mBaseScreen.showProgressDialog(strTitle, strMessage);
	}
	
	public void dismissProgressDialog(){
		mBaseScreen.dismissProgressDialog();
	}
	
	public void showXProgressDialog(){
		mBaseScreen.showXProgressDialog();
	}
	
	public void showXProgressDialog(String text){
		mBaseScreen.showXProgressDialog(text);
	}
	
	public boolean	isXProgressDialogShowing(){
		return mBaseScreen.isXProgressDialogShowing();
	}
	
	public FrameLayout addCoverView(){
	    return mBaseScreen.addCoverView();
	}
	
	public void dismissXProgressDialog(){
		mBaseScreen.dismissXProgressDialog();
	}
	
	public void setXProgressText(String text){
		mBaseScreen.setXProgressText(text);
	}
	
	public Dialog showYesNoDialog(int msgTextId,DialogInterface.OnClickListener listener){
		return mBaseScreen.showYesNoDialog(msgTextId, listener);
	}
	
	public Dialog showYesNoDialog(int yesTextId,int msgTextId,DialogInterface.OnClickListener listener){
		return mBaseScreen.showYesNoDialog(yesTextId, msgTextId, listener);
	}
	
	public Dialog showYesNoDialog(int yesTextId,int noTextId,int msgTextId,DialogInterface.OnClickListener listener){
		return mBaseScreen.showYesNoDialog(yesTextId, noTextId, msgTextId, listener);
	}
	
	public Dialog showYesNoDialog(int yesTextId,int noTextId,String message,DialogInterface.OnClickListener listener){
		return mBaseScreen.showYesNoDialog(yesTextId, noTextId, message, listener);
	}
	
	public Dialog showYesNoDialog(int yesTextId,int noTextId,int msgTextId,int titleTextId,DialogInterface.OnClickListener listener){
		return mBaseScreen.showYesNoDialog(yesTextId, noTextId, msgTextId, titleTextId, listener);
	}
	
	public Dialog showYesNoDialog(String yesText, String noText, String message,
			int titleIcon, String title,DialogInterface.OnClickListener listener){
		return mBaseScreen.showYesNoDialog(yesText, noText, message, titleIcon, title, listener);
	}
	
	public Context getDialogContext(){
		return mBaseScreen.getDialogContext();
	}
	
	public void showDatePicker(){
		showDatePicker(XApplication.getFixSystemTime(), 0, 0);
	}
	
	public void showDatePicker(long maxDate,long minDate){
		showDatePicker(XApplication.getFixSystemTime(), maxDate, minDate);
	}
	
	public void showDatePicker(long time,long maxDate,long minDate){
		new DatePickerDialogLauncher()
			.setTime(time)
			.setMaxTime(maxDate)
			.setMinTime(minDate)
			.setOnDateChooseListener(new DatePickerDialogLauncher.OnDateChooseListener() {
				@Override
				public void onDateChoosed(Calendar cal) {
					onDateChooseResult(cal);
				}
			})
			.onLaunch(this);
	}
	
	protected void onDateChooseResult(Calendar cal){
		
	}
	
	public TimePickerDialog showTimePicker(){
		Calendar c = Calendar.getInstance();
		return showTimePicker(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
	}
	
	public TimePickerDialog showTimePicker(int hourOfDay, int minute, boolean is24HourView){
		TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener(){
			@Override
			public void onTimeSet(net.simonvt.timepicker.TimePicker view, int hourOfDay, int minute) {
				onTimeChooseResult(hourOfDay, minute);
			}
		};
		
		TimePickerDialog d = new TimePickerDialog(this, R.style.DatePickerDialog,
				listener, hourOfDay,minute,is24HourView);
		d.show();
		return d;
	}
	
	protected void onTimeChooseResult(int hourOfDay,int minute){
	}
	
	public TextView addSubTitle(){
		return mTextViewSubTitle = mBaseScreen.addSubTitle();
	}
	
	public View addImageButtonInTitleRight(int resId){
		View v = mBaseScreen.addImageButtonInTitleRight(resId);
		v.setOnClickListener(mOnClickListener);
		return v;
	}
	
	public View addTextButtonInTitleRight(int textId){
		View v = mBaseScreen.addTextButtonInTitleRight(textId);
		v.setOnClickListener(mOnClickListener);
		return v;
	}

	protected void 		onInitAttribute(BaseAttribute ba){
		if(getIntent().hasExtra(EXTRA_HasTitle)){
			ba.mHasTitle = getIntent().getBooleanExtra(EXTRA_HasTitle, true);
		}
		if(getIntent().hasExtra(EXTRA_AddBackButton)){
			ba.mAddBackButton = getIntent().getBooleanExtra(EXTRA_AddBackButton, false);
		}
		for(InitAttributePlugin p : getPlugins(InitAttributePlugin.class)){
			p.onInitAttribute(ba);
		}
	}
	
	protected void onTitleRightButtonClicked(View v){
		for(TitleRightButtonClickActivityPlugin ap : getPlugins(TitleRightButtonClickActivityPlugin.class)){
			ap.onHandleTitleRightButtonClick(v);
		}
	}
	
	public void setIsForbidPushEvent(boolean bForbid){
		mIsForbidPushEvent = bForbid;
	}
	
	protected void setAvatar(ImageView iv,String userId){
		VCardProvider.getInstance().setAvatar(iv, userId);
	}
	
	protected void setName(TextView tv,String userId,String defaultName){
		VCardProvider.getInstance().setName(tv, userId,defaultName,false);
	}
	
	public Event pushEvent(int eventCode,Object...params){
		return pushEventEx(eventCode, true, false,null,params);
	}
	
	public Event pushEvent(String eventCode,Object...params){
		return pushEventEx(eventCode, true, false,null,params);
	}
	
	public Event pushEventSuccessFinish(int eventCode,int toastSuccessId,Object...params){
		return pushEventSuccessFinish(String.valueOf(eventCode), toastSuccessId, params);
	}
	
	public Event pushEventSuccessFinish(String code,int toastSuccessId,Object...params){
		mEventSuccessFinish = pushEvent(code, params);
		mToastSuccessId = toastSuccessId;
		return mEventSuccessFinish;
	}
	
	public Event pushEventBlock(int eventCode,Object...params){
		return pushEventEx(eventCode, true, true, null, params);
	}
	
	public Event pushEventNoProgress(int eventCode,Object...params){
		return pushEventEx(eventCode, false, false, null, params);
	}
	
	public Event pushEventNoProgress(String code,Object...params){
		return pushEventEx(code, false, false, null, params);
	}
	
	public Event pushEventShowProgress(int eventCode,Object...params){
		Event e = pushEvent(eventCode, params);
		mEventManager.addEventProgressListener(e, this);
		setXProgressText(generateProgressText(0));
		return e;
	}
	
	@SuppressLint("UseSparseArrays")
	protected Event pushEventEx(int eventCode,
			boolean bShowProgress,boolean bBlock,String progressMsg,
			Object... params){
		return pushEventEx(String.valueOf(eventCode), 
				bShowProgress, bBlock, progressMsg, params);
	}
	
	@SuppressLint("UseSparseArrays")
	protected Event pushEventEx(String code,
			boolean bShowProgress,boolean bBlock,String progressMsg,
			Object... params){
		if(mIsForbidPushEvent){
			return new Event(-1, null);
		}
		
		for(OnEventParamInterceptActivityPlugin ap : getPlugins(OnEventParamInterceptActivityPlugin.class)){
			params = ap.onInterceptEventParam(params);
		}
		
		Event e = null;
		if(mMapCodeToListener.get(code) != null){
			e = mEventManager.pushEvent(code, params);
		}else{
			e = mEventManager.pushEventEx(code,this,params);
			if(mMapPushEvents == null){
				mMapPushEvents = new HashMap<Event, Event>();
			}
			mMapPushEvents.put(e, e);
		}
		
		if(mMapEventToProgressBlock == null){
			mMapEventToProgressBlock = new HashMap<Event, Boolean>();
		}
		
		if(!mMapEventToProgressBlock.containsKey(e)){
			if(bShowProgress){
				if(bBlock){
					showProgressDialog(null,progressMsg);
				}else{
					showXProgressDialog();
				}
				mMapEventToProgressBlock.put(e, bBlock);
			}
		}
		
		return e;
	}

	public void addAndManageEventListener(int eventCode){
		addAndManageEventListener(eventCode, true);
	}
	
	public void addAndManageEventListener(String code){
		addAndManageEventListener(code, true);
	}
	
	public void addAndManageEventListener(int eventCode,boolean bToast){
		addAndManageEventListener(String.valueOf(eventCode), bToast);
	}
	
	public void addAndManageEventListener(String code,boolean bToast){
		if(mMapCodeToListener == null){
			mMapCodeToListener = new HashMap<String, EventManager.OnEventListener>();
		}
		if(mMapCodeToListener.get(code) == null){
			mMapCodeToListener.put(code, this);
			
			mEventManager.addEventListener(code, this);
		}
		
		if(!bToast){
			if(mMapNotToastCode == null){
				mMapNotToastCode = new HashMap<String, String>();
			}
			mMapNotToastCode.put(code, code);
		}
	}
	
	public void removeEventListener(int eventCode){
		if(mMapCodeToListener == null){
			return;
		}
		mMapCodeToListener.remove(eventCode);
		
		mEventManager.removeEventListener(eventCode, this);
	}
	
	public void registerActivityEventHandler(int eventCode,ActivityEventHandler handler){
		registerActivityEventHandler(String.valueOf(eventCode), handler);
	}
	
	public void registerActivityEventHandler(String code,ActivityEventHandler handler){
		if(mMapCodeToActivityEventHandler == null){
			mMapCodeToActivityEventHandler = new MultiValueMap<String, ActivityEventHandler>();
		}
		mMapCodeToActivityEventHandler.put(code, handler);
	}
	
	public void unregisterActivityEventHandler(int eventCode,ActivityEventHandler handler) {
		unregisterActivityEventHandler(String.valueOf(eventCode), handler);
	}
	
	public void unregisterActivityEventHandler(String code,ActivityEventHandler handler) {
		if(mMapCodeToActivityEventHandler == null){
			return;
		}
		mMapCodeToActivityEventHandler.removeMapping(code, handler);
	}
	
	public void registerActivityEventHandlerEx(int eventCode,ActivityEventHandler handler){
		registerActivityEventHandler(eventCode, handler);
		addAndManageEventListener(eventCode);
	}
	
	public void registerActivityEventHandlerEx(String code,ActivityEventHandler handler){
		registerActivityEventHandler(code, handler);
		addAndManageEventListener(code);
	}
	
	@Override
	public void onEventProgress(Event e, int progress) {
		setXProgressText(generateProgressText(progress));
	}
	
	protected String	generateProgressText(int progress){
		return progress + "%";
	}

	@Override
	public void onEventRunEnd(Event event) {
		final String code = event.getStringCode();
		
		if(!event.isSuccess()){
			final Exception e = event.getFailException();
			if(e != null){
				onHandleEventException(event,e);
			}
		}
		
		if(mMapPushEvents != null){
			mMapPushEvents.remove(event);
		}
		
		if(mMapEventToProgressBlock != null){
			Boolean block = mMapEventToProgressBlock.remove(event);
			if(block != null){
				if(block.booleanValue()){
					dismissProgressDialog();
				}else{
					dismissXProgressDialog();
				}
			}
		}
		
		if(event.getEventCode() == EventCode.LoginActivityLaunched){
			if(mDestroyWhenLoginActivityLaunch){
				finish();
			}
		}else{
			if(event.equals(mEventSuccessFinish)){
				onSuccessFinishEventEnd(event);
			}
		}
		
		if(mMapCodeToActivityEventHandler != null){
			Collection<ActivityEventHandler> list = mMapCodeToActivityEventHandler.getCollection(code);
			if(list != null){
				for(ActivityEventHandler handler : list){
					handler.onHandleEventEnd(event, this);
				}
			}
		}
		
		for(OnActivityEventEndPlugin p : getPlugins(OnActivityEventEndPlugin.class)){
			p.onActivityEventEnd(event);
		}
	}
	
	protected void onHandleEventException(Event event,Exception e){
		if(mMapNotToastCode == null || 
				!mMapNotToastCode.containsKey(event.getStringCode())){
			if(e instanceof XException){
				onHandleXException(event, (XException)e);
			}
		}
	}
	
	protected void onHandleXException(Event event,XException exception){
		boolean bRet = false;
		for(OnHandleXExceptionPlugin p : getPlugins(OnHandleXExceptionPlugin.class)){
			if(p.onHandleXException(event, exception)){
				bRet = true;
			}
		}
		if(!bRet){
			if (exception instanceof StringIdException){
				onHandleStringIdException(event,(StringIdException)exception);
			} else if(exception instanceof XHttpException){
				onHandleXHttpException(event,(XHttpException)exception);
			}
		}
	}
	
	public void onHandleStringIdException(Event event,StringIdException exception){
		if(mIsResume){
			final int id = exception.getStringId();
			if(id == R.string.toast_disconnect){
				onHandleDisconnectStringId(event);
			}else{
				onHandleOtherStringId(event, exception);
			}
		}
	}
	
	public boolean isDisconnectException(XException	exception){
		if (exception instanceof StringIdException){
			return ((StringIdException)exception).getStringId() == R.string.toast_disconnect;
		}
		return false;
	}
	
	protected void onHandleDisconnectStringId(Event event){
		if(SystemUtils.isNetworkAvailable(this)){
			mToastManager.show(R.string.toast_disconnect);
		}else{
			getBaseScreen().showNetworkErrorTip();
		}
	}
	
	protected void onHandleOtherStringId(Event event,StringIdException exception){
		mToastManager.show(exception.getStringId());
	}
	
	protected void onHandleXHttpException(Event event,XHttpException exception){
		if(mIsResume){
			if(!TextUtils.isEmpty(exception.getMessage())){
				mToastManager.show(exception.getMessage());
			}
		}
	}
	
	protected void onSuccessFinishEventEnd(Event event){
		if(event.isSuccess()){
			if(mToastSuccessId != 0){
				mToastManager.show(mToastSuccessId);
			}
			finish();
		}else{
			mEventSuccessFinish = null;
		}
	}
	
	public static interface OnHandleXExceptionPlugin extends ActivityBasePlugin{
		public boolean onHandleXException(Event event,XException exception);
	}
	
	public static interface OnActivityEventEndPlugin extends ActivityBasePlugin{
		public void onActivityEventEnd(Event event);
	}
	
	public static interface OnEventParamInterceptActivityPlugin extends ActivityBasePlugin{
		public Object[] onInterceptEventParam(Object params[]);
	}
	
	public static interface ActivityEventHandler{
		public void onHandleEventEnd(Event event,BaseActivity activity);
	}
	
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if(v == getButtonBack()){
				onBackPressed();
			}else{
				onTitleRightButtonClicked(v);
			}
		}
	};
	
	public static interface TitleRightButtonClickActivityPlugin extends ActivityBasePlugin{
		public void onHandleTitleRightButtonClick(View v);
	}
	
	public static interface ChoosePictureEndPlugin extends ActivityBasePlugin{
		public void onPictureChoosed(String filePath, String displayName);
	}
	
	public static interface InitAttributePlugin extends ActivityBasePlugin{
		public void onInitAttribute(BaseAttribute ba);
	}
	
	static interface GetPluginDelegate{
		public <T extends ActivityBasePlugin> Collection<T> getPlugins(Class<T> cls);
	}

	public static class BaseAttribute{
		public boolean  mSetContentView = true;
		public int		mActivityLayoutId;
		
		public boolean  mHasTitle = true;
		
		public boolean 	mAddTitleText = true;
		public int		mTitleTextStringId;
		public String	mTitleText;
		
		public boolean 	mAddBackButton = false;
	}
}
