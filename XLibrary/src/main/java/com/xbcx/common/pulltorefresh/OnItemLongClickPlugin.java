package com.xbcx.common.pulltorefresh;

import android.view.View;
import android.widget.AdapterView;

import com.xbcx.core.ActivityBasePlugin;

public interface OnItemLongClickPlugin extends ActivityBasePlugin{

	public boolean onItemLongClicked(AdapterView<?> parent, Object item,View view);
}
