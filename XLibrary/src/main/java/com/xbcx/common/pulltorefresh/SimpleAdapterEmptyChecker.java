package com.xbcx.common.pulltorefresh;

import java.util.ArrayList;
import java.util.List;

import android.widget.BaseAdapter;

public class SimpleAdapterEmptyChecker implements AdapterEmptyChecker{
	
	private List<BaseAdapter> mCheckAdapters = new ArrayList<BaseAdapter>();
	
	public SimpleAdapterEmptyChecker(){
	}
	
	public SimpleAdapterEmptyChecker(BaseAdapter checkAdapter){
		mCheckAdapters.add(checkAdapter);
	}
	
	public SimpleAdapterEmptyChecker addCheckAdapter(BaseAdapter checkAdapter){
		mCheckAdapters.add(checkAdapter);
		return this;
	}
	
	@Override
	public boolean onCheckAdapterEmpty(PullToRefreshPlugin<?> prp) {
		for(BaseAdapter ba : mCheckAdapters){
			if(ba.getCount() > 0){
				return false;
			}
		}
		return true;
	}
}
