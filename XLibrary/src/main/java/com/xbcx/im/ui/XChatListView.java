package com.xbcx.im.ui;

import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;
import com.xbcx.view.PulldownableListView;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

public class XChatListView extends PulldownableListView {

	private XChatEditView 	mEditView;
	private View			mHeaderView;
	
	public XChatListView(Context context) {
		super(context);
		init();
	}

	public XChatListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init(){
		setCacheColorHint(0x00000000);
		setSelector(new ColorDrawable(0x00000000));
		setDivider(null);
		
		addHeaderView(mHeaderView = 
				LayoutInflater.from(getContext()).inflate(
						R.layout.xlibrary_chatlist_header, null));
	}
	
	public void setEditView(XChatEditView editView){
		mEditView = editView;
	}
	
	public void	removeHeaderView(){
		removeHeaderView(mHeaderView);
	}

	@Override
	protected View onCreatePullDownView() {
		View v = LayoutInflater.from(getContext()).inflate(R.layout.xlibrary_refreshview_loadmoremessage, null);
		return v;
	}

	@Override
	protected int getPullDownBeyondHeight() {
		return SystemUtils.dipToPixel(getContext(), 30);
	}

	@Override
	protected void onStartRun() {
	}

	@Override
	protected void onEndRun() {
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(ev.getAction() == MotionEvent.ACTION_DOWN){
			if(mEditView != null){
				mEditView.hideAllPullUpView(true);
				mEditView.hideInputMethod();
			}
		}
		return super.onInterceptTouchEvent(ev);
	}
}
