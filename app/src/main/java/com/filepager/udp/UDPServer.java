package com.filepager.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;

import com.filepager.interfaces.UdpPacketBroadCastReceiver;
import com.filepager.main.MyApp;
import com.filepager.settings.SettingsActivity;

public class UDPServer extends Thread {

	private static final int MAX_UDP_DATAGRAM_LEN = 1024;
	public static final String IP = "ip";
	public static final String HOSTNAME = "hostname";
	int port;
	private boolean bKeepRunning = true;
	DatagramSocket socket = null;
	RestartMechanism rm;
	boolean restart = true;
	DatagramPacket packet;
	byte[] lmessage;
	String TAG = "UDPServer";
	MasterService ms;
	private WifiManager mWifiManager;
	public UDPServer(int port, RestartMechanism rm,MasterService ms) {
		this.port = port;
		this.rm = rm;
		lmessage = new byte[MAX_UDP_DATAGRAM_LEN];
		packet = new DatagramPacket(lmessage, lmessage.length);
		this.ms=ms;
		myApp=MyApp.getInstance();
	}
MyApp myApp;



	public void run() {
		String message1;
		try {
			socket = new DatagramSocket(port);
			socket.setBroadcast(true);
			sendhello();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (bKeepRunning) {
			try {			

				Log.d(TAG, "Waiting for packet");
				socket.receive(packet);
				InetAddress ia = packet.getAddress();
				String ip = ia.getHostAddress(); // Ip address
				String hostname = ia.getHostName(); // Hostname is the machine
				// name
				message1 = new String(lmessage, 0, packet.getLength());
				String message=message1.substring(0,5);
				String name=message1.substring(5);
				
				Log.d(TAG, "Rcvd packet Message is " + message);
				if (message.equals("Hello")) {
					send(new String("Alive"+myApp.firstname+" "+myApp.lastname).getBytes(), ip);
					if(!checkIfSelf(ip))
					{
					Intent i=new Intent();
					i.putExtra(IP, ip);
					i.putExtra(HOSTNAME, name);
					i.setAction(UdpPacketBroadCastReceiver.ACTION_UDPACKET_RECEIVED);
					ms.sendBroadcast(i);
					}
					
				} else if (message.equals("Alive")) {
					if(!checkIfSelf(ip))
					{
					Intent i=new Intent();
					
					i.putExtra(IP, ip);
					i.putExtra(HOSTNAME, name);
					i.setAction(UdpPacketBroadCastReceiver.ACTION_UDPACKET_RECEIVED);
					ms.sendBroadcast(i);
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
				bKeepRunning = false;
			}
		}

		if (socket != null && bKeepRunning == false) {
			
			socket.close();
		}
		socket = null;
		packet = null;
		if (restart)
			rm.restart();

	}
	public boolean checkIfSelf(String ip)
	{
		LinkedList<String> temp = getLocalIpAddress();
		for(String a : temp)
		{
			if(a.equals(ip))
				return !PreferenceManager.getDefaultSharedPreferences(myApp).getBoolean(SettingsActivity.KEY_SHOWMYPROFILE, true);
		}
		return false;
	}
	public void send(byte[] a, String ip) throws IOException {
		DatagramPacket pack = new DatagramPacket(a, a.length,
				new InetSocketAddress(ip, port));
		if(socket!=null)
		socket.send(pack);
	}

	public void stopServer() {
		bKeepRunning = false;
		restart = false;
		if(socket!=null)
		socket.close();
		
}
	public void sendhello(){
		 mWifiManager = (WifiManager) ms.getSystemService(Context.WIFI_SERVICE);
		  ConnectivityManager connectivity = (ConnectivityManager) ms.getSystemService(Context.CONNECTIVITY_SERVICE);
		  NetworkInfo wifi = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		  InetAddress a = null;
		try {
			a = ms.getBroadcastAddress(mWifiManager, ms.getCodecIpAddress(mWifiManager, wifi));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			Log.d(TAG, "Sending broadcast to " + a.getHostAddress()); 
			
			send(new String("Hello"+myApp.firstname+" "+myApp.lastname).getBytes(),a.getHostAddress());
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	public LinkedList<String> getLocalIpAddress() {
		LinkedList<String> temp = new LinkedList<String>();
	    
		try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    String ip = inetAddress.getHostAddress();
	                    temp.add(ip);
	                    
	                   
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        Log.e(TAG, ex.toString());
	    }
		
		if(temp.size()==0)
			temp.add("192.168.43.1");
		return temp;
	}

}
