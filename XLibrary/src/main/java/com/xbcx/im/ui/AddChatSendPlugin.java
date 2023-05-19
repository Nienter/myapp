package com.xbcx.im.ui;

import com.xbcx.core.module.AppBaseListener;

public interface AddChatSendPlugin extends AppBaseListener{

	public SendPlugin onCreateChatSendPlugin(ChatActivity activity);
}
