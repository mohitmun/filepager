/*
 * Copyright (C) 2013 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.filepager.afilechooser;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.filepager.afilechooser.FileListFragment.FilesHolder;
import com.filepager.main.FileSearchActivity;
import com.filepager.main.MainActivity;
import com.filepager.main.R;
import com.filepager.udp.MasterService;
import com.filepager.utils.IntentConstants;
import com.filepager.utils.Utils;

/**
 * Main Activity that handles the FileListFragments
 *
 * @version 2013-06-25
 *
 * @author paulburke (ipaulpro)
 * @author alexbbb
 */
@SuppressLint("NewApi")
public class FileChooserActivity extends FragmentActivity implements
		OnBackStackChangedListener {

    public static final String PATH = "path";
    public static final String ALLOWED_FILE_EXTENSION = "allowedFileExtension";
	public static final String EXTERNAL_BASE_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath();

	private static final boolean HAS_ACTIONBAR = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

	private FragmentManager mFragmentManager;
	private final BroadcastReceiver mStorageListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, R.string.storage_removed, Toast.LENGTH_LONG).show();
			finishWithResult(null,null);
		}
	};

	private String mPath;
	private String mAllowedExtension;
	String ip;
	public String hostname;
	private boolean mBound;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.chooser);

		mFragmentManager = getSupportFragmentManager();
		mFragmentManager.addOnBackStackChangedListener(this);
		
		Intent intent = getIntent();
		ip=intent.getStringExtra("ip");
		hostname=intent.getStringExtra("hostname");
		
	if (intent != null) {
		    final String customPath = intent.getStringExtra(PATH);
		    if (customPath != null && !customPath.equals("")) {
		        mPath = customPath;
		    }

		    final String allowedExtension = intent.getStringExtra(ALLOWED_FILE_EXTENSION);
		    if (allowedExtension != null) {
		        mAllowedExtension = allowedExtension;
		    }
		}

		if (mPath == null) {
    		if (savedInstanceState == null) {
    			mPath = EXTERNAL_BASE_PATH;
    			addFragment();
    		} else {
    			mPath = savedInstanceState.getString(PATH);
    		}
		}

		if (mAllowedExtension == null) {
		    if (savedInstanceState == null) {
		        mAllowedExtension = "";
		    } else {
		        mAllowedExtension = savedInstanceState.getString(ALLOWED_FILE_EXTENSION);
		    }
		}

		setTitle(mPath);
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterStorageListener();
	}

	@Override
	protected void onResume() {
		super.onResume();

		registerStorageListener();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(PATH, mPath);
		outState.putString(ALLOWED_FILE_EXTENSION, mAllowedExtension);
	}

	@Override
	public void onBackStackChanged() {

		int count = mFragmentManager.getBackStackEntryCount();
		if (count > 0) {
            BackStackEntry fragment = mFragmentManager.getBackStackEntryAt(count - 1);
            mPath = fragment.getName();
		} else {
		    mPath = EXTERNAL_BASE_PATH;
		}

		setTitle(mPath);
		if (HAS_ACTIONBAR) invalidateOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		   ActionBar actionBar = getActionBar();
	         
		if (HAS_ACTIONBAR) {
            boolean hasBackStack = mFragmentManager.getBackStackEntryCount() > 0;

            actionBar.setDisplayHomeAsUpEnabled(hasBackStack);
            actionBar.setHomeButtonEnabled(hasBackStack);
        }
		getMenuInflater().inflate(R.menu.file_manager_activity, menu);
		
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            mFragmentManager.popBackStack();
	            return true;
	        
	       /* case R.id.file_search:
	            Intent a = new Intent(FileChooserActivity.this,FileSearchActivity.class);
	    		startActivityForResult(a, Utils.START_SEARCH_ACTIVITY);
	      
	    		return true;
	        */case R.id.exit_file_viewer:
	        	finish();
	        	
	    		return true;
	     	
	    }

	    return super.onOptionsItemSelected(item);
	}

	public void showPopUp(View v){
		  	PopupMenu popup = new PopupMenu(this, v);
	        MenuInflater inflater = popup.getMenuInflater();
	        inflater.inflate(R.menu.file_type_selection, popup.getMenu());
	        popup.show();
	    
	}
	/**
	 * Add the initial Fragment with given path.
	 */
	private void addFragment() {
		FileListFragment fragment = FileListFragment.newInstance(mPath, mAllowedExtension);
		mFragmentManager.beginTransaction()
				.add(R.id.explorer_fragment, fragment).commit();
	}

	/**
	 * "Replace" the existing Fragment with a new one using given path.
	 * We're really adding a Fragment to the back stack.
	 *
	 * @param file The file (directory) to display.
	 */
	private void replaceFragment(File file) {
        mPath = file.getAbsolutePath();

        FileListFragment fragment = FileListFragment.newInstance(mPath, mAllowedExtension);
		mFragmentManager.beginTransaction()
				.replace(R.id.explorer_fragment, fragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
				.addToBackStack(mPath).commit();
	}

	/**
	 * Finish this Activity with a result code and URI of the selected file.
	 *
	 * @param file The file selected.
	 * @param fh 
	 */
	private void finishWithResult(File file, FilesHolder fh) {
		if (file != null) {
			Uri uri = Uri.fromFile(file);
			Intent i=new Intent();
			if(fh!=null){
			if(fh.holder.size()>0){
			i.putExtra(IntentConstants.IS_MULTIPLE, true);}
			else {
				i.putExtra(IntentConstants.IS_MULTIPLE, false);	
			}}
			
			i.putExtra("path", file.getAbsolutePath());
			i.setData(uri);
			if(fh!=null)
			i.putParcelableArrayListExtra("files", fh.holder);
			setResult(RESULT_OK, i);
			finish();
		} else {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestcode, int resultcode, Intent data) {
		// TODO Auto-generated method stub
	if(resultcode==RESULT_OK){
		boolean multiple=data.getBooleanExtra(IntentConstants.IS_MULTIPLE, false);
		final String path=data.getStringExtra("path");
		File f=new File(path);
		
		finishWithResult(f, null);
	}
	else{}
		super.onActivityResult(requestcode, resultcode, data);
	}
	
	/**
	 * Called when the user selects a File
	 *
	 * @param file The file that was selected
	 * @param fh 
	 */
	protected void onFileSelected(File file, FilesHolder fh) {
		if (file != null) {
			if (file.isDirectory()) {
				replaceFragment(file);
			} else {
				finishWithResult(file,fh);
			}
		} else {
			Toast.makeText(FileChooserActivity.this, R.string.error_selecting_file, Toast.LENGTH_SHORT).show();
		}
	}
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = ((com.filepager.udp.MasterService.LocalBinder) service).getService();
			mBound = true;
			
				for(Runnable a1 : temps)
				{
					a1.run();
				}
				temps=new LinkedList<Runnable>();
				
						}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.e("test", "service disconnect");
			mBound = false;
		}
	};
	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, MasterService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		
		
	}
	@Override
	protected void onStop() {
		super.onStop();
		if(mBound)
		{
			unbindService(mConnection);
			mBound=false;
		
		}
		
	}
	MasterService mService;
	protected void onFileSelected(File file) {

		
			final String path=file.getAbsolutePath();
			
			
			if(mBound)
				mService.sendFile(path, ip,hostname);
			else
			{
				temps.add(new Runnable() {
					public void run() {
						mService.sendFile(path, ip,hostname);
					
					}
				});
			}
		}
		
	
private LinkedList<Runnable> temps =  new LinkedList<Runnable>();
	/**
	 * Register the external storage BroadcastReceiver.
	 */
	private void registerStorageListener() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		registerReceiver(mStorageListener, filter);
	}

	/**
	 * Unregister the external storage BroadcastReceiver.
	 */
	private void unregisterStorageListener() {
		unregisterReceiver(mStorageListener);
	}

	
}
