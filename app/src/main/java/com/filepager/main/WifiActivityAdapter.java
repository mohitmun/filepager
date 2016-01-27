package com.filepager.main;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class WifiHolder
{
	LinkedList<ScanResult> all = new LinkedList<ScanResult>();
	public void add(ScanResult sr)
	{
		all.add(sr);
	}
	public void clear()
	{
		all.clear();
	}
	
}
public class WifiActivityAdapter extends BaseAdapter {
	private Context context;
	private WifiHolder wh;
	public WifiActivityAdapter(Context c,WifiHolder wh)
	{
		context=c;
		this.wh=wh;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return wh.all.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return wh.all.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		HolderView holder = new HolderView();
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if(convertView==null)
		{
		convertView = mInflater.inflate(R.layout.wifi_list_row, null);
		
		holder.hostname=(TextView)convertView.findViewById(R.id.ssid);
		convertView.setTag(holder);
		}
		else
		{
			holder = (HolderView)convertView.getTag();
		}
		ScanResult sr = (ScanResult) getItem(position);
		
		holder.hostname.setText(sr.SSID);
				
		return convertView;
	}
	class HolderView{
		TextView hostname;
	}

}
