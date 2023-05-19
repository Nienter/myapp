package com.xbcx.adapter;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.tsz.afinal.FinalActivity;

import com.xbcx.core.IDObject;
import com.xbcx.utils.SystemUtils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

public abstract class SetBaseAdapter<E extends Object> extends BaseAdapter implements
															SelectableAdapter<E>,
															AnimatableAdapter{
	
	protected List<E> 					mListObject;
	
	protected AnimatableAdapter 		mAnimatableAdapter;
	
	protected HashMap<E, E> 			mMapSeleteItem = new HashMap<E, E>();
	protected boolean					mSingleMode = true;
	
	protected List<ItemObserver> 		mItemObservers;
	
	private	  int						mOldCount = -1;
	
	public SetBaseAdapter(){
		mListObject = new ArrayList<E>();
	}
	
	public void setMultiSelectMode(){
		mSingleMode = false;
	}
	
	public void setSingleSelectMode(){
		mSingleMode = true;
	}
	
	public E getSingleModeSelectItem(){
		return mMapSeleteItem.size() > 0 ? 
				mMapSeleteItem.values().iterator().next() : null;
	}
	
	public void setSingleModeSelectItem(E item){
		if(mSingleMode){
			addSelectItem(item);
		}
	}
	
	public void setSelectItem(E item){
		setSingleModeSelectItem(item);
	}
	
	public E getSelectItem(){
		return getSingleModeSelectItem();
	}
	
	public void toggleSelectItem(E item){
		if(isSelected(item)){
			removeSelectItem(item);
		}else{
			addSelectItem(item);
		}
	}
	
	@Override
	public void addSelectItem(E item) {
		if(mSingleMode){
			mMapSeleteItem.clear();
		}
		if(item != null){
			mMapSeleteItem.put(item, item);
		}
		notifyDataSetChanged();
	}
	
	public <T extends E> void addAllSelectItem(Collection<T> items){
		if(items == null){
			return;
		}
		if(mSingleMode){
			if(items.size() > 0){
				final E item = items.iterator().next();
				mMapSeleteItem.put(item, item);
			}
		}else{
			for(E item : items){
				mMapSeleteItem.put(item, item);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public void addSelectItem(int pos) {
		final E item = mListObject.get(pos);
		addSelectItem(item);
	}

	@Override
	public void removeSelectItem(E item) {
		mMapSeleteItem.remove(item);
		notifyDataSetChanged();
	}

	@Override
	public void removeSelectItem(int pos) {
		final E item = mListObject.get(pos);
		mMapSeleteItem.remove(item);
		notifyDataSetChanged();
	}

	@Override
	public void clearSelectItem() {
		mMapSeleteItem.clear();
		notifyDataSetChanged();
	}

	@Override
	public boolean isSelected(E item) {
		return mMapSeleteItem.containsKey(item);
	}
	
	@Override
	public boolean containSelect() {
		return mMapSeleteItem.size() > 0;
	}
	
	@Override
	public Collection<E> getAllSelectItem() {
		return mMapSeleteItem.values();
	}
	
	public Collection<E> getFixAllSelectItem(){
		Collection<E> items = new ArrayList<E>();
		for(E e : mMapSeleteItem.keySet()){
			int index = mListObject.indexOf(e);
			if(index >= 0){
				items.add(mListObject.get(index));
			}
		}
		return items;
	}
	
	public void registerItemObserver(ItemObserver observer){
		if(mItemObservers == null){
			mItemObservers = new ArrayList<SetBaseAdapter.ItemObserver>();
		}
		mItemObservers.add(observer);
	}
	
	@Override
	public void setAnimatableAdapter(AnimatableAdapter adapter){
		mAnimatableAdapter = adapter;
	}

	@Override
	public void playAddAnimation(int pos, BaseAdapter adapter) {
		if(mAnimatableAdapter != null){
			mAnimatableAdapter.playAddAnimation(pos, adapter);
		}
	}
	
	@Override
	public void playRemoveAnimation(int pos, BaseAdapter adapter) {
		if(mAnimatableAdapter != null){
			mAnimatableAdapter.playRemoveAnimation(pos, adapter);
		}
	}
	
	@Override
	public void setAbsListView(AbsListView listView) {
	}

	@Override
	public int getCount() {
		return mListObject.size();
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		final int count = getCount();
		if(count != mOldCount){
			mOldCount = count;
			notifyItemObservers();
		}
	}

	public Object getItem(int position) {
		if(position >= mListObject.size() || position < 0){
			return null;
		}
		return mListObject.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	public abstract View getView(int position, View convertView, ViewGroup parent);
	
	public <T extends E> void replaceAll(Collection<T> collection){
		mListObject.clear();
		
		if(collection != null){
			mListObject.addAll(collection);
		}
		
		notifyDataSetChanged();
	}
	
	public <T extends E> void addAll(Collection<T> collection){
		if(collection != null){
			mListObject.addAll(collection);
		}
		notifyDataSetChanged();
	}
	
	public <T extends E> void addAll(int pos,Collection<T> collection){
		if(collection != null){
			mListObject.addAll(pos, collection);
		}
		notifyDataSetChanged();
	}

	public void addItem(E e){
		mListObject.add(e);
		playAddAnimation(getCount() - 1, this);
		notifyDataSetChanged();
	}
	
	public void addItem(int pos,E e){
		mListObject.add(pos,e);
		playAddAnimation(pos, this);
		notifyDataSetChanged();
	}
	
	public void addItemWithoutAnim(E e){
		mListObject.add(e);
		notifyDataSetChanged();
	}
	
	public void addItemWithoutAnim(int pos,E e){
		mListObject.add(pos, e);
		notifyDataSetChanged();
	}
	
	public void removeItem(E e){
		if(mAnimatableAdapter != null){
			int pos = mListObject.indexOf(e);
			if(pos >= 0){
				playRemoveAnimation(pos, this);
				mListObject.remove(pos);
			}
		}else{
			mListObject.remove(e);
		}
		notifyDataSetChanged();
	}
	
	public E removeItemById(String id){
		if(mListObject.size() > 0){
			int index = 0;
			Class<?> clz = SystemUtils.getSingleGenericClass(getClass(), SetBaseAdapter.class);
			if(clz == null){
				for(Object o : mListObject){
					if(o instanceof IDObject){
						IDObject ido = (IDObject)o;
						if(ido.getId().equals(id)){
							playRemoveAnimation(index, this);
							E remove = mListObject.remove(index);
							notifyDataSetChanged();
							return remove;
						}
						++index;
					}
				}
			}else{
				if(IDObject.class.isAssignableFrom(clz)){
					for(Object o : mListObject){
						IDObject ido = (IDObject)o;
						if(ido.getId().equals(id)){
							playRemoveAnimation(index, this);
							E remove = mListObject.remove(index);
							notifyDataSetChanged();
							return remove;
						}
						++index;
					}
				}
			}
		}
		return null;
	}
	
	public E removeItemByIdWithOutAnim(String id){
		final AnimatableAdapter save = mAnimatableAdapter;
		mAnimatableAdapter = null;
		E ret = removeItemById(id);
		mAnimatableAdapter = save;
		return ret;
	}
	
	public boolean updateItem(E item){
		final int index = mListObject.indexOf(item);
		if(index >= 0){
			mListObject.set(index, item);
			notifyDataSetChanged();
			return true;
		}
		return false;
	}
	
	public void updateOrInsertItem(E item){
		if(!updateItem(item)){
			addItem(item);
		}
	}
	
	public void updateOrInsertItem(int pos,E item){
		if(!updateItem(item)){
			addItem(pos, item);
		}
	}
	
	protected void notifyItemObservers(){
		if(mItemObservers != null){
			for(ItemObserver o : mItemObservers){
				o.onItemCountChanged(this);
			}
		}
	}
	
	public Object getItemById(String id){
		if(mListObject.size() > 0){
			Object item = getItem(0);
			if(item instanceof IDObject){
				for(Object o : mListObject){
					IDObject ido = (IDObject)o;
					if(ido.getId().equals(id)){
						return o;
					}
				}
			}
		}
		return null;
	}
	
	public int	indexOf(E e){
		return mListObject.indexOf(e);
	}
	
	public <T extends E> void removeAllItem(Collection<T> collection){
		mListObject.removeAll(collection);
		notifyDataSetChanged();
	}
	
	public List<E>	getAllItem(){
		return mListObject;
	}
	
	public void clear(){
		if(mListObject.size() > 0){
			mListObject.clear();
			notifyDataSetChanged();
		}
	}
	
	public <T> View buildConvertView(Class<T> holderClass,View convertView,Context context,int layoutId){
		if(convertView == null){
			convertView = SystemUtils.inflate(context, layoutId);
			try{
				Constructor<T> c = holderClass.getDeclaredConstructor();
				c.setAccessible(true);
				T holder = c.newInstance();
				FinalActivity.initInjectedView(holder, convertView);
				convertView.setTag(holder);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return convertView;
	}
	
	public static interface ItemObserver{
		public void onItemCountChanged(SetBaseAdapter<?> adapter);
	}
}
