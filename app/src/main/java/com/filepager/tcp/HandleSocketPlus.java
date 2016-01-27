package com.filepager.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.filepager.interfaces.FileUploadProgress;
import com.filepager.udp.RestartMechanism;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HandleSocketPlus extends Thread {
	Socket socket;
	//BufferedReader in ;
	//BufferedInputStream in;
    InputStream in;
	DataOutputStream out;
	HandleDataPlus hd;
	RestartMechanism rm;
	boolean run=true;
	boolean restart=true;
	Context context;
	public HandleSocketPlus(Socket s,HandleDataPlus hds,RestartMechanism rm,Context con) throws IOException{
		socket=s;
		out = new DataOutputStream(socket.getOutputStream());
		in =socket.getInputStream();
		hd=hds;
		this.rm = rm;
		context=con;
	intent=new Intent();
	intent.setAction(FileUploadProgress.NOTIFY_PROGRESS);
	
	}
	InputStreamReader isr;
	Intent intent;
	public boolean send(byte[] data)
	{
		try {
			out.write(data);
			out.flush();
			
		} catch (IOException e) {
			
			e.printStackTrace();
		return false;
		}
		return true;
	}
	public boolean send(byte[] data, int length, int file_db_id)
	{
		try {
			out.write(data,0,length);
			out.flush();
			/*intent.putExtra(FileUploadProgress.FILE_DB_ID, file_db_id);
			intent.putExtra(FileUploadProgress.PUT_PROGRESS, data.length);
			intent.putExtra(FileUploadProgress.PUT_PROGRESS_TYPE,FileUploadProgress.UPLOAD );
			context.sendBroadcast(intent);*/
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	byte[] data = new byte[600*1024];
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(run)
		{
			int today = -1;
			int l1=0;
			int a1=3;
			int c1=0;
			
			byte[] tmp = new byte[3];
			try {
				while(l1!=-1 && a1>0)
				{
					
				l1 = in.read(tmp,c1,a1);
		
				
				a1=a1-l1;
				c1=c1+l1;
				}
	/*
			int a = in.read();
	*/		Log.d("Socket","yaabyy dabby doo data in");
	/*		tmp[0] = (byte) a;
			today = in.read();
			tmp[1] = (byte) today;
			today = in.read();
			tmp[2] = (byte) today;
	*/		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				l1=-1;
			}
			if (l1!= -1) {
				int bu = tmp[0] & 0xff;
				int bu1 = tmp[1] & 0xff;
				int bu2 = tmp[2] & 0xff;
				
				int s = (bu*256*256 + bu1*256 + bu2);	
			
				/*try {
					in.read(data, 0, s);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
				Log.d("Testing", "reading begins");
/*				for (int i = 0; i < s; i++) {

					try {
						data[i] = (byte) in.read();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}*/
				try {
					Log.d("Va","Value of s" + String.valueOf(s));
					int l=0;
					int a =s;
					int c=0;
					while(l!=-1 && a>0)
					{
						
					l = in.read(data,c,a);
					a=a-l;
					c=c+l;
					}
					//Log.d("Testing","Value of l is " + String.valueOf(l));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Log.d("Testing", "reading end");
				hd.handle(data,s);
			}
			else
			{
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				run=false;
				rm.restart();
			}
				

		}
		
		
	}	

}
