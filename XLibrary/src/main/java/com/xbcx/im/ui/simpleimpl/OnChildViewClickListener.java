package com.xbcx.im.ui.simpleimpl;

import android.view.View;
import android.widget.BaseAdapter;

public interface OnChildViewClickListener {
	
	public void onChildViewClicked(BaseAdapter adapter,Object item,int viewId,View v);
}
