package com.filepager.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

public abstract class TcpServer extends Thread{
	public ServerSocket mainsocket;
	private NotificationManager noti_manager;
	private Builder builder;
	public HashMap<Integer ,Integer > id_progress=new HashMap<Integer, Integer>();
	public void stopServer(){
		if(mainsocket!=null)
			try {
				mainsocket.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		
	}
	public void startServer(int port)
	{
		  Socket socket = null;
            try {
            	mainsocket = new ServerSocket();
            	mainsocket.setReceiveBufferSize(512*1024);
            	
            	mainsocket.bind(new InetSocketAddress(port));
            	//mainsocket = new ServerSocket(port);
		        	
            } catch (IOException e) {
		               e.printStackTrace();
		               
		     }
            
      //      Log.d("Registat",mainsocket.getInetAddress().getHostAddress());
            
            while(!mainsocket.isClosed())
            {
            	try {
            		Log.d("Registar", "Waiting for connection");
					socket = mainsocket.accept();
					Log.d("Socket","Socket receive buffer size is " + String.valueOf(socket.getReceiveBufferSize()));
					doWithSocket(socket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}      	
            	
            }
	}
	
	public abstract void doWithSocket(Socket s);
	
	

}
