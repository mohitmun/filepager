package com.filepager.udp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.filepager.interfaces.FileUploadProgress;
import com.filepager.main.FileConnector;
import com.filepager.main.FileUploader;
import com.filepager.main.MainActivity;
import com.filepager.main.MyApp;
import com.filepager.main.R;
import com.filepager.sql.DatabaseHandler;
import com.filepager.sql.FileDBTransfer;
import com.filepager.sql.MyFile;
import com.filepager.tcp.FileInfo;
import com.filepager.tcp.FileServer;
import com.filepager.tcp.HttpGo;
import com.filepager.utils.IntentConstants;
import com.filepager.utils.Utils;
import com.filepager.utils.WifiApManager;

public class MasterService extends Service {
	public boolean statehaschanged =false;
	public boolean wifistatebefore=false;
	public boolean datastatebefore=false;
	public boolean apstarted=false;
	private NotificationManager noti_manager;
	private NotificationCompat.Builder builder;
	private HttpGo httpshare;
	LinkedList<FileUploader> fileUploadersList=new LinkedList<FileUploader>();
	String TAG ="Master Service";
	UDPServer updserver;
	NotificationHandler notificationHandler;
	FileServer fs;
	public HashMap<Integer, Long> id_progress=new HashMap<Integer, Long>();
	private int dcount=0;
	private int ucount=0;
	int trys=0;
	private final IBinder local = new LocalBinder();
	DatabaseHandler db;
	public MyApp myApp;
	private WifiManager mWifiManager;
	public ArrayList<String> RECEIVE_DEFAULTS=new ArrayList<String>();
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
	@Override
	public void onDestroy() {
	
		if(updserver!=null)
		updserver.stopServer();
		
		if(fs!=null)
		fs.stopServer();
		
		if(registeredreciever)
		unregisterReceiver(fup);
		
		if(ft!=null)
			if(ft.isAlive())
				ft.stopit();
		if(apstarted)
		{
			try {
				stopwifihotspot();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		restoreStateOfWifiAndData();
		super.onDestroy();
	}
	
	RestartMechanism udprm = new RestartMechanism() {
		
		@Override
		public void restart() {
			updserver.stopServer();
			trys++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(trys<60)
			{
			updserver = new UDPServer(8000,udprm,MasterService.this);
			updserver.start();
			}
			else
				updserver=null;
		}
	};
	public LinkedList<String> getSelfIp(){
		return updserver.getLocalIpAddress();
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		
		return local;
	}
	public class LocalBinder extends Binder {
       public MasterService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MasterService.this;
        }
    }
	
	class NotificationHolder{
		String name;
		long progress;
	}

	FileUploadProgress fup=new FileUploadProgress() {
		
		MyFile file;
		
		
		@Override
		public void onUploadProgess(int progress,int file_db_id) {
			// TODO Auto-generated method stub
			ucount++;
			
			if(id_progress.get(file_db_id)!=null && ucount%6==0)
			{
				long pro=id_progress.get(file_db_id);
				
try {
	notificationHandler.UpdateNotiUploading(file_db_id, pro);
}
catch(Exception E)
{
	noti_manager.cancel(0);
}
			}	
			else
			{
			}
			
		
		}
		@Override
		public void onDownloadProgess(int progress,int file_db_id) {
			dcount++;
			// TODO Auto-generated method stub
			if(id_progress.get(file_db_id)!=null)
			{
				long pro=id_progress.get(file_db_id);
			try{
			if(dcount%6==0)
				notificationHandler.UpdateNotiDownloading(file_db_id, pro);
			}
				catch(Exception E)
				{
					noti_manager.cancel(0);
				}			
				
			}	
			else
			{
			
			}
			
		
				}
		
		@Override
		public void onUploadCompleted(int file_db_id2) {
			// TODO Auto-generated method stub
			try{
				db.setFileStatus(file_db_id2, MyFile.STATUS_UPLOADED);
				notificationHandler.DoneUploading(file_db_id2);

	}
		catch(Exception E)
		{
			noti_manager.cancel(0);
		}			
	
		}
		@Override
		public void onDownloadCompleted(int file_db_id2) {

try{
	db.setFileStatus(file_db_id2, MyFile.STATUS_DOWNLOADED);
		notificationHandler.DoneDownoading(file_db_id2);
		}
		catch(Exception E)
		{
			noti_manager.cancel(0);
		}			
	
		}
		
	};
	private SharedPreferences name;
	boolean registeredreciever =false;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG,"Service Started");
		db=new DatabaseHandler(this);
		myApp=MyApp.getInstance();
		name = getSharedPreferences(com.filepager.utils.IntentConstants.SHARED_PREF, 0);
		registerReceiver(fup, new IntentFilter(FileUploadProgress.NOTIFY_PROGRESS));
		registeredreciever=true;
		//UpdateDB();
		updserver = new UDPServer(8000,udprm,this);
		updserver.start();
		startForeground(5481,makenotificer());
		if(!(fs!=null && fs.isAlive()))
		{
			fs = new FileServer(this);
			fs.start();
		}
		return START_STICKY;
	}
	FileDBTransfer ft;
	private void UpdateDB() {
		// TODO Auto-generated method stub

		ft=new FileDBTransfer(this);
		if(!name.getBoolean(IntentConstants.IS_FILEDB_INDEXED, false)){
		ft.name=name;
		ft.start();
		}
		else {
			Thread t=new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					db.updateDB();		
				}
			});
			t.start();
		}
		
		
	}
	public void sendHello(){
		updserver.sendhello();
	}
	public void readyToReceive(int con_id,int file_id)
	{
		fs.readyToRecevie(con_id, file_id);
	}
	public void initNewFileDownload(int con_id,int file_id)
	{
		fs.initNewDownload(con_id, file_id);
	}
	
	public Notification makenotificer(){
		noti_manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		builder = new NotificationCompat.Builder(this);
		builder.setAutoCancel(true);
		builder.setSmallIcon(R.drawable.newnoti);
		builder.setContentTitle("FilePager");
		builder.setContentText("");
		notificationHandler=new NotificationHandler(builder, noti_manager,db);
		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		builder.setContentIntent(pendingIntent);
		return builder.getNotification();
				}
	public InetAddress getBroadcastAddress(WifiManager wm, int ipAddress) throws IOException {
	    DhcpInfo dhcp = wm.getDhcpInfo();
	    if(dhcp == null)
	    {
	    	Log.d("MasterService","DHCp  is null ");
	        return InetAddress.getByName("192.168.43.255");
	    }
		try {
			WifiApManager ap = new WifiApManager(this);
			if(ap.getWifiApState()!=11)
			{
				return InetAddress.getByName("192.168.43.255");
			}
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    
	    int broadcast = (ipAddress & dhcp.netmask) | ~dhcp.netmask;
	    byte[] quads = new byte[4];
	    for (int k = 0; k < 4; k++)
	      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
	    return InetAddress.getByAddress(quads);
	}
	
	public static int getCodecIpAddress(WifiManager wm, NetworkInfo wifi){
	    WifiInfo wi = wm.getConnectionInfo();
	    if(wifi.isConnected())
	        return wi.getIpAddress();
	    Method method = null;
	    try {
	        method = wm.getClass().getDeclaredMethod("getWifiApState");
	    } catch (NoSuchMethodException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
	    if(method != null)
	        method.setAccessible(true);
	    int actualState = -1;
	    try {
	        if(method!=null)
	            actualState = (Integer) method.invoke(wm, (Object[]) null);
	    } catch (IllegalArgumentException e) {
	        e.printStackTrace();
	    } catch (IllegalAccessException e) {
	        e.printStackTrace();
	    } catch (InvocationTargetException e) {
	        e.printStackTrace();
	    }
	    if(actualState==13){
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
	                        	Log.d("MasterService","Ip address is " + inetAddress.getHostAddress());
	                            return convertIP2Int(inetAddress.getAddress());
	                        }
	                    }
	                }
	            }
	        } catch (SocketException ex) {
	        }
	    }
	        return 0;
	}
	public static int convertIP2Int(byte[] ipAddress){
	    return (int) (Math.pow(256, 3)*Integer.valueOf(ipAddress[3] & 0xFF)+Math.pow(256, 2)*Integer.valueOf(ipAddress[2] & 0xFF)+256*Integer.valueOf(ipAddress[1] & 0xFF)+Integer.valueOf(ipAddress[0] & 0xFF));
	}
	
	void makeToast(String s){
		Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
	}

	
	public void sendFile(final String path,final String to, final String hostname)
	{
		FileUploader fileUploader=new FileUploader(this, to, path, hostname);
		fileUploadersList.add(fileUploader);
		fileUploader.start();
	}
	
	public void resumeFile(final int file_db_id,final String toip)
	{
		final MyFile temp = db.getFile(file_db_id);
		Log.d("Resume", "Began");
		Runnable a = new Runnable() {
			
			@Override
			public void run() {
				FileInfo temp1 = new FileInfo();
				FileConnector conn = new FileConnector(MasterService.this);
				conn.connect(toip, 36081);
				Log.d("Resume", "Connected");
				File f = new File(temp.path);
				int blockstoskip =(int)(f.length()/512/1024)-1;
				try {
					RandomAccessFile fra = new RandomAccessFile(f, "rw");
					fra.seek(blockstoskip*512*1024);
					temp1.fra=fra;
					
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				try {
				
					int a = conn.beginResumingDownload(temp.file_db_id, temp.passcode, blockstoskip);
					
					temp1.fileid=a;
					temp1.blocks=temp.blocks;
					temp1.blockwritten=blockstoskip;
					temp1.file_db_id=file_db_id;
					Log.d("Resume", "Values blocks = "+String.valueOf(temp1.blocks) + " value of blocks to skip =" + String.valueOf(blockstoskip));
					conn.addFiletoResume(temp1);
					Log.d("Resume", "File added");
					
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
			}
		};
		new Thread(a).start();
	}
	public void stopSendFile(int file_db_id){
		for(FileUploader fileUploader:fileUploadersList){
			if(fileUploader.file_db_id==file_db_id)
			{
				
			fileUploader.stopSending();
			if(id_progress.get(file_db_id)!=null){
				id_progress.remove(file_db_id);
				
			}
			
			}
		}
	}
	
	
	
public int addFile2db(String name,int status,long size, String hostname,String senderid , int passcode,String path) {
		
		MyFile mf=new MyFile();
		mf.path=path;
		
		mf.name=name;
		mf.status=status;
		mf.tsize=size;
		mf.sender=hostname;
		mf.type=Utils.getMimeType(name);
		mf.passcode=String.valueOf(passcode);
		mf.senderid=senderid;
		int id=myApp.db.AddFile(mf);
		sendBroadcast(new Intent("NOTIFY_DATA"));
		// 
		
	if(id==-1){
		
	
	}
		return id;
	}
public void restoreStateOfWifiAndData()
{
	Log.d("Restor", "Value of wifii and data "+ String.valueOf(wifistatebefore) +"  "+ String.valueOf(datastatebefore));
	if(statehaschanged)
	{
		 mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		 mWifiManager.setWifiEnabled(wifistatebefore);
		try
		{
			    final ConnectivityManager conman = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			    final Class conmanClass = Class.forName(conman.getClass().getName());
			    final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
			    iConnectivityManagerField.setAccessible(true);
			    final Object iConnectivityManager = iConnectivityManagerField.get(conman);
			    final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
			    final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
			    setMobileDataEnabledMethod.setAccessible(true);
			    
			    setMobileDataEnabledMethod.invoke(iConnectivityManager, datastatebefore);
		
		}
		catch(Exception e)
		{
			
		}
	    
		statehaschanged=false;
		wifistatebefore=false;
		datastatebefore=false;
	}
}
public boolean stopwifihotspot() throws SecurityException, NoSuchMethodException{
	boolean done = false;
    WifiApManager ap = new WifiApManager(this);
	WifiConfiguration config = new WifiConfiguration();
	config.SSID = myApp.firstname+" "+myApp.lastname;
	config.preSharedKey = "";
	config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
	config.preSharedKey="";
	ap.setWifiApState(config, false);

	//makeToast(getWifiApIpAddress());
	done=true;		
	return done;

}
public void startHttpAppShare(String path)
{
	if(httpshare!=null)
	{
		httpshare.stopme();
	}
	httpshare = new HttpGo(path);
	httpshare.start();
}

}
