package com.xbcx.core.http;

import org.apache.http.client.methods.HttpUriRequest;

import com.xbcx.core.Event;
import com.xbcx.core.EventCanceller;

public class HttpEventCanceller implements EventCanceller {
	
	private HttpUriRequest mRequest;
	
	public HttpEventCanceller(HttpUriRequest request){
		mRequest = request;
	}

	@Override
	public void cancelEvent(Event e) {
		mRequest.abort();
	}

}
