package com.filepager.settings;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.filepager.main.MyApp;
import com.filepager.main.R;
import com.flurry.android.FlurryAgent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;



public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{
   MyApp myApp;
   public static final String KEY_FIRSTNAME="key_firstname";
   public static final String KEY_LASTNAME="key_lastname";
   public static final String KEY_SETDIRECTORY="key_setdirectory";
   public static final String KEY_SERVICE="key_service";
   public static final String KEY_HIDDENFILES="key_showhiddenfiles";
   public static final String KEY_SHOWMYPROFILE="key_showmyprofile";
   SharedPreferences sharedPreferences;
   
   @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       myApp=MyApp.getInstance();
       sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
       addPreferencesFromResource(R.xml.prefs);
         firstname=findPreference("key_firstname");
        firstname.setSummary(myApp.firstname);
        setdirectory=findPreference(KEY_SETDIRECTORY);
        setdirectory.setSummary(MyApp.DIRECTORY);
         lastname=findPreference("key_lastname");
        lastname.setSummary(myApp.lastname);
        /*CustomListPreference directory=(CustomListPreference) findPreference(KEY_DIRECTORY);
        directory.setSummary(myApp.DIRECTORY);
        */
   }
   Preference firstname;
   Preference lastname;
   Preference setdirectory;
	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
		if(key.equals(KEY_FIRSTNAME)){
			firstname.setSummary(arg0.getString(KEY_FIRSTNAME, myApp.firstname));
			myApp.firstname=arg0.getString(KEY_FIRSTNAME, myApp.firstname);
		}
		else if(key.equals(KEY_LASTNAME)){

			lastname.setSummary(arg0.getString(KEY_LASTNAME, myApp.lastname));
			myApp.lastname=arg0.getString(KEY_LASTNAME, myApp.lastname);
		}
		else if(key.equals(KEY_SETDIRECTORY)){
			MyApp.DIRECTORY=arg0.getString(KEY_SETDIRECTORY, MyApp.DIRECTORY);
			setdirectory.setSummary(MyApp.DIRECTORY);
		}
		else if(key.equals(KEY_HIDDENFILES)){
			MyApp.showhidden=arg0.getBoolean(KEY_HIDDENFILES, false);
		}
	}
	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
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
	
}