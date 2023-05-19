package com.xbcx.im.recentchat;

import com.xbcx.core.BaseActivity;
import com.xbcx.core.module.AppBaseListener;

public interface RecentChatLaunchPlugin extends AppBaseListener{
	public boolean onLaunchRecentChat(BaseActivity activity,RecentChat rc);
}
