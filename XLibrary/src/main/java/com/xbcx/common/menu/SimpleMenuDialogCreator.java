package com.xbcx.common.menu;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.xbcx.common.menu.MenuFactory.MenuDialogCreator;

public class SimpleMenuDialogCreator implements MenuDialogCreator{

	private CharSequence	mTitle;
	
	public SimpleMenuDialogCreator(CharSequence title){
		mTitle = title;
	}
	
	@Override
	public Dialog onCreateMenuDialog(Context context,final List<Menu> menus,final OnMenuClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		MenuItemAdapter adapter = new MenuItemAdapter(context);
		adapter.addAll(menus);
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(listener != null){
					listener.onMenuClicked((Dialog)dialog, menus.get(which));
				}
			}
		});
		if(mTitle != null){
			builder.setTitle(mTitle);
		}
		return builder.create();
	}

}
