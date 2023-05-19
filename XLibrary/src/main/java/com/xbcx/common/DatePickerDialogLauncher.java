package com.xbcx.common;

import java.util.Calendar;

import net.simonvt.datepicker.DatePickerDialog;

import com.xbcx.library.R;
import com.xbcx.utils.DateUtils;

import android.app.Activity;

public class DatePickerDialogLauncher {
	
	private long					mTime;
	private long					mMaxTime;
	private long					mMinTime;
	private OnDateChooseListener	mListener;
	
	public DatePickerDialogLauncher setTime(long time){
		mTime = time;
		return this;
	}
	
	public DatePickerDialogLauncher setMaxTime(long maxTime){
		mMaxTime = maxTime;
		return this;
	}
	
	public DatePickerDialogLauncher setMinTime(long minTime){
		mMinTime = minTime;
		return this;
	}
	
	public DatePickerDialogLauncher setOnDateChooseListener(OnDateChooseListener l){
		mListener = l;
		return this;
	}

	public DatePickerDialog onLaunch(Activity activity){
		DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(net.simonvt.datepicker.DatePicker view, 
					int year, int monthOfYear, int dayOfMonth) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, monthOfYear);
				cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				if(mListener != null){
					mListener.onDateChoosed(cal);
				}
			}
		};
		Calendar cal = DateUtils.ThreadLocalCalendar.get();
		cal.setTimeInMillis(mTime);
		DatePickerDialog d = new DatePickerDialog(activity,
				R.style.DatePickerDialog,listener, 
				cal.get(Calendar.YEAR), 
				cal.get(Calendar.MONTH), 
				cal.get(Calendar.DAY_OF_MONTH));
		if(mMaxTime > 0){
			d.getDatePicker().setMaxDate(mMaxTime);
		}
		if(mMinTime > 0){
			d.getDatePicker().setMinDate(mMinTime);
		}
		d.show();
		return d;
	}
	
	public static interface OnDateChooseListener{
		public void onDateChoosed(Calendar cal);
	}
}
