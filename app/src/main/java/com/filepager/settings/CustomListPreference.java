package com.filepager.settings;
import java.io.File;
import java.io.FileFilter;

import com.filepager.main.R;


import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
	
	public class CustomListPreference extends ListPreference {
	
		CustomListPreferenceAdapter customListPreferenceAdapter = null;
		Context mContext;
	
		public CustomListPreference(Context context, AttributeSet attrs) {
			super(context, attrs);
			mContext = context;
			 setEntries();
		       
		}
		public EntryValues getFolderList(String path){
			EntryValues ev=new EntryValues();
			FileFilter ff=new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
				
					return pathname.canWrite()&& pathname.isDirectory()&& !pathname.isHidden();
				}
			};
			File file=new File(path);
			File[] files=file.listFiles(ff);
			ev.folders=new String[files==null?1:files.length+1];
			ev.values=new String[files==null?1:files.length+1];
			if(files!=null)
			for(int i=1;i<files.length+1;i++)
			{
			ev.folders[i]=files[i-1].getName();
			ev.values[i]=files[i-1].getAbsolutePath();
			}
			ev.folders[0]="Folder Up";
			ev.values[0]=path;
			return ev;
			
		}
		class EntryValues
		{
			String[] folders;
			String[] values;
		}
	  
		 private void setEntries() {
				// TODO Auto-generated method stub
					 final EntryValues ev=getFolderList(Environment.getExternalStorageDirectory().getAbsolutePath());
				   setEntries(ev.folders);
				     setEntryValues(ev.values);
				      
			}
	
		
		@Override
		protected void onPrepareDialogBuilder(final Builder builder) {
			CharSequence[] entries = getEntries();
			final CharSequence[] entryValues = getEntryValues();
			if (entries == null || entryValues == null
					|| entries.length != entryValues.length) {
				throw new IllegalStateException(
						"ListPreference requires an entries array "
     					+"and an entryValues array which are both the same length");
			}
			
			// setting my custom list adapter
			customListPreferenceAdapter = new CustomListPreferenceAdapter(mContext,entries);
			builder.setAdapter(customListPreferenceAdapter, new OnClickListener() {
				
				@SuppressLint("NewApi")
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EntryValues ev=getFolderList(entryValues[which].toString());
				setEntries(ev.folders);
				setEntryValues(ev.values);
				customListPreferenceAdapter.setListItems(ev.folders);
				customListPreferenceAdapter.notifyDataSetChanged();
				
					}
			});
			builder.setPositiveButton("Set", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
						}
			});
		}
		

		private class CustomListPreferenceAdapter extends BaseAdapter {

			private final static int ICON_FOLDER = R.drawable.ic_folder;
			private final static int ICON_FILE = R.drawable.ic_file;
			
			private  CharSequence[] files; 
			private final LayoutInflater mInflater;
			
			public CustomListPreferenceAdapter(Context context, CharSequence[] entries) {
				mInflater = LayoutInflater.from(context);
				files=entries;
			}

		
			public void setListItems(CharSequence[] files) {
				this.files=files;
				notifyDataSetChanged();
			}

			@Override
		    public int getCount() {
				return files.length;
			}

			

			
			@Override
		    public CharSequence getItem(int position) {
				return files[position];
			}

			@Override
		    public long getItemId(int position) {
				return position;
			}

			@Override
		    public View getView(int position, View convertView, ViewGroup parent) {
				View row = convertView;
				ViewHolder holder = null;

				if (row == null) {
					row = mInflater.inflate(R.layout.file, parent, false);
					holder = new ViewHolder(row);
					row.setTag(holder);
				} else {
					// Reduce, reuse, recycle!
					holder = (ViewHolder) row.getTag();
				}
				
			holder.nameView.setText(getItem(position));
				
				
			holder.iconView.setImageResource(ICON_FOLDER);
				
				return row;
			}

			 class ViewHolder {
				TextView nameView;
				TextView fileSize;
				ImageView iconView;
				CheckBox cb;
				ViewHolder(View row) {
					nameView = (TextView) row.findViewById(R.id.file_name);
					iconView = (ImageView) row.findViewById(R.id.file_icon);
					 cb=(CheckBox)row.findViewById(R.id.checkbox);
					 cb.setVisibility(View.INVISIBLE);
				fileSize=(TextView)row.findViewById(R.id.file_size);
				}
			}

			
}
	}
