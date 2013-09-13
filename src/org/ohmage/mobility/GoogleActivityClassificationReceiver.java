package org.ohmage.mobility;

import java.util.List;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class GoogleActivityClassificationReceiver 
extends IntentService
{
	public static final String TAG = "GACReceiver";
	
	public GoogleActivityClassificationReceiver() {
	    super("GoogleActivityClassificationReceiver");
	}
	
	public GoogleActivityClassificationReceiver(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (ActivityRecognitionResult.hasResult(intent)) {
	        Log.d(TAG, "GAC responded!"); 
			ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
	         // Put your application specific logic here (i.e. result.getMostProbableActivity())
	         DetectedActivity da = result.getMostProbableActivity();
	         String activity = ClassifierService.ERROR;
	         activity = toOhmageType(da.getType());
	         
	         
	         List<DetectedActivity> activities = result.getProbableActivities();
	         int max = 0;
	         int secondBest = -1;
	         for (DetectedActivity d : activities)
	         {
	        	 if (d.getConfidence() > max && d.getType() != da.getType()) // want second highest
	        	 {
	        		 max = d.getConfidence();
	        		 secondBest = d.getType();
	        	 }
	        	 
	         }
	         String guess = ClassifierService.UNKNOWN;
	         if (secondBest >= 0)
	        	 guess = toOhmageType(secondBest);
	         GoogleActivityClassifier.setGooglemodes(activity, guess);
	         Log.d(TAG, "Detected activity was " + activity);
	     }

		
	}

	private String toOhmageType(int type) {
		String activity;
		switch (type)
        {
        	case DetectedActivity.IN_VEHICLE:
        		activity = ClassifierService.DRIVE;
        		break;
        	case DetectedActivity.ON_BICYCLE:
        		activity = ClassifierService.BIKE;
        		break;
        	case DetectedActivity.ON_FOOT:
        		activity = ClassifierService.WALK;
        		break;
        	case DetectedActivity.STILL:
        		activity = ClassifierService.STILL;
        		break;
        	case DetectedActivity.TILTING:
        		activity = ClassifierService.TILT;
        		break;
        	case DetectedActivity.UNKNOWN:
        		activity = ClassifierService.UNKNOWN;
        		break;
        	default:
        		activity = ClassifierService.ERROR;
        		Log.d(TAG, "GAC error: " + type);
        
        }
		return activity;
	}
	
	

}
