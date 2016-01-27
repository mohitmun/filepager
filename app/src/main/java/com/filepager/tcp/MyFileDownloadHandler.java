package com.filepager.tcp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.filepager.interfaces.FileUploadProgress;
import com.filepager.main.MainActivity;
import com.filepager.udp.MasterService;
import com.filepager.utils.TaskManager;

public class MyFileDownloadHandler {
	public LinkedList<FileInfo> fileinfo = new LinkedList<FileInfo>();
	
	private MasterService ms;
	private TaskManager tm=new TaskManager();
	
	public MyFileDownloadHandler(MasterService c)
	{
		ms=c;
		tm.start();
	}

	public void addFileInfo(FileInfo fi)
	{
		fileinfo.add(fi);
	}
	
	public void addFileInfoResume(FileInfo fi)
	{
		if(ms.id_progress.get(fi.file_db_id)!=null)
		{
			ms.id_progress.put(fi.file_db_id,((long)fi.blockwritten)*512*1024 );
		}
		else
		{
			ms.id_progress.put(fi.file_db_id,((long) fi.blockwritten)*512*1024);
		}
		fileinfo.add(fi);
	}
	
	public void writeToFile(int fileid , byte[] buffer ,int blockid , int length,int offset) throws IOException
	{
		int i=0;
		for(FileInfo fi:fileinfo)
		{
			if(fi.fileid==fileid)
			{
				FileOutputStream fos;
				if(fi.fos!=null)
				{
					fos = fi.fos;
				}
				else
				{
					fos = new FileOutputStream(fi.f,true);
				}
				try{
				fos.write(buffer,offset, length);
				}
				catch(IOException io){
					
				}
				//fileinfo.remove(fi);
				fi.blockwritten++;
				
				if(fi.blockwritten>=fi.blocks+1)
				{
					final int db_id = fi.my_file_db_id;
					final String ptath = fi.f.getAbsolutePath();
					tm.addTask(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Intent intent=new Intent();
							intent.setAction(FileUploadProgress.NOTIFY_PROGRESS);
							intent.putExtra(FileUploadProgress.PUT_PROGRESS_TYPE,FileUploadProgress.TRANSFER_DOWNLOAD_COMPLETE );
							intent.putExtra(FileUploadProgress.FILE_DB_ID,db_id);
							ms.sendBroadcast(intent);
							Log.d("FileServer", "File Transfer Completed");
							MainActivity.TRANSFER=false;
							scanMedia(ptath);
		
						}
					});
				}
				//fileinfo.add(fi);
				
			return ;
			}
			i++;
		}
	}

	public void writeToFileRandom(int fileid , byte[] buffer ,int blockid , final int length) throws IOException
	{
		int i=0;
		for(final FileInfo fi:fileinfo)
		{
			if(fi.fileid==fileid)
			{
				RandomAccessFile fos;
				if(fi.fra!=null)
				{
					fos = fi.fra;
				}
				else
				{
					fos=new RandomAccessFile(fi.f, "rw");
				}
				
				fos.write(buffer,0, length);
				
				final int temp = fi.file_db_id;
				  tm.addTask(new Runnable() {
					  
					  @Override public void run() { // TODO Auto-generated
					 BroadcastDownloadProgress(temp,length);
					  } });

				//fileinfo.remove(fi);
				fi.blockwritten++;
				if(fi.blockwritten>=fi.blocks+1)
				{
					final int db_id = fi.file_db_id;
					tm.addTask(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Intent intent=new Intent();
							intent.setAction(FileUploadProgress.NOTIFY_PROGRESS);
							intent.putExtra(FileUploadProgress.PUT_PROGRESS_TYPE,FileUploadProgress.TRANSFER_DOWNLOAD_COMPLETE );
							intent.putExtra(FileUploadProgress.FILE_DB_ID,db_id);
							ms.sendBroadcast(intent);
							Log.d("FileServer", "File Transfer Completed");
							MainActivity.TRANSFER=false;
		
						}
					});
				}
				//fileinfo.add(fi);
				
			return ;
			}
			i++;
		}
	}


	private void BroadcastDownloadProgress(int current_id2, int length) {
		// TODO Auto-generated method stub
		Intent intent=new Intent();
		intent.setAction(FileUploadProgress.NOTIFY_PROGRESS);
		intent.putExtra(FileUploadProgress.PUT_PROGRESS_TYPE,FileUploadProgress.DOWNLOAD );

		intent.putExtra(FileUploadProgress.PUT_PROGRESS, length);
		
		intent.putExtra(FileUploadProgress.FILE_DB_ID, current_id2);
		
		if(ms.id_progress.get(current_id2)!=null)
		{
			ms.id_progress.put(current_id2, ms.id_progress.get(current_id2)+length);
		}
		else
		{
			ms.id_progress.put(current_id2, (long) length);
		}
		ms.sendBroadcast(intent);

	}
	private void scanMedia(String path) {
	    File file = new File(path);
	    Uri uri = Uri.fromFile(file);
	    Intent scanFileIntent = new Intent(
	            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
	    ms.sendBroadcast(scanFileIntent);
	}
}
