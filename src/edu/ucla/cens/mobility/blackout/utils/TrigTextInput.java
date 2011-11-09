package edu.ucla.cens.mobility.blackout.utils;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import edu.ucla.cens.mobility.R;

public class TrigTextInput 
			implements View.OnClickListener {

	public static int BUTTON_POSITIVE = 0;
	public static int BUTTON_NEGATIVE = 1;
	
	private EditText mEditText;
	private AlertDialog mDialog;
	private Context mContext;
	private Object mTag;
	private boolean mAllowEmptyText = true;
	private int mMax;
	private int mMin;
	private boolean mEnabledRange = false;
	private onClickListener mOnClickListener = null;
	private onTextChangedListener mTextChangedListener = null;
	private String mTitle = "";
	private View mContentView;
	

	public interface onClickListener {
		public abstract void onClick(TrigTextInput textInput, int which);
	}
	
	public interface onTextChangedListener {
		public abstract boolean onTextChanged(TrigTextInput textInput, 
											  String text);
	}
	
	public TrigTextInput(Context context) {

		mContext = context;	
		createContentView();
	}
	
	public void setOnClickListener(
							TrigTextInput.onClickListener listener) {
		
		mOnClickListener = listener;
	}
	
	public void setOnTextChangedListener(
							TrigTextInput.onTextChangedListener listener) {
		
		mTextChangedListener = listener;
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	public void setPositiveButtonText(String text) {
		Button b = (Button) mContentView.findViewById(R.id.text_input_postive);
		b.setText(text);
	}
	
	public void setNegativeButtonText(String text) {
		Button b = (Button) mContentView.findViewById(R.id.text_input_negative);
		b.setText(text);
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		if(id != R.id.text_input_postive && 
		   id != R.id.text_input_negative) {
			return;
		}
		
		int which = (id == R.id.text_input_postive) ?
					BUTTON_POSITIVE :
						BUTTON_NEGATIVE;
		
		if(mOnClickListener != null) {
			mOnClickListener.onClick(this, which);
		}

		mDialog.dismiss();
		
	}
	
	private void updateViewState(String newText) {
		if(mDialog == null) {
			return;
		}
		
		String text = newText.trim();
		
		boolean buttonStatus = true;
		if(mTextChangedListener != null && 
		   !mTextChangedListener.onTextChanged(TrigTextInput.this, 
					   						   text)) {
				
			buttonStatus = false;
		}
		else if(!mAllowEmptyText || mEnabledRange) {

			if(text.length() == 0) {
				buttonStatus = false;
			}
			else if(mEnabledRange) {
				
				int val = Integer.valueOf(text);
				if( val < mMin || val > mMax) {
					
					buttonStatus = false;
				}
			}
		}
		
		Button b = (Button) mContentView.findViewById(R.id.text_input_postive);
		b.setEnabled(buttonStatus);
	}
	
	private void createContentView() {
		
		LayoutInflater inf = (LayoutInflater) mContext.getSystemService(
 								Context.LAYOUT_INFLATER_SERVICE);
		mContentView = inf.inflate(R.layout.trigger_text_input, null);
		mEditText = (EditText) mContentView.findViewById(R.id.text_input_editor);
				
		mEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				updateViewState(s.toString());
			}
		});
		
		Button pos = (Button) mContentView.findViewById(R.id.text_input_postive);
		pos.setOnClickListener(this);
		
		Button neg = (Button) mContentView.findViewById(R.id.text_input_negative);
		neg.setOnClickListener(this);
	}
	
	public void setNumberMode(boolean enable) {
		if(enable) {
			mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
		}
		else {
			mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
		}
	}
	
	public void setPasswordMode(boolean enable) {
		if(enable) {
			mEditText.setTransformationMethod(
					new android.text.method.PasswordTransformationMethod());
		}
		else {
			mEditText.setTransformationMethod(
					new android.text.method.SingleLineTransformationMethod());
		}
	}
	
	public void setAllowEmptyText(boolean enable) {
		mAllowEmptyText = enable;
	}
	
	public String getText() {
		return mEditText.getText().toString();
	}
	
	public void setText(String text) {
		mEditText.setText(text);
	}
	
	public void setTag(Object tag) {
		mTag = tag;
	}
	
	public Object getTag() {
		return mTag;
	}
	
	public void setNumberModeRange(int min, int max) {
		if(min <= max) {
			mMax = max;
			mMin = min;
			mEnabledRange = true;
		}
	}
	
	public Dialog createDialog() {
		
		mDialog = new AlertDialog.Builder(mContext)
				  .setView(mContentView)
				  .setCancelable(true)
				  .setTitle(mTitle)
				  .create();
	
		updateViewState(mEditText.getText().toString());
		return mDialog;
	}
	
	public Dialog showDialog() {
		if(mDialog == null) {
			createDialog();
		}

		mDialog.show();
		return mDialog;
	}
}
