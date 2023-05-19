package com.xbcx.im.ui.friend;

import android.content.Context;
import android.text.TextUtils;

import com.xbcx.adapter.LetterSortAdapterWrapper.LetterSortInterface;
import com.xbcx.core.PicUrlObject;
import com.xbcx.im.extention.roster.IMContact;
import com.xbcx.im.vcard.VCardProvider;

public class IMContactAdapter extends IMAdbAdapter<IMContact> implements LetterSortInterface{
	
	public IMContactAdapter(Context context){
		super(context);
	}

	@Override
	protected void onUpdateView(AdbViewHolder viewHolder,IMContact item, int position) {
		super.onUpdateView(viewHolder, item, position);
		VCardProvider.getInstance().setAvatar(viewHolder.mImageViewAvatar, item.getId());
		VCardProvider.getInstance().setName(viewHolder.mTextViewName, item.getId(),item.getName());
	}
	
	@Override
	public String getItemName(int position) {
		IMContact c = (IMContact)getItem(position);
		PicUrlObject vcard = VCardProvider.getInstance().getCache(c.getId());
		String name = vcard == null ? null : vcard.getName();
		if(TextUtils.isEmpty(name)){
			name = c.getName();
		}
		return name;
	}
	
	@Override
	public boolean isTop(int position) {
		return false;
	}
}
