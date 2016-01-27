package com.filepager.interfaces;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public abstract class FileUploadProgress extends BroadcastReceiver {

	public  static final String NOTIFY_PROGRESS = "Babu";
	public  static final String UPLOAD = "u";
	public  static final String DOWNLOAD = "d";
	public  static final String TRANSFER_UPLOAD_COMPLETE = "tuc";
	public  static final String TRANSFER_DOWNLOAD_COMPLETE = "tdc";
	
	public  static final String PUT_PROGRESS_TYPE = "upload or download";
	public static final String FILE_DB_ID = "file_db_id";
	public static final String PUT_PROGRESS = "progress";
	
	

	@Override
	public void onReceive(Context arg0, Intent intent) {
		// TODO Auto-generated method stub
		if(intent!=null)
			if(intent.getAction().equals(FileUploadProgress.NOTIFY_PROGRESS))
			{
				int progress=intent.getIntExtra("progress", 0);
				String type="";
				type=intent.getStringExtra(PUT_PROGRESS_TYPE);
				int file_db_id=intent.getIntExtra(FILE_DB_ID, -1);
				  
				if (!type.equals(""))
					if (type.equals(UPLOAD))
						onUploadProgess(progress,file_db_id);

					else if (type.equals(DOWNLOAD))
						onDownloadProgess(progress,file_db_id);
					else if(type.equals(TRANSFER_DOWNLOAD_COMPLETE))
						
						onDownloadCompleted(file_db_id);
					else if(type.equals(TRANSFER_UPLOAD_COMPLETE))
						
						onUploadCompleted(file_db_id);
					
	}
		
	}
	public abstract void onUploadProgess(int progress, int file_db_id2);
	public abstract void onDownloadProgess(int progress, int file_db_id2);
	public abstract void onUploadCompleted( int file_db_id2);
	public abstract void onDownloadCompleted(int file_db_id2);
}
