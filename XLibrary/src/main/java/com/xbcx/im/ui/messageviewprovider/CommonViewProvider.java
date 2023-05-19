package com.xbcx.im.ui.messageviewprovider;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.xbcx.im.IMKernel;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.im.ui.IMGlobalSetting;
import com.xbcx.im.ui.IMMessageViewProvider;
import com.xbcx.library.R;

public abstract class CommonViewProvider<T extends CommonViewProvider.CommonViewHolder> extends IMMessageViewProvider implements
													View.OnClickListener,
													View.OnLongClickListener{
	
	public static final int ViewInfo_DEFAULT 	= 0;
	public static final int ViewInfo_SHOW		= 1;
	public static final int ViewInfo_HIDE		= 2;
	
	protected int	mViewInfoShowType = ViewInfo_DEFAULT;
	
	public CommonViewProvider(){
		mViewInfoShowType = IMGlobalSetting.msgViewInfoShowType;
	}
	
	public void setViewInfoShowType(int type){
		mViewInfoShowType = type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(XMessage message, View convertView, ViewGroup parent) {
		T viewHolder = null;
		XMessage m = (XMessage)message;
		
		if(convertView == null){
			viewHolder = onCreateViewHolder();
			convertView = onCreateView(m,parent.getContext());
			onSetViewHolder(convertView, viewHolder,m);
			
			if(viewHolder.mViewInfo != null){
				if(mViewInfoShowType == ViewInfo_SHOW){
					viewHolder.mViewInfo.setVisibility(View.VISIBLE);
				}else if(mViewInfoShowType == ViewInfo_HIDE){
					viewHolder.mViewInfo.setVisibility(View.GONE);
				}else{
					if(m.getFromType() == XMessage.FROMTYPE_SINGLE){
						viewHolder.mViewInfo.setVisibility(View.GONE);
					}else{
						viewHolder.mViewInfo.setVisibility(View.VISIBLE);
					}
				}
			}

			convertView.setTag(viewHolder);
		}else{
			viewHolder = (T)convertView.getTag();
		}
		
		onSetViewTag(viewHolder, m);
		
		if(mAdapter.isCheck()){
			viewHolder.mCheckBox.setVisibility(View.VISIBLE);
			viewHolder.mCheckBox.setChecked(mAdapter.isCheckedItem(m));
		}else{
			viewHolder.mCheckBox.setVisibility(View.GONE);
		}
		
		if(m.isFromSelf()){
			mAdapter.getAvatarNameManager().setAvatar(
					viewHolder.mImageViewAvatar, IMKernel.getLocalUser(), 
					ActivityType.SingleChat);
		}else{
			mAdapter.getAvatarNameManager().setAvatar(
					viewHolder.mImageViewAvatar, m.getUserId(), 
					ActivityType.SingleChat);
		}
		
		if(m.isFromSelf()){
			mAdapter.getAvatarNameManager().setName(
					viewHolder.mTextViewNickname, 
					IMKernel.getLocalUser(), null, 
					ActivityType.SingleChat);
		}else{
			mAdapter.getAvatarNameManager().setName(
					viewHolder.mTextViewNickname, 
					m.getUserId(), m.getUserName(), 
					ActivityType.SingleChat);
		}
		
		onSetContentViewBackground(viewHolder, m);
		
		onUpdateSendStatus(viewHolder, m);
		
		onUpdateView(viewHolder, m);
		
		return convertView;
	}

	protected View onCreateView(XMessage m,Context context){
		if(m.isFromSelf()){
			return LayoutInflater.from(context).inflate(R.layout.message_common_right, null);
		}else{
			return LayoutInflater.from(context).inflate(R.layout.message_common_left, null);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected T onCreateViewHolder(){
		return (T)new CommonViewHolder();
	}
	
	protected void onSetViewHolder(View convertView,T viewHolder,XMessage xm){
		viewHolder.mCheckBox = (CheckBox)convertView.findViewById(R.id.cb);
		viewHolder.mImageViewAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
		viewHolder.mViewInfo = convertView.findViewById(R.id.viewInfo);
		viewHolder.mTextViewNickname = (TextView)convertView.findViewById(R.id.tvNickname);
		viewHolder.mContentView = (FrameLayout)convertView.findViewById(R.id.viewContent);
		viewHolder.mViewWarning = (ImageView)convertView.findViewById(R.id.ivWarning);
		viewHolder.mViewSending = convertView.findViewById(R.id.pbSending);
		viewHolder.mButton = (TextView)convertView.findViewById(R.id.btn);
		viewHolder.mButton.setVisibility(View.GONE);
		
		viewHolder.mCheckBox.setOnClickListener(this);
		
		viewHolder.mImageViewAvatar.setOnClickListener(this);
		viewHolder.mContentView.setOnClickListener(this);
		viewHolder.mContentView.setOnLongClickListener(this);
		viewHolder.mButton.setOnClickListener(this);
		viewHolder.mViewWarning.setOnClickListener(this);
	}
	
	protected void onSetViewTag(T viewHolder,XMessage m){
		viewHolder.mImageViewAvatar.setTag(m);
		viewHolder.mContentView.setTag(m);
		viewHolder.mButton.setTag(m);
		viewHolder.mViewWarning.setTag(m);
		viewHolder.mCheckBox.setTag(m);
	}
	
	protected void onSetContentViewBackground(T viewHolder,XMessage m){
	}
	
	protected void onUpdateSendStatus(T viewHolder,XMessage m){
		if(IMKernel.isSendingMessage(m.getId())){
			viewHolder.mViewSending.setVisibility(View.VISIBLE);
			viewHolder.mViewWarning.setVisibility(View.GONE);
		}else{
			viewHolder.mViewSending.setVisibility(View.GONE);
			final View viewWarning = viewHolder.mViewWarning;
			if(m.isFromSelf()){
				if(m.isSended()){
					if(m.isSendSuccess()){
						viewWarning.setVisibility(View.GONE);
					}else{
						viewWarning.setVisibility(View.VISIBLE);
					}
				}else{
					viewWarning.setVisibility(View.GONE);
				}
			}else{
				viewWarning.setVisibility(View.GONE);
			}
		}
	}
	
	protected void setShowWarningView(ImageView viewWarning,boolean bShow){
		if(bShow){
			viewWarning.setVisibility(View.VISIBLE);
		}else{
			viewWarning.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onClick(View v) {
		if(getOnViewClickListener() != null){
			getOnViewClickListener().onViewClicked((XMessage)v.getTag(), v.getId());
		}
		if(v.getId() == R.id.cb){
			final CheckBox cb = (CheckBox)v;
			XMessage xm = (XMessage)cb.getTag();
			if(cb.isChecked()){
				mAdapter.setCheckItem(xm, true);
			}else{
				mAdapter.setCheckItem(xm, false);
			}
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if(getOnViewClickListener() != null){
			return getOnViewClickListener().onViewLongClicked((XMessage)v.getTag(), v.getId());
		}
		return false;
	}

	@Override
	public boolean acceptHandle(XMessage message) {
		return false;
	}

	protected abstract void onUpdateView(T viewHolder,XMessage m);
	
	protected void	setContentViewMatchParent(CommonViewHolder viewHolder){
		ViewGroup.LayoutParams lpContent = viewHolder.mContentView.getLayoutParams();
		lpContent.width = ViewGroup.LayoutParams.MATCH_PARENT;
		viewHolder.mContentView.setLayoutParams(lpContent);
		ViewGroup parent = (ViewGroup)viewHolder.mContentView.getParent();
		lpContent = parent.getLayoutParams();
		lpContent.width = ViewGroup.LayoutParams.MATCH_PARENT;
		parent.setLayoutParams(lpContent);
	}
	
	public static class CommonViewHolder{
		
		public CheckBox				mCheckBox;
		
		public ImageView 			mImageViewAvatar;
		
		public View					mViewInfo;
		
		public TextView				mTextViewNickname;
		
		public FrameLayout			mContentView;
		
		public ImageView			mViewWarning;
		
		public TextView				mButton;
		
		public View					mViewSending;
		
		public SparseArray<View> 	mMapIdToView;
	}
}
