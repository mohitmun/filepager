package com.filepager.main;
 
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;


import com.filepager.afilechooser.FileChooserActivity;
import com.filepager.main.FileLogAdapter.ViewHolder;
import com.filepager.udp.MasterService;
class AppInfo 
{
public String name;
public Drawable icon;
public ResolveInfo rinfo;
public String path;



}

public class ShareList extends Fragment implements OnItemClickListener, OnClickListener, OnItemLongClickListener{
	private static final int ACTION_SELECT_AUDIO = 3;
	private static final int ACTION_SELECT_IMAGE = 1;
	private static final int ACTION_SELECT_VIDEO = 2;
	private static final int ACTION_SELECT_APPS = 4;
	View rootview;
	TextView shareWith;
	 ListView shareWithlv;
	 ShareAdapter shareAdapter;
	 private ListHolder listh = new ListHolder();
	private int faltugiri;
	
	 @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		 mainActivity=(MainActivity)getActivity();
		 super.onActivityCreated(savedInstanceState);
		 	
	}
	
	 

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(listh!=null)
		{
			listh.clear();
		}
		if(shareAdapter!=null)
			shareAdapter.notifyDataSetChanged();
		
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootview=inflater.inflate(R.layout.share_list, null);
		shareAdapter = new ShareAdapter(getActivity(), listh);
		shareWithlv=(ListView)rootview.findViewById(R.id.listview2);
		shareWithlv.setAdapter(shareAdapter);
		shareWithlv.setOnItemClickListener(this);
		((MainActivity)getActivity()).isWifiEnabled();
	registerForContextMenu();
		
		
		return rootview;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		
		if (v.getId()==R.id.listview2) {
			ListHolder1 temp=(ListHolder1)shareAdapter.getItem(info.position); 
			 mainActivity.ip=temp.ip;
			 mainActivity.hostname=temp.hostname;
			if(temp.category!=ListHolder1.CAT_REFRESH)
			{
			    menu.setHeaderTitle(R.string.select_send_action);
			    String[] menuItems = getResources().getStringArray(R.array.action);
			    for (int i = 0; i<menuItems.length; i++) {
			      menu.add(0, i, i, menuItems[i]);
			 }
			}
			
			    else
			    	{
			    	sendHello();
			    	menu.close();
			    	
			    	}
		}
			    super.onCreateContextMenu(menu, v, menuInfo);
	}
	public void registerForContextMenu(){
		registerForContextMenu(shareWithlv);
		}
		public void UnregisterForContextMenu(){
			unregisterForContextMenu(shareWithlv);
			
			}
	float icon_size ;
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterView.AdapterContextMenuInfo menuinfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int position=menuinfo.position;
		
		Intent intent = new Intent();
		if(item.getGroupId()==0)
		switch(item.getItemId()){
		case 0:
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_PICK);//
			intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(
					Intent.createChooser(intent, "Select Picture"),
					ACTION_SELECT_IMAGE);

			return true;
		case 1:
			intent.setType("video/*");
			intent.setAction(Intent.ACTION_PICK);//

			startActivityForResult(
					Intent.createChooser(intent, "Select Video"),
					ACTION_SELECT_VIDEO);

			return true;
		case 2:
			intent.setType("audio/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);//

			startActivityForResult(
					Intent.createChooser(intent, "Select Audio"),
					ACTION_SELECT_AUDIO);

			return true;
		case 3:
		
	//		ProgressDialog pd=ProgressDialog.show(mainActivity, "Please Wait","Downloading App List" );
			    	  //          pd.dismiss();
			AppsSender as=new AppsSender(mainActivity);
			   icon_size = mainActivity.getResources().getDimension(R.dimen.app_icon);

			createDialogForAppShare(as.createAppList());
			return true;
		
		case 4:
			openFileMan(position);
			return true;
		}
		return super.onContextItemSelected(item);
	}
private void createDialogForAppShare(List<ResolveInfo> pkgAppsList) {
		// TODO Auto-generated method stub
	final ShareAppsAdapter appsAdapter=new ShareAppsAdapter(pkgAppsList,mainActivity);	
	AlertDialog.Builder adb=new AlertDialog.Builder(mainActivity);
	
	DialogInterface.OnClickListener cl = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			Intent data1=new Intent();
			data1.putExtra("path",appsAdapter.getPath(which) );
	        
	          mainActivity.sendFiles(data1);
	    
			
		
		}
	};
	adb.setAdapter(appsAdapter,cl );
adb.show();	
}

