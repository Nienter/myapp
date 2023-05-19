package com.xbcx.core.module;

import org.json.JSONObject;

import com.xbcx.core.Event;

public interface HttpLoginListener extends AppBaseListener{
	public void onHttpLogined(Event event,JSONObject joRet);
}
