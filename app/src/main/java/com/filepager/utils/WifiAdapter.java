package com.filepager.utils;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.filepager.main.R;
class WifiHolder1{
	ScanResult sc;
	boolean shareButtonVisiblity=false;
}

public class WifiAdapter extends BaseAdapter {
	class WifiHolder{
		LinkedList<ScanResult> scanresult;
	}
	
	public static final int SEND_FILE = 1;
	WifiHolder wh = new WifiHolder();
	Context context;
public WifiAdapter(Context c,WifiHolder w) {
	// TODO Auto-generated constructor stub
	this.context=c;
	wh=w;
	
}

	 class HolderView{
		public TextView ssid;
	}


	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return wh.scanresult.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return wh.scanresult.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	public void setButtonVisible(int position){
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		HolderView holder = new HolderView();
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if(convertView==null)
		{
		convertView = mInflater.inflate(R.layout.share_list_row, null);
		holder.ssid=(TextView)convertView.findViewById(R.id.ssid);
		convertView.setTag(holder);
		}
		else
		{
			holder = (HolderView)convertView.getTag();
		}
		WifiHolder1 member=(WifiHolder1)getItem(position);
		holder.ssid.setText(member.sc.SSID);
		
		
		return convertView;
	}

}