private Drawable resize(Drawable image) {
    Bitmap b = ((BitmapDrawable)image).getBitmap();
    Bitmap bitmapResized = Bitmap.createScaledBitmap(b,(int) icon_size,(int) icon_size, false);
    return new BitmapDrawable(getResources(), bitmapResized);
}
	
class ShareAppsAdapter extends BaseAdapter
{
private List<ResolveInfo> all;
private List<AppInfo> loadedList;
private Context context;
LayoutInflater mInflater;
public ShareAppsAdapter(List<ResolveInfo> pkgAppsList,Context c){
	all=pkgAppsList;
	context=c;
	
	pm=c.getPackageManager();
	mInflater = (LayoutInflater) context
			.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	loadedList=MyApp.getInstance().appList;
}
PackageManager pm;
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return all.size();
	}
	

	public String getPath(int position){
		return loadedList.get(position).path;
	}
	
	
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return all.get(position);
	}
	public  ResolveInfo getItem1(int position) {
		// TODO Auto-generated method stub
		return all.get(position);
	}
	
	class AppViewHolder{
		public AppViewHolder(View v){
			appName=(TextView)v.findViewById(R.id.app_name);
			appIcon=(ImageView)v.findViewById(R.id.app_icon);
			
		}
		TextView appName;
		ImageView appIcon;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//ResolveInfo resinfo=getItem1(position);
		ResolveInfo resinfo=loadedList.get(position).rinfo;
		
		AppViewHolder holder;
		if(convertView==null){
			convertView=mInflater.inflate(R.layout.share_apps_row, null);
			holder=new AppViewHolder(convertView);
			convertView.setTag(holder);
			
		}
		else {
			holder=(AppViewHolder) convertView.getTag();
		}
		AppInfo appinfo=loadedList.get(position);	
		
		
		holder.appName.setText(appinfo.name);

			holder.appIcon.setTag(resinfo);
			holder.appName.setTag(resinfo);
			
			
			
			if(appinfo.icon==null){
			holder.appIcon.setImageResource(android.R.drawable.sym_def_app_icon);
			new AppIconLoader(pm, holder.appIcon,holder.appName,resinfo,position).execute();
			}
			else
			{
				holder.appIcon.setImageDrawable(appinfo.icon);
					
			}
		
		
		
		
		
		return convertView;
	}
	
	
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
class NameIcon{
	Drawable icon;
	String name;
}
class AppIconLoader extends AsyncTask<Void, Void, NameIcon>{
	Drawable icon;
	PackageManager pm;
	ImageView iv;
	String name;
	int position;
	ResolveInfo appInfo;
	public AppIconLoader(PackageManager p,ImageView iv, TextView appName, ResolveInfo appinfo,int pos){
		pm=p;
		this.iv=iv;
		position=pos;
		appInfo=appinfo;
		
	}
	
	@Override
	protected void onPostExecute(NameIcon result) {
	
	if((ResolveInfo)iv.getTag()==appInfo){	
	iv.setImageDrawable(result.icon);
	//appname.setText(result.name);	
	}
	mainActivity.myApp.appList.get(position).icon=result.icon;

	super.onPostExecute(result);
	}

	@Override
	protected NameIcon doInBackground(Void... params) {
		// TODO Auto-generated method stub
		NameIcon ni=new NameIcon();
		
		ni.icon = resize(appInfo.loadIcon(pm));
		return ni;
	}
	
	

}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
	       
		if(resultCode ==Activity.RESULT_OK)
	{
			Uri uri=data.getData();
			
			String path=getFilePath(uri);
			Log.d("Path of file ", path);
			 Intent data1=new Intent();
		switch (requestCode) {
		case ACTION_SELECT_IMAGE:
		    mainActivity.makeToast(path);
	        data1.putExtra("path", path);
	        
	          mainActivity.sendFiles(data1);
	    		 	break;
		case ACTION_SELECT_VIDEO:
		    mainActivity.makeToast(path);
	        data1.putExtra("path", path);
	        
	          mainActivity.sendFiles(data1);
	    		 	break;

		case ACTION_SELECT_AUDIO:
		    mainActivity.makeToast(path);
	        data1.putExtra("path", path);
	        
	          mainActivity.sendFiles(data1);
	    		 	break;

	    		
	    		
	    		 	
		default:
			break;
		}
	}
	else 
	{
		
	}
	
		super.onActivityResult(requestCode, resultCode, data);
	}

	protected static final int SEND_FILE = 1;
	MainActivity mainActivity;
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	//	openFileMan(arg2);
	arg0.showContextMenuForChild(arg1);
	}
