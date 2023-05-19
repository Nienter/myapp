package com.xbcx.im.ui.friend;

import android.content.Context;

import com.xbcx.adapter.LetterSortAdapterWrapper.LetterSortInterface;
import com.xbcx.im.extention.roster.IMGroup;
import com.xbcx.im.vcard.VCardProvider;
import com.xbcx.library.R;

public class IMGroupAdapter extends IMAdbAdapter<IMGroup> implements LetterSortInterface{
	
	public IMGroupAdapter(Context context){
		super(context);
	}

	@Override
	protected void onUpdateView(AdbViewHolder viewHolder,IMGroup item, int position) {
		super.onUpdateView(viewHolder, item, position);
		VCardProvider.getInstance().setGroupAvatar(viewHolder.mImageViewAvatar, item.getId());
		viewHolder.mTextViewName.setText(item.getName());
		viewHolder.mTextViewNumber.setText(
				" (" + item.getMemberCount() + mContext.getString(R.string.people) + ")");
	}

	@Override
	public String getItemName(int position) {
		IMGroup g = (IMGroup)getItem(position);
		return g.getName();
	}

	@Override
	public boolean isTop(int position) {
		return true;
	}
}
