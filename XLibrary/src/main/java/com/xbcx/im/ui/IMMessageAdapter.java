package com.xbcx.im.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.xbcx.im.XMessage;
import com.xbcx.im.ui.IMMessageViewProvider.OnViewClickListener;
import com.xbcx.im.vcard.VCardProvider;
import com.xbcx.im.vcard.VCardProvider.AvatarNameManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class IMMessageAdapter extends BaseAdapter {

	protected Context mContext;
	
	protected List<XMessage> 				mListMessage 	= new ArrayList<XMessage>();
	protected HashMap<String, XMessage> 	mMapIdToMessage = new HashMap<String, XMessage>();
	
	protected boolean mCanAddProvider;
	protected List<IMMessageViewProvider> 	mListViewProvider = new ArrayList<IMMessageViewProvider>();
	protected IMMessageViewProvider			mDefaultProvider;
	
	protected HashMap<View, XMessage> 		mMapConvertViewToMessage = new HashMap<View, XMessage>();
	
	protected boolean						mIsCheck;
	protected HashMap<String, XMessage> 	mMapIdToCheckMessage;
	protected OnCheckListener				mOnCheckListener;
	
	protected OnViewClickListener			mOnViewClickListener;
	
	protected AvatarNameManager				mAvatarNameManager;
	
	protected MessageAnimationAdapter		mAnimationAdapter;
	
	public IMMessageAdapter(Context context){
		mContext = context;
		mCanAddProvider = true;
		mAvatarNameManager = VCardProvider.getInstance().createAvatarManager();
	}
	
	public IMMessageAdapter setOnViewClickListener(OnViewClickListener listener){
		mOnViewClickListener = listener;
		return this;
	}
	
	public OnViewClickListener getOnViewClickListener(){
		return mOnViewClickListener;
	}
	
	public Context getContext(){
		return mContext;
	}
	
	public AvatarNameManager getAvatarNameManager(){
		return mAvatarNameManager;
	}
	
	public void setAnimationAdapter(MessageAnimationAdapter adapter){
		mAnimationAdapter = adapter;
	}
	
	public void setIsCheck(boolean bCheck){
		mIsCheck = bCheck;
		notifyDataSetChanged();
	}
	
	public boolean isCheck(){
		return mIsCheck;
	}
	
	public void setOnCheckListener(OnCheckListener listener){
		mOnCheckListener = listener;
	}
	
	public void setCheckItem(int pos,boolean bCheck){
		final XMessage xm = mListMessage.get(pos);
		setCheckItem(xm, bCheck);
	}
	
	public void setCheckItem(XMessage xm,boolean bCheck){
		if(mMapIdToCheckMessage == null){
			mMapIdToCheckMessage = new HashMap<String, XMessage>();
		}
		if(bCheck){
			mMapIdToCheckMessage.put(xm.getId(), xm);
		}else{
			mMapIdToCheckMessage.remove(xm.getId());
		}
		onCheckChanged(xm);
	}
	
	public boolean isCheckedItem(XMessage xm){
		if(mMapIdToCheckMessage == null){
			return false;
		}
		return mMapIdToCheckMessage.containsKey(xm.getId());
	}
	
	public Collection<XMessage> getCheckedMessage(){
		if(mMapIdToCheckMessage == null){
			return Collections.emptyList();
		}
		return mMapIdToCheckMessage.values();
	}
	
	protected void onCheckChanged(XMessage xm){
		if(mOnCheckListener != null){
			mOnCheckListener.onCheckCountChanged(getCheckedMessage(), xm);
		}
	}
	
	@Override
	public int getCount() {
		return mListMessage.size();
	}
	
	@Override
	public int getItemViewType(int position) {
		XMessage m = mListMessage.get(position);
		int nIndex = 0;
		for(IMMessageViewProvider provider : mListViewProvider){
			if(provider.acceptHandle(m)){
				return nIndex;
			}
			++nIndex;
		}
		if(mDefaultProvider != null){
			return nIndex;
		}
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		int nCount = mListViewProvider.size();
		if(mDefaultProvider != null){
			++nCount;
		}
		return nCount > 0 ? nCount : 1;
	}

	@Override
	public Object getItem(int position) {
		return mListMessage.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		XMessage m = mListMessage.get(position);
		
		boolean bHandled = false;
		for(IMMessageViewProvider provider : mListViewProvider){
			if(provider.acceptHandle(m)){
				convertView =  provider.getView(m, convertView, parent);
				bHandled = true;
				break;
			}
		}
		if(!bHandled && mDefaultProvider != null){
			convertView = mDefaultProvider.getView(m, convertView, parent);
		}
		
		if(convertView != null){
			mMapConvertViewToMessage.put(convertView, m);
		}
		
		return convertView;
	}
	
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		super.registerDataSetObserver(observer);
		mCanAddProvider = false;
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		super.unregisterDataSetObserver(observer);
		mCanAddProvider = true;
	}
	
	public boolean isIMMessageViewVisible(XMessage m){
		if(m == null){
			return true;
		}
		for(XMessage message : mMapConvertViewToMessage.values()){
			if(message.equals(m)){
				return true;
			}
		}
		return false;
	}
	
	public void setDefaultIMMessageViewProvider(IMMessageViewProvider provider){
		mDefaultProvider = provider;
		if(provider != null){
			provider.setIMMessageAdapter(this);
		}
	}

	public void addIMMessageViewProvider(IMMessageViewProvider provider){
		if(checkFactorySetCanChange()){
			provider.setIMMessageAdapter(this);
			mListViewProvider.add(provider);
		}
	}
	
	public void removeIMMessageViewProvider(IMMessageViewProvider provider){
		if(checkFactorySetCanChange()){
			mListViewProvider.remove(provider);
		}
	}
	
	public void clearIMMessageViewProvider(){
		if(checkFactorySetCanChange()){
			mListViewProvider.clear();
		}
	}
	
	protected boolean checkFactorySetCanChange(){
		if(mCanAddProvider){
			return true;
		}else{
//			Assert.assertTrue("addIMMessageViewProvider must be call before registerDataSetObserver", false);
		}
		return false;
	}
	
	public boolean containsMessage(String msgId){
		return mMapIdToMessage.containsKey(msgId);
	}
	
	public List<XMessage> getAllItem(){
		return mListMessage;
	}
	
	public void addItem(XMessage m){
		mListMessage.add(m);
		mMapIdToMessage.put(m.getId(), m);
		if(mAnimationAdapter != null){
			mAnimationAdapter.setAnimate(true);
		}
		notifyDataSetChanged();
	}
	
	public void addItem(int nPos,XMessage m){
		mListMessage.add(nPos, m);
		mMapIdToMessage.put(m.getId(), m);
		notifyDataSetChanged();
	}
	
	public void addAllItem(int nPos,Collection<XMessage> list){
		mListMessage.addAll(nPos, list);
		for(XMessage xm : list){
			mMapIdToMessage.put(xm.getId(), xm);
		}
		notifyDataSetChanged();
	}
	
	public void addAllItem(Collection<XMessage> list){
		mListMessage.addAll(list);
		for(XMessage xm : list){
			mMapIdToMessage.put(xm.getId(), xm);
		}
		notifyDataSetChanged();
	}
	
	public void removeItem(XMessage m){
		mListMessage.remove(m);
		mMapIdToMessage.remove(m.getId());
		notifyDataSetChanged();
	}
	
	public void removeItem(int nIndex){
		XMessage xm = mListMessage.remove(nIndex);
		if(xm != null){
			mMapIdToMessage.remove(xm.getId());
		}
		notifyDataSetChanged();
	}
	
	public int	indexOf(XMessage m){
		return mListMessage.indexOf(m);
	}
	
	public void removeAllItem(List<XMessage> list){
		mListMessage.removeAll(list);
		for(XMessage xm : list){
			mMapIdToMessage.remove(xm.getId());
		}
		notifyDataSetChanged();
	}
	
	public void clear(){
		mListMessage.clear();
		mMapIdToMessage.clear();
		notifyDataSetChanged();
	}
	
	public XMessage findItem(String msgId){
		return mMapIdToMessage.get(msgId);
	}
	
	public static interface OnCheckListener{
		public void onCheckCountChanged(Collection<XMessage> checkedItems,XMessage xm);
	}
}