private boolean isSentFromOtherApps() {
		// TODO Auto-generated method stub
	if(notAgain){
		return false;
	}
	else{
		 notAgain=true;
	}
	Intent intent = getActivity().getIntent();
    String action = intent.getAction();
    String type = intent.getType();
    
    if (Intent.ACTION_SEND.equals(action) && type != null) 
    {
    
    	Uri uri=(Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
    	if(uri==null)
    	mainActivity.makeToast("URI null");
    	else
    		{
    		String filePath = getFilePath(uri);
          mainActivity.makeToast(filePath);
        Intent data=new Intent();
        data.putExtra("path", filePath);
        
          mainActivity.sendFiles(data);
    		
    		}
    return true;
    } 
    else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
    	return true;
    	} 
    	else
    	{
    		return false;
    	}				         
}public void openFileMan(int arg2){
	// TODO Auto-generated method stub
	final ListHolder1 listHolder1=(ListHolder1)shareAdapter.getItem(arg2);
	mainActivity.ip = listHolder1.ip;
	mainActivity.hostname=listHolder1.hostname;
	 if(!mainActivity.isMasterServiceRunning1())
		
		 {
		Intent i = new Intent(mainActivity, MasterService.class);
		mainActivity.startService(i);
		 }
	
	switch (listHolder1.category) {
	case ListHolder1.CAT_PEOPLE:
		if(isSentFromOtherApps())
		{

		}
			else	
		{			Intent timepass=new Intent(mainActivity, FileChooserActivity.class);
		timepass.putExtra("ip", mainActivity.ip);
		timepass.putExtra("hostname", mainActivity.hostname);
		mainActivity.startActivityForResult(timepass,SEND_FILE);
		}	
		break;

	case ListHolder1.CAT_NETWORK:
		
		break;
	
	case ListHolder1.CAT_REFRESH:
		sendHello();	
		break;
	default:
		break;
	}
	
	

}
public void sendHello(){
	if(mainActivity.mBound)
	{
		Runnable a= new Runnable() {
			public void run() {
				mainActivity.mService.sendHello();		
			}
		};
		mainActivity.tm.addTask(a);
		listh.clear();
		shareAdapter.notifyDataSetChanged();
	}

}
public String getFilePath(Uri uri){
	String filePath;
	if ( "content".equals(uri.getScheme())) {
          Cursor cursor = mainActivity.getContentResolver().query(uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
          cursor.moveToFirst();   
          filePath = cursor.getString(0);
          cursor.close();
      }
      else {
          filePath = uri.getPath();
      }
 return filePath;   
}
TextView wifi_dialog;
	public void makeWifiDialogVIsible() {

		
		wifi_dialog=(TextView)rootview.findViewById(R.id.wifi_dialog);
		wifi_dialog.setVisibility(View.VISIBLE);
		wifi_dialog.setOnClickListener(this);
	
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId()){
		case R.id.wifi_dialog:
			enableWifi();
			wifi_dialog.setVisibility(View.GONE);
			break;
		}
	}
	private void enableWifi() {
		// TODO Auto-generated method stub
		WifiManager wifi = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
		wifi.setWifiEnabled(true);
		if(wifi.isWifiEnabled())
		{
		wifi.startScan();
		}
		else
		{
			
			//Do something here , moron
		}
		
	}

	public void addtolist(List<ScanResult> results) {
		// TODO Auto-generated method stub
		for(ScanResult sr:results)
		{
			ListHolder1 lh=new ListHolder1();
			lh.hostname=sr.SSID;
			lh.ip=sr.BSSID;
			Toast.makeText(getActivity(), "ip:"+sr.BSSID, Toast.LENGTH_SHORT).show();
			this.listh.addGuest(lh);
			shareAdapter.notifyDataSetChanged();
		}
	}

	public void addToList(ListHolder1 lh1)
	{
		listh.addGuest(lh1);
		if(shareAdapter!=null)
		shareAdapter.notifyDataSetChanged();
	}
	

	public void removeRefreshButton(){
		for (ListHolder1 lh1:listh.all){
			if(lh1.category==ListHolder1.CAT_REFRESH)
				{
				listh.all.remove(lh1);
				break;
				}
			}
	} 

	boolean notAgain=false;

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		ListHolder1 lh1=(ListHolder1) shareAdapter.getItem(arg2);
if(lh1.category!=ListHolder1.CAT_REFRESH)
		openFileMan(arg2);
		
		return true;
	}

}
