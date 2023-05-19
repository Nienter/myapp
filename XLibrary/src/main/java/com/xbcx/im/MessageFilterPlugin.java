package com.xbcx.im;

import com.xbcx.core.module.AppBaseListener;

public interface MessageFilterPlugin extends AppBaseListener{

	public boolean isFilterMessage(XMessage xm);
}
