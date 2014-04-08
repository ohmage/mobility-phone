
package org.ohmage.mobility.blackout.notif;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import org.ohmage.mobility.blackout.Blackout;
import org.ohmage.mobility.blackout.base.TriggerActionDesc;
import org.ohmage.mobility.blackout.base.TriggerBase;
import org.ohmage.mobility.blackout.base.TriggerDB;
import org.ohmage.mobility.blackout.base.TriggerRunTimeDesc;
import org.ohmage.mobility.blackout.utils.TrigPrefManager;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/*
 * An interface to the survey list management. This class 
 * provides functions to keep track of the survey taken
 * time stamps, to get the list of active surveys and to 
 * get various trigger related JSON strings used while 
 * uploading survey responses. 
 * 
 * This class isolates the logic related to surveys from 
 * the Notifier class thus making the implementation of the
 * Notifier generic and independent of the surveys.  
 */
public class NotifSurveyAdaptor {

    private static final String DEBUG_TAG = "TriggerFramework";

    /* Tag used to log through SystemLog */
    private static final String SYSTEM_LOG_TAG = "TriggerFramework";

    /* Keys used in preparation of the JSON strings */
    private static final String KEY_ACTIVE_TRIGGERS = "active_triggers";
    private static final String KEY_TRIGGER_DESC = "trigger_description";
    private static final String KEY_NOTIF_DESC = "notification_description";
    private static final String KEY_RUNTIME_DESC = "runtime_description";
    private static final String KEY_TRIGGER_TYPE = "trigger_type";
    private static final String KEY_TRIGER_PREF = "trigger_preferences";
    private static final String KEY_SURVEY_LIST = "survey_list";
    private static final String KEY_UNTAKEN_SURVEYS = "surveys_not_taken";

    /*
     * Helper function to prepare the list of active surveys corresponding to
     * trigger. A trigger is active if it has not expired (the notification
     * duration has not been reached) after going off the last time. All surveys
     * associated with an active active trigger are active - EXCEPT those which
     * have already been taken by the user within the suppression window.
     */
    private static HashSet<String> getActiveSurveys(Context context, Cursor trig) {

        HashSet<String> actSurveys = new HashSet<String>();

        String runTime = trig.getString(trig.getColumnIndexOrThrow(TriggerDB.KEY_RUNTIME_DESCRIPT));

        // String notif = trig.getString(
        // trig.getColumnIndexOrThrow(TriggerDB.KEY_NOTIF_DESCRIPT));

        String actions = trig.getString(trig
                .getColumnIndexOrThrow(TriggerDB.KEY_TRIG_ACTIVE_DESCRIPT));

        Log.v(DEBUG_TAG, "NotifSurveyAdaptor: Calculating active surveys for trigger");

        TriggerRunTimeDesc rtDesc = new TriggerRunTimeDesc();
        NotifDesc notifDesc = new NotifDesc();
        TriggerActionDesc actDesc = new TriggerActionDesc();

        if (!rtDesc.loadString(runTime) ||
        // !notifDesc.loadString(notif) ||
                !actDesc.loadBoolean(actions)) {

            Log.w(DEBUG_TAG, "NotifSurveyAdaptor: Descritptor(s) failed to parse");

            return actSurveys;
        }

        if (!rtDesc.hasTriggerTimeStamp()) {
            Log.w(DEBUG_TAG, "NotifSurveyAdaptor: Trigger time stamp is invalid");

            return actSurveys;
        }

        long now = System.currentTimeMillis();
        long trigTS = rtDesc.getTriggerTimeStamp();

        if (trigTS > now) {
            Log.w(DEBUG_TAG, "NotifSurveyAdaptor: Trigger time stamp is in the future!");
            return actSurveys;
        }

        // How long it has been since the trigger went off
        long elapsedMS = now - trigTS;

        long durationMS = notifDesc.getDuration() * 60000;
        long suppressMS = notifDesc.getSuppression() * 60000;

        if (elapsedMS < durationMS) {

            // The trigger has not expired, check each survey

            String[] surveys = actDesc.getSurveys();
            for (int i = 0; i < surveys.length; i++) {

                // Has the survey been taken in within the
                // suppression window?
                if (IsSurveyTaken(context, surveys[i], trigTS - suppressMS)) {
                    continue;
                }

                // Add the active survey to the set
                actSurveys.add(surveys[i]);
            }
        }

        return actSurveys;
    }

    /*
     * Utility function to check if a survey has been taken by the user since a
     * given time. This function checks the time stamp of the survey stored in
     * shared preferences.
     */
    private static boolean IsSurveyTaken(Context context, String survey, long since) {

        SharedPreferences pref = context.getSharedPreferences(NotifSurveyAdaptor.class.getName(),
                Context.MODE_PRIVATE);

        if (!pref.contains(survey)) {
            return false;
        }

        if (pref.getLong(survey, 0) <= since) {
            return false;
        }

        return true;
    }

    /*
     * Get the list of all surveys active at the moment. This function creates a
     * set of all active surveys from all active triggers. This function is used
     * by the Notifier to prepare the notification item.
     */
    public static Set<String> getAllActiveSurveys(Context context) {
        HashSet<String> actSurveys = new HashSet<String>();

        TriggerDB db = new TriggerDB(context);
        db.open();

        Cursor c = db.getAllTriggers();
        if (c.moveToFirst()) {
            do {

                actSurveys.addAll(getActiveSurveys(context, c));

            } while (c.moveToNext());
        }
        c.close();
        db.close();

        return actSurveys;
    }

