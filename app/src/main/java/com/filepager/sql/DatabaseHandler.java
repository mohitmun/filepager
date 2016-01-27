package com.filepager.sql;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables

	// Database Version
	private static final int DATABASE_VERSION = 5;

	// Database Name
	private static final String DATABASE_NAME = "Connect_M";

	// table name
	private static final String TABLE_FILES = "myfiles";
	private static final String TABLE_FILE_TID = "transaction_ids";
	private static final String TABLE_PHONE_FILES = "phone_files";

	// Files
	private static final String FILE_ID = "id";
	private static final String FILE_NAME = "name";
	private static final String FILE_FROM = "sender";
	private static final String FILE_BLOCKS = "blocks";
	private static final String FILE_BLOCKSIZE = "blocksize";
	private static final String FILE_TIME = "time";
	private static final String FILE_STORAGE = "storage";
	private static final String FILE_TOTALSIZE = "tsize";
	private static final String FILE_TYPE = "type";
	private static final String FILE_STATUS = "status";
	private static final String FILE_TID = "tid";
	
	//Additions for resume feature
	private static final String FILE_SENDER_ID = "senderid";
	private static final String FILE_PASSCODE = "passcode";
	private static final String FILE_DB_ID  ="senderdbid"; //this is the sender db id
	
//FTS Files
	private static final String FTS_TITLE = "title";
	private static final String FTS_PATH = "path";
	private static final String FTS_OTHER = "other";
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
this.context=context;
	}
	Context context;
