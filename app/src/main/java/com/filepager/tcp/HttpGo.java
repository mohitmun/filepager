package com.filepager.tcp;

import java.io.IOException;
import java.net.ServerSocket;

public class HttpGo extends Thread{
	int PORT = 8080;
	   ServerSocket serverConnect;
	boolean runthis=true;
	String path;


		public HttpGo(String path)
		{
		try {
			serverConnect = new ServerSocket(PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.path =path;
		}
	
	
	   
@Override
	public void run() {
	
	while (runthis) //listen until user halts execution
    {
      HttpSender server = null;
	try {
		server = new HttpSender(
		
				serverConnect.accept(),path);
		
		Thread threadRunner = new Thread(server);
	      threadRunner.start(); //start thread
	      
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		runthis=false;
	} //instantiate HttpServer
      //create new thread
      
    
}

}
public void stopme()
{
	runthis=false;
	try {
		serverConnect.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}
