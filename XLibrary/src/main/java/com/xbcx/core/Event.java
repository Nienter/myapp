package com.xbcx.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.text.TextUtils;

import com.xbcx.core.EventManager.OnEventListener;
import com.xbcx.utils.SystemUtils;

public class Event {
	
	protected final String 		mEventCode;
	
	/**
	 * 兼任老代码
	 */
	private	  int				mIntCode;
	
	protected boolean			mIsSuccess = false;
	
	protected Exception			mFailException;
	
	protected Object 			mParams[];
	
	protected int				mHashCode;
	
	protected List<Object>		mReturnParams;
	
	protected boolean			mIsCancel;
	
	protected List<OnEventListener>			mEventListeners;
	protected EventCanceller				mCanceller;
	protected List<OnEventProgressListener>	mProgressListeners;
	protected int							mProgress;
	
	public Event(int eventCode,Object params[]){
		this(String.valueOf(eventCode),params);
		mIntCode = eventCode;
	}
	
	public Event(String eventCode,Object params[]){
		mEventCode = eventCode;
		mParams = params;
		
		mHashCode = getStringCode().hashCode();
		if(mParams != null){
			for(Object obj : mParams){
				if(obj != null){
					mHashCode = mHashCode * 29 + obj.hashCode();
				}
			}
		}
	}
	
	public boolean 	isEventCode(String code){
		return TextUtils.equals(code, getStringCode());
	}

	public int 		getEventCode(){
		if(mIntCode > 0){
			return mIntCode;
		}
		return mIntCode = SystemUtils.safeParseInt(mEventCode);
	}
	
	public String	getStringCode(){
		return mEventCode;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this){
			return true;
		}
		if(o != null && o instanceof Event){
			final Event other = (Event)o;
			return mHashCode == other.mHashCode;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return mHashCode;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("code=");
		sb.append(mEventCode);
		sb.append("{");
		for(Object obj : mParams){
			if(obj != null){
				sb.append(obj.toString()).append(",");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	public void			setSuccess(boolean bSuccess){
		mIsSuccess = bSuccess;
	}
	
	public boolean		isSuccess(){
		return mIsSuccess;
	}
	
	public void			setCanceller(EventCanceller canceller){
		mCanceller = canceller;
	}
	
	void				cancel(){
		mIsCancel = true;
		mIsSuccess = false;
		if(mCanceller != null){
			mCanceller.cancelEvent(this);
		}
	}
	
	void				setResult(Event other){
		mReturnParams = other.mReturnParams;
		mFailException = other.mFailException;
		mIsSuccess = other.mIsSuccess;
		mIsCancel = other.mIsCancel;
	}
	
	public void			addEventListener(OnEventListener listener){
		if(mEventListeners == null){
			mEventListeners = new ArrayList<EventManager.OnEventListener>();
		}
		mEventListeners.add(listener);
	}
	
	public void			addEventListener(int pos,OnEventListener listener){
		if(mEventListeners == null){
			mEventListeners = new ArrayList<EventManager.OnEventListener>();
		}
		mEventListeners.add(pos,listener);
	}
	
	public void			addAllEventListener(Collection<OnEventListener> listeners){
		if(listeners == null){
			return;
		}
		if(mEventListeners == null){
			mEventListeners = new ArrayList<EventManager.OnEventListener>();
		}
		mEventListeners.addAll(listeners);
	}
	
	public void			removeEventListener(OnEventListener listener){
		if(mEventListeners != null){
			mEventListeners.remove(listener);
		}
	}
	
	void				clearEventListener(){
		mEventListeners = null;
	}
	
	List<OnEventListener>	getEventListeners(){
		return mEventListeners;
	}
	
	void				addProgressListener(OnEventProgressListener listener){
		if(mProgressListeners == null){
			mProgressListeners = new ArrayList<OnEventProgressListener>();
		}
		mProgressListeners.add(listener);
	}
	
	void				addAllProgressListener(Collection<OnEventProgressListener> listeners){
		if(listeners == null){
			return;
		}
		if(mProgressListeners == null){
			mProgressListeners = new ArrayList<OnEventProgressListener>();
		}
		mProgressListeners.addAll(listeners);
	}
	
	void				removeProgressListener(OnEventProgressListener listener){
		if(mProgressListeners != null){
			mProgressListeners.remove(listener);
		}
	}
	
	public List<OnEventProgressListener> getProgressListeners(){
		return mProgressListeners;
	}
	
	public void			setProgress(int progress){
		if(mProgress != progress){
			mProgress = progress;
			if(mProgressListeners != null){
				AndroidEventManager.getInstance().notifyEventProgress(this);
			}
		}
	}
	
	public int			getProgress(){
		return mProgress;
	}
	
	public boolean		isCancel(){
		return mIsCancel;
	}
	
	public Object[]		getParams(){
		return mParams;
	}
	
	public Object		getParamAtIndex(int index){
		if(mParams != null && mParams.length > index){
			return mParams[index];
		}
		return null;
	}
	
	public void			setFailException(Exception e){
		mFailException = e;
	}
	
	public String		getFailMessage(){
		return mFailException == null ? null : mFailException.getMessage();
	}
	
	public Exception	getFailException(){
		return mFailException;
	}
	
	public void			addReturnParam(Object obj){
		if(mReturnParams == null){
			mReturnParams = new ArrayList<Object>();
		}
		mReturnParams.add(obj);
	}
	
	public Object		getReturnParamAtIndex(int index){
		if(mReturnParams == null || index >= mReturnParams.size()){
			return null;
		}
		return mReturnParams.get(index);
	}
	
	@SuppressWarnings("unchecked")
	public <T>T			findParam(Class<T> c){
		if(mParams != null){
			for(Object obj : mParams){
				if(c.isInstance(obj)){
					return (T)obj;
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T		findReturnParam(Class<T> c){
		if(mReturnParams != null){
			for(Object obj : mReturnParams){
				if(c.isInstance(obj)){
					return (T)obj;
				}
			}
		}
		return null;
	}
}
