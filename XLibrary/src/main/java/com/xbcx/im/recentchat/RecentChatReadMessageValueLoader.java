package com.xbcx.im.recentchat;

import android.text.TextUtils;

import com.xbcx.core.db.XDB;
import com.xbcx.im.XMessage;
import com.xbcx.im.db.DBColumns;
import com.xbcx.im.db.MessageBaseRunner;
import com.xbcx.im.db.MessageCreator;
import com.xbcx.im.recentchat.RecentChatAdapter.ActivityTypeAdapterViewValueLoader;
import com.xbcx.im.recentchat.RecentChatAdapter.RcViewHolder;

public class RecentChatReadMessageValueLoader extends ActivityTypeAdapterViewValueLoader<String> {

	private int	mFromType;
	
	public RecentChatReadMessageValueLoader(int fromType){
		mFromType = fromType;
	}
	
	@Override
	protected String doInBackground(RecentChat item) {
		XMessage xm = XDB.getInstance().readLast(MessageBaseRunner.getTableName(item.getId()),
					DBColumns.Message.COLUMN_AUTOID, 
					new MessageCreator(item.getId(), mFromType));
		String old = item.getContent();
		if(xm != null){
			RecentChatManager.getInstance().getRecentChatProvider(XMessage.class)
				.handleRecentChat(item, xm);
		}
		final String newContent = item.getContent();
		if(!TextUtils.equals(old, newContent)){
			RecentChatManager.getInstance().editRecentChat(item.getId(), 
					new RecentChatEditCallback() {
						@Override
						public boolean onEditRecentChat(RecentChat rc) {
							return true;
						}
					});
		}
		return newContent;
	}

	@Override
	public void onUpdateView(RcViewHolder holder, RecentChat item, String result) {
	}

}
