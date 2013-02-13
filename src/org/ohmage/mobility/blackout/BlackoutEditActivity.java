
package org.ohmage.mobility.blackout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.ohmage.logprobe.Log;
import org.ohmage.mobility.R;
import org.ohmage.mobility.blackout.config.TrigUserConfig;
import org.ohmage.mobility.blackout.utils.TimePickerPreference;

import java.util.LinkedHashMap;

public class BlackoutEditActivity extends PreferenceActivity implements View.OnClickListener,
        OnPreferenceClickListener, OnPreferenceChangeListener,
        DialogInterface.OnMultiChoiceClickListener, DialogInterface.OnClickListener {

    public static final String KEY_TRIG_DESC = "trig_desc";
    public static final String KEY_TRIG_ID = "trig_id";
    public static final String KEY_ADMIN_MODE = "admin_mode";
    private static final String KEY_SAVE_DAYS = "days";
    private static final String KEY_SAVE_REPEAT_STATUS = "repeat_status";

    // private static final String PREF_KEY_TRIGGER_TIME = "trigger_time";
    // private static final String PREF_KEY_RANDOMIZE =
    // "randomize_trigger_time";
    // private static final String PREF_KEY_ENABLE_RANGE = "enable_time_range";
    private static final String PREF_KEY_START_TIME = "interval_start_time";
    private static final String PREF_KEY_END_TIME = "interval_end_time";
    private static final String PREF_KEY_REPEAT_DAYS = "repeat_days";

    private static final int DIALOG_ID_REPEAT_SEL = 0;
    private static final int DIALOG_ID_INVALID_TIME_ALERT = 1;
    private static final String TAG = "BlackoutEditActivity";

    private BlackoutDesc mTrigDesc;
    private String[] mDays;
    private boolean[] mRepeatStatus;
    // private boolean mAdminMode = false;
    private AlertDialog mRepeatDialog = null;

    public interface ExitListener {

        public void onDone(Context context, int trigId, String trigDesc);
    }

    private static ExitListener mExitListener = null;
    private int mTrigId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.blackout_edit_preferences);
        setContentView(R.layout.blackout_editor);

        mTrigDesc = new BlackoutDesc();

        PreferenceScreen screen = getPreferenceScreen();
        int prefCount = screen.getPreferenceCount();
        for (int i = 0; i < prefCount; i++) {
            screen.getPreference(i).setOnPreferenceClickListener(this);
            screen.getPreference(i).setOnPreferenceChangeListener(this);
        }

        ((Button) findViewById(R.id.trig_edit_done)).setOnClickListener(this);
        ((Button) findViewById(R.id.trig_edit_cancel)).setOnClickListener(this);

        String config = null;

        if (savedInstanceState != null) {
            config = savedInstanceState.getString(KEY_TRIG_DESC);
        } else {
            config = getIntent().getStringExtra(KEY_TRIG_DESC);
        }

        if (config != null) {
            mTrigId = getIntent().getIntExtra(KEY_TRIG_ID, 0);
            // mAdminMode = getIntent().getBooleanExtra(KEY_ADMIN_MODE, false);

            if (mTrigDesc.loadString(config)) {
                initializeGUI();
            } else {
                getPreferenceScreen().setEnabled(false);
                Toast.makeText(this, "Invalid trigger settings!", Toast.LENGTH_SHORT).show();
            }
        }

        if (savedInstanceState == null) {
            LinkedHashMap<String, Boolean> repeatList = mTrigDesc.getRepeat();
            mDays = repeatList.keySet().toArray(new String[repeatList.size()]);
            mRepeatStatus = new boolean[mDays.length];
            updateRepeatStatusArray();
        } else {
            mDays = savedInstanceState.getStringArray(KEY_SAVE_DAYS);
            mRepeatStatus = savedInstanceState.getBooleanArray(KEY_SAVE_REPEAT_STATUS);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        udateTriggerDesc();
        outState.putString(KEY_TRIG_DESC, mTrigDesc.toString());
        outState.putStringArray(KEY_SAVE_DAYS, mDays);
        outState.putBooleanArray(KEY_SAVE_REPEAT_STATUS, mRepeatStatus);
    }

    public static void setOnExitListener(ExitListener listener) {

        mExitListener = listener;
    }

    private void initializeGUI() {
        // TimePickerPreference trigTimePref = (TimePickerPreference)
        // getPreferenceScreen()
        // .findPreference(PREF_KEY_TRIGGER_TIME);
        // CheckBoxPreference rangePref = (CheckBoxPreference)
        // getPreferenceScreen()
        // .findPreference(PREF_KEY_ENABLE_RANGE);
        // CheckBoxPreference randPref = (CheckBoxPreference)
        // getPreferenceScreen()
        // .findPreference(PREF_KEY_RANDOMIZE);
        TimePickerPreference startPref = (TimePickerPreference) getPreferenceScreen()
                .findPreference(PREF_KEY_START_TIME);
        TimePickerPreference endPref = (TimePickerPreference) getPreferenceScreen().findPreference(
                PREF_KEY_END_TIME);

        // if(mTrigDesc.isRangeEnabled()) {
        // // rangePref.setChecked(true);
        //
        // // if(mTrigDesc.isRandomized()) {
        // // randPref.setChecked(true);
        // // }
        // // else {
        // // trigTimePref.setTime(mTrigDesc.getTriggerTime());
        // // }
        //
        startPref.setTime(mTrigDesc.getRangeStart());
        endPref.setTime(mTrigDesc.getRangeEnd());
        // }
        // // else {
        // // trigTimePref.setTime(mTrigDesc.getTriggerTime());
        // // }

        updateTriggerTimePrefStatus();
        updateRepeatPrefStatus();

        // if(!mAdminMode && !TrigUserConfig.editTimeTriggerTime) {
        //
        // trigTimePref.setEnabled(false);
        // }
        //
        // if(!mAdminMode && !TrigUserConfig.editTimeTriggerRange) {
        //
        // rangePref.setEnabled(false);
        // }
        //
        // if(!mAdminMode && !TrigUserConfig.editTimeTriggerRange) {
        //
        // randPref.setEnabled(false);
        // }

        // if(!mAdminMode && !TrigUserConfig.editTimeTriggerRange) {
        //
        // startPref.setEnabled(false);
        // }
        //
        // if(!mAdminMode && !TrigUserConfig.editTimeTriggerRange) {
        //
        // endPref.setEnabled(false);
        // }

        Preference repeatPref = getPreferenceScreen().findPreference(PREF_KEY_REPEAT_DAYS);

        // if(!mAdminMode && !TrigUserConfig.editTimeTriggerRepeat) {
        //
        // repeatPref.setEnabled(false);
        // }

        ((Button) findViewById(R.id.trig_edit_done))
                .setEnabled(/* mAdminMode || */TrigUserConfig.editTimeTrigger);
    }

    private void udateTriggerDesc() {
        // CheckBoxPreference rangePref = (CheckBoxPreference)
        // getPreferenceScreen()
        // .findPreference(PREF_KEY_ENABLE_RANGE);
        // CheckBoxPreference randPref = (CheckBoxPreference)
        // getPreferenceScreen()
        // .findPreference(PREF_KEY_RANDOMIZE);
        // TimePickerPreference timePref = (TimePickerPreference)
        // getPreferenceScreen()
        // .findPreference(PREF_KEY_TRIGGER_TIME);
        TimePickerPreference startPref = (TimePickerPreference) getPreferenceScreen()
                .findPreference(PREF_KEY_START_TIME);
        TimePickerPreference endPref = (TimePickerPreference) getPreferenceScreen().findPreference(
                PREF_KEY_END_TIME);

        // if(rangePref.isChecked()) {
        // mTrigDesc.setRangeEnabled(true);

        // if(randPref.isChecked()) {
        // mTrigDesc.setRandomized(true);
        // }
        // else {
        // mTrigDesc.setRandomized(false);
        // mTrigDesc.setTriggerTime(timePref.getTime());
        // }

        mTrigDesc.setRangeStart(startPref.getTime());
        mTrigDesc.setRangeEnd(endPref.getTime());
    }

    // else {
    // mTrigDesc.setRangeEnabled(false);
    // mTrigDesc.setRandomized(false);
    // mTrigDesc.setTriggerTime(timePref.getTime());
    // }
    // }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.trig_edit_done:
                if (mExitListener != null) {
                    udateTriggerDesc();
                    if (mTrigDesc.validate(this, mTrigId)) {
                        Log.v(TAG, "Validation succeeded!");
                        mExitListener.onDone(this, mTrigId, mTrigDesc.toString());
                    } else {
                        Log.v(TAG, "Failed validation");
                        showDialog(DIALOG_ID_INVALID_TIME_ALERT);
                        return;
                    }
                } else
                    Log.w(TAG, "Exit listener is null for no reason.");

                break;
        }

        finish();
    }

    private void updateTriggerTimePrefStatus() {
        // TimePickerPreference trigTimePref = (TimePickerPreference)
        // getPreferenceScreen()
        // .findPreference(PREF_KEY_TRIGGER_TIME);

        // CheckBoxPreference rangePref = (CheckBoxPreference)
        // getPreferenceScreen()
        // .findPreference(PREF_KEY_ENABLE_RANGE);

        // CheckBoxPreference randPref = (CheckBoxPreference)
        // getPreferenceScreen()
        // .findPreference(PREF_KEY_RANDOMIZE);

        // if(rangePref.isChecked() && randPref.isChecked()) {
        // trigTimePref.setSummary("Random");
        // trigTimePref.setEnabled(false);
        // }
        // else {
        // trigTimePref.setSummary(trigTimePref.getTime().toString());
        // trigTimePref.setEnabled(true);
        // }
    }

    private void updateRepeatPrefStatus() {
        Preference repeatPref = getPreferenceScreen().findPreference(PREF_KEY_REPEAT_DAYS);
        repeatPref.setSummary(mTrigDesc.getRepeatDescription());
    }

    private void updateRepeatStatusArray() {
        LinkedHashMap<String, Boolean> repeatList = mTrigDesc.getRepeat();

        for (int i = 0; i < mDays.length; i++) {
            mRepeatStatus[i] = repeatList.get(mDays[i]);
        }
    }

    private Dialog createRepeatSelDialog() {

        updateRepeatStatusArray();
        mRepeatDialog = new AlertDialog.Builder(this).setTitle("Select days")
                .setPositiveButton("Done", this).setNegativeButton("Cancel", this)
                .setMultiChoiceItems(mDays, mRepeatStatus, this).create();

        return mRepeatDialog;
    }

    private Dialog createInvalidTimeAlert() {
        final String msg = "Make sure that the End Time is after the Start Time and that the range does not overlap/touch any other blackouts";

        return new AlertDialog.Builder(this).setTitle("Invalid time settings!")
                .setNegativeButton("Cancel", null).setMessage(msg).create();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_ID_REPEAT_SEL:
                return createRepeatSelDialog();
            case DIALOG_ID_INVALID_TIME_ALERT:
                return createInvalidTimeAlert();
        }

        return null;
    }

    @Override
    public boolean onPreferenceClick(Preference pref) {

        /*
         * if(pref.getKey().equals(PREF_KEY_RANDOMIZE) ||
         * pref.getKey().equals(PREF_KEY_ENABLE_RANGE)) {
         * updateTriggerTimePrefStatus(); } else
         */if (pref.getKey().equals(PREF_KEY_REPEAT_DAYS)) {
            removeDialog(DIALOG_ID_REPEAT_SEL);
            showDialog(DIALOG_ID_REPEAT_SEL);
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

        mRepeatStatus[which] = isChecked;

        int repeatCount = 0;
        for (int i = 0; i < mRepeatStatus.length; i++) {
            if (mRepeatStatus[i]) {
                repeatCount++;
            }
        }

        if (mRepeatDialog != null) {
            if (mRepeatDialog.isShowing()) {
                mRepeatDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(repeatCount != 0);
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        if (which == DialogInterface.BUTTON_POSITIVE) {

            for (int i = 0; i < mDays.length; i++) {
                mTrigDesc.setRepeatStatus(mDays[i], mRepeatStatus[i]);
            }

        }

        dialog.dismiss();
        updateRepeatPrefStatus();
    }
}
