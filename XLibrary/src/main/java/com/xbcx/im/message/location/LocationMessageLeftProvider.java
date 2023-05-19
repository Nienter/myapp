package com.xbcx.im.message.location;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.xbcx.im.XMessage;
import com.xbcx.im.ui.messageviewprovider.CommonViewProvider;
import com.xbcx.library.R;

public class LocationMessageLeftProvider extends CommonViewProvider<LocationMessageLeftProvider.LocationViewHolder> {

	@Override
	public boolean acceptHandle(XMessage message) {
		if(!message.isFromSelf()){
			return message.getType() == XMessage.TYPE_LOCATION;
		}
		return false;
	}

	@Override
	protected LocationViewHolder onCreateViewHolder() {
		return new LocationViewHolder();
	}

	@Override
	protected void onSetViewHolder(View convertView, LocationViewHolder holder,XMessage xm) {
		super.onSetViewHolder(convertView, holder,xm);
		View v = LayoutInflater.from(convertView.getContext()).inflate(R.layout.message_content_location, null);
		holder.mTextViewLocation = (TextView)v.findViewById(R.id.tvLocation);
		holder.mContentView.addView(v);
	}

	@Override
	protected void onUpdateView(LocationViewHolder holder, XMessage m) {
		holder.mTextViewLocation.setText(m.getContent());
	}

	protected static class LocationViewHolder extends CommonViewProvider.CommonViewHolder{
		public TextView		mTextViewLocation;
	}
}
