package org.ohmage.mobility;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.ohmage.logprobe.Log;
import org.ohmage.mobility.blackout.ui.TriggerListActivity;
import org.ohmage.mobility.glue.MobilityInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MobilityControl extends PreferenceActivity
{
	private final static String TAG = "MobilityControl";

	// KEYS FOR PREFERENCES
	private static final String KEY_MOBILITY_ONOFF_PREF = "pref_mobility_onoff";
	private static final String KEY_MOBILITY_SAMPLERATE_PREF = "pref_mobility_samplerate";

	public static final String MOBILITY_ON = "mobility_on";

	NotificationManager mNM;

//	public boolean checkMobilityOff()
//	{
//
//		CheckBoxPreference moPref = (CheckBoxPreference) this.findPreference(KEY_MOBILITY_ONOFF_PREF);
//
//		return moPref.isChecked();
//	}

	@Override
	public void onCreate(Bundle state)
	{
		super.onCreate(state);
		setTitle(getTitle() + " - Settings");
		
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//		Cursor c = MobilityInterface.getMobilityCursor(MobilityControl.this, "0");
//		Log.i(TAG, c.getColumnCount() + " is the column count");
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.control_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_show_summary:
	            startActivity(new Intent(this, MobilitySummary.class));
	            return true;
	        case R.id.menu_dump_data:
	            WritePointsTask task = new WritePointsTask(this);
	            task.execute();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	int rate = 60;
	boolean running = false;
	
	@Override
	public void onResume()
	{
		super.onResume();
		SharedPreferences settings = getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
		rate = settings.getInt(Mobility.SAMPLE_RATE, 60);
		running = settings.getBoolean(MOBILITY_ON, false);
		setPreferenceScreen(createPreferenceHierarchy());
	}

	private static final int DIALOG_IDX_TRIGGERINFO = 1;
	private static final int DIALOG_IDX_NEWUPDATE = 2;
	private static final int DIALOG_IDX_NOUPDATE = 3;
	private static final int DIALOG_IDX_TIMEOUT = 4;
	private static final String KEY_MOBILITY_BLACKOUT_PREF = "pref_mobility_blackout";


	public static View createSpacer(Context c, int height)
	{
		View spacer = new View(c);
		spacer.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, height));
		// spacer.setMinimumHeight(height);
		spacer.setBackgroundColor(Color.argb(200, 226, 226, 226));
		spacer.setPadding(15, 15, 15, 15);

		return spacer;
	}

	public static TableRow createRow(String text1, String text2, int fontSize, int[] pad, Context c)
	{
		TableRow row = new TableRow(c);

		TextView textView1 = new TextView(c);
		textView1.setText(text1);
		textView1.setPadding(pad[0], pad[1], pad[2], pad[3]);
		textView1.setTextSize(fontSize);
		textView1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		textView1.setLayoutParams(new TableRow.LayoutParams(100, LayoutParams.FILL_PARENT));
		// LayoutUtils.Layout.WidthFill_HeightFill.applyTableRowParams(textView1);

		TextView textView2 = new TextView(c);
		textView2.setText(text2);
		textView2.setPadding(pad[0], pad[1], pad[2], pad[3]);
		textView2.setTextSize(fontSize);
		textView2.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		textView2.setTextAppearance(c, android.R.attr.textAppearanceLarge);
		// LayoutUtils.Layout.WidthFill_HeightFill.applyTableRowParams(textView2);
		textView2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		row.addView(textView1);
		row.addView(textView2);

		return row;
	}

	// HANDLE BUTON CLICK EVENTS
	private ProgressDialog pd;

