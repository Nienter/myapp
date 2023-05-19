package com.xbcx.common.pulltorefresh;

import android.view.View;
import android.widget.AdapterView;

import com.xbcx.core.ActivityBasePlugin;

public interface OnItemClickPlugin extends ActivityBasePlugin{

	public boolean onItemClicked(AdapterView<?> parent, Object item,View view);
}
