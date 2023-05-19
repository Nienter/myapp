package com.xbcx.common.pulltorefresh;

import com.xbcx.core.BaseActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

public class OnItemLongClickPluginListener implements OnItemLongClickListener{

	private BaseActivity	mActivity;
	
	public OnItemLongClickPluginListener(BaseActivity ba){
		mActivity = ba;
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Object item = parent.getItemAtPosition(position);
		if(item != null){
			for(OnItemLongClickPlugin p : mActivity.getPlugins(OnItemLongClickPlugin.class)){
				if(p.onItemLongClicked(parent, item, view)){
					return true;
				}
			}
		}
		return false;
	}

}
