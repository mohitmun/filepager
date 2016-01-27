package com.filepager.main;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
class ListHolder1{
	final public static int  CAT_PEOPLE=1;
	final  public static int  CAT_NETWORK=2;
	final  public static int CAT_REFRESH=3;
	int category=0;
	String ip;
	String hostname;
	boolean shareButtonVisibility=true;
}

class ListHolder {
	LinkedList<ListHolder1> all;
	public ListHolder() {
		all=new LinkedList<ListHolder1>();
	}
	public void addGuest(ListHolder1 lh1){
		for(ListHolder1 lh : all)
		{
			if(lh.ip.equals(lh1.ip))
				return;

		}
		all.add(lh1);

	}

	public void clear(){
		all.clear();
	}
}
public class ShareAdapter extends BaseAdapter {

	protected static final int SEND_FILE = 1;
	ListHolder1 refresh=new ListHolder1();
	ListHolder lh=new ListHolder();
	Context context;
	public ShareAdapter(Context c,ListHolder w) {
		// TODO Auto-generated constructor stub
		this.context=c;
		lh=w;
		
		refresh.hostname="Refresh";
		refresh.category=ListHolder1.CAT_REFRESH;
		refresh.ip="null";

	}

	class HolderView{
		public ImageView image;
		public TextView ip;
		public TextView hostname;
	}
	public int getCategoty(int arg){
		if(arg==0)
		{
			return refresh.category;
		}
		
		return lh.all.get(arg-1).category;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return lh.all.size()+1;
		
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		if(arg0==0)
		{
			return refresh;
		}
		else
		return lh.all.get(arg0-1);
	}

	

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	public void setButtonVisible(int position){
		lh.all.get(position).shareButtonVisibility=true;
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

			holder.hostname=(TextView)convertView.findViewById(R.id.ssid);
			holder.image=(ImageView)convertView.findViewById(R.id.ssid_image);
			convertView.setTag(holder);
		}
		else
		{
			holder = (HolderView)convertView.getTag();
		}
		ListHolder1 member;
		
		member=(ListHolder1)getItem(position);
		
		
		holder.hostname.setText(member.hostname/*+" "+member.ip*/);
		//holder.hostname.setTypeface(MainActivity.typeface);
		switch (member.category) {
		case ListHolder1.CAT_PEOPLE:
			holder.image.setImageResource(R.drawable.person);

			break;
		case ListHolder1.CAT_REFRESH:
			holder.image.setImageResource(R.drawable.refreah);
			break;
		case ListHolder1.CAT_NETWORK:
			holder.image.setImageResource(R.drawable.ic_action_network_wifi);
			break;
		default:
			break;
		}


		return convertView;
	}

}
