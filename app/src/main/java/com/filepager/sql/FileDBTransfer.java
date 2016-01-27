package com.filepager.sql;

import java.util.ArrayList;
import java.util.List;

import com.filepager.utils.IntentConstants;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FileDBTransfer extends Thread {
private static Context context;
DatabaseHandler db;
boolean run =true;
public FileDBTransfer(Context c) {
	context=c;
	db=new DatabaseHandler(context);
	
}
public void stopit()
{
	run=false;
}
public void run(){
	if(Build.VERSION.SDK_INT < 11)
		return;
	else{
	Uri uri = MediaStore.Files.getContentUri("external");
	ContentResolver cr = context.getContentResolver();
	
	// every column, although that is huge waste, you probably need
	// BaseColumns.DATA (the path) only.
	String[] projection = null;
	// exclude media files, they would be here also.
	String selection = null;
	String[] selectionArgs = null; // there is no ? in selection so null here
	String sortOrder = null; // unordered
	Cursor allNonMediaFiles = cr.query(uri, projection, selection, selectionArgs, sortOrder);
int count=0;
Log.d("path","Scanning started"+ allNonMediaFiles.moveToFirst());	
PhoneFiles pf=new PhoneFiles();
if(allNonMediaFiles.moveToFirst() && run)
	do{
	count++;
	String path=allNonMediaFiles.getString(1);
	pf.title=allNonMediaFiles.getString(10);
	if(pf.title==null){
		pf.title=allNonMediaFiles.getString(8);
	}
	if(allNonMediaFiles.getString(7)==null)
		continue;
	pf.path=path;
	pf.fileid=Integer.valueOf(allNonMediaFiles.getString(0));
	pf.other=" ";
	db.AddFTS(pf.fileid, pf.title, pf.path, pf.other);
	
}while(allNonMediaFiles.moveToNext()&& run);
Log.d("count","Scanning done with count c:"+ count);
name.edit().putBoolean(IntentConstants.IS_FILEDB_INDEXED, true).commit();

}

	Log.d("FileDBTransfer", "Done");
	}

public  void AddNewFiles(int id){
	String[] projection = null;
	ContentResolver cr = context.getContentResolver();
	Uri uri = MediaStore.Files.getContentUri("external");

	// exclude media files, they would be here also.
	String selection =  MediaStore.Files.FileColumns._ID+ " > '" +String.valueOf(id) +"'";
	String[] selectionArgs = null;// there is no ? in selection so null here
	String sortOrder = null; // unordered
	
	Cursor allNonMediaFiles = cr.query(uri, projection, selection, selectionArgs, sortOrder);	
	PhoneFiles pf=new PhoneFiles();
	if(allNonMediaFiles.moveToFirst())
		{
		do
		{
		String path=allNonMediaFiles.getString(1);
		pf.title=allNonMediaFiles.getString(10);
		if(allNonMediaFiles.getString(7)!=null )
		{	
		pf.path=path;
		pf.fileid=Integer.valueOf(allNonMediaFiles.getString(0));
		pf.other=" ";
		db.AddFTS(pf.fileid, pf.title, pf.path, pf.other);}
		}while(allNonMediaFiles.moveToNext());
}}
ContentResolver cr;
Uri uri;
String[] projection;
String selection;
Cursor allNonMediaFiles;
public SharedPreferences name;
public  boolean checkLastId(int id)
{
	if(cr==null)
	cr = context.getContentResolver();
	if(uri==null)
	uri = MediaStore.Files.getContentUri("external");

	// every column, although that is huge waste, you probably need
	// BaseColumns.DATA (the path) only.
	projection = null;

	// exclude media files, they would be here also.
	selection = MediaStore.Files.FileColumns._ID+" = '"+String.valueOf(id)+"'";
	
	allNonMediaFiles = cr.query(uri, projection, selection,null, null);	
	if(allNonMediaFiles.moveToFirst())
	{
		allNonMediaFiles.close();
		return true;
	}
	else
	{
		allNonMediaFiles.close();
		return false;
		
	}
	
		
}

}
