package com.xbcx.im.ui.messageviewprovider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xbcx.im.XMessage;
import com.xbcx.im.ui.IMMessageViewProvider;
import com.xbcx.library.R;

public class PromptViewProvider extends IMMessageViewProvider {

	@Override
	public boolean acceptHandle(XMessage message) {
		return message.getType() == XMessage.TYPE_PROMPT;
	}

	@Override
	public View getView(XMessage message, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_time, null);
		}
		
		XMessage m = (XMessage)message;
		
		TextView textView = (TextView)convertView.findViewById(R.id.tvGroupTime);
		textView.setText(m.getContent());
		
		return convertView;
	}

}
