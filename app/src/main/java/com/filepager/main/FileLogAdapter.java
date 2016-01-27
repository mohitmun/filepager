package com.filepager.main;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.filepager.sql.MyFile;
import com.filepager.utils.Utils;

public class FileLogAdapter extends BaseAdapter {
	LinkedList<MyFile> all;
	Context context;
	public FileLogAdapter(Context con){
		all=new LinkedList<MyFile>();
		context=con;
	}
	
	public void AddFile(MyFile mf){
		all.add(0, mf);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return all.size();
	}
	public String getFilePath(int arg0) {
		// TODO Auto-generated method stub
		return all.get(arg0).path;
	}
	@Override
	public Object getItem(int arg0) {
		return all.get(arg0);
	}
	public MyFile getFileInfo(int arg0) {
		// TODO Auto-generated method stub
		return all.get(arg0);
	}
	
	public void setProgress(int id,Long long1){
		
		for(MyFile mf:all){
			if(mf.id==id){
				mf.perprogress=(double)(((double)long1/mf.tsize)*100);
				mf.realprogress=long1;
			}
		}
		notifyDataSetChanged();
	}
	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	class ViewHolder{
		TextView filename;
		TextView sender;
		ProgressBar pb;
		ImageView fileState;
		ImageView cancle;
		LinearLayout progress;
		TextView textprogress;
		public ViewHolder(View row){
			filename=(TextView) row.findViewById(R.id.file_log_name);
		pb=(ProgressBar)row.findViewById(R.id.progress);
		sender=(TextView)row.findViewById(R.id.file_log_from);
	fileState=(ImageView)row.findViewById(R.id.file_state);
		cancle=(ImageView)row.findViewById(R.id.cancle);
	progress=(LinearLayout)row.findViewById(R.id.progress_);
		cancle.setVisibility(View.GONE);
		textprogress=(TextView)row.findViewById(R.id.file_log_text_progress);
		}
	}
	@Override
	public View getView(int arg0, View convertview, ViewGroup arg2) {
		final MyFile myFile=(MyFile)this.getItem(arg0);
		
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		ViewHolder holder;
		if(convertview==null)
		{
			convertview=mInflater.inflate(R.layout.file_log_row, null);
			holder=new ViewHolder(convertview);
			holder.cancle.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
						((MainActivity)context).stopSending(myFile.id);
				}
			});
			convertview.setTag(holder);
		}
		else
		{
			holder=(ViewHolder) convertview.getTag();
		}
		holder.filename.setText(myFile.name);
		holder.progress.setVisibility(View.VISIBLE);
		switch (myFile.status) {
		case MyFile.STATUS_DOWNLOADED:
			holder.progress.setVisibility(View.GONE);
			holder.sender.setText("From "+myFile.sender);
			holder.fileState.setImageResource(R.drawable.download);
			holder.textprogress.setText("Received");
			break;
		case MyFile.STATUS_UPLOADED:
			holder.progress.setVisibility(View.GONE);
			holder.sender.setText("To "+myFile.sender);
			holder.fileState.setImageResource(R.drawable.upload);
			holder.textprogress.setText("Sent");
			break;
		
		case MyFile.STATUS_UPLOADING:
			holder.sender.setText("To "+myFile.sender);
			if((int)myFile.perprogress==0){
				holder.fileState.setImageResource(R.drawable.warning);
				holder.progress.setVisibility(View.GONE);
				holder.textprogress.setText(R.string.cancel);				
			}else
			{
				holder.pb.setProgress((int)myFile.perprogress);
			holder.fileState.setImageResource(R.drawable.upload);
			holder.progress.setVisibility(View.VISIBLE);
			holder.textprogress.setText(Utils.formatFileSize(myFile.realprogress)+"/"+Utils.formatFileSize(myFile.tsize));
			}
			break;
		
		case MyFile.STATUS_DOWNLOADING:
			holder.sender.setText("From "+myFile.sender);
			if((int)myFile.perprogress==0){
				holder.fileState.setImageResource(R.drawable.warning);
				holder.progress.setVisibility(View.GONE);
				holder.textprogress.setText(R.string.cancel);				
											
			}else
			{holder.pb.setProgress((int)myFile.perprogress);
			holder.fileState.setImageResource(R.drawable.download);
			holder.progress.setVisibility(View.VISIBLE);
			holder.textprogress.setText(Utils.formatFileSize(myFile.realprogress)+"/"+Utils.formatFileSize(myFile.tsize));
			}	break;
		
		default:
			break;
		}
		return convertview;
	}

	
}
