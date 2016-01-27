package com.filepager.afilechooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.filepager.afilechooser.FileListFragment.FilesHolder;
import com.filepager.main.R;
import com.filepager.utils.Utils;

public class FileListAdapter extends BaseAdapter {

	private final static int ICON_FOLDER = R.drawable.ic_folder;
	private final static int ICON_FILE = R.drawable.ic_file;

	private List<File> mFiles = new ArrayList<File>();
	private final LayoutInflater mInflater;
	private FilesHolder fileholder;
	public FileListAdapter(Context context, FilesHolder filesHolder) {
		mInflater = LayoutInflater.from(context);
		fileholder=filesHolder;
	}

	public ArrayList<File> getListItems() {
		return (ArrayList<File>) mFiles;
	}

	public void setListItems(List<File> files) {
		this.mFiles = files;
		notifyDataSetChanged();
	}

	@Override
    public int getCount() {
		return mFiles.size();
	}

	public void add(File file) {
		mFiles.add(file);
		notifyDataSetChanged();
	}

	public void clear() {
		mFiles.clear();
		notifyDataSetChanged();
	}

	@Override
    public Object getItem(int position) {
		return mFiles.get(position);
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

		// Get the file at the current position
		final File file = (File) getItem(position);

		// Set the TextView as the file name
		holder.nameView.setText(file.getName());
		
		if(file.isDirectory())
		{holder.cb.setVisibility(View.INVISIBLE);
		if(file.list()!=null)
		holder.fileSize.setText(file.list().length+" files");
		}
		else
		{		holder.cb.setVisibility(View.INVISIBLE);	
		holder.fileSize.setText(Utils.formatFileSize(file.length()));
		
		}holder.cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				
				fileholder.addFile(file, arg0.isChecked());
			}
		});
		// If the item is not a directory, use the file icon
	
		holder.iconView.setImageResource(file.isDirectory() ? ICON_FOLDER
				: ICON_FILE);
		if(position==0)
		if(file.getParentFile()!=null){
		
			holder.fileSize.setText("folder up");
			holder.iconView.setImageResource(R.drawable.ic_action_back);
		}
		
		return row;
	}

	static class ViewHolder {
		TextView nameView;
		TextView fileSize;
		ImageView iconView;
		CheckBox cb;
		ViewHolder(View row) {
			nameView = (TextView) row.findViewById(R.id.file_name);
			iconView = (ImageView) row.findViewById(R.id.file_icon);
			 cb=(CheckBox)row.findViewById(R.id.checkbox);
		fileSize=(TextView)row.findViewById(R.id.file_size);
		}
	}

	public FilesHolder getFH() {
	
		return fileholder;
	}
}
