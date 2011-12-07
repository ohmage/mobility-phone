package edu.ucla.cens.mobility.blackout.notif;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import edu.ucla.cens.mobility.R;
import edu.ucla.cens.mobility.blackout.config.NotifConfig;
import edu.ucla.cens.mobility.blackout.utils.TrigTextInput;

/*
 * Activity to edit the notification description visually. 
 * Currently, this activity is called by the trigger list 
 * activity to edit the global notification description. 
 * It can also be called (without any change) to edit the 
 * notification description associated with individual trigger
 * if required in the future. 
 */
public class NotifEditActivity extends ListActivity 
							   implements OnClickListener {

	private static final String DEBUG_TAG = "TriggerFramework";
	
	public static final String KEY_NOTIF_CONFIG = "notif_config";
	private static final String KEY_SAVE_LIST_DATA = "list_data";
	
	private static final int LIST_POS_DURATION = 0;
	private static final int LIST_POS_SUPPRESSION = 1;
	private static final int LIST_POS_REPEAT_HEADER = 2;
	private static final int LIST_POS_REPEAT_START = 3;
	
	//Minimum value allowed for notification duration
	private static final int DURATION_MIN = 1;
	//Minimum value allowed for suppression window
	private static final int SUPPRESSION_MIN = 0; //Disables suppression
	
	private ArrayList<Integer> mListData = null;
	private NotifDesc mNotifDesc;
	private ArrayAdapter<Integer> mListAdapter;
	private Dialog mDialog = null;

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Get the notification description from the intent
		Intent intent = getIntent();
		String desc = intent.getStringExtra(KEY_NOTIF_CONFIG);
		if(desc == null) {
			Log.e(DEBUG_TAG, "NotifEditActivity: No notification " +
					"description passed. Exiting...");
			
			finish();
			return;
		}
		
		//Parse and load the description
		mNotifDesc = new NotifDesc();
		if(!mNotifDesc.loadString(desc)) {
			Log.e(DEBUG_TAG, "NotifEditActivity: Failed to parse the " +
					"notification description passed. Exiting...");
			
			finish();
			return;
		}
		
		setContentView(R.layout.trigger_notif_editor);
		
		Button b = (Button) findViewById(R.id.notif_edit_done);
		b.setOnClickListener(this);
		b = (Button) findViewById(R.id.notif_edit_cancel);
		b.setOnClickListener(this);
		
		//Update the GUI with the data
		initializeListData();
		setupListAdaptor();
		validateDataAndUpdateView();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		
		out.putIntegerArrayList(KEY_SAVE_LIST_DATA, mListData);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		
		ArrayList<Integer> savedArray = state.getIntegerArrayList(KEY_SAVE_LIST_DATA);
		if(mListData != null && savedArray != null) {
			mListData.clear();
			mListData.addAll(savedArray);
		}
		
		mListAdapter.notifyDataSetChanged();
	}

	private void initializeListData() {
		mListData = new ArrayList<Integer>();
		//Add duration and suppression at the top
		mListData.add(LIST_POS_DURATION, mNotifDesc.getDuration());
		mListData.add(LIST_POS_SUPPRESSION, mNotifDesc.getSuppression());
		//Add something at the repeat header pos to make the array index
		//consistent
		mListData.add(LIST_POS_REPEAT_HEADER, 0);
		//Now add all the repeat items at the end
		mListData.addAll(mNotifDesc.getSortedRepeats());
	}

	private void setupListAdaptor() {
	
		mListAdapter = new ArrayAdapter<Integer>(this, R.id.repeat_list_text, 
												 mListData) {
			
			@Override
			public boolean isEnabled(int pos) {

				return (pos == LIST_POS_REPEAT_HEADER) ? false : true;
			}

		
			@Override
			public boolean areAllItemsEnabled() {
				return false;
			}
			
			private View getTwoLineView(LayoutInflater inflater, int pos) {
				View view = inflater.inflate(R.layout.trigger_two_line_list_item, 
												null);
				
				TextView tvTitle = (TextView) view.findViewById(R.id.text1);
				
				if(pos == LIST_POS_DURATION) {
					tvTitle.setText("Notification duration");
				}
				else {
					tvTitle.setText("Suppression window");
				}
				
				TextView tvSummary = (TextView) view.findViewById(R.id.text2);
				tvSummary.setText(mListData.get(pos) + " minutes");
				
				return view;
			}
			
			private View getRepeatHeaderView(LayoutInflater inflater, int pos) {
				View view = inflater.inflate(R.layout.trigger_add_new, null);
				TextView tv = (TextView) view.findViewById(R.id.add_new_label);
				tv.setText("Reminders");
				
				ImageButton bAdd = (ImageButton) view.findViewById(R.id.button_add_new);
				bAdd.setOnClickListener(NotifEditActivity.this);
				
				boolean enable = true;
				if((mListData.get(LIST_POS_DURATION) - 1) < 
				   mNotifDesc.getMinAllowedRepeat()) {
					
					enable = false;
				}
				bAdd.setEnabled(enable);
				
				return view;
			}
			
			private View getRepeatItemView(LayoutInflater inflater, int pos) {
				View view = inflater.inflate(R.layout.trigger_notif_edit_repeat_row, 
												null);
				
				ImageButton ib = (ImageButton) view.findViewById(
									R.id.button_remove_repeat);
				
				ib.setFocusable(false);
				ib.setTag(new Integer(pos));
				ib.setOnClickListener(NotifEditActivity.this);
				
				TextView tv = (TextView) view.findViewById(
						 				R.id.repeat_list_text);
				
				tv.setText(String.valueOf(mListData.get(pos)) + " minutes");
				return view;
			}
		
			@Override
			public View getView(int pos, View convertView, ViewGroup parent) {
			
				LayoutInflater inflater = (LayoutInflater)getSystemService(
										Context.LAYOUT_INFLATER_SERVICE);
				View view;
				
				if(pos == LIST_POS_DURATION || pos == LIST_POS_SUPPRESSION) {
				
					view = getTwoLineView(inflater, pos);
				}
				
				else if(pos == LIST_POS_REPEAT_HEADER) {
					
					view = getRepeatHeaderView(inflater, pos);
				}
				
				else {
				
					view = getRepeatItemView(inflater, pos);
				}
				
				return view;
			}
		};
		
		getListView().setAdapter(mListAdapter);
	}
	
	private void validateDataAndUpdateView() {
		
		Iterator<Integer> i = mListData.iterator();
		
		//Skip the initial items until repeat
		int pos = 0;
		while(i.hasNext() && pos < LIST_POS_REPEAT_START) {
			pos++;
			i.next();
		}
		
		while(i.hasNext()) {
			//Remove all items which is greater than the allowed value
			//which is max(duration) - 1
			if(i.next() > (mListData.get(LIST_POS_DURATION) - 1)) {
				i.remove();
			}
		}
		
		mListAdapter.notifyDataSetChanged();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		
		if(id >= mListData.size()) {
			return null;
		}
		
		String title = "";
		String text = "";
		int max = 0;
		int min = 0;
		
		switch(id) {
		
		case LIST_POS_DURATION: //Edit notification duration
			title = "Notification duration \n(1 - " + 
			   				NotifConfig.maxDuration + ")";
 	
			text = String.valueOf(mListData.get(LIST_POS_DURATION));
			max = NotifConfig.maxDuration;
			min = DURATION_MIN;
			break;
		
		case LIST_POS_SUPPRESSION: //Edit suppression window
			title = "Suppression window \n(0 - " + 
			   		NotifConfig.maxSuppression + ")";

			text = String.valueOf(mListData.get(LIST_POS_SUPPRESSION));
			max = NotifConfig.maxSuppression;
			min = SUPPRESSION_MIN;
			break;
			
		case LIST_POS_REPEAT_HEADER: //Repeat header, ignore. Nothing
									//to edit here.
			return null;
			
		default: //Any other id would correspond to a repeat value
			
			text = String.valueOf(mListData.get(id));
			min = mNotifDesc.getMinAllowedRepeat();
			max = mListData.get(LIST_POS_DURATION) - 1;
			
			title = "Set reminder \n(" + min + " - " + max + ")";
			
			if(min == max) {
				//Nothing to edit. Do nothing
				return null;
			}
		}
		
		//Launch the text edit dialog
		TrigTextInput ti = new TrigTextInput(this);
		ti.setTitle(title);
		ti.setText(text);
		ti.setNumberMode(true);
		ti.setNumberModeRange(min, max);
		ti.setTag(new Integer(id));
		ti.setPositiveButtonText("Done");
		ti.setNegativeButtonText("Cancel");
		ti.setOnClickListener(new TrigTextInput.onClickListener() {
			
			public void onClick(TrigTextInput textInput, int which) {

				if(which == TrigTextInput.BUTTON_POSITIVE) {
					int listPos = (Integer) textInput.getTag();
					if(listPos < mListData.size()) {
						mListData.set(listPos, 
									  Integer.valueOf(textInput.getText()));
		
						//Validate the data
						validateDataAndUpdateView();
					}
				}
			}
		});
		
		mDialog = ti.createDialog();
		return mDialog;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int pos, long id) {
		super.onListItemClick(l, v, pos, id);
		
		if(pos != LIST_POS_REPEAT_HEADER) {
			//Remove the dialog first to force onCreateDialog().
			//We need to prepare the dialog every time based
			//on the list index. This is possible without this
			//approach in future API levels (by passing a bundle)
			//but not in 1.6
			removeDialog(pos);
			showDialog(pos);
		}
	}
	
	public void onClick(View v) {
		//Ignore all clicks on the list once a dialog has been 
		//created. This is needed to guard against quikly pressing
		//the buttons multiple times.
		if(mDialog != null && mDialog.isShowing()) {
			return;
		}
		
		switch(v.getId()) {
		
		case R.id.button_remove_repeat: //The 'minus' button
			int iList = (Integer) v.getTag();
			mListData.remove(iList);
			mListAdapter.notifyDataSetChanged();
			break;
			
		case R.id.button_add_new: //The 'plus' button
			
			//Find a new value for the repeat and add a 
			//repeat item based on it. This is done by simply 
			//adding the default repeat value to the maximum 
			//value in the list and then bounding it using the
			//maximum repeat value (which is max(duration) - 1)
			int maxRepeat = 0;
			for(int i = LIST_POS_REPEAT_START; i < mListData.size(); i++) {
				int repeat = mListData.get(i);
				if(repeat > maxRepeat) {
					maxRepeat = repeat;
				}
			}
	
			int duration = mListData.get(LIST_POS_DURATION);
			maxRepeat += NotifConfig.defaultRepeat;
			
			if(maxRepeat >= duration) {
				maxRepeat = duration - 1;
			}
			
			//Add the new repeat item to the list
			mListData.add(LIST_POS_REPEAT_START, new Integer(maxRepeat));
			mListAdapter.notifyDataSetChanged();
			break;
			
		case R.id.notif_edit_done: //Done with editing the notification settings
			//TODO validate
			
			//Copy the values from the UI to the description class, 
			//convert to string and then send back the result
			mNotifDesc.setDuration(mListData.get(LIST_POS_DURATION));
			mNotifDesc.setSuppression(mListData.get(LIST_POS_SUPPRESSION));
			
			if(LIST_POS_REPEAT_START < mListData.size()) {
				mNotifDesc.setRepeats(
						mListData.subList(LIST_POS_REPEAT_START, mListData.size()));
			}
			else {
				mNotifDesc.setRepeats(new ArrayList<Integer>());
			}
			
			Intent intent = new Intent();
			intent.putExtra(KEY_NOTIF_CONFIG, mNotifDesc.toString());
			setResult(0, intent);
			
			finish();
			break;
			
		case R.id.notif_edit_cancel: //Canceled editing
			finish();
			break;
		}
		
	}
}
