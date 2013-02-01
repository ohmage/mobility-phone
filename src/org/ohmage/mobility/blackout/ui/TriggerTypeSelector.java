package org.ohmage.mobility.blackout.ui;
//package edu.ucla.cens.mobility.blackout.ui;
//
//
//import java.util.Collection;
//
//import android.app.Dialog;
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//import edu.ucla.cens.mobility.R;
//import edu.ucla.cens.mobility.blackout.Blackout;
//import edu.ucla.cens.mobility.blackout.base.BlackoutList;
//
//public class TriggerTypeSelector extends Dialog {
//
//	private Context mContext;
//	private BlackoutList mTrigTypeMap;
//	private OnClickListener mClickListener; 
//	private OnListItemChangeListener mItemChangeListener; 
//	ArrayAdapter<String> mAdapter;
//	
//	public interface OnClickListener {
//		public void onClick(/*String trigType*/); 
//	}
//	
//	public interface OnListItemChangeListener {
//		public boolean onAddItem(String trigType); 
//	}
//	
//	public void setOnClickListener(TriggerTypeSelector.OnClickListener listener) {
//		mClickListener = listener;
//	}
//	
//	public void setOnListItemChangeListener(
//				TriggerTypeSelector.OnListItemChangeListener listener) {
//		mItemChangeListener = listener;
//	}
//	
//	public TriggerTypeSelector(Context context) {
//		super(context);
//		
//		initialize(context);
//	}
//	
//	protected TriggerTypeSelector(Context context, boolean cancelable,
//			OnCancelListener cancelListener) {
//		super(context, cancelable, cancelListener);
//
//		initialize(context);
//	}
//	
//	@Override
//	protected void onStart() {
//		mAdapter.clear();
//		
////		Collection<String> types = mTrigTypeMap.getAllTriggerTypes();
////		for(String type : types) {
////			if(mItemChangeListener != null) {
////				if(!mItemChangeListener.onAddItem(type)) {
////					continue;
////				}
////			}
////			
////			mAdapter.add(type);
////		}
//		
//		super.onStart();
//	}
//	
//	private void initialize(Context context) {
//		mContext = context;
//		mTrigTypeMap = new BlackoutList();
//		
//		setContentView(R.layout.trigger_type_picker);
//		
//		setCancelable(true);
//		
//		mAdapter = new ArrayAdapter<String>(context, 
//												R.id.label_trigger_type) {
//			@Override
//			public View getView(int pos, View convertView, ViewGroup parent) {
//				LayoutInflater inf = (LayoutInflater) 
//					mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				
//				View v = inf.inflate(R.layout.trigger_type_picker_row, null);
//				
//				TextView tv = (TextView) v.findViewById(R.id.label_trigger_type);
//				tv.setText(mAdapter.getItem(pos));
//
//				
//				ImageView iv = (ImageView) v.findViewById(R.id.icon_trigger_type);
//				iv.setImageResource(new Blackout()//);mTrigTypeMap.getTrigger(mAdapter.getItem(pos))
//												.getIcon());
//				return v;
//			}
//		};
//		
//		ListView lv = (ListView) findViewById(R.id.trigger_type_list);
//		lv.setAdapter(mAdapter);
//		
//		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View v, int pos,
//													long arg3) {
//				
//				/*if(mClickListener != null) {
//					mClickListener.onClick(mAdapter.getItem(pos));
//				}*/
//				
//				dismiss();
//			}
//			
//		});
//	}
//}
