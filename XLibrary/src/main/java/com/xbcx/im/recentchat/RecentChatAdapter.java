package com.xbcx.im.recentchat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.xbcx.common.valueloader.AdapterViewValueLoader;
import com.xbcx.core.PicUrlObject;
import com.xbcx.core.XApplication;
import com.xbcx.im.ExpressionCoding;
import com.xbcx.im.ui.ActivityType;
import com.xbcx.im.ui.simpleimpl.AbsBaseAdapter;
import com.xbcx.im.vcard.SimpleVCardNameLoader;
import com.xbcx.im.vcard.VCardProvider;
import com.xbcx.im.vcard.VCardProvider.AvatarNameManager;
import com.xbcx.library.R;
import com.xbcx.utils.DateUtils;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class RecentChatAdapter extends AbsBaseAdapter<RecentChat,RecentChatAdapter.RcViewHolder> {
	
	private static final SimpleDateFormat DATEFORMAT_HM 	= new SimpleDateFormat("H:mm",Locale.getDefault());
	private static SimpleDateFormat DATEFORMAT_MD;
	private static SimpleDateFormat DATEFORMAT_YMD;
	
	protected AvatarNameManager	mAvatarManager = VCardProvider.getInstance().createAvatarManager();
	
	protected SparseArray<ActivityTypeAdapterViewValueLoader<?>> mMapActivityTypeToValueLoader;
	
	public RecentChatAdapter(Context context){
		super(context);
		mAvatarManager.addNameLoader(ActivityType.SingleChat, 
				new SimpleVCardNameLoader(){
			@Override
			public void onUpdateView(TextView holder, String item,final PicUrlObject result) {
				super.onUpdateView(holder, item, result);
				RecentChatManager.getInstance().checkAndModifyName(item, result.getName());
			}
		});
	}
	
	public void registerAdapterViewValueLoader(int activityType,ActivityTypeAdapterViewValueLoader<?> loader){
		if(loader != null){
			if(mMapActivityTypeToValueLoader == null){
				mMapActivityTypeToValueLoader = new SparseArray<RecentChatAdapter.ActivityTypeAdapterViewValueLoader<?>>();
			}
			mMapActivityTypeToValueLoader.put(activityType, loader);
		}
	}
	
	public ActivityTypeAdapterViewValueLoader<?> getAdapterViewValueLoader(int activityType){
		if(mMapActivityTypeToValueLoader == null){
			return null;
		}
		return mMapActivityTypeToValueLoader.get(activityType);
	}
	
	@Override
	protected View onCreateConvertView(){
		return LayoutInflater.from(mContext).inflate(R.layout.xlibrary_adapter_recentchat, null);
	}
	
	@Override
	protected RcViewHolder onCreateViewHolder() {
		return new RcViewHolder();
	}

	@Override
	protected void onSetViewHolder(RcViewHolder viewHolder,View convertView) {
		viewHolder.mImageViewAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
		viewHolder.mTextViewName = (TextView)convertView.findViewById(R.id.tvName);
		viewHolder.mTextViewTime = (TextView)convertView.findViewById(R.id.tvTime);
		viewHolder.mTextViewMessage = (TextView)convertView.findViewById(R.id.tvMessage);
		viewHolder.mTextViewUnreadMessageCount = (TextView)convertView.findViewById(R.id.tvNumber);
		viewHolder.mImageViewAvatar.setOnClickListener(this);
	}

	@Override
	protected void onSetChildViewTag(RcViewHolder viewHolder,Object item) {
		
	}

	@Override
	protected void onUpdateView(RcViewHolder viewHolder,RecentChat item, int position) {
		viewHolder.mImageViewAvatar.setTag(item);
		
		onUpdateRecentChatView(viewHolder, item);
	}
	
	protected void onUpdateRecentChatView(RcViewHolder holder,RecentChat rc){
		final int nUnreadMessageCount = rc.getUnreadMessageCount();
		if(nUnreadMessageCount > 0){
			holder.mTextViewUnreadMessageCount.setVisibility(View.VISIBLE);
			holder.mTextViewUnreadMessageCount.setText(String.valueOf(nUnreadMessageCount));
		}else{
			holder.mTextViewUnreadMessageCount.setVisibility(View.GONE);
		}
		
		onSetAvatar(holder.mImageViewAvatar, rc);
		
		onSetName(holder.mTextViewName, rc);
		
		holder.mTextViewTime.setText(getSendTimeShow(rc.getTime()));
		holder.mTextViewMessage.setText(ExpressionCoding.spanAllExpression(rc.getContent()));
		
		if(mMapActivityTypeToValueLoader != null){
			int size = mMapActivityTypeToValueLoader.size();
			final int activityType = rc.getActivityType();
			ActivityTypeAdapterViewValueLoader<?> loader = null;
			for(int index = 0;index < size;++index){
				loader = mMapActivityTypeToValueLoader.valueAt(index);
				if(activityType == mMapActivityTypeToValueLoader.keyAt(index)){
					loader.bindView(holder, rc);
				}else{
					loader.removeBindView(holder);
				}
			}
		}
	}
	
	protected void onSetName(TextView tv,RecentChat rc){
		mAvatarManager.setName(tv, rc.getId(), rc.getName(), rc.getActivityType());
	}
	
	protected void onSetAvatar(ImageView iv,RecentChat rc){
		mAvatarManager.setAvatar(iv, rc.getId(), rc.getActivityType());
	}
	
	public static String getSendTimeShow(long lSendTime){
		if(lSendTime == 0){
			return "";
		}
		String strRet = null;
		try {
			Date date = new Date(lSendTime);
			if(DateUtils.isToday(lSendTime)){
				strRet = DATEFORMAT_HM.format(date);
			}else if(DateUtils.isInCurrentYear(lSendTime)){
				if(DATEFORMAT_MD == null){
					DATEFORMAT_MD = new SimpleDateFormat(
							XApplication.getApplication().getString(R.string.dateformat_md),Locale.getDefault());
				}
				strRet = DATEFORMAT_MD.format(date);
			}else{
				if(DATEFORMAT_YMD == null){
					DATEFORMAT_YMD = new SimpleDateFormat(
							XApplication.getApplication().getString(R.string.dateformat_ymd),Locale.getDefault());
				}
				strRet = DATEFORMAT_YMD.format(date);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return strRet;
	}
	
	public static abstract class ActivityTypeAdapterViewValueLoader<Result> extends AdapterViewValueLoader<RcViewHolder, RecentChat, Result>{
		
	}

	public static class RcViewHolder extends AbsBaseAdapter.ViewHolder{
		public ImageView 	mImageViewAvatar;
		
		public TextView		mTextViewName;
		
		public TextView		mTextViewTime;
		
		public TextView		mTextViewMessage;
		
		public TextView		mTextViewUnreadMessageCount;
	}
}
