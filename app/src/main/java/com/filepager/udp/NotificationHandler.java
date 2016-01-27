package com.filepager.udp;

import java.util.HashMap;

import com.filepager.sql.DatabaseHandler;
import com.filepager.sql.MyFile;
import com.filepager.utils.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

public class NotificationHandler {
	
	HashMap<Integer ,MyFile > notificationHolder; 
	public NotificationHandler(Builder b, NotificationManager nm, DatabaseHandler d) {
		// TODO Auto-generated constructor stub
	builder=b;
	noti_manager =nm;
	notificationHolder=new HashMap<Integer, MyFile>();
	db=d;
	}
	
	DatabaseHandler db;
	NotificationCompat.Builder builder;
	private NotificationManager noti_manager;
	public void UpdateNotiDownloading(int db_id,long progress){
		int hmsize;
		MyFile myFile;
		String title;
		String summery;
		long totalprogress = 0;
		long totalsize=0;
		int perprogress;
		if(notificationHolder.containsKey(db_id))
		{
			myFile=notificationHolder.get(db_id);
			Log.e("noti", "file from hm");
		}
		else
		{
			myFile=db.getFile(db_id);
			notificationHolder.put(db_id, myFile);
			Log.e("noti", "file from db");
		}
		
		
		//if((hmsize=notificationHolder.size())==1){
		summery="Receiving "+getProgressString(progress,myFile.tsize);
		title=myFile.name;
		perprogress=getProgress(progress, myFile.tsize);
	/*	}
		else
		{
			
			for(MyFile mf:notificationHolder.values()){
				totalprogress+=mf.realprogress;
				totalsize+=mf.tsize;
			}
			summery="Receiving "+getProgressString(totalprogress, totalsize);
			title=""+ hmsize+" files Receiving";
			perprogress=getProgress(totalprogress, totalsize);	
		}
	*/	builder.setContentText(summery);
		builder.setContentTitle(title);
		builder.setProgress(100, perprogress, false);
		builder.setSmallIcon(android.R.drawable.stat_sys_download);
		Notification noti= builder.build();
		noti_manager.notify(0,noti);	
		
	}
	private int getProgress(long progress, long tsize) {
		// TODO Auto-generated method stub
		
		return (int) (100*(double)progress/(double)tsize);
	}
	private String getProgressString(long progress, long tsize) {
		// TODO Auto-generated method stub
		return Utils.formatFileSize(progress) +"/"+Utils.formatFileSize(tsize);
	}
	public void UpdateNotiUploading(int db_id,long progress){
		int hmsize;
		MyFile myFile;
		String title;
		String summery;
		long totalprogress = 0;
		long totalsize=0;
		int perprogress;
		if(notificationHolder.containsKey(db_id))
		{
			myFile=notificationHolder.get(db_id);
			Log.e("noti", "file from hm");
		}
		else
		{
			myFile=db.getFile(db_id);
			notificationHolder.put(db_id, myFile);
			Log.e("noti", "file from db");
		}
		
		//if((hmsize=notificationHolder.size())==1){
		summery="Sending "+getProgressString(progress,myFile.tsize);
		title=myFile.name;
		perprogress=getProgress(progress, myFile.tsize);
		/*}
		else
		{
			
			for(MyFile mf:notificationHolder.values()){
				totalprogress+=mf.realprogress;
				totalsize+=mf.tsize;
			}
			summery="Sending "+getProgressString(totalprogress, totalsize);
			title=""+ hmsize+" files Uploading";
			perprogress=getProgress(totalprogress, totalsize);	
		}
		*/builder.setContentText(summery);
		builder.setContentTitle(title);
		builder.setProgress(100, perprogress, false);
		builder.setSmallIcon(android.R.drawable.stat_sys_upload);
		Notification noti= builder.build();
		noti_manager.notify(1,noti);	
		
	}
	public void DoneUploading(int db_id){
		MyFile myFile;
		
		if(notificationHolder.containsKey(db_id))
		{
			myFile=notificationHolder.get(db_id);
			Log.e("noti", "file from hm");
		}
		else
		{Log.e("noti", "file from db");
			myFile=db.getFile(db_id);
		}
		builder.setContentText("To "+myFile.sender);
		builder.setContentTitle(myFile.name +" Sent");
		builder.setSmallIcon(android.R.drawable.stat_sys_upload_done);
		builder.setProgress(0,0, false);
		Notification noti= builder.build();
		noti_manager.notify(1,noti);	
	
	
	
	}
public void DoneDownoading(int db_id){

	MyFile myFile;
	if(notificationHolder.containsKey(db_id))
	{
		myFile=notificationHolder.get(db_id);
		Log.e("noti", "file from hm");
	}
	else
	{Log.e("noti", "file from db");
		myFile=db.getFile(db_id);
	}
	builder.setContentText("From "+myFile.sender);
	builder.setContentTitle(myFile.name +" Received");
	builder.setProgress(0,0, false);
	builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
	Notification noti= builder.build();
	noti_manager.notify(0,noti);	

	
	
	}
}
