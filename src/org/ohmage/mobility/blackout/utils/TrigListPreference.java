package org.ohmage.mobility.blackout.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class TrigListPreference extends ListPreference {

	private TrigListPreference.onCancelListener mListener = null;
	
	public interface onCancelListener {
		public void onCancel();
	}
	
	public void setOnCancelListener(TrigListPreference.onCancelListener listener) {
		mListener = listener;
	}
	
	public TrigListPreference(Context context) {
		super(context);
	}
	
	public TrigListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		
		if(which == DialogInterface.BUTTON_NEGATIVE) {
			if(mListener != null) {
				mListener.onCancel();
			}
		}
		
		super.onClick(dialog, which);
	}

}