//	@Override
//	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
//	{
//		{
//
//			IntentFilter filter;
//			filter = new IntentFilter(UpdateService.CONFIG_UPDATE_RESULT);
//			mReceiver = new UpdateReceiver();
//			registerReceiver(mReceiver, filter);
//
//			// Initiate Manual Updates
//			Intent servIntent = new Intent(MobilityControl.this, UpdateService.class);
//			servIntent.putExtra(UpdateService.UPDATE_MODE_TYPE, UpdateService.MANUAL_UPDATE);
//
//			// show progress bar until receiving a broadcast (with a cancel
//			// button)
//			pd = ProgressDialog.show(this, "Checking...", "Checking for new surveys...", true, true, mOnCancelWaitListener);
//
//			startService(new Intent(MobilityControl.this, UpdateService.class));
//
//			// Thread thread = new Thread();
//
//			return true;
//		}
//
//		return super.onPreferenceTreeClick(preferenceScreen, preference);
//	}

	// ----start: UpdateService Related


	// ----end; UpdateService Related

	/***
	 * Trigger notification related
	 * 
	 * @param: id - can be used for each individual triggers. then we can use
	 *         this for notification id accumulated triggers can cancel the
	 *         previous trigger with same id (temporarly used 1234)
	 */
	// private void showNotification(int id) {
	// CharSequence text = "You have a survey to take!";
	//
	// // TODO: use this with time triggers
	//
	// // Set the icon, scrolling text and timestamp
	// Notification not = new Notification(R.drawable.diabetes_icon, text,
	// System.currentTimeMillis());
	//
	// // The PendingIntent to launch our activity if the user selects this
	// notification
	// Intent intent = new Intent(this, MainActivity.class);
	// intent.putExtra(SurveyActivity.KEY_NOTIFICATION_PROMPTID, id);
	//
	// Log.e(TAG, "showNotification(): intent " +
	// intent.getIntExtra(SurveyActivity.KEY_NOTIFICATION_PROMPTID, -1));
	//
	// PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
	// 0);
	//
	// String [] grpTitles = PromptHandler.getGroupTitles();
	//
	// // Set the info for the views that show in the notification panel.
	// not.setLatestEventInfo(this, "Take " + grpTitles[id] + " survey.",
	// text, contentIntent);
	//
	// // Send the notification.
	// // We use a string id because it is a unique number. We use it later to
	// cancel.
	// mNM.notify(1234, not);
	// }

	public void cancelNotification(int nId)
	{
		mNM.cancel(nId);
	}

	/***
	 * Handle Start/Stop button events
	 */
	private final OnPreferenceChangeListener mOnCheckBoxChangeListener = new OnPreferenceChangeListener()
	{

		@Override
        public boolean onPreferenceChange(Preference pref, Object val)
		{
			Boolean value = new Boolean(val.toString());
			Integer intValue = 0;
			try
			{
				intValue = new Integer(val.toString());
			} catch (NumberFormatException e)
			{
				// TODO Auto-generated catch block

			}
			String pref_key = pref.getKey();

			/**
			 * CATCHES TURN ON/OFF EVENTS TODO: Turn on/off background tasks
			 * here.
			 */
			if (pref_key.compareTo(KEY_MOBILITY_ONOFF_PREF) == 0)
			{
				Log.v(TAG, "Mobility Change: " + value.booleanValue());
				if (value.booleanValue())
				{
					SharedPreferences settings = MobilityControl.this.getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
					Editor editor = settings.edit();
					editor.putBoolean(MOBILITY_ON, true);
					editor.commit();
//					startService(new Intent(MobilityControl.this, Mobility.class));
					Mobility.start(MobilityControl.this.getApplicationContext());
					
				} 
				else
				{
					SharedPreferences settings = MobilityControl.this.getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
					Editor editor = settings.edit();
					editor.putBoolean(MOBILITY_ON, false);
					editor.commit();
//					stopService(new Intent(MobilityControl.this, Mobility.class));
					Mobility.stop(MobilityControl.this.getApplicationContext());
				}
			} 
			/*
			 * else if (pref_key.compareTo(KEY_TRIGGER_SUPPRESS_ONOFF_PREF) ==
			 * 0) { Log.e(TAG, "Quiet time change: " + value.booleanValue()); if
			 * (value.booleanValue() == true) {
			 * 
			 * SharedPreferences settings =
			 * MobilityControl.this.getSharedPreferences
			 * (TriggerService.TRIGGERS, Context.MODE_PRIVATE); Editor editor =
			 * settings.edit(); editor.putBoolean(QUIET_TIME_ON, true);
			 * editor.commit(); } else { SharedPreferences settings =
			 * MobilityControl
			 * .this.getSharedPreferences(TriggerService.TRIGGERS,
			 * Context.MODE_PRIVATE); Editor editor = settings.edit();
			 * editor.putBoolean(QUIET_TIME_ON, false); editor.commit(); } }
			 */
			else if (pref_key.equals(KEY_MOBILITY_SAMPLERATE_PREF))
			{
				SharedPreferences settings = getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
				Editor editor = settings.edit();
				editor.putInt(Mobility.SAMPLE_RATE, intValue);
				editor.commit();
				if (settings.getBoolean(MOBILITY_ON, false))
				{
//					Intent MobilityServiceIntent = new Intent(MobilityControl.this, MobilityService.class);
//					stopService(MobilityServiceIntent);
//					startService(MobilityServiceIntent);
					Mobility.stop(MobilityControl.this);
					Mobility.start(MobilityControl.this);
				}
				
			}
			else if (pref_key.equals(KEY_MOBILITY_BLACKOUT_PREF))
			{
				Intent intent = new Intent(MobilityControl.this, TriggerListActivity.class);
//				intent.setAction("android.intent.action.BLACKOUT");
		        MobilityControl.this.startActivity(intent);
			}
			else
				Log.w(TAG, "That wasn't recognized.");
			/*else if (pref_key.equals(KEY_UPDATES_ONOFF_PREF))
			{
				Log.e(TAG, "Auto-Updates Change: " + value.booleanValue());
				if (value.booleanValue())
				{
					SharedPreferences settings = MobilityControl.this.getSharedPreferences(UpdateService.UPDATE_ONOFF, Context.MODE_PRIVATE);
					Editor editor = settings.edit();
					editor.putBoolean(UPDATES_ON, true);
					editor.commit();
					Intent servIntent = new Intent(MobilityControl.this, UpdateService.class);
					servIntent.putExtra(UpdateService.UPDATE_MODE_TYPE, UpdateService.AUTO_UPDATE);
					startService(new Intent(MobilityControl.this, UpdateService.class));
				} else
				{
					SharedPreferences settings = MobilityControl.this.getSharedPreferences(UpdateService.UPDATE_ONOFF, Context.MODE_PRIVATE);
					Editor editor = settings.edit();
					editor.putBoolean(UPDATES_ON, false);
					editor.commit();
					stopService(new Intent(MobilityControl.this, UpdateService.class));
				}
			}*/
			return true;
		}
	};

	private final OnPreferenceClickListener mOnClickListener = new OnPreferenceClickListener()
	{

		@Override
        public boolean onPreferenceClick(Preference preference)
		{
			String pref_key = preference.getKey();
			if (pref_key.equals(KEY_MOBILITY_BLACKOUT_PREF))
			{
				Intent intent = new Intent(MobilityControl.this, /*DataSaverActivity.class);//*/TriggerListActivity.class);
//				intent.setAction("android.intent.action.BLACKOUT");
		        MobilityControl.this.startActivity(intent);
			}
			return false;
		}
	};
	
	
	/****
	 * Preference Tabs
	 */

	private PreferenceScreen createPreferenceHierarchy()
	{
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
		// createOnOffGroup(root);
		createMobilityGroup(root);
		// createUpdatesGroup(root);
		return root;
	}

	/****
	 * CAT 1: mobility
	 */

	private void createMobilityGroup(PreferenceScreen root)
	{
		Log.v(TAG, "Creating mobility group");
		PreferenceCategory mobilityPrefCat = new PreferenceCategory(this);
		mobilityPrefCat.setTitle("Mobility");// is " + running);
		root.addPreference(mobilityPrefCat);

		// Turn on/off
		CheckBoxPreference moOnOffPref = new CheckBoxPreference(this);
		moOnOffPref.setKey(KEY_MOBILITY_ONOFF_PREF);
		moOnOffPref.setTitle("Mobility");
		moOnOffPref.setSummary("Mobility is currently off.");
		moOnOffPref.setSummaryOn("Mobility is currently on.");
		moOnOffPref.setOnPreferenceChangeListener(mOnCheckBoxChangeListener);
		moOnOffPref.setDefaultValue(running);
		
		mobilityPrefCat.addPreference(moOnOffPref);
		moOnOffPref.setChecked(running);
		// Sampling Rate
		DisplayValueListPreference moSampPref = new DisplayValueListPreference(this);
		moSampPref.setKey(KEY_MOBILITY_SAMPLERATE_PREF);
		moSampPref.setTitle("Sampling rate");
		moSampPref.setEntries(R.array.pref_mobility_rate_options);
		moSampPref.setEntryValues(R.array.pref_mobility_rate_options_values);
		moSampPref.setDefaultValue("" + rate);
		
		moSampPref.setDialogTitle("Set sample rate");
		moSampPref.setDependency(KEY_MOBILITY_ONOFF_PREF);
		moSampPref.setOnPreferenceChangeListener(mOnCheckBoxChangeListener);
		mobilityPrefCat.addPreference(moSampPref);
		moSampPref.setValue(rate + "");
		// Blackout
		Preference blackoutPref = new Preference(this);
		blackoutPref.setKey(KEY_MOBILITY_BLACKOUT_PREF);
		blackoutPref.setTitle("Blackout times");
		blackoutPref.setTitle("Tap here to set blackout times");
//		blackoutPref.setDependency(KEY_MOBILITY_ONOFF_PREF);
		blackoutPref.setOnPreferenceClickListener(mOnClickListener);
		mobilityPrefCat.addPreference(blackoutPref);
	}

	

	/****
	 * Extended Preferences : for displaying selected value
	 * 
	 */
	public class CheckBoxPreferenceWithNotDependency extends CheckBoxPreference
	{
		public CheckBoxPreferenceWithNotDependency(Context context)
		{
			super(context);
		}

		private String mDependencyKey = null;

		@Override
		protected void onAttachedToActivity()
		{
			super.onAttachedToActivity();
			if (mDependencyKey != null)
			{
				Log.v(TAG, "onAttachedToActivity(): " + mDependencyKey);
				super.setDependency(mDependencyKey);
			}
		}

		@Override
		public void setDependency(String dependencyKey)
		{
			Log.v(TAG, "setDependency: " + dependencyKey);
			mDependencyKey = dependencyKey;
		}

		@Override
		public void onDependencyChanged(Preference dependency, boolean disableDependent)
		{
			super.onDependencyChanged(dependency, !disableDependent);
		}

	}

	public class DisplayValueListPreference extends ListPreference
	{

		private String mDependencyKey = null;

		public DisplayValueListPreference(Context context)
		{
			super(context);
			setWidgetLayoutResource(R.layout.new_ui_pref_widget_value_preview);
		}

		@Override
		protected void onBindView(View view)
		{
			super.onBindView(view);

			final TextView myTextView = (TextView) view.findViewById(R.id.mypreference_widget);
			if (myTextView != null)
				myTextView.setText(this.getEntry());
		}

		@Override
		protected void onAttachedToActivity()
		{
			super.onAttachedToActivity();
			if (mDependencyKey != null)
			{
				Log.v(TAG, "onAttachedToActivity(): " + mDependencyKey);
				super.setDependency(mDependencyKey);
			}
		}

		@Override
		public void setDependency(String dependencyKey)
		{
			Log.v(TAG, "setDependency: " + dependencyKey);
			mDependencyKey = dependencyKey;
		}

		@Override
		public void onDependencyChanged(Preference dependency, boolean disableDependent)
		{
			super.onDependencyChanged(dependency, !disableDependent);
		}

		@Override
		protected void onDialogClosed(boolean positiveResult)
		{
			super.onDialogClosed(positiveResult);
			notifyChanged();
		}

		@Override
		public void onClick()
		{
			Log.v(TAG, "checkbox: onClick " + this.isEnabled());
			super.onClick();
		}
	}
