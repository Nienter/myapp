package com.xbcx.im.ui.messageviewprovider;

import com.xbcx.core.XApplication;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.IMMessageViewProvider;
import com.xbcx.im.vcard.VCardProvider;
import com.xbcx.library.R;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendVerifyNoticeViewProvider extends IMMessageViewProvider {

	private Context mContext;
	
	public FriendVerifyNoticeViewProvider(Context context){
		mContext = context;
	}
	
	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.getType() == XMessage.TYPE_TEXT){
			return true;
		}
		return false;
	}

	@Override
	public View getView(XMessage message, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.message_friendverifynotice, null);
			viewHolder = new ViewHolder();
			viewHolder.mImageViewAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
			viewHolder.mTextViewName = (TextView)convertView.findViewById(R.id.tvName);
			viewHolder.mTextViewMessage = (TextView)convertView.findViewById(R.id.tvMessage);
			viewHolder.mTextViewStatus = (TextView)convertView.findViewById(R.id.tvStatus);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		
		XMessage m = (XMessage)message;
		VCardProvider.getInstance().setAvatar(viewHolder.mImageViewAvatar, m.getUserId());
		viewHolder.mTextViewName.setText(m.getUserName());
		if(TextUtils.isEmpty(m.getContent())){
			viewHolder.mTextViewMessage.setText(R.string.friendask_apply_add_friend);
		}else{
			viewHolder.mTextViewMessage.setText(
					XApplication.getApplication().getString(R.string.friend_verify_msg) + m.getContent());
		}
		
		if(m.isAddFriendAskHandled()){
			viewHolder.mTextViewStatus.setText(R.string.friendask_added);
			viewHolder.mTextViewStatus.setTextColor(0xff7d7d7d);
		}else{
			viewHolder.mTextViewStatus.setText(R.string.friendask_nothandled);
			viewHolder.mTextViewStatus.setTextColor(0xffeb6100);
		}
		
		return convertView;
	}

	private static class ViewHolder{
		public ImageView 	mImageViewAvatar;
		
		public TextView		mTextViewName;
		
		public TextView		mTextViewMessage;
		
		public TextView		mTextViewStatus;
	}

}
