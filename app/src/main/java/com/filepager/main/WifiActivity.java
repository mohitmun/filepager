package com.filepager.main;

import java.util.List;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.filepager.interfaces.ConnectedListner;
import com.filepager.interfaces.ScanedResultListner;

public class WifiActivity extends SherlockActivity implements ScanedResultListner, ConnectedListner, OnItemClickListener
{
	WifiActivityAdapter adapter;
	WifiHolder wh = new WifiHolder();
	ListView listview;
	WifiManager wifi;
	WiFiBroadCastReceiver wifireciever;
	boolean registered=false;
	private List<ScanResult> results;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi_list);
		adapter= new WifiActivityAdapter(this, wh);
		listview = (ListView)findViewById(R.id.listview2);
		listview.setAdapter(adapter);
		wifi=(WifiManager)getSystemService(Context.WIFI_SERVICE);
	 wifireciever = new WiFiBroadCastReceiver();
	 listview.setOnItemClickListener(this);
				
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		UnregisterBroadcastReciever();
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		UnregisterBroadcastReciever();
		super.onStop();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onRestart();
		RegisterBroadcastReceiver();
		if(isWifiEnabled())
		{
			wifi.startScan();
		}
		else
		{
			Toast.makeText(this,"Start Wifi", 1000);
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		
		super.onStart();
	}

	boolean isWifiEnabled() {
		// TODO Auto-generated method stub
		boolean r= wifi.isWifiEnabled();
	
		return r;
	}

	private void RegisterBroadcastReceiver() {
		// TODO Auto-generated method stub
		if(!registered)
		{
		wifireciever.setScanedResultListner(this);
		wifireciever.setConnectedListner(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(wifireciever, intentFilter);
		registered=true;
		}
	}
	private void UnregisterBroadcastReciever()
	{
		if(registered)
		{
		unregisterReceiver(wifireciever);
		registered=false;
		}
	}

	@Override
	public void OnScanedResult() {
		// TODO Auto-generated method stub
		results = wifi.getScanResults();
		wh.clear();
		for(ScanResult sr : results)
		{
			wh.add(sr);
			adapter.notifyDataSetChanged();
		}
		
	}

	@Override
	public void OnConnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		
	}

	private void connecttowifi(WifiConfiguration conf)
	{
		wifi.addNetwork(conf);
		List<WifiConfiguration> list = wifi.getConfiguredNetworks();
		    for( WifiConfiguration i : list ) {
		        if(i.SSID != null && i.SSID.equals(conf.SSID)) {
		            try {
		                wifi.disconnect();
		                wifi.enableNetwork(i.networkId, true);
		                wifi.reconnect();               
		                break;
		            }
		            catch (Exception e) {
		                e.printStackTrace();
		            }

		        }           
		    }
	}


}