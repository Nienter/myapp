package com.xbcx.common.pulltorefresh;

import com.xbcx.core.BaseActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class OnItemClickPluginListener implements OnItemClickListener{

	private BaseActivity	mActivity;
	
	public OnItemClickPluginListener(BaseActivity ba){
		mActivity = ba;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Object item = parent.getItemAtPosition(position);
		if(item != null){
			for(OnItemClickPlugin p : mActivity.getPlugins(OnItemClickPlugin.class)){
				if(p.onItemClicked(parent, item, view)){
					return;
				}
			}
		}
	}

}
