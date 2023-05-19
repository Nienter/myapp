package com.xbcx.im.message.imgtext;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.xbcx.core.XApplication;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.messageviewprovider.CommonViewProvider;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

public class ImgTextViewLeftProvider extends CommonViewProvider<ImgTextViewLeftProvider.DViewHolder> {

	@Override
	protected DViewHolder onCreateViewHolder() {
		return new DViewHolder();
	}

	@Override
	protected void onSetViewHolder(View convertView, DViewHolder viewHolder, XMessage xm) {
		super.onSetViewHolder(convertView, viewHolder, xm);
		DViewHolder holder = (DViewHolder)viewHolder;
		final View v = LayoutInflater.from(convertView.getContext()).inflate(R.layout.message_content_imgtext, null);
		holder.mImageViewPic = (ImageView)v.findViewById(R.id.ivPic);
		holder.mTextViewContent = (TextView)v.findViewById(R.id.tvContent);
		holder.mViewClick = v.findViewById(R.id.viewClick);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT, 
				FrameLayout.LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.TOP;
		lp.leftMargin = SystemUtils.dipToPixel(convertView.getContext(), 7);
		lp.rightMargin = SystemUtils.dipToPixel(convertView.getContext(), 7);
		holder.mContentView.addView(v,lp);
	}

	@Override
	public boolean acceptHandle(XMessage message) {
		if(!message.isFromSelf() && 
				message.getType() == XMessage.TYPE_IMGTEXT){
			return true;
		}
		return false;
	}

	@Override
	protected void onUpdateView(DViewHolder holder, XMessage m) {
		holder.mTextViewContent.setText(m.getContent());
		XApplication.setBitmap(holder.mImageViewPic, m.getUrl(), 0);
		if(TextUtils.isEmpty(m.getImgTextValue())){
			holder.mViewClick.setVisibility(View.GONE);
		}else{
			holder.mViewClick.setVisibility(View.VISIBLE);
		}
	}

	protected static class DViewHolder extends CommonViewProvider.CommonViewHolder{
		public ImageView	mImageViewPic;
		public TextView		mTextViewContent;
		public View			mViewClick;
	}
}
