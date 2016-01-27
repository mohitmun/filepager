package com.filepager.main;

 
import java.io.File;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.filepager.sql.MyFile;

public class FileLogs extends Fragment implements OnItemClickListener {

	private static final int FILELOGGROUPID = 2;
	View rootview;
	private FileLogAdapter adapter;
	private ListView logListView;
	MyApp myApp;
	
	private MainActivity mainActivity;
	private boolean fromFilelogs=false;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		myApp=MyApp.getInstance();
		rootview=inflater.inflate(R.layout.file_logs, null);
		adapter= new FileLogAdapter(getActivity());
		logListView=(ListView)rootview.findViewById(R.id.file_log_list);
		logListView.setAdapter(adapter);
		loadFiles();
		//registerForContextMenu();
		logListView.setOnItemClickListener(this);
		mainActivity=(MainActivity)getActivity();
		return rootview;
	}
	
	
	public void registerForContextMenu(){
	registerForContextMenu(logListView);
	}
	public void UnregisterForContextMenu(){
		unregisterForContextMenu(logListView);
	}
		
	
	int i=0;
	public void updateProgress(int id,Long long1){
		i++;
		if(mainActivity!=null)
		{
		if(mainActivity.mBound && i%6==0)
		{
			adapter.setProgress(id,mainActivity.mService.id_progress.get(id));
		}
		}
		else 
		{
			}
		
	}
			
		
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info=(AdapterContextMenuInfo) item.getMenuInfo();
		int position=info.position;
		if(item.getGroupId()==FILELOGGROUPID)
		{switch (item.getItemId()) {
		case 0:
		
			openFile(position);
		return true;
		case 1:
		if(mainActivity.mBound)
			mainActivity.mService.resumeFile(adapter.getFileInfo(position).id,"192.168.0.105" );
		return true;
		case 2:
			
			if(mainActivity.mBound)
				mainActivity.mService.stopSendFile(adapter.getFileInfo(position).id);
				
		return true;
		
		default:
		break;
		}
		
		
		}
		
		return false;//super.onContextItemSelected(item);
	}
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		
		if (v.getId()==R.id.file_log_list) {
			 	
			    menu.setHeaderTitle(R.string.select_action);
			    String[] menuItems = getResources().getStringArray(R.array.file_log_action		);
			    for (int i = 0; i<menuItems.length; i++) {
			      menu.add(FILELOGGROUPID, i, i, menuItems[i]);
			      
			    }
			    if(adapter.getFileInfo(info.position).status==MyFile.STATUS_DOWNLOADED || adapter.getFileInfo(info.position).status==MyFile.STATUS_UPLOADED)
			    {
			    	menu.removeItem(1);
			    menu.removeItem(2);
			    }
		
		}
		
			    super.onCreateContextMenu(menu, v, menuInfo);
	}
	public void addToList(MyFile mf)
	{
		this.adapter.AddFile(mf);
		adapter.notifyDataSetChanged();
	}

	public void loadFiles() {
		// TODO Auto-generated method stub
		adapter.all.clear();
		List<MyFile> all=myApp.db.getAllFile();
		for(MyFile mf:all)
		{
			
			adapter.AddFile(mf);
		}
		
		adapter.notifyDataSetChanged();
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		openFile(arg2);
	}
	
	public void openFile(int arg2){
		Intent intent123 = new Intent(Intent.ACTION_VIEW);
		intent123.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		MyFile fi=adapter.getFileInfo(arg2);
		Uri uri123 = Uri.fromFile(new File(fi.path));
		if(MyApp.isDebuggable)
		mainActivity.makeToast("ft: "+fi.type);
		intent123.setDataAndType(uri123,fi.type);
		try
		{
		getActivity().startActivity(intent123);
		}
		catch (Exception e)
		{
			mainActivity.makeToast("File type not supported");
		}
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		loadFiles();
	}

	
}
