package com.filepager.tcp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.util.Log;

import com.filepager.udp.RestartMechanism;

public class TcpClient {

	Socket socket;
	DataOutputStream out;
	BufferedReader in;
	HandleDataPlus hd;
	HandleSocketPlus hs;
	String ip;
	int port;
	public boolean restart =true;
	RestartMechanism rm = new RestartMechanism() {
		@Override
		public void restart() {
			// TODO Auto-generated method stub
			socket = null;
			out = null;
			in = null;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(restart)
				connect();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};
	private Context context;

	public TcpClient(HandleDataPlus hd, String ip, int port,Context c) {
		this.hd = hd;
		this.ip = ip;
		this.port = port;
		context=c;
	}

	public void connect() throws UnknownHostException, IOException {
		socket = new Socket(ip, port);
		socket.setTcpNoDelay(true);		
		hs = new HandleSocketPlus(socket, hd, rm,context);
		hs.start();
	}

	public boolean send(byte[] data) {
		if (socket!=null && socket.isConnected())
			{
			return hs.send(data);
			
			}
		return false;
	}

	public boolean send(byte[] data, int length, int file_db_id) {
		if (socket!=null && socket.isConnected())
			{
			return hs.send(data, length,file_db_id);
			
			}
	return false;
	}
	

	public static byte[] convert(int a) {
		byte[] b = new byte[3];
		b[2] = (byte) ((a) & 0xff);
		b[1] = (byte) ((a >> 8) & 0xff);
		b[0] = (byte) ((a >> 16) & 0xff);
		return b;

	}

	public void close() {
		// TODO Auto-generated method stub
		try {
			hs.run=false;
			if(socket!=null)
			socket.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
