package com.filepager.tcp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.filepager.interfaces.FileUploadProgress;
import com.filepager.main.MainActivity;
import com.filepager.udp.MasterService;
import com.filepager.udp.RestartMechanism;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HandleSocket extends Thread {
	Socket socket;
	BufferedReader in ;
	//DataOutputStream out;
	BufferedOutputStream out;
	HandleData hd;
	RestartMechanism rm;
	boolean run=true;
	boolean restart=true;
	Context context;
	public HandleSocket(Socket s,HandleData hds,RestartMechanism rm,Context con) throws IOException{
		socket=s;
		
	//	out = new DataOutputStream(socket.getOutputStream());
		out = new BufferedOutputStream(socket.getOutputStream());
		in = new BufferedReader(new InputStreamReader(
				socket.getInputStream(), "ISO-8859-1"));
		hd=hds;
		this.rm = rm;
		context=con;
	
	}
	
	Intent intent;
	public void send(byte[] data)
	{
		try {
			out.write(data);
			out.flush();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void send(byte[] data, int length, int file_db_id)
	{
		try {

			Log.d("Analyse","Putting the block");
			out.write(data,0,length);

			Log.d("Analyse","Block is putted");
			out.flush();

			Log.d("Analyse","Block is flushed");
			
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(run)
		{
			int today = -1;

			byte[] tmp = new byte[3];
			try {
			int a = in.read();
			Log.d("Socket","yaabyy dabby doo data in");
			tmp[0] = (byte) a;
			today = in.read();
			tmp[1] = (byte) today;
			today = in.read();
			tmp[2] = (byte) today;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (today!= -1) {
				int bu = tmp[0] & 0xff;
				int bu1 = tmp[1] & 0xff;
				int bu2 = tmp[2] & 0xff;
				
				int s = (bu*256*256 + bu1*256 + bu2);	
				final byte[] data = new byte[s];
				
				
				for (int i = 0; i < s; i++) {

					try {
						data[i] = (byte) in.read();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				///Do something with this
					
				}
				hd.handle(data);
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
