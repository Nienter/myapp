package com.xbcx.im.extention.roster;

import com.xbcx.core.module.IMServicePlugin;
import com.xbcx.im.IMBasePlugin;

public class RosterAppPlugin implements IMServicePlugin{

	@Override
	public IMBasePlugin createIMPlugin() {
		return new RosterServicePlugin();
	}

}
