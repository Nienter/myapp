package com.xbcx.im.vcard;

import android.widget.ImageView;

import com.xbcx.im.vcard.VCardProvider.AvatarLoader;

public abstract class VCardAvatarLoader<Result> extends AvatarLoader<Result>{
	
	protected VCardLoaderHelper<ImageView, Result> mLoaderHelper;
	
	@Override
	public void setActivityType(int activityType) {
		super.setActivityType(activityType);
		mLoaderHelper = new VCardLoaderHelper<ImageView, Result>(this);
	}
	
	@Override
	public boolean isFromVCardChange() {
		return mLoaderHelper.isFromVCardChange();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Result doInBackground(String item) {
		return (Result)VCardProvider.getInstance().loadVCard(item, getActivityType(), false);
	}
}
