package com.xbcx.common.menu;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

public class MenuFactory {

	public static MenuFactory getInstance(){
		return instance;
	}
	
	static{
		instance = new MenuFactory();
	}
	
	private static MenuFactory instance;
	
	private MenuDialogCreator	mMenuDialogCreator;
	
	private MenuFactory(){
	}
	
	public MenuDialogCreator getMenuDialogCreator(){
		return mMenuDialogCreator;
	}
	
	public MenuFactory setMenuDialogCreator(MenuDialogCreator creator){
		mMenuDialogCreator = creator;
		return this;
	}
	
	public void showMenu(Context context,List<Menu> menus,DialogInterface.OnClickListener listener){
		if(mMenuDialogCreator == null){
			mMenuDialogCreator = new SimpleMenuDialogCreator(null);
		}
		showMenu(context, menus, listener, mMenuDialogCreator);
	}
	
	public void showMenu(Context context,List<Menu> menus,DialogInterface.OnClickListener listener,
			MenuDialogCreator creator){
		creator.onCreateMenuDialog(context, menus, 
				new DialogClickMenuClickListenerWrapper(menus, listener)).show();
	}
	
	public void showMenu(Context context,List<Menu> menus,OnMenuClickListener listener){
		if(mMenuDialogCreator == null){
			mMenuDialogCreator = new SimpleMenuDialogCreator(null);
		}
		showMenu(context, menus, listener, mMenuDialogCreator);
	}
	
	public void showMenu(Context context,List<Menu> menus,OnMenuClickListener listener,
			MenuDialogCreator creator){
		creator.onCreateMenuDialog(context, menus, listener).show();
	}
	
	public static interface MenuDialogCreator{
		public Dialog onCreateMenuDialog(Context context,List<Menu> menus,OnMenuClickListener listener);
	}
	
	public static class Builder{
		private Context				mContext;
		private List<Menu> 			mMenus = new ArrayList<Menu>();
		private OnMenuClickListener	mListener;
		
		public Builder(Context context){
			this.mContext = context;
		}
		
		public Builder addMenu(Menu menu){
			mMenus.add(menu);
			return this;
		}
		
		public Builder setOnMenuClickListener(OnMenuClickListener listener){
			this.mListener = listener;
			return this;
		}
		
		public void build(){
			MenuFactory.getInstance().showMenu(mContext, mMenus, mListener);
		}
	}
}