FileDBTransfer fileDBTransfer;
	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_FILES_TABLE = "CREATE TABLE " + TABLE_FILES + "("
				+ FILE_ID + " INTEGER PRIMARY KEY, " 
				+ FILE_NAME + " TEXT, "
				+ FILE_FROM + " TEXT, " + FILE_BLOCKS + " INTEGER , "
				+ FILE_BLOCKSIZE + " INTEGER, " + FILE_TIME + " INTEGER, "
				+ FILE_STORAGE + " TEXT, " + FILE_TOTALSIZE + " TEXT, "
				+ FILE_TYPE + " TEXT, " + FILE_STATUS + " INTEGER, "
				+ FILE_TID + " INTEGER , "
				+ FILE_SENDER_ID + " TEXT , "
				
				+ FILE_PASSCODE + " TEXT , "
				+ FILE_DB_ID + " INTEGER "
				+ ")";

		String CREATE_FILE_TID_TABLE = "CREATE TABLE " + TABLE_FILE_TID + "("
				+ FILE_TID + " INTEGER PRIMARY KEY, " + FILE_TIME + "INTEGER "

				+ ")";
		
		String CREATE_PHONE_FILES_TABLE = 
				"CREATE VIRTUAL TABLE " + TABLE_PHONE_FILES +
                " USING fts3 (" +
                FTS_TITLE + ", " +
                FTS_PATH + ", " +
                FTS_OTHER + ")";
		
		db.execSQL(CREATE_FILES_TABLE);
		db.execSQL(CREATE_FILE_TID_TABLE);
		db.execSQL(CREATE_PHONE_FILES_TABLE);
		
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
String sql = new String();
		for(int i=oldVersion;i<newVersion;i++)
{

	switch(i)
	{
	
	case 4:
		sql = "ALTER TABLE "+ TABLE_FILES + " ADD COLUMN " + FILE_SENDER_ID +" TEXT DEFAULT 'null'";
		db.execSQL(sql);

		sql = "ALTER TABLE "+ TABLE_FILES + " ADD COLUMN " + FILE_PASSCODE +" TEXT DEFAULT 'null'";
		db.execSQL(sql);
		sql = "ALTER TABLE "+ TABLE_FILES + " ADD COLUMN " + FILE_DB_ID +" INTEGER DEFAULT '0'";
		db.execSQL(sql);
		break;
		default:
			break;
		
	}
}
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 * @return 
	 * 
	 */
	public int AddFile(MyFile f) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		values.put(FILE_NAME, f.name);
		values.put(FILE_FROM, f.sender);
		values.put(FILE_BLOCKS, f.blocks);
		values.put(FILE_BLOCKSIZE, f.blocksize);
		values.put(FILE_TIME, f.time);
		values.put(FILE_STORAGE, f.path);
		values.put(FILE_STATUS, f.status);
		values.put(FILE_TOTALSIZE, f.tsize);
		values.put(FILE_TYPE, f.type);
		values.put(FILE_TID, f.tid);
		values.put(FILE_PASSCODE, f.passcode);
		values.put(FILE_SENDER_ID,f.senderid);
		values.put(FILE_DB_ID,f.file_db_id);
		// Inserting Row
		try {
			int i=(int)db.insertOrThrow(TABLE_FILES, null, values);
			db.close();
			return i;	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
			
		}
		// Closing database connection
		
	}

	// Adding new tid
	public long AddTid(long t) {
		long i = 0;
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(FILE_TIME, t);
		// Inserting Row
		try{
			i = db.insert(TABLE_FILE_TID, null, values);			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.close(); // Closing database connection
		return i;
	}


	
	public String getFileName(int ID) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_FILES, new String[] { FILE_NAME },
				FILE_ID + "=?", new String[] { String.valueOf(ID) }, null,
				null, null, null);
		if (cursor.getCount() > 0)

		{
			cursor.moveToFirst();
			return String.valueOf(cursor.getString(0));
		}
		return "";

	}
	
	
	public List<MyFile> getAllFile() {
		List<MyFile> fileList = new ArrayList<MyFile>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_FILES;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				MyFile f = new MyFile();
				f.id = cursor.getInt(0);
				f.name = cursor.getString(1);
				f.sender = cursor.getString(2);
				f.blocks = cursor.getInt(3);
				f.blocksize = cursor.getInt(4);
				f.time = cursor.getInt(5);
				f.path = cursor.getString(6);
				f.tsize = cursor.getLong(7);
				f.type = cursor.getString(8);
				f.status = cursor.getInt(9);
				f.tid = cursor.getInt(10);
				f.senderid = cursor.getString(11);
				f.passcode = cursor.getString(12);
				f.file_db_id = cursor.getInt(13);
				fileList.add(f);

			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		// return file list
		return fileList;
	}

	public MyFile getFile(int id) {
		MyFile f = new MyFile();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_FILES, new String[] { FILE_ID,
				FILE_NAME, FILE_FROM, FILE_BLOCKS, FILE_BLOCKSIZE, FILE_TIME,
				FILE_STORAGE, FILE_TOTALSIZE, FILE_TYPE, FILE_STATUS, FILE_TID,FILE_SENDER_ID,FILE_PASSCODE,FILE_DB_ID }, FILE_ID
				+ "=?", new String[] { String.valueOf(id) }, null, null, null,
				null);

		if (cursor != null) {
			cursor.moveToFirst();
		}

		f.id = cursor.getInt(0);
		f.name = cursor.getString(1);
		f.sender = cursor.getString(2);
		f.blocks = cursor.getInt(3);
		f.blocksize = cursor.getInt(4);
		f.time = cursor.getInt(5);
		f.path = cursor.getString(6);
		f.tsize = cursor.getLong(7);
		f.type = cursor.getString(8);
		f.status = cursor.getInt(9);
		f.tid = cursor.getInt(10);
		f.senderid = cursor.getString(11);
		f.passcode = cursor.getString(12);
		f.file_db_id = cursor.getInt(13);

		return f;
	}
	
	public List<FileTid> getAllTid(int limit) {
		List<FileTid> contactList = new ArrayList<FileTid>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_FILE_TID;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				FileTid f = new FileTid();
				f.tid = cursor.getInt(0);
				f.time = cursor.getInt(1); 

				contactList.add(f);

			} while (cursor.moveToNext());
		}

		// return contact list
		return contactList;
	}
	public int setFileStatus(int i,int status) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(FILE_STATUS, status);
		// updating row
		return db.update(TABLE_FILES, values, FILE_ID + " = ?",
				new String[] { String.valueOf(i) });
	}

	public FileTid getFileTid(long tid) {
		FileTid f = new FileTid();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_FILE_TID, new String[] { FILE_TID, FILE_TIME
				}, FILE_TID	+ "=?", new String[] { String.valueOf(tid) }, null, null, null,
				null);

		if (cursor != null) {
			cursor.moveToFirst();
		}

		f.tid = cursor.getInt(0);
		f.time = cursor.getInt(1);

		return f;
	}

	public void deleteLogs(boolean all) {
		// TODO Auto-generated method stub
			all=true;;
			SQLiteDatabase db = this.getWritableDatabase();
			if(!all)
				db.execSQL("DELETE FROM "+TABLE_FILES +" WHERE "+FILE_STATUS + " = "+ MyFile.STATUS_DOWNLOADED +" OR "+ FILE_STATUS +" = "+MyFile.STATUS_UPLOADED);
			else 
				db.execSQL("DELETE FROM "+TABLE_FILES);
			db.close();
		
	}
	
	public long AddFTS(int fileid, String title , String path , String other) {
		long i = 0;
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("docid", fileid);
		values.put(FTS_TITLE, title);
		values.put(FTS_PATH, path);
		values.put(FTS_OTHER, other);
		// Inserting Row
		try{
			i = db.insert(TABLE_PHONE_FILES, null, values);			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.d("FTS","Error in add fts");
		}
		db.close(); // Closing database connection
		return i;
	}

	public List<PhoneFiles> searchFTS(String text,int start , int end) {
		List<PhoneFiles> contactList = new ArrayList<PhoneFiles>();
		// Select All Query
		String selectQuery = "SELECT  docid ," +FTS_TITLE +"," + FTS_PATH +"," + FTS_OTHER +" FROM " + TABLE_PHONE_FILES + " WHERE " + TABLE_PHONE_FILES +" MATCH ? ;";
		

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, new String [] {text+"* "});
		
	int count =start;	
		// looping through all rows and adding to list
		if (cursor.moveToPosition(start)) {
			do {
				PhoneFiles tmp =  new PhoneFiles();
				tmp.fileid = cursor.getInt(0);
				tmp.path = cursor.getString(2);
				tmp.title = cursor.getString(1);
				tmp.other = cursor.getString(3);
				contactList.add(tmp);
				count++;
			} while (cursor.moveToNext()&&count<end);
		}

		// return contact list
		return contactList;
	}


	public long updateFTS(int fileid, String title , String path , String other) {
		long i = 0;
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(FTS_TITLE, title);
		values.put(FTS_PATH, path);
		values.put(FTS_OTHER, other);
		// Inserting Row
		try{
			i=db.update(TABLE_PHONE_FILES, values, "docid = ?", new String[]{String.valueOf(fileid)});
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.close(); // Closing database connection
		return i;
	}
	
	public long deleteFTS(int fileid,SQLiteDatabase db) {
		long i = 0;
		try{
			i=db.delete(TABLE_PHONE_FILES, "docid = ?", new String[]{String.valueOf(fileid)});
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 // Closing database connection
		return i;
	}

public void updateDB(){
	fileDBTransfer =new FileDBTransfer(context);
	Log.d("FileDb","Updating File db");
	List<PhoneFiles> contactList = new ArrayList<PhoneFiles>();
	// Select All Query
	String selectQuery = "SELECT  docid FROM " + TABLE_PHONE_FILES ;
	
int a =0;
	SQLiteDatabase db = this.getWritableDatabase();
	Cursor cursor = db.rawQuery(selectQuery,null);
	// looping through all rows and adding to list
	if (cursor.moveToFirst()) {
		do {
			
			a= cursor.getInt(0);
			if(!fileDBTransfer.checkLastId(a))
			{
				deleteFTS(a,db);
			}
			
		} while (cursor.moveToNext());
	}
	Log.d("FileDb","Completed checking");
	selectQuery = "SELECT  MAX(docid) FROM " + TABLE_PHONE_FILES ;
	db.close();
	db = this.getWritableDatabase();
	cursor = db.rawQuery(selectQuery,null);
	int max=0;
	if (cursor.moveToFirst()) {
		max = cursor.getInt(0);
		fileDBTransfer.AddNewFiles(max);
	}
	Log.d("FileDb","completed adding");
}
	


}