package com.filepager.main;

import java.io.File;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.filepager.interfaces.FileUploadProgress;
import com.filepager.interfaces.UdpPacketBroadCastReceiver;
import com.filepager.sql.DatabaseHandler;
import com.filepager.sql.MyFile;
import com.filepager.udp.MasterService;
import com.filepager.utils.IntentConstants;
import com.filepager.utils.TaskManager;

public class MainActivity1 extends SherlockFragmentActivity {

	private WifiManager wifi;
	private DatabaseHandler db;
	private SharedPreferences name;
	private MyApp myApp;
	private MainActivityAdapter mainActivityAdapter;
	private ViewPager viewPager;
	private Object tabs;
	private Intent i;
	public static Typeface typeface;
	private boolean registered = false;
	private MasterService mService;
	private TaskManager tm;
	private boolean mBound;


	private UdpPacketBroadCastReceiver udpPacketBroadCastReceiver=new UdpPacketBroadCastReceiver() {
		
		boolean first=true;
		@Override
		public void OnUDPacketReceived(String ip,String hostname) {
			// TODO Auto-generated method stub
			if(first)
				{
				///mainActivityAdapter.sharelist.shareAdapter.removeRefreshButton();
				first=false;
				}
			ListHolder1 member=new ListHolder1();
			member.hostname=hostname;
			member.ip=ip;
			member.category=ListHolder1.CAT_PEOPLE;
			mainActivityAdapter.sharelist.addToList(member);
			
		}
	};
	
	FileUploadProgress fup=new FileUploadProgress() {
		boolean first=true;
		int previousprogress=0;
		
		
		@Override
		public void onUploadProgess(int progress,int file_db_id) {
			// TODO Auto-generated method stub
			
			if(mService.id_progress.get(file_db_id)!=null)
			{
				mService.id_progress.put(file_db_id, mService.id_progress.get(file_db_id)+progress);
			}
			else
			{
				mService.id_progress.put(file_db_id, (long) progress);
			}
			
			mainActivityAdapter.filelogs.updateProgress(file_db_id, (long) progress);
		}
		@Override
		public void onDownloadProgess(int progress,int file_db_id) {
			// TODO Auto-generated method stub
			Log.e("status", "id: "+file_db_id+" "+"p: "+progress);

			if(mService.id_progress.get(file_db_id)!=null)
			{
				mService.id_progress.put(file_db_id, mService.id_progress.get(file_db_id)+progress);
			}
			else
			{
				mService.id_progress.put(file_db_id, (long) progress);
			}
			
			
			if(progress==0){
				mainActivityAdapter.filelogs.loadFiles();
			}
			mainActivityAdapter.filelogs.updateProgress(file_db_id, (long) progress);	
		}
		
		@Override
		public void onUploadCompleted(int file_db_id2) {
			// TODO Auto-generated method stub
			String fname=db.getFileName(file_db_id2);
			makeToast("File sent: "+fname);
			db.setFileStatus(file_db_id2, MyFile.STATUS_UPLOADED);
			mainActivityAdapter.filelogs.loadFiles();
		}
		@Override
		public void onDownloadCompleted(int file_db_id2) {
			// TODO Auto-generated method stub
		
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
						}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		wifi=(WifiManager)getSystemService(Context.WIFI_SERVICE);
		typeface=Typeface.createFromAsset(getAssets(), "opensans2.ttf");
		db=new DatabaseHandler(this);
		//Check if first anme last name there else start
		CheckforFirstLastName();
		// UI
		setContentView(R.layout.main_act_view_pager);
		if(mainActivityAdapter==null)
		{
		mainActivityAdapter=new MainActivityAdapter(getSupportFragmentManager());
		viewPager=(ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter(mainActivityAdapter);
		}
		
		File f = new File(Environment.getExternalStorageDirectory()+"/Page/");
		f.mkdir();
		i = new Intent(this, MasterService.class);
		if(!isMasterServiceRunning1())
    		 startService(i);
		tm = new TaskManager();
		tm.start();
	 
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	

	private void CheckforFirstLastName() {
		// TODO Auto-generated method stub
		name = getSharedPreferences(IntentConstants.SHARED_PREF, 0);
		String fn = name.getString(IntentConstants.SHARED_PREF_FIRST_NAME, null);
		String ln = name.getString(IntentConstants.SHARED_PREF_LAST_NAME, null);
		
		if( fn == null || ln == null){
			Intent intent = new Intent(this, Start.class);
			startActivity(intent);			
		}
		else {
			myApp=MyApp.getInstance();
			myApp.firstname=fn;
			myApp.lastname=ln;
		}
		
		
	}
	private boolean isMasterServiceRunning1() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (MasterService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	private void RegisterBroadcastReceiver() {
		// TODO Auto-generated method stub
		
		if(!registered)
		{
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(UdpPacketBroadCastReceiver.ACTION_UDPACKET_RECEIVED);
		registerReceiver(udpPacketBroadCastReceiver, filter);
		IntentFilter ifi=new IntentFilter();
		ifi.addAction(FileUploadProgress.NOTIFY_PROGRESS);
		registerReceiver(fup, ifi);
		
		}
	}
	
	private void UnRegisterBroadcastReceiver()
	{
		if(registered)
		{
			
		}
	}
	private void makeToast(String s){
		Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
	}
	

}
