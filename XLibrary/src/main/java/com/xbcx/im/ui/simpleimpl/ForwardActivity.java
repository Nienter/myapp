package com.xbcx.im.ui.simpleimpl;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.xbcx.common.pulltorefresh.PullToRefreshActivity;
import com.xbcx.core.Event;
import com.xbcx.core.EventCode;
import com.xbcx.core.XEndlessAdapter;
import com.xbcx.core.db.XDB;
import com.xbcx.im.IMKernel;
import com.xbcx.im.XMessage;
import com.xbcx.im.db.DBColumns;
import com.xbcx.im.db.MessageBaseRunner;
import com.xbcx.im.db.MessageCreator;
import com.xbcx.im.recentchat.RecentChat;
import com.xbcx.im.recentchat.RecentChatAdapter;
import com.xbcx.im.recentchat.RecentChatManager;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

public class ForwardActivity extends PullToRefreshActivity {
	
	public static final String Extra_DialogMsg = "dialog_msg";
	public static final String Extra_JumpChat  = "jump_chat";
	
	protected ForwardRecentChatAdapter	mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setIsCreateRefresh(false);
		setIsHideViewFirstLoad(false);
		super.onCreate(savedInstanceState);
		
		disableRefresh();
		
		addAndManageEventListener(EventCode.RecentChatChanged);
		RecentChatManager.getInstance().asyncLoadDataNotify();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onInitAttribute(BaseAttribute ba) {
		super.onInitAttribute(ba);
		ba.mActivityLayoutId = R.layout.activity_simple_pulltorefresh;
		ba.mTitleTextStringId = R.string.forwardmessage;
		ba.mAddBackButton = true;
	}
	
	@Override
	public void onStartLoadMore(XEndlessAdapter adapter) {
	}

	@Override
	public ListAdapter onCreateAdapter() {
		mAdapter = new ForwardRecentChatAdapter(this);
		return mAdapter;
	}
	
	protected RecentChatAdapter	onCreateRecentChatAdapter(){
		return new RecentChatAdapter(this);
	}
	
	@Override
	public void onPullDownToRefresh() {
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onEventRunEnd(Event event) {
		super.onEventRunEnd(event);
		if(event.getEventCode() == EventCode.RecentChatChanged){
			removeEventListener(EventCode.RecentChatChanged);
			List<RecentChat> rcs = (List<RecentChat>)event.getParamAtIndex(0);
			List<RecentChat> news = new ArrayList<RecentChat>();
			for(RecentChat rc : rcs){
				if(rc.getActivityType() == ActivityType.SingleChat ||
						rc.getActivityType() == ActivityType.GroupChat ||
						rc.getActivityType() == ActivityType.DiscussionChat){
					news.add(rc);
				}
			}
			mAdapter.replaceAll(news);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Object obj = parent.getItemAtPosition(position);
		if(obj != null){
			if(obj instanceof RecentChat){
				final RecentChat rc = (RecentChat)obj;
				String msg = getIntent().getStringExtra(Extra_DialogMsg);
				if(TextUtils.isEmpty(msg)){
					msg = getString(R.string.dialog_msg_forward);
				}
				showYesNoDialog(R.string.yes, R.string.no, 
						msg,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(which == DialogInterface.BUTTON_POSITIVE){
									int forwardfromType = XMessage.FROMTYPE_SINGLE;
									if(rc.getActivityType() == ActivityType.GroupChat){
										forwardfromType = XMessage.FROMTYPE_GROUP;
									}else if(rc.getActivityType() == ActivityType.DiscussionChat){
										forwardfromType = XMessage.FROMTYPE_DISCUSSION;
									}
									fowardMessage(forwardfromType, rc.getId(), rc.getName());
								}
							}
						});
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void fowardMessage(int fromType,String toId,String toName){
		List<String> pics = (List<String>)getIntent().getSerializableExtra("pics");
		if(pics == null){
			XMessage xm = getIncomingXMessage(ForwardActivity.this);
			if(xm != null){
				IMKernel.forwardMessage(xm, fromType, toId, toName);
				onForwardFinish(fromType,toId,toName);
			}
		}else{
			for(String pic : pics){
				IMKernel.forwardMessage(pic, fromType, toId, toName);
			}
			onForwardFinish(fromType,toId,toName);
		}
	}
	
	protected void onForwardFinish(int fromType,String uid,String uname){
		if(getIntent().getBooleanExtra(Extra_JumpChat, false)){
			ActivityType.launchChatActivity(this, 
					IMKernel.fromTypeToActivityType(fromType), 
					uid, uname);
		}
		mToastManager.show(mTextViewTitle.getText() + getString(R.string.success));
		finish();
	}
	
	public static XMessage getIncomingXMessage(Activity activity){
		final Intent intent = activity.getIntent();
		XMessage xm = (XMessage)intent.getSerializableExtra("data");
		if(xm == null){
			final String id = intent.getStringExtra("id");
			final int fromType = intent.getIntExtra("fromtype", XMessage.FROMTYPE_SINGLE);
			final String message_id = intent.getStringExtra("message_id");
			xm = XDB.getInstance().readItem(
					MessageBaseRunner.getTableName(id), 
					DBColumns.Message.COLUMN_ID + "='" + message_id + "'", 
					new MessageCreator(id, fromType));
		}
		return xm;
	}
	
	protected static class ForwardViewHolder{
		public 	TextView	mTextViewName;
		public	ImageView	mImageViewAvatar;
	}
	
	protected static class ForwardRecentChatAdapter extends RecentChatAdapter{
		
		public ForwardRecentChatAdapter(Context context) {
			super(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ForwardViewHolder holder = null;
			if(convertView == null){
				convertView = SystemUtils.inflate(parent.getContext(), R.layout.xlibrary_adapter_forward_recentchat);
				holder = new ForwardViewHolder();
				holder.mImageViewAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
				holder.mTextViewName = (TextView)convertView.findViewById(R.id.tvName);
				convertView.setTag(holder);
			}else{
				holder = (ForwardViewHolder)convertView.getTag();
			}
			
			final RecentChat rc = (RecentChat)getItem(position);
			
			onSetName(holder.mTextViewName, rc);
			onSetAvatar(holder.mImageViewAvatar, rc);
			
			return convertView;
		}
	}
}
