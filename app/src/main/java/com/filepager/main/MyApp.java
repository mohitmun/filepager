package com.filepager.main;


import java.util.HashMap;
import java.util.List;

import com.filepager.sql.DatabaseHandler;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;

public class MyApp extends Application {

	public static MyApp getInstance() {
		// TODO Auto-generated method stub
		return myApp;
	}
	private static MyApp myApp;
	public DatabaseHandler db;
	@Override
	
	public void onCreate() {
		super.onCreate();
		myApp=this;
		db=new DatabaseHandler(this);
		isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
 	}
	public static boolean isDebuggable;
static public boolean showhidden;
	public String firstname;
public String lastname;
public static String DIRECTORY;
public List<AppInfo> appList;

}
