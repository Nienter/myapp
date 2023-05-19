package com.xbcx.common.menu;

import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;

public class DialogClickMenuClickListenerWrapper implements OnMenuClickListener{

	private List<Menu> 						mMenus;
	private DialogInterface.OnClickListener	mListener;
	
	public DialogClickMenuClickListenerWrapper(
			List<Menu> menus,
			DialogInterface.OnClickListener listener){
		mMenus = menus;
		mListener = listener;
	}
	
	@Override
	public void onMenuClicked(Dialog dialog, Menu menu) {
		mListener.onClick(dialog, mMenus.indexOf(menu));
	}
 
}
