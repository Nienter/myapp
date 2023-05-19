package com.xbcx.core.http;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpRequest;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;
import com.xbcx.core.Event;
import com.xbcx.core.EventCanceller;
import com.xbcx.core.http.XHttpRunner.ResponceHandlerWare;

public class XSyncHttpClient extends AsyncHttpClient {
	
	private boolean	mUseOnce;
	
	/**
     * Creates a new SyncHttpClient with default constructor arguments values
     */
    public XSyncHttpClient() {
        super(false, 80, 443);
    }

    /**
     * Creates a new SyncHttpClient.
     *
     * @param httpPort non-standard HTTP-only port
     */
    public XSyncHttpClient(int httpPort) {
        super(false, httpPort, 443);
    }

    /**
     * Creates a new SyncHttpClient.
     *
     * @param httpPort  non-standard HTTP-only port
     * @param httpsPort non-standard HTTPS-only port
     */
    public XSyncHttpClient(int httpPort, int httpsPort) {
        super(false, httpPort, httpsPort);
    }

    /**
     * Creates new SyncHttpClient using given params
     *
     * @param fixNoHttpResponseException Whether to fix or not issue, by ommiting SSL verification
     * @param httpPort                   HTTP port to be used, must be greater than 0
     * @param httpsPort                  HTTPS port to be used, must be greater than 0
     */
    public XSyncHttpClient(boolean fixNoHttpResponseException, int httpPort, int httpsPort) {
        super(fixNoHttpResponseException, httpPort, httpsPort);
    }

    /**
     * Creates a new SyncHttpClient.
     *
     * @param schemeRegistry SchemeRegistry to be used
     */
    public XSyncHttpClient(SchemeRegistry schemeRegistry) {
        super(schemeRegistry);
    }
    
    public XSyncHttpClient setUseOnce(boolean b){
    	mUseOnce = b;
    	return this;
    }
    
    @Override
    protected ExecutorService getDefaultThreadPool() {
    	return null;
    }
    
    @Override
    protected DefaultHttpClient onCreateHttpClient(HttpParams params, SchemeRegistry registry) {
    	if(mUseOnce){
    		return new DefaultHttpClient(params);
    	}else{
    		return super.onCreateHttpClient(params, registry);
    	}
    }

    @Override
    protected RequestHandle sendRequest(DefaultHttpClient client,
                                        HttpContext httpContext, HttpUriRequest uriRequest,
                                        String contentType, ResponseHandlerInterface responseHandler,
                                        Context context) {
        if (contentType != null) {
            uriRequest.addHeader("Content-Type", contentType);
        }

        responseHandler.setUseSynchronousMode(true);

		/*
         * will execute the request directly
		*/
        
        httpContext = new BasicHttpContext();
        ResponceHandlerWare ware = (ResponceHandlerWare)responseHandler;
        AsyncHttpRequest request = new AsyncHttpRequest(client, httpContext, uriRequest, responseHandler);
        ware.mEvent.setCanceller(new HttpCanceller(request));
        
        try{
        	request.run();
        }finally{
        	if(mUseOnce){
        		getHttpClient().getConnectionManager().shutdown();
        	}
        }
        
        // Return a Request Handle that cannot be used to cancel the request
        // because it is already complete by the time this returns
        return new RequestHandle(null);
    }
    
    private static class HttpCanceller implements EventCanceller{
    	
    	private final WeakReference<AsyncHttpRequest> mRequest;
    	
    	public HttpCanceller(AsyncHttpRequest request){
    		mRequest = new WeakReference<AsyncHttpRequest>(request);
    	}
    	
		@Override
		public void cancelEvent(Event e) {
			final AsyncHttpRequest request = mRequest.get();
			if(request != null){
				request.cancel(true);
			}
		}
    }
}
