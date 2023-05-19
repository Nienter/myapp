package com.xbcx.im.ui.friend;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xbcx.im.ui.simpleimpl.AbsBaseAdapter;
import com.xbcx.library.R;

public abstract class IMAdbAdapter<E extends Object> extends AbsBaseAdapter<E,IMAdbAdapter.AdbViewHolder>{
	
	protected OnCheckCallBack			mOnCheckCallBack;
	
	protected boolean					mIsCheck;
	
	public IMAdbAdapter(Context context){
		super(context);
	}
	
	public void setOnCheckCallBack(OnCheckCallBack callback){
		mOnCheckCallBack = callback;
	}
	
	public void setIsCheck(boolean bCheck){
		mIsCheck = bCheck;
		notifyDataSetChanged();
	}
	
	protected View onCreateConvertView(){
		return LayoutInflater.from(mContext).inflate(R.layout.xlibrary_adapter_adb, null);
	}
	
	protected IMAdbAdapter.AdbViewHolder onCreateViewHolder(){
		return new AdbViewHolder();
	}
	
	protected void	onSetViewHolder(AdbViewHolder viewHolder,View convertView){
		viewHolder.mViewBackground = convertView.findViewById(R.id.viewBackground);
		viewHolder.mImageViewCheck = (ImageView)convertView.findViewById(R.id.ivCheck);
		viewHolder.mImageViewAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
		viewHolder.mTextViewName = (TextView)convertView.findViewById(R.id.tvName);
		viewHolder.mTextViewNumber = (TextView)convertView.findViewById(R.id.tvNumber);
		viewHolder.mTextViewDetail = (TextView)convertView.findViewById(R.id.tvDetail);
		
		viewHolder.mImageViewAvatar.setOnClickListener(this);
		if(viewHolder.mTextViewDetail != null){
			viewHolder.mTextViewDetail.setVisibility(View.GONE);
		}
		
		if(viewHolder.mImageViewCheck != null){
			viewHolder.mImageViewCheck.setOnClickListener(this);
			viewHolder.mImageViewCheck.setFocusable(false);
		}
	}
	
	protected void	onSetChildViewTag(AdbViewHolder viewHolder,Object item){
		viewHolder.mImageViewAvatar.setTag(item);
		if(viewHolder.mImageViewCheck != null){
			viewHolder.mImageViewCheck.setTag(item);
		}
	}

	protected void	onUpdateView(AdbViewHolder viewHolder,E item,int position){
		if(viewHolder.mImageViewCheck != null){
			if(mIsCheck){
				viewHolder.mImageViewCheck.setVisibility(View.VISIBLE);
				if(mOnCheckCallBack != null){
					viewHolder.mImageViewCheck.setSelected(mOnCheckCallBack.isCheck(item));
				}
			}else{
				viewHolder.mImageViewCheck.setVisibility(View.GONE);
			}
		}
	}

	protected static class AdbViewHolder extends AbsBaseAdapter.ViewHolder{
		public View			mViewBackground;
		public ImageView	mImageViewCheck;
		public ImageView 	mImageViewAvatar;
		public TextView		mTextViewName;
		public TextView		mTextViewNumber;
		public TextView		mTextViewDetail;
	}
}
