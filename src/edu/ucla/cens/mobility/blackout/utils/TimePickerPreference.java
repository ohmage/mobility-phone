package edu.ucla.cens.mobility.blackout.utils;

import java.util.Calendar;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class TimePickerPreference extends DialogPreference {

	private SimpleTime mTime = new SimpleTime();
	private TimePicker mTimePicker = null;
	
	public TimePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initialize();
	}
	
	public TimePickerPreference(Context context, AttributeSet attrs, int flags) {
		super(context, attrs, flags);
		
		initialize();
	}
	
	private void handleTimeChange(int hour, int minute) {
		mTime.setHour(hour);
		mTime.setMinute(minute);
		setSummary(mTime.toString());
	}
	

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		mTimePicker.clearFocus();
		if(positiveResult) {
			handleTimeChange(mTimePicker.getCurrentHour(), 
							 mTimePicker.getCurrentMinute());
		}
	}
	
	@Override
	protected View onCreateDialogView() {
		
		mTimePicker = new TimePicker(getContext());
		mTimePicker.setIs24HourView(false);
		mTimePicker.setCurrentHour(mTime.getHour());
		mTimePicker.setCurrentMinute(mTime.getMinute());
		mTimePicker.clearFocus();
		return mTimePicker; 
	}

	private void initialize() {
		Calendar cal = Calendar.getInstance();
		handleTimeChange(cal.get(Calendar.HOUR_OF_DAY), 
						 cal.get(Calendar.MINUTE));
	}
	
	public void setTime(int hour, int minute) {
		handleTimeChange(hour, minute);
	}
	
	public void setTime(SimpleTime time) {
		handleTimeChange(time.getHour(), time.getMinute());
	}
	
	public SimpleTime getTime() {
		return new SimpleTime(mTime);
	}
}
