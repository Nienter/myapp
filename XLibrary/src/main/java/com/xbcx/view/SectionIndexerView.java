package com.xbcx.view;

import java.util.ArrayList;
import java.util.List;

import com.xbcx.utils.SystemUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class SectionIndexerView extends View {
	
	private Paint				mPaint = new Paint();
	
	private List<String> 		mSections = new ArrayList<String>();
	
	private int					mSelectSection = -1;
	
	private	OnSectionListener 	mOnSectionListener;
	private TextView			mTextViewPrompt;

	public SectionIndexerView(Context context) {
		super(context);
		init();
	}
	
	public SectionIndexerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init(){
		mPaint.setColor(0xff7D7D7D);
		mPaint.setTextSize(SystemUtils.dipToPixel(getContext(), 14));
		mPaint.setTextAlign(Paint.Align.CENTER);
	}

	public void setSections(List<String> sections){
		mSections.clear();
		mSections.addAll(sections);
		invalidate();
	}

	public void setTextViewPrompt(TextView tv){
		mTextViewPrompt = tv;
		mTextViewPrompt.setVisibility(View.GONE);
	}
	
	public void setOnSectionListener(OnSectionListener listener){
		mOnSectionListener = listener;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		if(action == MotionEvent.ACTION_DOWN ||
				action == MotionEvent.ACTION_MOVE){
			if(mSections.size() > 0){
				float fy = event.getY();
				int sectionHeight = getSectionHeight();
				int section = (int)fy / sectionHeight;
				
				if(mSelectSection != section && 
						section >= 0 && 
						section < mSections.size()){
					mSelectSection = section;
					mTextViewPrompt.setVisibility(View.VISIBLE);
					mTextViewPrompt.setText(mSections.get(mSelectSection));
					if(mOnSectionListener != null){
						mOnSectionListener.onSectionSelected(this, mSelectSection);
					}
				}
				return true;
			}
		}else{
			mSelectSection = -1;
			mTextViewPrompt.setVisibility(View.GONE);
		}
		
		return super.onTouchEvent(event);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int count = mSections.size();
		if(count > 0){
			final int widthBy2 = getMeasuredWidth() / 2;
			int sectionHeight = getMeasuredHeight() / count;
			for(int index = 0;index < count;++index){
				canvas.drawText(mSections.get(index), 
						widthBy2, sectionHeight * index + (sectionHeight / 2),
						mPaint);
			}
		}
	}
	
	protected int getSectionHeight(){
		return mSections.size() > 0 ? getMeasuredHeight() / mSections.size() : 0;
	}
	
	public static interface OnSectionListener{
		public void onSectionSelected(SectionIndexerView view,int section);
	}
}
