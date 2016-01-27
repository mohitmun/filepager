package com.filepager.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;


public class AppsSender {


	private MainActivity mainActivity;
	float icon_size;
	public AppsSender(MainActivity ma){
		mainActivity=ma;
	}
	class AppHolder
	{
		private List<AppInfo> appinfo;
		private List<ResolveInfo> resolveinfo;
	}
	public List<ResolveInfo> createAppList(){
		
	
	
	final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    
    PackageManager pm=mainActivity.getPackageManager();
    final List<ResolveInfo> pkgAppsList = pm.queryIntentActivities( mainIntent, 0);
 	 
    List<AppInfo> appList = new ArrayList<AppInfo>();
 if(mainActivity.myApp.appList==null) 	 
{ for (ResolveInfo info : pkgAppsList) {
       AppInfo appInfo=new AppInfo();
       
       appInfo.name=info.activityInfo.applicationInfo.loadLabel(pm).toString();
       appInfo.rinfo=info;
       info.activityInfo.name=appInfo.name;
       appInfo.path=info.activityInfo.applicationInfo.publicSourceDir;
       appList.add(appInfo);
}

Collections.sort(appList, new Comparator<AppInfo>() {

	@Override
	public int compare(AppInfo lhs, AppInfo rhs) {
		// TODO Auto-generated method stub
		return lhs.name.compareTo(rhs.name);
	}

});
/*
Collections.sort(pkgAppsList, new Comparator<ResolveInfo>() {

	@Override
	public int compare(ResolveInfo lhs, ResolveInfo rhs) {
		// TODO Auto-generated method stub
		return lhs.activityInfo.name.compareTo(rhs.activityInfo.name);
	}

});	
*/mainActivity.myApp.appList=appList;
}
else{
	appList=mainActivity.myApp.appList;

}  
	return pkgAppsList;
	}
	            
   
}
