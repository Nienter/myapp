package com.xbcx.im.extention.blacklist;

import com.xbcx.im.ui.simpleimpl.AbsBaseAdapter;
import com.xbcx.im.vcard.VCardProvider;
import com.xbcx.library.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class BlackListAdapter extends AbsBaseAdapter<String,BlackListAdapter.BlackViewHolder> {

	public BlackListAdapter(Context context) {
		super(context);
	}

	@Override
	protected View onCreateConvertView() {
		return LayoutInflater.from(mContext).inflate(R.layout.xlibrary_adapter_blacklist, null);
	}

	@Override
	protected BlackViewHolder onCreateViewHolder() {
		return new BlackViewHolder();
	}

	@Override
	protected void onSetViewHolder(BlackViewHolder viewHolder, View convertView) {
		viewHolder.mImageViewAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
		viewHolder.mTextViewName = (TextView)convertView.findViewById(R.id.tvName);
	}

	@Override
	protected void onSetChildViewTag(BlackViewHolder viewHolder, Object item) {
	}

	@Override
	protected void onUpdateView(BlackViewHolder viewHolder, String item, int position) {
		VCardProvider.getInstance().setAvatar(viewHolder.mImageViewAvatar, item);
		VCardProvider.getInstance().setName(viewHolder.mTextViewName, item);
	}

	protected static class BlackViewHolder extends AbsBaseAdapter.ViewHolder{
		public ImageView 	mImageViewAvatar;
		public TextView		mTextViewName;
	}
}