    /*
     * Get all the active surveys corresponding to a specific trigger.
     */
    public static Set<String> getActiveSurveysForTrigger(Context context, int trigId) {
        HashSet<String> actSurveys = new HashSet<String>();

        TriggerDB db = new TriggerDB(context);
        db.open();

        Cursor c = db.getTrigger(trigId);
        if (c.moveToFirst()) {
            actSurveys.addAll(getActiveSurveys(context, c));
        }

        c.close();
        db.close();

        return actSurveys;
    }

    /*
     * Saves the current time stamp against the given survey name. This must be
     * called whenever a survey is taken by the user.
     */
    public static void recordSurveyTaken(Context context, String survey) {

        SharedPreferences pref = context.getSharedPreferences(NotifSurveyAdaptor.class.getName(),
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(survey, System.currentTimeMillis());
        editor.commit();

        TrigPrefManager.registerPreferenceFile(context, NotifSurveyAdaptor.class.getName());
    }

    /*
     * Utility function to add the JSON description of a active trigger to JSON
     * array.
     */
    private static void addTriggerInfoToArray(Context context, Cursor trig, JSONArray jArray) {
        String rtDesc = trig.getString(trig.getColumnIndexOrThrow(TriggerDB.KEY_RUNTIME_DESCRIPT));
        TriggerRunTimeDesc desc = new TriggerRunTimeDesc();
        desc.loadString(rtDesc);

        // String notifDesc = trig.getString(
        // trig.getColumnIndexOrThrow(TriggerDB.KEY_NOTIF_DESCRIPT));

        String trigDesc = trig.getString(trig.getColumnIndexOrThrow(TriggerDB.KEY_TRIG_DESCRIPT));

        // String trigType = trig.getString(
        // trig.getColumnIndexOrThrow(TriggerDB.KEY_TRIG_TYPE));

        JSONObject jPref = new JSONObject();
        TriggerBase trigBase = new Blackout();// new
                                              // TriggerTypeMap().getTrigger(/*trigType*/);
        if (trigBase != null) {
            jPref = trigBase.getPreferences(context);
        }

        JSONObject jTrigInfo = new JSONObject();

        try {
            // jTrigInfo.put(KEY_TRIGGER_TYPE, trigType);
            jTrigInfo.put(KEY_TRIGGER_DESC, new JSONObject(trigDesc));
            // jTrigInfo.put(KEY_NOTIF_DESC, new JSONObject(notifDesc));
            jTrigInfo.put(KEY_RUNTIME_DESC, new JSONObject(desc.toHumanReadableString()));
            jTrigInfo.put(KEY_TRIGER_PREF, jPref);

        } catch (JSONException e) {
            return;
        }

        jArray.put(jTrigInfo);
    }

    /*
     * Get the JSON array containing the details of all the triggers which have
     * activated a given survey at the moment.
     */
    public static JSONArray getActiveTriggerInfo(Context context, String survey) {
        JSONObject jInfo = new JSONObject();
        JSONArray jTrigs = new JSONArray();

        TriggerDB db = new TriggerDB(context);
        db.open();

        Cursor c = db.getAllTriggers();
        if (c.moveToFirst()) {
            do {
                if (getActiveSurveys(context, c).contains(survey)) {
                    addTriggerInfoToArray(context, c, jTrigs);
                }

            } while (c.moveToNext());
        }
        c.close();
        db.close();

        try {
            jInfo.put(KEY_ACTIVE_TRIGGERS, jTrigs);
        } catch (JSONException e) {
            return null;
        }

        return jTrigs;
    }

    /*
     * To be called when a trigger expires. This function logs using SystemLog,
     * the list of all surveys not taken by the user but were activated by the
     * given trigger.
     */
    public static void handleExpiredTrigger(Context context, int trigId) {
        TriggerDB db = new TriggerDB(context);
        db.open();

        boolean sActDesc = db.getActionDescription(trigId);
        String sTrigDesc = db.getTriggerDescription(trigId);
        // String sTrigType = db.getTriggerType(trigId);
        String sRTDesc = db.getRunTimeDescription(trigId);

        db.close();

        if (/* sActDesc == null || */
        sTrigDesc == null ||
        // sTrigType == null ||
                sRTDesc == null) {

            return;
        }

        TriggerActionDesc actDesc = new TriggerActionDesc();
        if (/* !actDesc.loadBoolean( */sActDesc/* ) */) {
            return;
        }

        TriggerRunTimeDesc rtDesc = new TriggerRunTimeDesc();
        if (!rtDesc.loadString(sRTDesc)) {
            return;
        }

        LinkedList<String> untakenList = new LinkedList<String>();
        for (String survey : actDesc.getSurveys()) {

            if (!IsSurveyTaken(context, survey, rtDesc.getTriggerTimeStamp())) {

                untakenList.add(survey);
            }
        }

        if (untakenList.size() == 0) {
            return;
        }

        JSONArray jSurveyList = new JSONArray();
        for (String survey : actDesc.getSurveys()) {
            jSurveyList.put(survey);
        }

        JSONArray jUntakenSurveys = new JSONArray();
        for (String unTakenSurvey : untakenList) {
            jUntakenSurveys.put(unTakenSurvey);
        }

        JSONObject jExpired = new JSONObject();

        try {
            // jExpired.put(KEY_TRIGGER_TYPE, sTrigType);
            jExpired.put(KEY_TRIGGER_DESC, new JSONObject(sTrigDesc));
            jExpired.put(KEY_SURVEY_LIST, jSurveyList);
            jExpired.put(KEY_UNTAKEN_SURVEYS, jUntakenSurveys);
        } catch (JSONException e) {
            return;
        }

        // Log the info
        Log.i(DEBUG_TAG, "Expired trigger has surveys not taken: " + jExpired.toString());
    }
}
