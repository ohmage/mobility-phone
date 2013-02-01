package org.ohmage.mobility.blackout.base;

import java.util.LinkedHashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Class which can parse and store the description of the action 
 * to be taken when a trigger goes off. Represented as a JSON string 
 * and is stored in the db against each trigger. 
 * 
 * Currently, the action corresponds to the list of surveys associated 
 * with each trigger. The notification module uses this list to display 
 * the alert. 
 * 
 * 
 * In order to generalize the trigger action (instead of just displaying
 * the notification), this class can include a type of the action and
 * the data associated with that action. According to the type, different
 * handlers (such as notification module) can be invoked when a trigger 
 * goes off. 
 * 
 * An example trigger action description:
 * 
 * {
 * 		"surveys": ["Sleep", "Stress"]
 * }
 */
public class TriggerActionDesc {

	private static final String KEY_SURVEYS = "surveys";
	
	private LinkedHashSet<String> mSurveyList
							 = new LinkedHashSet<String>();

	private void initialize() {
		mSurveyList.clear();
	}
	
	/*
	 * Load the description JSON string into this 
	 * object
	 */
	public boolean loadBoolean(String desc) {
		
		initialize();
		
		if(desc == null) {
			return false;
		}
		
//		try {
//			//Create the JSON object from the string
//			JSONObject jDesc = new JSONObject(desc);
//			
//			//Get the survey list 
//			if(jDesc.has(KEY_SURVEYS)) {
//				
//				mSurveyList.clear();
//			
//				JSONArray surveys = jDesc.getJSONArray(KEY_SURVEYS);
//				
//				//Add the surveys to the local list
//				for(int i = 0; i < surveys.length(); i++) {
//					mSurveyList.add(surveys.getString(i));
//				}
//			}
//			
//		} catch (JSONException e) {
//			return false;
//		}
		if (desc.equals(TriggerDB.ON))
			return true;
		else
			return false;
	}
	
	/*
	 * Returns the list of surveys associated
	 * with this object
	 */
	public String[] getSurveys() {
		return mSurveyList.toArray(new String[mSurveyList.size()]);
	}
	
	/*
	 * Set the list of surveys
	 */
	public void setSurveys(String[] surveys) {
		mSurveyList.clear();
		
		for(int i = 0; i < surveys.length; i++) {
			mSurveyList.add(surveys[i]);
		}
	}
	
	/*
	 * Check if a specific survey is present in 
	 * the list associated with this object
	 */
	public boolean hasSurvey(String survey) {
		return mSurveyList.contains(survey) ? true
											: false;
	}
	
	/*
	 * Remove all surveys from the list
	 */
	public void clearAllSurveys() {
		mSurveyList.clear();
	}
	
	/*
	 * Add a survey to the list
	 */
	public void addSurvey(String survey) {
		mSurveyList.add(survey);
	}
	
	/*
	 * Get the size of the survey list
	 */
	public int getCount() {
		return mSurveyList.size();
	}
	
	/*
	 * Convert this object to JSON string
	 */
	public String toString() {
		
		JSONObject jDesc = new JSONObject();
		
		//Create the JSON Array of surveys
		JSONArray surveys = new JSONArray();
		for(String survey : mSurveyList) {
			surveys.put(survey);
		}
		
		//Add the array to the JSON object
		if(surveys.length() > 0) {
			try {
				jDesc.put(KEY_SURVEYS, surveys);
			} catch (JSONException e) {
				return null;
			}
		}
	
		return jDesc.toString();
	}
	
	/*
	 * Get the string representation of the default 
	 * action description. This can be used to set the
	 * action description when a new trigger is added
	 * to the database. Currently, this returns a description
	 * with no surveys.
	 */
	public static boolean getDefaultDesc() {
		return true;//new JSONObject().toString();
	}
}
