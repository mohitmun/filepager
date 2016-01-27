package com.filepager.main;

import com.filepager.udp.MasterService;
import com.filepager.utils.IntentConstants;
import com.filepager.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ConnectionDialog extends Activity {
	MasterService mService;
	boolean mBound;
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = ((com.filepager.udp.MasterService.LocalBinder) service).getService();
			mBound = true;
		Intent i=getIntent();
			final int fileid= i.getIntExtra(IntentConstants.FILE_DB_ID, -1);
		final int conid=i.getIntExtra("conid", -1);
		final String fsize=Utils.formatFileSize(i.getLongExtra(IntentConstants.RECEIVE_FILE_SIZE, 0));
		final String fname=i.getStringExtra(IntentConstants.RECEIVE_FILE_NAME);
		final String sender=i.getStringExtra(IntentConstants.SENDER);
		final String senderid=i.getStringExtra(IntentConstants.SENDER_ID);
		if(fileid ==-1)return;
	
		
		View checkBoxView = View.inflate(ConnectionDialog.this, R.layout.checkbox, null);
		CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.checkbox);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		    @Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    	if(isChecked)
		    	{
		    		if(!mService.RECEIVE_DEFAULTS.contains(senderid))
		    		mService.RECEIVE_DEFAULTS.add(senderid);
		    	}
		    	else 
		    		mService.RECEIVE_DEFAULTS.remove(senderid);
		    	
		    	}
		});

		
		
		AlertDialog.Builder adb=new AlertDialog.Builder(ConnectionDialog.this);
			adb.setMessage("Receive file "+fname+" ? ("+fsize+") from "+sender );
			adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				MainActivity.TRANSFER=true;
				mService.initNewFileDownload(conid, fileid);
				mService.readyToReceive(conid, fileid);
				finish();
				}
			});
			
			checkBox.setText(R.string.remember_choice);
			
			adb.setView(checkBoxView);
			adb.setNegativeButton("No", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				finish();	
				}
				
			});
			
			AlertDialog d=adb.create();
			d.setCanceledOnTouchOutside(false);
			d.show();
						}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.e("test", "service disconnect");
			mBound = false;
		}
	};

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
		super.onBackPressed();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		savedInstanceState=null;
		
		
		super.onCreate(savedInstanceState);
		
		}
   
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Intent intent = new Intent(this, MasterService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(mBound)
		{
			unbindService(mConnection);
		}
	
	}

}
