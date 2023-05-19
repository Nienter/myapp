package com.xbcx.core.http.impl;

import org.json.JSONObject;

import com.xbcx.core.http.XHttpPagination;
import com.xbcx.utils.JsonParseUtils;

public class HttpPageParam implements XHttpPagination {
	
	private boolean	hasmore;
	private String	offset;
	
	public HttpPageParam(JSONObject jo){
		JsonParseUtils.parse(jo, this);
	}

	@Override
	public boolean hasMore() {
		return hasmore;
	}
	
	@Override
	public String getOffset(){
		return offset;
	}

	public void setHasMore(boolean bHasMore){
		hasmore = bHasMore;
	}
}
