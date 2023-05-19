package com.xbcx.im.ui;

import android.text.TextUtils;
import android.widget.TextView;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.BaseActivity.ActivityEventHandler;
import com.xbcx.core.BaseActivity.BaseAttribute;
import com.xbcx.im.IMKernel;
import com.xbcx.im.IMStatus;
import com.xbcx.library.R;

public class TitleEventHandler implements ActivityEventHandler{
	
	public TitleEventHandler(BaseActivity activity){
		final TextView tvTitle = activity.getTextViewTitle();
		activity.addAndManageEventListener(EventCode.IM_Login);
		activity.addAndManageEventListener(EventCode.IM_LoginStart);
		activity.addAndManageEventListener(EventCode.IM_ConnectionInterrupt);
		activity.registerActivityEventHandler(EventCode.IM_Login, this);
		activity.registerActivityEventHandler(EventCode.IM_LoginStart, this);
		activity.registerActivityEventHandler(EventCode.IM_ConnectionInterrupt, this);
		IMStatus status = IMKernel.getIMStatus();
		if(status.mIsConflict){
			ActivityType.launchConflictActivity(activity);
		}else{
			if(!status.mIsLoginSuccess){
				if(tvTitle != null){
					tvTitle.setText(R.string.connecting);
					if(!status.mIsLogining){
						AndroidEventManager.getInstance().pushEvent(EventCode.IM_Login);
					}
				}
			}
		}
	}
	
	@Override
	public void onHandleEventEnd(Event event, BaseActivity activity) {
		final TextView tvTitle = activity.getTextViewTitle();
		final int code = event.getEventCode();
		if(tvTitle != null){
			if(code == EventCode.IM_LoginStart){
				tvTitle.setText(R.string.connecting);
			}else if(code == EventCode.IM_Login){
				if(event.isSuccess()){
					final BaseAttribute ba = activity.getBaseAttribute();
					if(TextUtils.isEmpty(ba.mTitleText)){
						tvTitle.setText(ba.mTitleTextStringId);
					}else{
						tvTitle.setText(ba.mTitleText);
					}
				}else{
					tvTitle.setText(R.string.disconnect);
				}
			}else if(code == EventCode.IM_ConnectionInterrupt){
				tvTitle.setText(R.string.disconnect);
			}
		}
	}
}
