//package edu.ucla.cens.mobility.blackout.ui;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
//import android.content.DialogInterface.OnMultiChoiceClickListener;
//import edu.ucla.cens.mobility.blackout.base.TriggerActionDesc;
//import edu.ucla.cens.mobility.blackout.config.TrigUserConfig;
//
//public class TriggerActionSelector 
//			 implements OnClickListener, 
//			 OnMultiChoiceClickListener {
//	
//	public interface OnClickListener {
//		public void onDone(TriggerActionSelector selector, String actDesc);
//	}
//	
//
//	private TriggerActionDesc mActDesc = new TriggerActionDesc();
//	private String[] mActions;
//	private boolean[] mSelected;
//	private TriggerActionSelector.OnClickListener mOnClickListener;
//	private int mTag;
//	private AlertDialog mDialog = null;
//	
//	
//	public TriggerActionSelector(String[] actions, String actDesc) {
//		mActDesc.loadBoolean(actDesc);
//		
//		mActions = new String[actions.length];
//		System.arraycopy(actions, 0, mActions, 0, actions.length);
//		
//		mSelected = new boolean[actions.length];
//		for(int i = 0; i < mSelected.length; i++) {
//			mSelected[i] = mActDesc.hasSurvey(mActions[i]) ? true
//													       : false;
//		}
//	}
//	
//	
//	public TriggerActionSelector setOnClickListener(
//							TriggerActionSelector.OnClickListener listener) {
//		
//		mOnClickListener  = listener;
//		return this;
//	}
//	
//	public TriggerActionSelector setTag(int tag) {
//		mTag = tag;
//		return this;
//	}
//	
//	public int getTag() {
//		return mTag;
//	}
//	
//	public Dialog createDialog(Context context, boolean adminMode) {
//		 AlertDialog.Builder builder = 
//			 			new AlertDialog.Builder(context)
//					   .setTitle("Select surveys")
//					   .setNegativeButton("Cancel", this)
//					   .setMultiChoiceItems(mActions, mSelected, this);
//		 
//		 if(adminMode || TrigUserConfig.editTriggerActions) {
//			 builder.setPositiveButton("Done", this);
//		 }
//		 
//			
//		 mDialog = builder.create();
//		 return mDialog;
//	}
//	
//
//	@Override
//	public void onClick(DialogInterface dialog, int which) {
//		if(which == AlertDialog.BUTTON_POSITIVE) {
//			mActDesc.clearAllSurveys();
//			
//			for(int i = 0; i < mSelected.length; i++) {
//				if(mSelected[i]) {
//					mActDesc.addSurvey(mActions[i]);
//				}
//			}
//			
//			if(mOnClickListener != null) {
//				mOnClickListener.onDone(this, mActDesc.toString());
//			}
//		}
//		
//		dialog.dismiss();
//	}
//
//	@Override
//	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//		mSelected[which] = isChecked;
//	}
//}
