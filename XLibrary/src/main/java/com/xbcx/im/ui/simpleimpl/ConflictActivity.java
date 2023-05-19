package com.xbcx.im.ui.simpleimpl;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.EventCode;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.library.R;

public class ConflictActivity extends Activity implements DialogInterface.OnClickListener{

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		showDialog(1);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		if(id == 1){
			AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppBaseTheme);
			builder.setMessage(R.string.dialogmessage_logout)
			.setCancelable(false)
			.setTitle("")
			.setIcon(R.drawable.ic_dialog_alert)
			.setPositiveButton(R.string.ok, this);
			return builder.create();
		}
		return super.onCreateDialog(id);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		
		AndroidEventManager.getInstance().runEvent(EventCode.LoginActivityLaunched);
		
		Class<?> cls = ActivityType.getActivityClass(ActivityType.ConflictJumpActivity);
		if(cls != null){
			try{
				Intent intent = new Intent(this, cls);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		finish();
	}
}
