package com.xbcx.im.ui.messageviewprovider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xbcx.core.XApplication;
import com.xbcx.im.XMessage;
import com.xbcx.im.ui.IMMessageViewProvider;
import com.xbcx.library.R;
import com.xbcx.utils.DateUtils;

public class TimeViewProvider extends IMMessageViewProvider {

	private static SimpleDateFormat TIME_FORMAT;
	
	private static final SimpleDateFormat TIME_FORMAT_TODAY = new SimpleDateFormat("HH:mm",Locale.getDefault());
	
	private boolean	mUseYesterdayTime;
	
	public TimeViewProvider setUseYesterdayTime(boolean bUse){
		mUseYesterdayTime = bUse;
		return this;
	}
	
	@Override
	public boolean acceptHandle(XMessage message) {
		if(message.getType() == XMessage.TYPE_TIME){
			return true;
		}
		return false;
	}

	@Override
	public View getView(XMessage message, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_time, null);
		}
		
		XMessage m = (XMessage)message;
		
		TextView textView = (TextView)convertView.findViewById(R.id.tvGroupTime);
		Date date = new Date(m.getSendTime());
		
		if(DateUtils.isToday(m.getSendTime())){
			if(mUseYesterdayTime){
				textView.setText(TIME_FORMAT_TODAY.format(date));
			}else{
				textView.setText(parent.getContext().getString(R.string.today) + "  " + 
						TIME_FORMAT_TODAY.format(date));
			}
		}else{
			if(mUseYesterdayTime){
				if(DateUtils.isYestoday(m.getSendTime())){
					textView.setText(parent.getContext().getString(R.string.yesterday) + "  " + 
							TIME_FORMAT_TODAY.format(date));
				}else{
					textView.setText(formatNormal(date));
				}
			}else{
				textView.setText(formatNormal(date));
			}
		}
		
		return convertView;
	}

	protected String formatNormal(Date date){
		if(TIME_FORMAT == null){
			TIME_FORMAT = new SimpleDateFormat(
					XApplication.getApplication().getString(R.string.dateformat_mdhm), 
					Locale.getDefault());
		}
		return TIME_FORMAT.format(date);
	}
}
