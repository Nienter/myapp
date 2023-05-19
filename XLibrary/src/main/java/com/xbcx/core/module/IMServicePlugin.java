package com.xbcx.core.module;

import com.xbcx.im.IMBasePlugin;

public interface IMServicePlugin extends AppBaseListener{
	public IMBasePlugin createIMPlugin();
}
