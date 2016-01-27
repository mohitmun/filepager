package com.filepager.main;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.filepager.sql.PhoneFiles;


class FileSearchHolder
{
	LinkedList<PhoneFiles> all = new LinkedList<PhoneFiles>();
	public void add(PhoneFiles sr)
	{
		all.add(sr);
	}
	public void clear()
	{
		all.clear();
	}
	
}
public class FileSearchAdapter extends BaseAdapter {
	private Context context;
	private FileSearchHolder wh;
	public FileSearchAdapter(Context c,FileSearchHolder wh)
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
	public PhoneFiles getItem1(int arg0) {
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
		convertView = mInflater.inflate(R.layout.file_search_row, null);
		
		holder.name=(TextView)convertView.findViewById(R.id.file_name);

		holder.path=(TextView)convertView.findViewById(R.id.file_path);
		convertView.setTag(holder);
		}
		else
		{
			holder = (HolderView)convertView.getTag();
		}
		PhoneFiles sr = (PhoneFiles) getItem(position);
		
		holder.name.setText(sr.title);
		holder.path.setText(sr.path);
				
		return convertView;
	}
	
	class HolderView{
		TextView name;
		TextView path;
	}

}
