package com.xbcx.im.ui.simpleimpl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.xbcx.core.Event;
import com.xbcx.im.XMessage;
import com.xbcx.im.extention.roster.IMGroup;
import com.xbcx.im.extention.roster.RosterServicePlugin;
import com.xbcx.im.ui.BaseChatActivity;
import com.xbcx.library.R;

public class GroupChatActivity extends BaseChatActivity {

	protected	View	mViewTitleRight;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addAndManageEventListener(RosterServicePlugin.IM_ChangeGroupChatName);
		addAndManageEventListener(RosterServicePlugin.IM_DeleteGroupChat);
		addAndManageEventListener(RosterServicePlugin.IM_QuitGroupChat);
		addAndManageEventListener(RosterServicePlugin.IM_GroupChatListChanged);
		
		if(TextUtils.isEmpty(mName)){
			final IMGroup group = RosterServicePlugin.getInterface().getGroup(mId);
			if(group != null){
				getTextViewTitle().setText(group.getName());
			}
		}
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
		Intent intent = new Intent(activity, GroupChatActivity.class);
		intent.putExtra(EXTRA_ID, id);
		intent.putExtra(EXTRA_NAME, name);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
	}
	
	@Override
	public View addImageButtonInTitleRight(int resId) {
		mViewTitleRight = super.addImageButtonInTitleRight(resId);
		if(RosterServicePlugin.getInterface().getGroup(mId) == null){
			mViewTitleRight.setVisibility(View.GONE);
		}
		return mViewTitleRight;
	}
	
	@Override
	public View addTextButtonInTitleRight(int textId) {
		mViewTitleRight = super.addTextButtonInTitleRight(textId);
		if(RosterServicePlugin.getInterface().getGroup(mId) == null){
			mViewTitleRight.setVisibility(View.GONE);
		}
		return mViewTitleRight;
	}
	
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		final int code = event.getEventCode();
		if(code == RosterServicePlugin.IM_ChangeGroupChatName){
			if(event.isSuccess()){
				final String id = (String)event.getParamAtIndex(0);
				if(mId.equals(id)){
					final String name = (String)event.getParamAtIndex(1);
					getTextViewTitle().setText(name);
				}
			}
		}else if(code == RosterServicePlugin.IM_DeleteGroupChat || 
				code == RosterServicePlugin.IM_QuitGroupChat){
			if(event.isSuccess()){
				finish();
			}
		}else if(code == RosterServicePlugin.IM_GroupChatListChanged){
			if(RosterServicePlugin.getInterface().getGroup(mId) == null){
				finish();
			}else{
				if(mViewTitleRight != null){
					mViewTitleRight.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	@Override
	protected int getFromType() {
		return XMessage.FROMTYPE_GROUP;
	}

	@Override
	protected boolean isGroupChat() {
		return true;
	}

	protected String getContextMenuTitle(XMessage message){
		return message.getUserName();
	}
}
