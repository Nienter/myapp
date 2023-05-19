package com.xbcx.im.ui;

import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.BaseActivity.ActivityEventHandler;
import com.xbcx.im.IMKernel;
import com.xbcx.library.R;

public class NotifyConnectionEventHandler implements ActivityEventHandler{
	
	private BaseActivity	mActivity;
	
	private View			mViewPromptConnection;
	private View			mViewConnecting;
	private View			mViewNormal;
	private ImageView		mImageViewPromptConnection;
	
	public NotifyConnectionEventHandler(BaseActivity activity){
		mActivity = activity;
		activity.addAndManageEventListener(EventCode.IM_ConnectionInterrupt);
		activity.addAndManageEventListener(EventCode.IM_Login);
		activity.addAndManageEventListener(EventCode.IM_LoginStart);
		activity.registerActivityEventHandler(EventCode.IM_ConnectionInterrupt, this);
		activity.registerActivityEventHandler(EventCode.IM_Login, this);
		activity.registerActivityEventHandler(EventCode.IM_LoginStart, this);
		if(!IMKernel.isIMConnectionAvailable()){
			addConnectionPromptView();
			AndroidEventManager.getInstance().pushEvent(EventCode.IM_Login);
		}
	}
	
	@Override
	public void onHandleEventEnd(Event event, BaseActivity activity) {
		final int code = event.getEventCode();
		if(code == EventCode.IM_Login){
			if(IMKernel.isIMConnectionAvailable()){
				removeConnectionPromptView();
			}else{
				if(mViewPromptConnection != null){
					mViewPromptConnection.setVisibility(View.VISIBLE);
					mViewConnecting.setVisibility(View.GONE);
					mViewNormal.setVisibility(View.VISIBLE);
				}
			}
		}else if(code == EventCode.IM_ConnectionInterrupt){
			addConnectionPromptView();
			mViewConnecting.setVisibility(View.GONE);
			mViewNormal.setVisibility(View.VISIBLE);
		}else if(code == EventCode.IM_LoginStart){
			addConnectionPromptView();
			mViewConnecting.setVisibility(View.VISIBLE);
			mViewNormal.setVisibility(View.GONE);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void startUnConnectionAnimation(){
		if(mViewPromptConnection != null && mViewNormal.getVisibility() == View.VISIBLE){
			mImageViewPromptConnection = (ImageView)mViewPromptConnection.findViewById(R.id.iv);
			mImageViewPromptConnection.setBackgroundDrawable(null);
			mImageViewPromptConnection.setBackgroundResource(R.drawable.animlist_prompt_connection);
			AnimationDrawable d = (AnimationDrawable)mImageViewPromptConnection.getBackground();
			d.start();
		}
	}
	
	private void addConnectionPromptView(){
		final BaseActivity activity = mActivity;
		if(activity != null){
			if(mViewPromptConnection == null){
				mViewPromptConnection = LayoutInflater.from(activity).inflate(R.layout.xlibrary_prompt_connection, null);
				mViewConnecting = mViewPromptConnection.findViewById(R.id.viewConnecting);
				mViewNormal = mViewPromptConnection.findViewById(R.id.viewNormal);
				mViewConnecting.setVisibility(View.VISIBLE);
				mViewNormal.setVisibility(View.GONE);
				activity.addContentView(mViewPromptConnection, new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			}else{
				mViewPromptConnection.setVisibility(View.VISIBLE);
			}
		}
	}
	
	private void removeConnectionPromptView(){
		if(mViewPromptConnection != null){
			mViewPromptConnection.setVisibility(View.GONE);
		}
	}
}
