package com.filepager.interfaces;

import com.filepager.udp.UDPServer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class UdpPacketBroadCastReceiver extends BroadcastReceiver{

	public final static String ACTION_UDPACKET_RECEIVED="hello";
	@Override
	public void onReceive(Context arg0, Intent intent) {
		// TODO Auto-generated method stub
		if(intent!=null){
			if(intent.getAction().equals(ACTION_UDPACKET_RECEIVED)){
			String ip=intent.getStringExtra(UDPServer.IP);
			String hostname=intent.getStringExtra(UDPServer.HOSTNAME);
				OnUDPacketReceived(ip,hostname);
			}
		}
	}
	public  abstract void OnUDPacketReceived(String ip, String hostname);

}
