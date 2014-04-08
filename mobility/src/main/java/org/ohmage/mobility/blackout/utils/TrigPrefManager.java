package org.ohmage.mobility.blackout.utils;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class TrigPrefManager {
	
	private static final String PREF_FILE_NAME
								= TrigPrefManager.class.getName();
	
	public static void registerPreferenceFile(Context context, String fileName) {
		SharedPreferences prefs = context.getSharedPreferences(
									PREF_FILE_NAME, Context.MODE_PRIVATE);

		Editor editor = prefs.edit();
		editor.putBoolean(fileName, true);
		editor.commit();
	}
	
	public static void clearAllPreferenceFiles(Context context) {
		
		SharedPreferences prefMan = context.getSharedPreferences(
										PREF_FILE_NAME, Context.MODE_PRIVATE);

		Map<String, ?> prefFileList = prefMan.getAll();
		if(prefFileList == null) {
			return;
		}
		
		for(String prefFile : prefFileList.keySet()) {
			SharedPreferences pref = context.getSharedPreferences(
											prefFile, Context.MODE_PRIVATE);
			Editor editPref = pref.edit();
			editPref.clear();
			editPref.commit();
		}
		
		//Clear the preference manager itself
		Editor editMan = prefMan.edit();
		editMan.clear();
		editMan.commit();
	}
}
