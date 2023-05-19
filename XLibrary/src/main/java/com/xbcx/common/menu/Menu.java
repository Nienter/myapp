package com.xbcx.common.menu;

import com.xbcx.core.XApplication;

public class Menu{
	private int 			mId;
	private CharSequence 	mText;
	private int 			mTextAppearance;
	
	public Menu(int nId,int nStringId){
		this(nId, nStringId,0);
	}
	
	public Menu(int nId,String text){
		this.mId = nId;
		this.mText = text;
	}
	
	public Menu(int nId,int nStringId,int textAppearance){
		mId = nId;
		mText = XApplication.getApplication().getString(nStringId);
		mTextAppearance = textAppearance;
	}
	
	public Menu setTextAppearance(int textAppearance){
		this.mTextAppearance = textAppearance;
		return this;
	}
	
	public Menu setText(CharSequence text){
		mText = text;
		return this;
	}
	
	public int getId(){
		return mId;
	}
	
	public CharSequence getText(){
		return mText;
	}
	
	public int getTextAppearance(){
		return mTextAppearance;
	}
}
