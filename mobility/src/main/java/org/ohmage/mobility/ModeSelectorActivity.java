package org.ohmage.mobility;

import java.util.HashMap;

import org.ohmage.mobility.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.app.Activity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class ModeSelectorActivity extends Activity {
	
	public static final String TAG = "ModeSelectorActivity";
	public static final String NO_MODE = "undefined";
	public static final String CURRENT_MODE = "current_mode";
	public static String currentMode = NO_MODE;
	public static int currentCheck = R.id.undefined;
	public static String CURRENT_CHECK = "current_check";
	public static Object modeSynch = new Object();
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
	}




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mode_selection);
//		RadioGroup modes = (RadioGroup)findViewById(R.id.modes);
//		loadMode();
//		modes.check(R.id.undefined);
		
//		Log.d(TAG, "Checking " + currentCheck + " ("+currentMode+")");
		
	}




	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		RadioGroup modes = (RadioGroup)findViewById(R.id.modes);
		loadMode();
		modes.check(currentCheck);
	}

	public static void resetMode()
	{
		currentMode = NO_MODE;
		currentCheck = R.id.undefined;
	}

	private void changeMode(String mode, int checkId)
	{
		synchronized (modeSynch) 
		{
			SharedPreferences settings = getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
			Editor editor = settings.edit();
			editor.putString(CURRENT_MODE, mode);
			editor.putInt(CURRENT_CHECK, checkId);
			editor.commit();
			currentMode = mode;
			currentCheck = checkId;
		}
	}
	
	private void loadMode()
	{
		synchronized (modeSynch) 
		{
			SharedPreferences settings = getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
			currentMode = settings.getString(CURRENT_MODE, NO_MODE);
			currentCheck = settings.getInt(CURRENT_CHECK, R.id.undefined);
		}
	}
	
	public static String getTrainingMode()
	{
		synchronized (modeSynch) 
		{
			return currentMode;
		}
	}

	public void onRadioButtonClicked(View view) {
	    // Is the button now checked?
	    boolean checked = ((RadioButton) view).isChecked();
	    
		
	    // Check which radio button was clicked
	    int sel = view.getId();
	    switch(sel) {
	        case R.id.still:
	            if (checked)
		            changeMode(ClassifierService.STILL, sel);
	            break;
	        case R.id.walk:
	            if (checked)
	            	changeMode(ClassifierService.WALK, sel);
	            break;
	        case R.id.run:
	            if (checked)
	            	changeMode(ClassifierService.RUN, sel);
	            break;
	        case R.id.drive:
	            if (checked)
	            	changeMode(ClassifierService.DRIVE, sel);
	            break;
	        case R.id.bike:
	            if (checked)
	            	changeMode(ClassifierService.BIKE, sel);
	            break;
	        case R.id.rail:
	            if (checked)
	            	changeMode("rail", sel);
	            break;
	        case R.id.motorcycle:
	            if (checked)
	            	changeMode("motorcycle", sel);
	            break;
	        case R.id.undefined:
	            if (checked)
	            	changeMode(NO_MODE, sel);
	            break;
	    }
	}




	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
//		changeMode(NO_MODE, r.id.undefined);
	}
}
