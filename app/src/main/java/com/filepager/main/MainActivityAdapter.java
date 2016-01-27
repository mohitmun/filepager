package com.filepager.main;

import java.util.LinkedList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MainActivityAdapter extends FragmentPagerAdapter {
	
	FileLogs filelogs;
	ShareList sharelist;
	String type;
	LinkedList<String> tabs;
	public void settype(String s){
		type=s;
	}

	  public MainActivityAdapter(FragmentManager fm) {
	        super(fm);
	        filelogs=new FileLogs();
	        sharelist =new ShareList();
	        tabs = new LinkedList<String>();
	}

	    @Override
	    public Fragment getItem(int i) {
	        switch (i) {
	            case 0:
	            	return sharelist;
		         	
	            case 1:
	               return filelogs;
	   	            
	            default:
	                // The other sections of the app are dummy placeholders.
	          
	                return new FileLogs();
	        }
	    }

	    @Override
	    public int getCount() {
	        return 2;
	    }

	    @Override
	    public CharSequence getPageTitle(int position) {
	        return  tabs.get(position);
	    }

		public void setTabsName(LinkedList<String> tabs) {
			// TODO Auto-generated method stub
			this.tabs=tabs;
		}
		
		public boolean addToShareList(ListHolder1 lh)
		{
			sharelist.addToList(lh);
			return true;
		}

}
