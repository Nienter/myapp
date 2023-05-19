package com.xbcx.im.ui.friend;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

import com.xbcx.core.ActivityPlugin;
import com.xbcx.core.Event;
import com.xbcx.core.ToastManager;
import com.xbcx.core.BaseActivity;
import com.xbcx.core.BaseActivity.ActivityEventHandler;
import com.xbcx.im.extention.roster.RosterServicePlugin;
import com.xbcx.library.R;

public class AddFriendActivityPlugin extends ActivityPlugin<BaseActivity> implements
													ActivityEventHandler{

	@Override
	protected void onAttachActivity(BaseActivity activity) {
		super.onAttachActivity(activity);
		activity.registerActivityEventHandler(RosterServicePlugin.IM_AddFriendApply, this);
	}

	@Override
	public void onHandleEventEnd(Event event, BaseActivity activity) {
		if(event.getEventCode() == RosterServicePlugin.IM_AddFriendApply){
			if(event.isSuccess()){
				ToastManager.getInstance(activity).show(R.string.add_friend_success);
			}else{
				Exception e = event.getFailException();
				if(e != null && e instanceof XMPPException){
					final XMPPError error = ((XMPPException)e).getXMPPError();
					if(error != null){
						if(error.getCode() == 405){
							ToastManager.getInstance(activity).show(R.string.toast_cannot_add_friend);
						}else if(error.getCode() == 407){
							onShowAddFriendVerifyDialog(event, activity.getDialogContext());
						}
					}else{
						ToastManager.getInstance(activity).show(R.string.toast_disconnect);
					}
				}else{
					ToastManager.getInstance(activity).show(R.string.toast_disconnect);
				}
			}
		}
	}
	
	protected void onShowAddFriendVerifyDialog(final Event event,Context context){
		final EditText editText = new EditText(context);
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == DialogInterface.BUTTON_POSITIVE){
					mActivity.pushEvent(RosterServicePlugin.IM_AddFriendVerify, 
							event.getParamAtIndex(0),editText.getText().toString());
				}
			}
		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.add_friend_verify_dialog_title)
		.setMessage(R.string.add_friend_verify_dialog_message)
		.setPositiveButton(R.string.send, listener)
		.setNegativeButton(R.string.cancel, listener)
		.setView(editText)
		.show();
	}
}
