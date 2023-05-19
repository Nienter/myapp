package com.xbcx.im;

import android.app.Service;

public interface ServicePlugin<T extends Service> extends IMBasePlugin{

	public void onAttachService(T service);
	
	public void onServiceDestory();
}
