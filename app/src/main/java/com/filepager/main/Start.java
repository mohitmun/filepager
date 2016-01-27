package com.filepager.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.filepager.settings.SettingsActivity;
import com.filepager.utils.IntentConstants;
import com.flurry.android.FlurryAgent;

public class Start extends SherlockActivity implements OnClickListener{

	SharedPreferences name;
	private Button moveon;  
	private EditText fname;
	private EditText lname;
	private String ffname;
	private String llname;
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		FlurryAgent.onStartSession(this, "KH8CNHBS69ZTMWG8B8QC");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.name);
		name=PreferenceManager.getDefaultSharedPreferences(this);
		ffname = "";
		llname = "";
		moveon = (Button)findViewById(R.id.button1);
		fname = (EditText)findViewById(R.id.editText1);
		lname = (EditText)findViewById(R.id.editText2);
		moveon.setOnClickListener(this);
	
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	public void onClick(View v){
	switch(v.getId())	
	{
	case R.id.button1:
		ffname = fname.getText().toString();
		llname = lname.getText().toString();
		
		if(ffname == "" || llname == ""){
			Toast.makeText(getApplicationContext(), "enter your name", 1000);			
		}
		else{
			name.edit().putString(SettingsActivity.KEY_FIRSTNAME, ffname).commit();
			name.edit().putString(SettingsActivity.KEY_LASTNAME, llname).commit();
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}
	finish();
	break;
	}
	}

}
