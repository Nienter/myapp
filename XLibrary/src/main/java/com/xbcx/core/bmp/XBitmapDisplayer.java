package com.xbcx.core.bmp;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

public class XBitmapDisplayer implements BitmapDisplayer {

    @Override
    public void display(Bitmap bitmap, ImageAware iv, LoadedFrom from) {
        if (from == LoadedFrom.MEMORY_CACHE) {
            iv.setImageBitmap(bitmap);
        } else {
            iv.setImageBitmap(bitmap);
//			if(iv.getDrawable() == null || bitmap.hasAlpha()){
//	    		final TransitionDrawable td =
//	                    new TransitionDrawable(new Drawable[] {
//	                            new ColorDrawable(android.R.color.transparent),
//	                            new BitmapDrawable(iv.getResources(), bitmap)
//	                    });
//	    		iv.setImageDrawable(td);
//	    		td.startTransition(300);
//	    	}else{
//	    		final TransitionDrawable td = new XTransitionDrawable(
//	        			iv.getDrawable(),
//	        			new BitmapDrawable(iv.getResources(), bitmap));
//				iv.setImageDrawable(td);
//				td.startTransition(500);
//	    	}
        }
    }

    private static class XTransitionDrawable extends TransitionDrawable {
        Drawable mSecond;

        public XTransitionDrawable(Drawable first, Drawable second) {
            super(new Drawable[]{first, second});
            mSecond = second;
        }

        @Override
        public int getIntrinsicWidth() {
            return mSecond.getIntrinsicWidth();
        }

        @Override
        public int getIntrinsicHeight() {
            return mSecond.getIntrinsicHeight();
        }
    }
}
