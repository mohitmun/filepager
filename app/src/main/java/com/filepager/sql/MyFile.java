package com.filepager.sql;

public class MyFile {
	public static final int STATUS_UPLOADED=1;
	public static final int STATUS_DOWNLOADED=2;
	public static final int STATUS_DOWNLOADING=3;
	public static final int STATUS_UPLOADING=4;
	public int id;
	public long tid;
	public String name;
	public String sender;
	public int blocks;
	public int blocksize;
	public long time;
	public String path;
	public long tsize;
	public String type;
	public int status;
	public double perprogress;
	public long realprogress;
	public String senderid="null";
	public String passcode="null";
	public int file_db_id=0; // oopposite persons 
}

class FileTid{
	public long tid;
	public long time;
}
