package com.filepager.main;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.filepager.interfaces.FileUploadProgress;
import com.filepager.interfaces.ScanedResultListner;
import com.filepager.interfaces.UdpPacketBroadCastReceiver;
import com.filepager.settings.SettingsActivity;
import com.filepager.sql.DatabaseHandler;
import com.filepager.sql.MyFile;
import com.filepager.udp.MasterService;
import com.filepager.utils.IntentConstants;
import com.filepager.utils.TaskManager;
import com.filepager.utils.Utils;
import com.filepager.utils.WifiAdapter;
import com.filepager.utils.WifiApManager;
import com.flurry.android.FlurryAgent;

public class MainActivity extends SherlockFragmentActivity implements OnItemClickListener, OnClickListener, ScanedResultListner, OnPageChangeListener{
	public static boolean TRANSFER = false;
	TextView wifi_dialog;
	public static Typeface typeface;
	public static String PAGE_FOLDER=Environment.getExternalStorageDirectory()+"/Page/";
	private WifiManager wifi;
	private List<ScanResult> results;
	boolean clicked=false;
	public FileConnector fc;
	public String ip;
	public String hostname;
	private boolean registered=false; 
	TaskManager tm = new TaskManager();
	public MasterService mService;
	public boolean mBound;
	public MyApp myApp;
	private SharedPreferences name;
	private DatabaseHandler db;
	private LinkedList<String> tabs = new LinkedList<String>();
	private MainActivityAdapter mainActivityAdapter;
	private ViewPager viewPager;
	private Intent i;
	private boolean nd_registered=false;
	private Intent i2;
	private LinkedList<Runnable> temps =  new LinkedList<Runnable>();
	
	UdpPacketBroadCastReceiver udpPacketBroadCastReceiver=new UdpPacketBroadCastReceiver() {
		
		boolean first=true;
		@Override
		public void OnUDPacketReceived(String ip,String hostname) {
		if(first)
				{
				mainActivityAdapter.sharelist.removeRefreshButton();
				first=false;
				}
			ListHolder1 member=new ListHolder1();
			member.hostname=hostname;
			member.ip=ip;
			member.category=ListHolder1.CAT_PEOPLE;
			mainActivityAdapter.addToShareList(member);
			
		}
	};
	



	FileUploadProgress fup=new FileUploadProgress() {
		
		
		@Override
		public void onUploadProgess(int progress,int file_db_id) {
			
			
			if(mService.id_progress.get(file_db_id)!=null)
			{
				mainActivityAdapter.filelogs.updateProgress(file_db_id, mService.id_progress.get(file_db_id));	
			}
			else
			{
			}
			
			
			//mainActivityAdapter.filelogs.updateProgress(file_db_id, progress);
		}
		@Override
		public void onDownloadProgess(int progress,int file_db_id) {
			
			
			if(mService.id_progress.get(file_db_id)!=null)
			{
				mainActivityAdapter.filelogs.updateProgress(file_db_id, mService.id_progress.get(file_db_id) );	
			}
			else
			{
			
			}
			
			Log.e("pro", "p:"+progress);
			
			if(progress==0){
				mainActivityAdapter.filelogs.loadFiles();
				viewPager.setCurrentItem(1);
				Log.e("pr", "p:"+progress);
			}
		
		}
		
		@Override
		public void onUploadCompleted(int file_db_id2) {
			String fname=db.getFileName(file_db_id2);
			makeToast("File sent: "+fname);
			db.setFileStatus(file_db_id2, MyFile.STATUS_UPLOADED);
			mainActivityAdapter.filelogs.loadFiles();
		}
		@Override
		public void onDownloadCompleted(int file_db_id2) {
		
			String fname=db.getFileName(file_db_id2);
			makeToast("File Received: "+fname);
			db.setFileStatus(file_db_id2, MyFile.STATUS_DOWNLOADED);
			mainActivityAdapter.filelogs.loadFiles();
		}
		
	};
	

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = ((com.filepager.udp.MasterService.LocalBinder) service).getService();
			mBound = true;
			
