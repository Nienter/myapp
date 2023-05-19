package com.xbcx.im.ui;

import com.xbcx.im.ui.XChatEditView.OnEditListener;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

public abstract class EditViewExpressionProvider {
	
	protected EditText			mEditText;
	protected OnEditListener	mOnEditListener;
	
	public void	onAttachToEditView(XChatEditView ev){
		mEditText = ev.getEditText();
		mOnEditListener = ev.getOnEditListener();
	}
	
	public abstract View		createTabButton(Context context);
	
	public abstract boolean		isTabSeletable();
	
	public abstract View		createTabContent(Context context);
}
