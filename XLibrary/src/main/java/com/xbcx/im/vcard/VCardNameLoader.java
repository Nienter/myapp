package com.xbcx.im.vcard;

import android.widget.TextView;

import com.xbcx.im.vcard.VCardProvider.NameLoader;

public abstract class VCardNameLoader<Result> extends NameLoader<Result> {
	
	protected VCardLoaderHelper<TextView, Result> mLoaderHelper;
	
	@Override
	public void setActivityType(int activityType) {
		super.setActivityType(activityType);
		mLoaderHelper = new VCardLoaderHelper<TextView, Result>(this);
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