/*
	public class TimePickPreference extends EditTextPreference
	{

		// private EditText mUsername;
		// private EditText mPassword;

		// private String _username = null;
		// private String _password = null;

		private String mDependencyKey = null;

		private TimePicker mStrTimeView;
		private TimePicker mEndTimeView;

		private int _str_hh = -1;
		private int _str_mm = -1;
		private int _end_hh = -1;
		private int _end_mm = -1;

		public int getStrHh()
		{
			return _str_hh;
		}

		public int getStrMm()
		{
			return _str_mm;
		}

		public int getEndHh()
		{
			return _end_hh;
		}

		public int getEndMm()
		{
			return _end_mm;
		}

		public TimePickPreference(Context context)
		{
			super(context);
			setWidgetLayoutResource(R.layout.new_ui_pref_widget_doublerow_value_preview);
		}

		@Override
		protected void onBindDialogView(View view)
		{
			super.onBindDialogView(view);

			Log.d(TAG, "onBindDialogView()");

			mStrTimeView = (TimePicker) view.findViewById(R.id.timepick_dialog_strtime);
			mEndTimeView = (TimePicker) view.findViewById(R.id.timepick_dialog_endtime);

			// mUsername = (EditText) view.findViewById(R.id.username_edit);
			// mPassword = (EditText) view.findViewById(R.id.password_edit);

			this.getText();
			Log.d(TAG, "onBindDialogView() getText=" + this.getText());

			// mUsername.setText(_username);
			// mPassword.setText(_password);

			mStrTimeView.setCurrentHour(_str_hh);
			mStrTimeView.setCurrentMinute(_str_mm);
			mEndTimeView.setCurrentHour(_end_hh);
			mEndTimeView.setCurrentMinute(_end_mm);
		}

		@Override
		protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
		{
			if (restoreValue)
			{ // when there is a stored value, use that.
				super.onSetInitialValue(restoreValue, defaultValue);
			} else
			{ // when no stored value, use the provided default value
				StrEndTimeParcelable u = (StrEndTimeParcelable) defaultValue;
				super.onSetInitialValue(restoreValue, u.xml());
			}
		}

		@Override
		protected void onBindView(View view)
		{
			super.onBindView(view);

			final TextView firstRowView = (TextView) view.findViewById(R.id.pref_widget_doublerow_firstrow);
			final TextView secondRowView = (TextView) view.findViewById(R.id.pref_widget_doublerow_secondrow);

			StrEndTimeParcelable u = new StrEndTimeParcelable(this.getText());

			// _username = u.username;
			// _password = u.password;
			_str_hh = u.str_hh;
			_str_mm = u.str_mm;
			_end_hh = u.end_hh;
			_end_mm = u.end_mm;

			if (firstRowView != null && secondRowView != null)
			{
				// myTextView.setText(this.getText());
				// myTextView.setText("ID:" + _username);
				StringBuilder sb = new StringBuilder();
				Formatter f = new Formatter(sb, Locale.US);
				String s_str_hh = f.format("%2d", toAmPm(_str_hh)).toString();
				sb = new StringBuilder();
				f = new Formatter(sb, Locale.US);
				String s_str_mm = f.format("%02d", _str_mm).toString();
				sb = new StringBuilder();
				f = new Formatter(sb, Locale.US);
				String s_end_hh = f.format("%2d", toAmPm(_end_hh)).toString();
				sb = new StringBuilder();
				f = new Formatter(sb, Locale.US);
				String s_end_mm = f.format("%02d", _end_mm).toString();
				f.close();

				Log.d(TAG, "strtime=" + s_str_hh + ":" + s_str_mm + " " + isAmPm(_str_hh));
				Log.d(TAG, "endtime=" + s_end_hh + ":" + s_end_mm + " " + isAmPm(_end_hh));

				firstRowView.setText(s_str_hh + ":" + s_str_mm + " " + isAmPm(_str_hh));
				secondRowView.setText("- " + s_end_hh + ":" + s_end_mm + " " + isAmPm(_end_hh));
			}
		}

		private int toAmPm(int hh)
		{
			if (hh == 0)
				return 12;
			else if (hh > 0 && hh <= 12)
				return hh;
			else if (hh > 12 && hh < 24)
				return hh - 12;
			else
				return 12;
		}

		private String isAmPm(int hh)
		{
			if (hh >= 0 && hh < 12)
				return "AM";
			else
				return "PM";
		}

		@Override
		protected void onAttachedToActivity()
		{
			super.onAttachedToActivity();
			if (mDependencyKey != null)
			{
				if (DEBUG)
					Log.d(TAG, "onAttachedToActivity(): " + mDependencyKey);
				super.setDependency(mDependencyKey);
			}
		}

		@Override
		public void setDependency(String dependencyKey)
		{
			if (DEBUG)
				Log.d(TAG, "setDependency: " + dependencyKey);
			mDependencyKey = dependencyKey;
		}

		@Override
		public void onDependencyChanged(Preference dependency, boolean disableDependent)
		{
			super.onDependencyChanged(dependency, !disableDependent);
		}

		@Override
		protected void onDialogClosed(boolean positiveResult)
		{
			super.onDialogClosed(positiveResult);

			if (positiveResult)
			{
				// _username = mUsername.getText().toString();
				// _password = mPassword.getText().toString();
				_str_hh = mStrTimeView.getCurrentHour();
				_str_mm = mStrTimeView.getCurrentMinute();
				_end_hh = mEndTimeView.getCurrentHour();
				_end_mm = mEndTimeView.getCurrentMinute();

				Log.d(TAG, "onDialogClosed:" + _str_hh + "," + _str_mm + "," + _end_hh + "," + _end_mm);

				// StrEndTimeParcelable u = new StrEndTimeParcelable(_username,
				// _password);
				StrEndTimeParcelable u = new StrEndTimeParcelable(_str_hh, _str_mm, _end_hh, _end_mm);
				this.setText(u.xml());
				SharedPreferences settings = MobilityControl.this.getSharedPreferences("triggers", Context.MODE_PRIVATE);
				Editor editor = settings.edit();
				editor.putString(this.getTitle().toString(), _str_hh + ":" + _str_mm + "-" + _end_hh + ":" + _end_mm);
				editor.commit();
			}

			// if (DEBUG) Log.d(TAG, "username=" +
			// mUsername.getText().toString());
			// if (DEBUG) Log.d(TAG, "password=" +
			// mPassword.getText().toString());

			notifyChanged();
		}

		@Override
		public void onClick()
		{
			Log.e(TAG, "checkbox: onClick " + this.isEnabled());
			super.onClick();
		}

	}*/

    public static class WritePointsTask extends AsyncTask<Void, Void, Boolean> {

        private final Context mContext;

        private static final String[] PROJECTION = new String[] {
                MobilityInterface.KEY_ID + " as _id", MobilityInterface.KEY_MODE,
                MobilityInterface.KEY_TIME
        };

        public WritePointsTask(Context context) {
            mContext = context.getApplicationContext();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                File myFile = new File(Environment.getExternalStorageDirectory().getPath(),
                        "mobility_dump_" + System.currentTimeMillis() + ".txt");
                myFile.createNewFile();
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

                Cursor points = mContext.getContentResolver().query(MobilityInterface.CONTENT_URI,
                        PROJECTION, MobilityInterface.KEY_USERNAME + "=?", new String[] {
                            Utilities.getUserName(mContext)
                        }, "time");

                while (points.moveToNext()) {
                    myOutWriter.append(points.getString(0) + "," + points.getString(1) + ","
                            + points.getString(2) + "\n");

                }

                myOutWriter.close();
                fOut.close();
            } catch (IOException e) {
                Log.e(TAG, "Error saving mobility data to sdcard", e);
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result)
                Toast.makeText(mContext, R.string.data_dump_finished, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(mContext, R.string.data_dump_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
