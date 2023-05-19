package com.xbcx.view;

import com.xbcx.utils.SystemUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class PageIndicator extends View {
	
	private int mPageCount;
	private int mPageCurrent;
	
	private int mIndicatorSpacing;
	private int mIndicatorSize;
	private float mIndicatorSizeBy2;
	
	private int mColorSelect = 0xffc7ff9a;
	private int mColorNormal = 0xff172b04;
	
	private Rect mRectTemp = new Rect();
	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	public PageIndicator(Context context) {
		super(context);
		init();
	}

	public PageIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init(){
		mPageCurrent = -1;
		mIndicatorSpacing = SystemUtils.dipToPixel(getContext(), 10);
		mIndicatorSize = SystemUtils.dipToPixel(getContext(),6);
		mIndicatorSizeBy2 = (float)mIndicatorSize / 2;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int nHeight = getHeight();
		for(int nIndex = 0;nIndex < mPageCount;++nIndex){
			mRectTemp.set(nIndex * (mIndicatorSize + mIndicatorSpacing), 0, 0, nHeight);
			mRectTemp.right = mRectTemp.left + mIndicatorSize;
			if(nIndex == mPageCurrent){
				mPaint.setColor(mColorSelect);
			}else{
				mPaint.setColor(mColorNormal);
			}
			canvas.drawCircle(mRectTemp.left + mIndicatorSizeBy2, mIndicatorSizeBy2, mIndicatorSizeBy2, mPaint);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int nMeasuredWidth = 0;
		int nMeasuredHeight = 0;
		LayoutParams layoutParams = getLayoutParams();
		if(layoutParams.width == LayoutParams.WRAP_CONTENT || 
				View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.UNSPECIFIED){
			nMeasuredWidth = mPageCount * mIndicatorSize + (mPageCount - 1) * mIndicatorSpacing;
		}else{
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			nMeasuredWidth = getMeasuredWidth();
		}
		
		if(layoutParams.height == LayoutParams.WRAP_CONTENT || 
				View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.UNSPECIFIED){
			nMeasuredHeight = mIndicatorSize;
		}else{
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			nMeasuredHeight = getMeasuredHeight();
		}
		
		setMeasuredDimension(nMeasuredWidth, nMeasuredHeight);
		
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	public void setSelectColor(int nColor){
		mColorSelect = nColor;
	}
	
	public void setNormalColor(int nColor){
		mColorNormal = nColor;
	}

	public void setPageCount(int nCount){
		mPageCount = nCount;
		requestLayout();
	}
	
	public void setPageCurrent(int nPage){
		mPageCurrent = nPage;
		invalidate();
	}

	public void setIndicatorSpacing(int nSpacing){
		mIndicatorSpacing = nSpacing;
	}
	
	public int getPageCount(){
		return mPageCount;
	}
}
