package com.xbcx.im.ui.simpleimpl;

import com.xbcx.core.Event;
import com.xbcx.im.XMessage;
import com.xbcx.im.extention.roster.RosterServicePlugin;
import com.xbcx.im.ui.BaseChatActivity;
import com.xbcx.library.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SingleChatActivity extends BaseChatActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setName(getTextViewTitle(), mId, mName);
		
		addAndManageEventListener(RosterServicePlugin.IM_DeleteFriend);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mActivityLayoutId = R.layout.xlibrary_activity_chat;
	}

	public static void launch(Activity activity,String id,String name){
		Intent intent = new Intent(activity, SingleChatActivity.class);
		intent.putExtra(EXTRA_ID, id);
		intent.putExtra(EXTRA_NAME, name);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
	}

	@Override
	protected int getFromType() {
		return XMessage.FROMTYPE_SINGLE;
	}

	@Override
	protected boolean isGroupChat() {
		return false;
	}

	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == RosterServicePlugin.IM_DeleteFriend){
			if(event.isSuccess()){
				final String id = (String)event.getParamAtIndex(0);
				if(mId.equals(id)){
					finish();
				}
			}
		}
	}
	
}
