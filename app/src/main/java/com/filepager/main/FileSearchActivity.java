package com.filepager.main;

import java.io.File;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.filepager.sql.DatabaseHandler;
import com.filepager.sql.PhoneFiles;
import com.filepager.utils.IntentConstants;

public class FileSearchActivity extends SherlockActivity implements OnItemClickListener
{
	FileSearchAdapter adapter;
	FileSearchHolder wh = new FileSearchHolder();
	ListView listview;
	DatabaseHandler db;
	GetSearch sr;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
		setTitle("Search File");
		setContentView(R.layout.file_list);
		adapter= new FileSearchAdapter(this, wh);
		listview = (ListView)findViewById(R.id.listview2);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(this);
		db = new DatabaseHandler(this);
		sr= new GetSearch(db);
		EditText et = (EditText) findViewById(R.id.search);
		et.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				wh.clear();
				String[] a = new String[1];
				a[0]=new String(s.toString());
				sr.cancel(true);
				sr= new GetSearch(db);
				sr.execute(a);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
				
	}
	

class GetSearch extends AsyncTask<String, Void, Void>
{
	DatabaseHandler db;
	public GetSearch(DatabaseHandler db)
	{
		super();
		this.db=db;		
	}
	List<PhoneFiles> all;
	@Override
	protected Void doInBackground(String... params) {
		
		all=db.searchFTS(params[0], 0, 20);
		
		return null;
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void onPostExecute(Void result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		for(PhoneFiles a : all)
		{
			
			wh.add(a);
		}
		adapter.notifyDataSetChanged();
	
	}
	
	
}


@Override
public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	// TODO Auto-generated method stub
	String path=adapter.getItem1(arg2).path;
	File f=new File(path);
	finishWithResult(f);
}

public void finishWithResult(File file){
	if (file != null) {
		Uri uri = Uri.fromFile(file);
		Intent i=new Intent();
		i.putExtra(IntentConstants.IS_MULTIPLE, false);	
		i.putExtra("path", file.getAbsolutePath());
		i.setData(uri);
		setResult(RESULT_OK, i);
		finish();
	} else {
		setResult(RESULT_CANCELED);
		finish();
	}
}

}