package com.xbcx.im.ui.simpleimpl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore.Video;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.xbcx.core.FilePaths;
import com.xbcx.core.XApplication;
import com.xbcx.core.BaseActivity;
import com.xbcx.library.R;
import com.xbcx.utils.FileHelper;
import com.xbcx.utils.SystemUtils;

public class CameraActivity extends BaseActivity implements 
													SurfaceHolder.Callback,
													View.OnClickListener,
													Runnable{

	public static final int ORIENTATION_HYSTERESIS = 5;
	
	private int				mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
	private MyOrientationEventListener	mOrientationEventListener;
	
	private SurfaceView		mSurfaceView;
	private TextView		mBtnRecord;
	private TextView		mTextViewTime;
	
	private boolean			mIsRecording;
	private boolean			mIsRecordFinish;
	private MediaRecorder	mMediaRecorder;
	
	private int				mVideoWidth;
	private int				mVideoHeight;
	
	private Camera			mCamera;
	private int				mCameraId;
	private boolean			mIsPreview;
	
	private String			mPath;
	
	private long			mRecordingStartTime;
	private Uri				mCurrentVideoUri;
	
	private ContentValues	mContentValues = new ContentValues();
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mIsShowChatRoomBar = false;
		super.onCreate(savedInstanceState);
		
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		
		mVideoWidth = 320;
		mVideoHeight = 240;
		
		mCameraId = getDefaultCameraId();
		if(mCameraId == -1){
			mToastManager.show(R.string.toast_no_camera);
			finish();
			return;
		}
		
		mOrientationEventListener = new MyOrientationEventListener(this);
		mOrientationEventListener.enable();
		
		mBtnRecord = (TextView)findViewById(R.id.btnRecord);
		mBtnRecord.setOnClickListener(this);
		mBtnRecord.setVisibility(View.GONE);
		
		mTextViewTime = (TextView)findViewById(R.id.tvTime);
		
		generateVideoFilepath();
		FileHelper.checkOrCreateDirectory(mPath);
		
		final SurfaceView sv = (SurfaceView)findViewById(R.id.sv);
		sv.getHolder().addCallback(this);
		sv.getHolder().setFixedSize(mVideoWidth, mVideoHeight);
		boolean bUsePushBuffer = false;
		if(!bUsePushBuffer){
			if(SystemUtils.getArmArchitecture() < 6){
				bUsePushBuffer = true;
			}else{
				if("GT-S5830".equals(Build.MODEL)){
					bUsePushBuffer = true;
				}
			}
		}
		bUsePushBuffer = true;
		
		if(bUsePushBuffer){
			sv.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		mSurfaceView = sv;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		mOrientationEventListener.disable();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mActivityLayoutId = R.layout.xlibrary_activity_camera;
		ba.mTitleTextStringId = R.string.shoot_video;
	}

	public static void launchForResult(Activity activity,int requestCode){
		Intent intent = new Intent(activity, CameraActivity.class);
		activity.startActivityForResult(intent, requestCode);
	}
	
	private void generateVideoFilepath(){
		long dateTaken = SystemClock.uptimeMillis();
        final String title = createName(dateTaken);
        mPath = FilePaths.getCameraVideoFolderPath() + title + ".mp4";
        mContentValues.put(Video.Media.TITLE, title);
        mContentValues.put(Video.Media.DISPLAY_NAME, title + ".mp4");
        mContentValues.put(Video.Media.DATE_TAKEN, dateTaken);
        mContentValues.put(Video.Media.MIME_TYPE, "video/mp4");
        mContentValues.put(Video.Media.DATA, mPath);
        mContentValues.put(Video.Media.RESOLUTION,mVideoWidth + "x" + mVideoHeight);
        mContentValues.put(Video.Media.SIZE,new File(mPath).length());
	}
	
	private void addVideoToMediaStore() {
		Uri videoTable = Uri.parse("content://media/external/video/media");
		try {
			mCurrentVideoUri = getContentResolver().insert(videoTable,mContentValues);
		} catch (Exception e) {
			mCurrentVideoUri = null;
		}
		mContentValues.clear();
	}
	
	private String createName(long dateTaken) {
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat("'VID'_yyyyMMdd_HHmmss",Locale.getDefault());

        return dateFormat.format(date);
    }

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mBtnRecord.setVisibility(View.VISIBLE);
		
		if(mCamera == null){
			try{
				mCamera = Camera.open();
			}catch(Exception e){
				e.printStackTrace();
			}
			if(mCamera == null){
				mToastManager.show(R.string.toast_open_camera_fail);
				finish();
				return;
			}
			try{
				/*Camera.Parameters p = mCamera.getParameters();
				p.setPreviewSize(mVideoWidth, mVideoHeight);
				p.setSceneMode(Camera.Parameters.SCENE_MODE_FIREWORKS);
				p.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_FLUORESCENT);
				p.setPictureFormat(ImageFormat.JPEG);
				p.set("jpeg_quality", 85);
				p.setPictureSize(mVideoWidth, mVideoHeight);
				mCamera.setParameters(p);*/
				
				Camera.Parameters params = mCamera.getParameters();  
				List<String> focuses = params.getSupportedFocusModes();
				if(focuses == null){
					mCamera.autoFocus(null);
				}else{
					boolean bSet = false;
					for(String focus : focuses){
						if(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO.equals(focus)){
							params.setFocusMode(focus);
							bSet = true;
						}
					}
					if(!bSet){
						params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
					}
				}
				mCamera.setParameters(params);
				
				mCamera.setPreviewDisplay(holder);
				int orientation = getDisplayOrientation(getDisplayRotation(this), mCameraId);
				mCamera.setDisplayOrientation(orientation);
				
				mCamera.startPreview();
				
				mIsPreview = true;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		try{
			mCamera.stopPreview();
			
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		stopRecording();
		if(mIsPreview){
			mCamera.stopPreview();
			mIsPreview = false;
		}
		if(mCamera != null){
			mCamera.release();
		}
	}
	
	public static int getDisplayRotation(Activity activity) {
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
			return 0;
		case Surface.ROTATION_90:
			return 90;
		case Surface.ROTATION_180:
			return 180;
		case Surface.ROTATION_270:
			return 270;
		}
		return 0;
	}

	@SuppressLint("NewApi")
	public static int getDisplayOrientation(int degrees, int cameraId) {
		// See android.hardware.Camera.setDisplayOrientation for
		// documentation.
		if(Build.VERSION.SDK_INT >= 9){
			Camera.CameraInfo info = new Camera.CameraInfo();
			Camera.getCameraInfo(cameraId, info);
			int result;
			if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				result = (info.orientation + degrees) % 360;
				result = (360 - result) % 360; // compensate the mirror
			} else { // back-facing
				result = (info.orientation - degrees + 360) % 360;
			}
			return result;
		}else{
			return 90;
		}
	}
	
	public static int roundOrientation(int orientation, int orientationHistory) {
        boolean changeOrientation = false;
        if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
            changeOrientation = true;
        } else {
            int dist = Math.abs(orientation - orientationHistory);
            dist = Math.min( dist, 360 - dist );
            changeOrientation = ( dist >= 45 + ORIENTATION_HYSTERESIS );
        }
        if (changeOrientation) {
            return ((orientation + 45) / 90 * 90) % 360;
        }
        return orientationHistory;
    }
	
	@SuppressLint("NewApi")
	protected int getDefaultCameraId()
    {
        int defaultId = -1;

        if(Build.VERSION.SDK_INT >= 9){
        	// Find the total number of cameras available
            int numberOfCameras = Camera.getNumberOfCameras();

            // Find the ID of the default camera
            CameraInfo cameraInfo = new CameraInfo();
            for (int i = 0; i < numberOfCameras; i++)
            {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK)
                {
                    defaultId = i;
                }
            }
            if (-1 == defaultId)
            {
                if (numberOfCameras > 0)
                {
                    // 如果没有后向摄像头
                    defaultId = 0;
                }
            }
            return defaultId;
        }else{
        	return 0;
        }
    }

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		if(id == R.id.btnRecord){
			if(mIsRecording){
				stopRecording();
				mIsRecordFinish = true;
				
				long duration = SystemClock.uptimeMillis() - mRecordingStartTime;
				if (duration > 0) {
					mContentValues.put(Video.Media.DURATION, duration);
				}
				
				mBtnRecord.setBackgroundResource(R.drawable.btn_video_capture_send);
				mBtnRecord.setText(R.string.send);
			}else{
				if(mIsRecordFinish){
					addVideoToMediaStore();
					
					Intent data = new Intent();
					data.setData(mCurrentVideoUri);
					setResult(RESULT_OK,data);
					finish();
				}else{
					startRecording();
				}
			}
		}
	}
	
	@SuppressLint("NewApi")
	private void startRecording(){
		if(mIsPreview){
			mCamera.stopPreview();
			mIsPreview = false;
		}
		
		if(mIsRecording){
			return;
		}
		try {
        	mMediaRecorder = new MediaRecorder();

        	if(mCamera != null){
        		mCamera.unlock();
        		
            	mMediaRecorder.setCamera(mCamera);
            	if(Build.VERSION.SDK_INT > 9){
            		int rotation = 0;
                    if (mOrientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                    	CameraInfo info = new CameraInfo();
                    	Camera.getCameraInfo(mCameraId, info);
                        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                            rotation = (info.orientation - mOrientation + 360) % 360;
                        } else {  // back-facing camera
                            rotation = (info.orientation + mOrientation) % 360;
                        }
                    }
                	mMediaRecorder.setOrientationHint(rotation);
            	}
        	}
        	mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        	if(Build.VERSION.SDK_INT >= 9){
        		CamcorderProfile profile = CamcorderProfile.get(mCameraId, CamcorderProfile.QUALITY_LOW);
        		if(!"ZTE-T U960s".equals(Build.MODEL)){
        			if(Build.VERSION.SDK_INT >= 10){
        				profile.audioCodec = MediaRecorder.AudioEncoder.AAC;
        			}
        			profile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
        			profile.videoCodec = MediaRecorder.VideoEncoder.H264;
        		}
        		
        		mMediaRecorder.setProfile(profile);
        	}else{
        		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mMediaRecorder.setVideoSize(mVideoWidth, mVideoHeight);
                mMediaRecorder.setVideoFrameRate(20);
                mMediaRecorder.setVideoEncodingBitRate(128000);
                mMediaRecorder.setMaxDuration(60000);
                if(Build.VERSION.SDK_INT >= 10){
                	mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                }else{
                	mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                }
                mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        	}
        	mMediaRecorder.setOutputFile(mPath);
            mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
			mMediaRecorder.prepare();
	        mMediaRecorder.start();
	        
	        mRecordingStartTime = SystemClock.uptimeMillis();
	        
	        updateTimeShow();
	        
	        mSurfaceView.postDelayed(this, 1000);
	        
			mIsRecording = true;
			mBtnRecord.setBackgroundResource(R.drawable.video_recorder_recording_btn);
		} catch (Exception e) {
			e.printStackTrace();
			XApplication.getLogger().warning(SystemUtils.throwableToString(e));
		}
	}
	
	private void stopRecording(){
		if(mIsRecording){
			try{
				mMediaRecorder.stop();
				mMediaRecorder.release();
			}catch(Exception e){
				e.printStackTrace();
			}
			mIsRecording = false;
			
			mCamera.stopPreview();
			
			mSurfaceView.removeCallbacks(this);
		}
	}
	
	private void updateTimeShow(){
		final long elapsedTime = SystemClock.uptimeMillis() - mRecordingStartTime;
		int seconds = (int)(elapsedTime / 1000);
		int minute = seconds / 60;
		if(seconds >= 60){
			seconds = seconds % 60;
		}
		mTextViewTime.setText(minute + ":" + seconds);
	}

	@Override
	public void run() {
		updateTimeShow();
		
		mSurfaceView.postDelayed(this, 1000);
	}
	
	private class MyOrientationEventListener extends OrientationEventListener {
		public MyOrientationEventListener(Context context) {
			super(context);
		}

		@Override
		public void onOrientationChanged(int orientation) {
			if (orientation == ORIENTATION_UNKNOWN)
				return;
			mOrientation = roundOrientation(orientation, mOrientation);
		}
	}
}
