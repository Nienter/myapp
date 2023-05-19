package com.xbcx.im.ui.simpleimpl;

import com.xbcx.adapter.HideableAdapter;
import com.xbcx.adapter.StickyHeader;
import com.xbcx.library.R;
import com.xbcx.utils.SystemUtils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AdbSectionAdapter extends HideableAdapter implements StickyHeader{

	private Context	mContext;
	private String	mText;
	private boolean	mIsStickyHeader = true;
	
	private View	mViewStickyHeader;
	
	public AdbSectionAdapter(Context context,String text) {
		mContext = context;
		mText = text;
	}
	
	public AdbSectionAdapter(Context context,String text,boolean bStickyHeader) {
		mContext = context;
		mText = text;
		mIsStickyHeader = bStickyHeader;
	}
	
	public void setText(String text){
		mText = text;
		if(mViewStickyHeader != null){
			final TextView tv = (TextView)mViewStickyHeader.findViewById(R.id.tvName);
			tv.setText(mText);
		}
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if(convertView == null){
			convertView = SystemUtils.inflate(mContext,R.layout.xlibrary_adapter_adb_section);
			viewHolder = new ViewHolder();
			viewHolder.mTextViewName = (TextView)convertView.findViewById(R.id.tvName);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		
		viewHolder.mTextViewName.setText(mText);
		
		return convertView;
	}

	private static class ViewHolder{
		public TextView mTextViewName;
	}

	@Override
	public boolean isItemViewTypeSticky(int viewType) {
		return mIsStickyHeader;
	}

	@Override
	public View getStickyHeaderView(View convertView,int viewType,int index,ViewGroup parent) {
		if(mViewStickyHeader == null){
			mViewStickyHeader = SystemUtils.inflate(mContext,R.layout.xlibrary_adapter_adb_section);
			final TextView tv = (TextView)mViewStickyHeader.findViewById(R.id.tvName);
			tv.setText(mText);
		}
		return mViewStickyHeader;
	}
}
