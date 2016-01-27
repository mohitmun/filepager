package com.filepager.utils;

import java.util.LinkedList;

import android.net.wifi.ScanResult;

public class WifiHolder {
	LinkedList<WifiHolder1> scanresult;
	public WifiHolder() {
		// TODO Auto-generated constructor stub
		scanresult=new LinkedList<WifiHolder1>();
	}
	public void addit(ScanResult sr)
	{
		WifiHolder1 member=new WifiHolder1();
		member.sc=sr;
		
		this.scanresult.add(member);
	}
	public void clear(){
		scanresult.clear();
	}

}
