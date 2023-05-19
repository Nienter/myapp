package com.xbcx.core.bmp;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;
import com.nostra13.universalimageloader.utils.ImageSizeUtils;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.nostra13.universalimageloader.utils.L;
import com.xbcx.core.bmp.XImageDecoder.BytesBufferPool.BytesBuffer;
import com.xbcx.utils.SystemUtils;

public class XImageDecoder extends BaseImageDecoder {
	
	private BytesBufferPool	mByteBufferPool;

	public XImageDecoder(boolean loggingEnabled) {
		super(loggingEnabled);
		mByteBufferPool = new BytesBufferPool(4, 200 * 1024);
	}
	
	public Bitmap decode(ImageDecodingInfo decodingInfo) throws IOException {
		Bitmap decodedBitmap;
		ImageFileInfo imageInfo;
		BytesBuffer bb = mByteBufferPool.get();
		InputStream imageStream = getImageStream(decodingInfo);
		try {
			imageInfo = defineImageSizeAndRotation(imageStream, decodingInfo);
			imageStream = resetStream(imageStream, decodingInfo);
			Options decodingOptions = prepareDecodingOptions(imageInfo.imageSize, decodingInfo);
			if(bb.data.length < imageStream.available()){
				bb.data = new byte[imageStream.available()];
			}
			int len = 0;
			byte[] buffer = new byte[1024];
			int offset = 0;
			while ((len = imageStream.read(buffer)) != -1) {
				System.arraycopy(buffer, 0, bb.data, offset, len);
				offset += len;
			}
			decodedBitmap = BitmapFactory.decodeByteArray(bb.data, 0, offset, decodingOptions);
		} finally {
			IoUtils.closeSilently(imageStream);
			mByteBufferPool.recycle(bb);
		}

		if (decodedBitmap == null) {
			L.e(ERROR_CANT_DECODE_IMAGE, decodingInfo.getImageKey());
		} else {
			decodedBitmap = considerExactScaleAndOrientatiton(decodedBitmap, decodingInfo, imageInfo.exif.rotation,
					imageInfo.exif.flipHorizontal);
		}
		return decodedBitmap;
	}
	
	protected Options prepareDecodingOptions(ImageSize imageSize, ImageDecodingInfo decodingInfo) {
		ImageScaleType scaleType = decodingInfo.getImageScaleType();
		int scale;
		if (scaleType == ImageScaleType.NONE) {
			scale = ImageSizeUtils.computeMinImageSampleSize(imageSize);
		} else {
			ImageSize targetSize = decodingInfo.getTargetSize();
			boolean powerOf2 = scaleType == ImageScaleType.IN_SAMPLE_POWER_OF_2;
			//scale = ImageSizeUtils.computeImageSampleSize(imageSize, targetSize, decodingInfo.getViewScaleType(), powerOf2);
			
			final int height = imageSize.getHeight();
	        final int width = imageSize.getWidth();
	        final int reqHeight = targetSize.getHeight();
	        final int reqWidth = targetSize.getWidth();
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
	        if(powerOf2){
	        	inSampleSize = SystemUtils.nextPowerOf2(inSampleSize);
	        }
			scale = inSampleSize;
		}
		if (scale > 1 && loggingEnabled) {
			//L.d(LOG_SABSAMPLE_IMAGE, imageSize, imageSize.scaleDown(scale), scale, decodingInfo.getImageKey());
		}

		Options decodingOptions = decodingInfo.getDecodingOptions();
		decodingOptions.inSampleSize = scale;
		return decodingOptions;
	}
	
	public static class BytesBufferPool {

	    public static class BytesBuffer {
	        public byte[] data;

	        private BytesBuffer(int capacity) {
	            this.data = new byte[capacity];
	        }
	    }

	    private final int mPoolSize;
	    private final int mBufferSize;
	    private final ArrayList<SoftReference<BytesBuffer>> mList;

	    public BytesBufferPool(int poolSize, int bufferSize) {
	        mList = new ArrayList<SoftReference<BytesBuffer>>(poolSize);
	        mPoolSize = poolSize;
	        mBufferSize = bufferSize;
	    }

	    public synchronized BytesBuffer get() {
	        int n = mList.size();
	        if(n > 0){
	        	SoftReference<BytesBuffer> sr = mList.remove(n - 1);
	        	final BytesBuffer bb = sr.get();
	        	if(bb == null){
	        		return new BytesBuffer(mBufferSize);
	        	}else{
	        		return bb;
	        	}
	        }
	        return new BytesBuffer(mBufferSize);
	    }

	    public synchronized void recycle(BytesBuffer buffer) {
	        if (buffer.data.length != mBufferSize) return;
	        if (mList.size() < mPoolSize) {
	            mList.add(new SoftReference<BytesBuffer>(buffer));
	        }
	    }

	    public synchronized void clear() {
	        mList.clear();
	    }
	}
}
