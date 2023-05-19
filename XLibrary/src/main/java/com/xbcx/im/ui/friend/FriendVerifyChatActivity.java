package com.xbcx.im.ui.friend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.xbcx.core.Event;
import com.xbcx.im.XMessage;
import com.xbcx.im.extention.roster.RosterServicePlugin;
import com.xbcx.im.ui.BaseChatActivity;
import com.xbcx.im.ui.messageviewprovider.FriendVerifyNoticeViewProvider;
import com.xbcx.im.ui.messageviewprovider.TimeViewProvider;
import com.xbcx.library.R;

public class FriendVerifyChatActivity extends BaseChatActivity implements
												AdapterView.OnItemClickListener{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addAndManageEventListener(RosterServicePlugin.IM_AddFriendConfirm);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public static void launch(Activity activity,String id,String name){
		Intent intent = new Intent(activity,FriendVerifyChatActivity.class);
		activity.startActivity(intent);
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mAddBackButton = true;
		ba.mTitleText = mName;
		ba.mActivityLayoutId = R.layout.xlibrary_activity_friendverifychat;
	}

	@Override
	protected void onInit() {
		super.onInit();
		mListView.setOnItemClickListener(this);
	}

	@Override
	protected void onAddMessageViewProvider() {
		mMessageAdapter.addIMMessageViewProvider(new TimeViewProvider());
		mMessageAdapter.addIMMessageViewProvider(new FriendVerifyNoticeViewProvider(this));
	}

	@Override
	protected int getFromType() {
		return XMessage.FROMTYPE_GROUP;
	}

	@Override
	protected boolean isGroupChat() {
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	}

	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int nCode = event.getEventCode();
		if(nCode == RosterServicePlugin.IM_AddFriendConfirm){
			if(event.isSuccess()){
				final String userId = (String)event.getParamAtIndex(0);
				for(XMessage xm : mMessageAdapter.getAllItem()){
					if(userId.equals(xm.getUserId())){
						xm.setAddFriendAskHandled(true);
						xm.updateDB();
					}
				}
				redrawMessage(null);
			}
		}
	}

}
