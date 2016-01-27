package com.filepager.main;

import com.filepager.interfaces.ConnectedListner;
import com.filepager.interfaces.ScanedResultListner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.net.wifi.WifiInfo;

public class WiFiBroadCastReceiver extends BroadcastReceiver {

	ScanedResultListner srl;
	ConnectedListner cl;

	public void setScanedResultListner(ScanedResultListner scanedResultListner) {
		srl = scanedResultListner;
	}

	public void setConnectedListner(ConnectedListner connectedListner) {
		cl = connectedListner;
	}

	@Override
	public void onReceive(Context arg0, Intent intent) {
		// TODO Auto-generated method stub

		String action = intent.getAction();
		if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
			srl.OnScanedResult();
		} else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
			int iTemp = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
					WifiManager.WIFI_STATE_UNKNOWN);
			checkState(iTemp);
		} else if (action
				.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {

			DetailedState state = WifiInfo
					.getDetailedStateOf((SupplicantState) intent
							.getParcelableExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED));
			changeState(state);
		} else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			DetailedState state = ((NetworkInfo) intent
					.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO))
					.getDetailedState();
			changeState(state);
		}
	}

	private void changeState(DetailedState aState) {
		if (aState == DetailedState.SCANNING) {
			Log.d("wifiSupplicanState", "SCANNING");
		} else if (aState == DetailedState.CONNECTING) {
			Log.d("wifiSupplicanState", "CONNECTING");
		} else if (aState == DetailedState.OBTAINING_IPADDR) {
			Log.d("wifiSupplicanState", "OBTAINING_IPADDR");
		} else if (aState == DetailedState.CONNECTED) {
			cl.OnConnected();
			Log.d("wifiSupplicanState", "CONNECTED");
		} else if (aState == DetailedState.DISCONNECTING) {
			Log.d("wifiSupplicanState", "DISCONNECTING");
		} else if (aState == DetailedState.DISCONNECTED) {
			Log.d("wifiSupplicanState", "DISCONNECTTED");
		} else if (aState == DetailedState.FAILED) {
		}
	}

	public void checkState(int aInt) {
		if (aInt == WifiManager.WIFI_STATE_ENABLING) {
			Log.d("WifiManager", "WIFI_STATE_ENABLING");
		} else if (aInt == WifiManager.WIFI_STATE_ENABLED) {
			Log.d("WifiManager", "WIFI_STATE_ENABLED");
		} else if (aInt == WifiManager.WIFI_STATE_DISABLING) {
			Log.d("WifiManager", "WIFI_STATE_DISABLING");
		} else if (aInt == WifiManager.WIFI_STATE_DISABLED) {
			Log.d("WifiManager", "WIFI_STATE_DISABLED");
		}
	}
}
