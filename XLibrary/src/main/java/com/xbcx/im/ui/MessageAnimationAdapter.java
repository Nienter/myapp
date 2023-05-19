package com.xbcx.im.ui;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.xbcx.adapter.AdapterWrapper;
import com.xbcx.core.XApplication;
import com.xbcx.utils.SystemUtils;

import java.util.HashMap;

public class MessageAnimationAdapter extends AdapterWrapper{
	
	private ListView				mListView;
	private int						mLastAddPosition;
	private HashMap<View, View> 	mMapViews = new HashMap<View, View>();
	
	private boolean					mAnimate;

	public MessageAnimationAdapter(BaseAdapter baseAdapter,ListView lv) {
		super(baseAdapter);
		mListView = lv;
	}
	
	public void setAnimate(boolean b){
		mAnimate = b;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		mMapViews.put(v, v);
		if(position > mLastAddPosition){
			mLastAddPosition = position;
			if(mAnimate){
				int height = 0;
				try{
					v.measure(MeasureSpec.makeMeasureSpec(XApplication.getScreenWidth(), MeasureSpec.EXACTLY),
							MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
					height = v.getMeasuredHeight();
				}catch(Exception e){
					//e.printStackTrace();
					height = SystemUtils.dipToPixel(parent.getContext(), 30);
				}
				if(mListView.getFirstVisiblePosition() == 0){
					ObjectAnimator.ofFloat(v, "translationY", height,0)
					.setDuration(200)
					.start();
				}else{
					for(View animateView : mMapViews.keySet()){
						ObjectAnimator.ofFloat(animateView, "translationY", height,0)
						.setDuration(200)
						.start();
					}
				}
			}
		}
		return v;
	}
}
