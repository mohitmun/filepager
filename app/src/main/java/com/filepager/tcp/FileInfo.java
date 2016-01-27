package com.filepager.tcp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

public class FileInfo {
	public String filename;
	public int blocks;
	public int blocksize;
	public String type;
	public int fileid;
	public long filesize;
	public File f;
	public int blockwritten=0;
	public FileOutputStream fos;
	public RandomAccessFile fra;
public String sender;
public String passcode;
public String senderid;
public int file_db_id;
public int my_file_db_id;
}
