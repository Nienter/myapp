package com.xbcx.common.menu;

import com.xbcx.adapter.SetBaseAdapter;
import com.xbcx.library.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MenuItemAdapter extends SetBaseAdapter<Menu> {

	private Context mContext;
	
	public MenuItemAdapter(Context context){
		mContext = context;
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if(mListObject.size() > position){
			return 0;
		}else{
			return 1;
		}
	}

	@Override
	public int getCount() {
		return super.getCount() + 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(mListObject.size() > position){
			if(convertView == null){
				final LayoutInflater inflater = LayoutInflater.from(mContext);
				convertView = inflater.inflate(R.layout.xlibrary_menuitem, null);
			}
			
			final TextView textView = (TextView)convertView.findViewById(R.id.tv);
			
			final Menu item = (Menu)getItem(position);
			textView.setText(item.getText());
			
			return convertView;
		}else{
			if(convertView == null){
				convertView = LayoutInflater.from(mContext).inflate(R.layout.xlibrary_menuitem_footer, null);
			}
			return convertView;
		}
	}
	
	public void addItem(int nPos,Menu item){
		mListObject.add(nPos, item);
	}
	
	public void removeItem(int nId){
		for(Menu item : mListObject){
			if(item.getId() == nId){
				mListObject.remove(item);
				break;
			}
		}
	}
	
	public Menu getMenuItem(int nId){
		for(Menu item : mListObject){
			if(item.getId() == nId){
				return item;
			}
		}
		return null;
	}
}
