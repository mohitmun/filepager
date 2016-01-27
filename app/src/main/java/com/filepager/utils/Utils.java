package com.filepager.utils;

import android.os.Environment;
import android.webkit.MimeTypeMap;

public class Utils {
	public static final int START_SEARCH_ACTIVITY = 23;
	public static String PAGE_FOLDER=Environment.getExternalStorageDirectory()+"/Page/";
	
	public static String getExtension(String fname){
		int l = fname.lastIndexOf('.');
		if(l==-1)
			return "";
		return fname.substring(fname.lastIndexOf('.')+1);
		}
	
	public static String getFileTitle(String fname){
		int l = fname.lastIndexOf('.');
		if(l==-1)
			return fname;
		 return fname.substring(0,fname.lastIndexOf('.'));
		}
	 public static String formatFileSize(long size) {
	        if (size < 1024) {
	            return String.format("%d B", size);
	        } else if (size < 1024 * 1024) {
	            return String.format("%.1f KB", size / 1024.0f);
	        } else if (size < 1024 * 1024 * 1024) {
	            return String.format("%.1f MB", size / 1024.0f / 1024.0f);
	        } else {
	            return String.format("%.1f GB", size / 1024.0f / 1024.0f / 1024.0f);
	        }
	    }

	public static String getMimeType(String url)
    {
    	
        String parts[]=url.split("\\.");
        String extension=parts[parts.length-1];
        String type = null;
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
            if(type==null)
            	return "application/vnd."+extension;
            return type;
            
        }
        return "unknown";
    }
}