				Runnable a= new Runnable() {
					public void run() {
						mService.sendHello();		
					}
				};
				tm.addTask(a);
				for(Runnable a1 : temps)
				{
					a1.run();
				}
				temps=new LinkedList<Runnable>();
				
						}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.e("test", "service disconnect");
			mBound = false;
		}
	};
	

	@Override
	protected void onStart() {
		super.onStart();
		tm.start();
		Intent intent = new Intent(this, MasterService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		FlurryAgent.onStartSession(this, "KH8CNHBS69ZTMWG8B8QC");
		
		
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		UnregisterBroadcastReciever();
		if(mBound)
		{
			unbindService(mConnection);
			mBound=false;
		
		}
		FlurryAgent.onEndSession(this);
		
		
	}
	
	@Override
	public void onBackPressed() {
		if(mBound)
		{
			unbindService(mConnection);
			mBound=false;
		
		}
		Log.e("sp--tran", "sp:"+name.getBoolean(SettingsActivity.KEY_SERVICE, false)+" tx:"+TRANSFER);
		if(!name.getBoolean(SettingsActivity.KEY_SERVICE, true)&& !TRANSFER)
			stopService(i);
	
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		super.onPause();
		UnregisterBroadcastReciever();
	}

	@Override
	protected void onResume() {
		super.onResume();
		RegisterBroadcastReceiver();
		if(mBound)
		{
			Runnable a= new Runnable() {
				public void run() {
					mService.sendHello();		
				}
			};
			tm.addTask(a);
		}
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		savedInstanceState = null;
		super.onCreate(savedInstanceState);
		wifi=(WifiManager)getSystemService(Context.WIFI_SERVICE);
		typeface=Typeface.createFromAsset(getAssets(), "opensans2.ttf");
		db=new DatabaseHandler(this);
		myApp=MyApp.getInstance();
		CheckforFirstLastName();
		UI();		
		File f = new File(Environment.getExternalStorageDirectory()+"/Page/");
		f.mkdir();
		i = new Intent(this, MasterService.class);
	 if(!isMasterServiceRunning1())
	 startService(i);
     
	}
	boolean isWifiEnabled() {
	
		boolean r= wifi.isWifiEnabled();
	
		if(r)
		{
			
		}
		else
		{
	//	mainActivityAdapter.sharelist.makeWifiDialogVIsible();	
		}
		return r;
	}
	
	
	
	boolean isMasterServiceRunning1() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (MasterService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	
	private void CheckforFirstLastName() {
		
		name = PreferenceManager.getDefaultSharedPreferences(this);
		String fn = name.getString(SettingsActivity.KEY_FIRSTNAME, null);
		String ln = name.getString(SettingsActivity.KEY_LASTNAME, null);
		String directory=name.getString(SettingsActivity.KEY_SETDIRECTORY,null );
		boolean showhi=name.getBoolean(SettingsActivity.KEY_HIDDENFILES, false);
		if(directory==null){
			directory=PAGE_FOLDER;
		}
		myApp.DIRECTORY=directory;
		myApp.showhidden=showhi;
		if( fn == null || ln == null){
			Intent intent = new Intent(this, Start.class);
			startActivity(intent);
			finish();
		}
		else {
			myApp.firstname=fn;
			myApp.lastname=ln;
			
		}
		
		
	}

	private void enableWifi() {
		if(wifi.isWifiEnabled())
		{
		wifi.startScan();
		}
		else
		{
			
			//Do something here , moron
		}
		
	}

	private void RegisterBroadcastReceiver() {

		if(!registered)
		{
		IntentFilter filter = new IntentFilter();
		filter.addAction(UdpPacketBroadCastReceiver.ACTION_UDPACKET_RECEIVED);
		registerReceiver(udpPacketBroadCastReceiver, filter);
		
		IntentFilter ifi=new IntentFilter();
		ifi.addAction(FileUploadProgress.NOTIFY_PROGRESS);
		registerReceiver(fup, ifi);

		registered = true;
		}
	}
	
	private void UnregisterBroadcastReciever()
	{
		if(nd_registered)
		{
			unregisterReceiver(nd);
		nd_registered=false;
		}
		
		if(registered)
		{
			unregisterReceiver(udpPacketBroadCastReceiver);
			unregisterReceiver(fup);
			
			registered=false;
		}
	}
	
	private void UI() {
		setContentView(R.layout.main_act_view_pager);
		
		mainActivityAdapter=new MainActivityAdapter(getSupportFragmentManager());
		viewPager=(ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(mainActivityAdapter);
		viewPager.setOnPageChangeListener(this);
		tabs.add("Friends on Network");
		tabs.add("File Logs");
		mainActivityAdapter.setTabsName(tabs);
		
	}
	Menu menu;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//this.menu=menu;
		getSupportMenuInflater().inflate(R.menu.main, menu);
		this.menu=menu;
		WifiApManager ap;
		try {
			ap = new WifiApManager(this);
			Log.d("Ap", String.valueOf(ap.getWifiApState()));
			if(ap.getWifiApState()==11)
			{
				MenuItem i =menu.findItem(R.id.start_hotspot);
				i.setTitle("Start Hotspot");
				
				WIFI_AP_STATE=false;
			}
			else
			{
				MenuItem i =menu.findItem(R.id.start_hotspot);
				i.setTitle("Stop Hotspot");
				
				WIFI_AP_STATE=true;
				
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return super.onCreateOptionsMenu(menu);
	}
	public static boolean WIFI_AP_STATE;
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		
	switch (item.getItemId()) {
	case R.id.settings:
		Intent settings=new Intent(this, SettingsActivity.class);
		startActivity(settings );
		break;
	/*case R.id.share_filepager:
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
	    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
	    mainIntent.setPackage(getPackageName());
	    PackageManager pm=getPackageManager();
	    final List<ResolveInfo> pkgAppsList = pm.queryIntentActivities( mainIntent, 0);
	 	String path = null;
	    for(ResolveInfo ri:pkgAppsList){
	 		Log.d("mm", "path: "+ri.activityInfo.applicationInfo.publicSourceDir);
	 path=ri.activityInfo.applicationInfo.publicSourceDir;
	    }
		if(mBound)
			mService.startHttpAppShare(path);
		AlertDialog.Builder adb=new AlertDialog.Builder(this);
		adb.setNeutralButton("OK", null);
		String s=getString(R.string.share_filepager_dialog)+"http://"+mService.getSelfIp().get(1)+":8080";
				
				adb.setMessage(s);
		adb.show();
		break;
	*/case R.id.delete_all_logs:
		db.deleteLogs(false);
		mainActivityAdapter.filelogs.loadFiles();
		
		break;
	case R.id.stop_service:
		if(mBound)
		{
			if(mService.apstarted)
			{
		MenuItem i =menu.findItem(R.id.start_hotspot);
		i.setTitle("Start Hotspot");
		
		WIFI_AP_STATE=false;
			}
			}
		if(mBound)
		{
			unbindService(mConnection);
		}
		mBound=false;
		stopService(i);
		
		
		break;

	/*case R.id.file_search:
		Intent a = new Intent(MainActivity.this,FileSearchActivity.class);
		startActivity(a);
		
		break;
	*/case R.id.search_wifi:
		startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
		break;
	case R.id.start_hotspot:
		
		if(getMobileDataState(this)&& !WIFI_AP_STATE)
		{
			
			AlertDialog.Builder builder= new AlertDialog.Builder(this);
			builder.setMessage("Your Mobile Data will be turned off.");
			builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try {
						setMobileDataEnabled(MainActivity.this, false);
						if(mBound)
						{
							Log.d("Restore","Data is set");
							mService.statehaschanged=true;
							mService.datastatebefore=true;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Jugaad(item);
				}
			});
	builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
		
					/*try {
						setMobileDataEnabled(MainActivity.this, false);
					} catch (Exception e) {
					makeToast("Unable to Turn off Mobile data");
					}*/
				}
			});	
		builder.create().show();
		}
		else
			Jugaad(item);
		
		default:
		break;
	}
		return super.onOptionsItemSelected(item);
	}
	
	private void setMobileDataEnabled(Context context, boolean enabled) throws Exception{
	    final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    final Class conmanClass = Class.forName(conman.getClass().getName());
	    final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
	    iConnectivityManagerField.setAccessible(true);
	    final Object iConnectivityManager = iConnectivityManagerField.get(conman);
	    final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
	    final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
	    setMobileDataEnabledMethod.setAccessible(true);
	    
	    setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
	}
	private void Jugaad(MenuItem item) {
		if(wifi.getWifiState()==WifiManager.WIFI_STATE_ENABLED)
		{
			if(mBound)
			{
				Log.d("Restore","wifi is set");
				mService.statehaschanged=true;
				mService.wifistatebefore=true;
			}
		}
		boolean result=false;
		boolean start =false;
		try {
			if(!WIFI_AP_STATE)
			{
				result=startwifihotspot(true);
				WIFI_AP_STATE=true;
				start=true;
				item.setTitle(R.string.stop_hot_spot);
			}
			else
			{
				result=stopwifihotspot(false);
				WIFI_AP_STATE=false;	
				item.setTitle(R.string.start_hot_spot);
				if(mBound)
					mService.restoreStateOfWifiAndData();
			}
		} catch (SecurityException e) {
		
			makeToast("Unable to start Hotspot"+e.toString());
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			makeToast("Unable to start Hotspot"+e.toString());
			e.printStackTrace();
		}
		if(result)
			if(start)
			makeToast("HotSpot started");
			else
				makeToast("HotSpot Stopped");
		else makeToast("Unable to start Hotspot");

	}


	public boolean stopwifihotspot(boolean enable) throws SecurityException, NoSuchMethodException
	{
		boolean done = false;
		WifiApManager ap = new WifiApManager(this);
		WifiConfiguration config = ap.getWifiApConfiguration();
		config.SSID = myApp.firstname+" "+myApp.lastname;
		config.preSharedKey="";
		//config.preSharedKey="\""+"mohit123"+"\"";
		ap.setWifiApState(config, enable);
		makeToast("HotStop stopped");
		done=true;
		if(mBound)
			mService.apstarted=false;
		return done;
		
	}

	public boolean startwifihotspot(boolean enable) throws SecurityException, NoSuchMethodException{
		boolean done = false;
        WifiApManager ap = new WifiApManager(this);
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = myApp.firstname+" "+myApp.lastname;
		config.preSharedKey = "";
		config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		config.preSharedKey="";
		ap.setWifiApState(config, enable);
if(mBound)
	mService.apstarted=true;
		//makeToast(getWifiApIpAddress());
		done=true;		
		return done;
	
	}
	public boolean getMobileDataState(Context context)
	{
		boolean mobileDataEnabled = false; // Assume disabled
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	      
	    try {
	    	  Class cmClass = Class.forName(cm.getClass().getName());
	  		
	    	Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
	        method.setAccessible(true); // Make the method callable
	        // get the setting for "mobile data"
	        mobileDataEnabled = (Boolean)method.invoke(cm);
	    } catch (Exception e) {
	  makeToast("Unable to get Mobile data State");
	    }
	return mobileDataEnabled;
	}
	
	
	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()){
		case R.id.wifi_dialog:
			enableWifi();
			wifi_dialog.setVisibility(View.GONE);
			break;
	}
	}
	
	void makeToast(String s){
		Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
	}
		
	private int addFile2db(String name,int status,long size) {
		
		MyFile mf=new MyFile();
		mf.path=PAGE_FOLDER+name;
		
		mf.name=name;
		mf.status=status;
		mf.tsize=size;
		mf.sender=hostname;
		mf.type=Utils.getMimeType(name);
		int id=myApp.db.AddFile(mf);
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub 
				loadFiles();		
			}
		});
		
	if(id==-1){makeToast("Cannot add File to Database Line 587 MainActivity");
	}
		return id;
	}
	
	private void loadFiles() {
	mainActivityAdapter.filelogs.loadFiles();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) 
		{
			switch (requestCode) 
			{
			case WifiAdapter.SEND_FILE:
			sendFiles(data);
			break;

			default:
			
			break;
			}
		} 
		else
		{
			//makeToast("Error");
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void sendFiles(Intent data) {
		
		boolean multiple=data.getBooleanExtra(IntentConstants.IS_MULTIPLE, false);
		if(multiple){
			makeToast("multiple selcted");
			final ArrayList<Uri> boom = data.getParcelableArrayListExtra("files");
			for(final Uri u : boom )
			{
				if(mBound)
					mService.sendFile(u.getPath(), ip,hostname);
				else
				{
					temps.add(new Runnable() {
						public void run() {
							mService.sendFile(u.getPath(), ip,hostname);
						}
					});
				}

			}
		}
		else{
			final String path=data.getStringExtra("path");
			IntentFilter inf=new IntentFilter("NOTIFY_DATA");
			registerReceiver(nd, inf);
			nd_registered=true;
			viewPager.setCurrentItem(1);
			if(mBound)
				mService.sendFile(path, ip,hostname);
			else
			{
				temps.add(new Runnable() {
					public void run() {
						mService.sendFile(path, ip,hostname);
					
					}
				});
			}
		}
	}
	NotifyDatasetBroadcast nd=new NotifyDatasetBroadcast();
class NotifyDatasetBroadcast extends BroadcastReceiver{

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		Log.e("nd", "nd");
		loadFiles();
	}
	
}
	private String getWifiApIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
	                .hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            if (intf.getName().contains("wlan")) {
	                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
	                        .hasMoreElements();) {
	                    InetAddress inetAddress = enumIpAddr.nextElement();
	                    if (!inetAddress.isLoopbackAddress()
	                            && (inetAddress.getAddress().length == 4)) {
	                        Log.d("IP", inetAddress.getHostAddress());
	                        return inetAddress.getHostAddress();
	                    }
	           }
	            }
	        }
	    } catch (SocketException ex) {
	        Log.e("IP", ex.toString());
	    }
	    return null;
	}

	public void OnScanedResult() {
		results = wifi.getScanResults();
        mainActivityAdapter.sharelist.addtolist(results);
     }
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
		
	}

	public void stopSending(int id) {
		// TODO Auto-generated method stub
		if(mBound)
			mService.stopSendFile(id);
		else 
			makeToast("Unable to Cancle, Service not binded");
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int arg0) {
		
	}
}
