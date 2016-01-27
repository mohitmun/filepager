package com.filepager.main;

import java.io.File;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.filepager.sql.MyFile;
import com.filepager.udp.MasterService;

public class FileUploader extends Thread {

	private MasterService masterService;
	private String ip;
	private String path;
	private String hostname;
	
	public FileUploader(MasterService masterService,String to,String path,String hostname)
	{
		this.masterService=masterService;
		this.ip=to;
		this.path=path;
		this.hostname=hostname;
	}
	public  int file_db_id;
	FileConnector temp;
	public void run(){try {
		 temp = new FileConnector(masterService);
		temp.connect(ip, 36081);
		File f=new File(path);
		Random rand = new Random();
		int a = rand.nextInt(100000);
		WifiManager manager = (WifiManager)masterService.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		String senderid = info.getMacAddress();
		//Added into the database
	    file_db_id= masterService.addFile2db(f.getName(), MyFile.STATUS_UPLOADING,f.length(),hostname,senderid,a,path);
	    
		int i=temp.sendFileInfo(f,masterService.myApp.firstname+" "+masterService.myApp.lastname,a,senderid,file_db_id);
		FileHolder fh = new FileHolder();
		fh.f=f;
		fh.file_db_id=file_db_id;
		fh.fileid=i;
		temp.addFiletoSend(fh);
		
	
	} catch (ParserConfigurationException e) {

		e.printStackTrace();
	} catch (TransformerException e) {

	}
	}
	public void stopSending() {
		// TODO Auto-generated method stub
		temp.stopSending();
	}
}
