package com.xbcx.im;

import java.util.List;

import android.text.TextUtils;

import com.xbcx.core.AndroidEventManager;
import com.xbcx.core.Event;
import com.xbcx.core.IDObject;
import com.xbcx.core.IDProtocol;
import com.xbcx.core.XApplication;
import com.xbcx.core.db.XDB;
import com.xbcx.core.module.HttpLoginGetPlugin;

public class MessageNotifyManager implements HttpLoginGetPlugin{
	
	public static MessageNotifyManager getInstance(){
		return sInstance;
	}
	
	static{
		sInstance = new MessageNotifyManager();
	}
	
	private static MessageNotifyManager sInstance;
	
	private String	mGetAvoidDisturbEventCode;
	
	private MessageNotifyManager(){
	}
	
	public MessageNotifyManager setGetAvoidDisturbEventCode(String code){
		mGetAvoidDisturbEventCode = code;
		if(TextUtils.isEmpty(code)){
			XApplication.removeManager(this);
		}else{
			XApplication.addManager(this);
		}
		return this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onHttpLoginGet(boolean bAuto) throws Exception{
		if(!bAuto){
			if(!TextUtils.isEmpty(mGetAvoidDisturbEventCode)){
				Event event = AndroidEventManager.getInstance().runEvent(mGetAvoidDisturbEventCode);
				if(event.isSuccess()){
					XDB.getInstance().deleteAll(NotNotify.class, true);
					List<IDProtocol> idps = event.findReturnParam(List.class);
					for(IDProtocol idp : idps){
						setNotify(idp.getId(), false);
					}
				}
			}
		}
	}
	
	public boolean	isNotify(String id){
		if(TextUtils.isEmpty(id)){
			return true;
		}
		NotNotify nn = XDB.getInstance().readById(id, NotNotify.class, true);
		return nn == null;
	}
	
	public void 	setNotify(String id,boolean bNotify){
		if(!TextUtils.isEmpty(id)){
			if(bNotify){
				XDB.getInstance().delete(id, NotNotify.class, true);
			}else{
				XDB.getInstance().updateOrInsert(new NotNotify(id), true);
			}
		}
	}
	
	private static class NotNotify extends IDObject{
		private static final long serialVersionUID = 1L;

		public NotNotify(String id) {
			super(id);
		}
	}
}
