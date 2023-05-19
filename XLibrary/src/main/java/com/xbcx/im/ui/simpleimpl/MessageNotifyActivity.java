package com.xbcx.im.ui.simpleimpl;

import com.xbcx.core.BaseActivity;
import com.xbcx.im.IMConfigManager;
import com.xbcx.library.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MessageNotifyActivity extends BaseActivity implements OnClickListener{
	
	private CheckBox mCheckBoxNewMessageNotification;
	private CheckBox mCheckBoxNewMessageVoiceNotification;
	private CheckBox mCheckBoxNewMessageShakeNotification;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mCheckBoxNewMessageNotification = (CheckBox)findViewById(R.id.cbNew_message_notification);
		mCheckBoxNewMessageVoiceNotification = (CheckBox)findViewById(R.id.cbNew_message_voice_notification);
		mCheckBoxNewMessageShakeNotification = (CheckBox)findViewById(R.id.cbNew_message_shake_notification);
		
		mCheckBoxNewMessageNotification.setChecked(IMConfigManager.getInstance().isReceiveNewMessageNotify());
		mCheckBoxNewMessageVoiceNotification.setChecked(IMConfigManager.getInstance().isReceiveNewMessageSoundNotify());
		mCheckBoxNewMessageShakeNotification.setChecked(IMConfigManager.getInstance().isReceiveNewMessageVibrateNotify());
		
		mCheckBoxNewMessageNotification.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mCheckBoxNewMessageVoiceNotification.setEnabled(isChecked);
				mCheckBoxNewMessageShakeNotification.setEnabled(isChecked);
				IMConfigManager.getInstance().setReceiveNewMessageNotify(isChecked);
			}
		});
		mCheckBoxNewMessageVoiceNotification.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				IMConfigManager.getInstance().setReceiveNewMessageSoundNotify(isChecked);
			}
		});
		mCheckBoxNewMessageShakeNotification.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				IMConfigManager.getInstance().setReceiveNewMessageVibrateNotify(isChecked);
			}
		});
		
		findViewById(R.id.viewNew_message_notification).setOnClickListener(this);
		findViewById(R.id.viewNew_message_voice_notification).setOnClickListener(this);
		findViewById(R.id.viewNew_message_shake_notification).setOnClickListener(this);
		
	}
	
	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mTitleTextStringId = R.string.message_notify_title;
		ba.mActivityLayoutId = R.layout.xlibrary_activity_messagenotify;
		ba.mAddBackButton = true;
	}
	
	public static void launch(Activity activity){
		Intent intent = new Intent(activity, MessageNotifyActivity.class);
		activity.startActivity(intent);
	}

	private void changeCheckBoxChecked(CheckBox checkBox){
		if(checkBox.isChecked()){
			checkBox.setChecked(false);
		}else {
			checkBox.setChecked(true);
		}
	}
	
	@Override
	public void onClick(View v) {
		final int nId = v.getId();
		if(nId == R.id.viewNew_message_notification){
			changeCheckBoxChecked(mCheckBoxNewMessageNotification);
			return;
		}
		
		if(mCheckBoxNewMessageNotification.isChecked()){
			if(nId == R.id.viewNew_message_voice_notification){
				changeCheckBoxChecked(mCheckBoxNewMessageVoiceNotification);
				return;
			}
			if(nId == R.id.viewNew_message_shake_notification){
				changeCheckBoxChecked(mCheckBoxNewMessageShakeNotification);
				return;
			}
		}
		
	}

	
	
}
