/*
 * Copyright (C) 2012 Paul Burke
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
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.filepager.main.MyApp;
import com.filepager.main.R;
import com.filepager.sql.MyFile;
import com.filepager.utils.Utils;

/**
 * Fragment that displays a list of Files in a given path.
 *
 * @version 2012-10-28
 *
 * @author paulburke (ipaulpro)
 *
 */
public class FileListFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<List<File>>, OnItemLongClickListener {

	private static final int LOADER_ID = 0;

	private FileListAdapter mAdapter;
	private String mPath;
	private String mAllowedFileExtension;

	

	/**
	 * Create a new instance with the given file path.
	 *
	 * @param path The absolute path of the file (directory) to display.
	 * @return A new Fragment with the given file path.
	 */
	public static FileListFragment newInstance(String path, String allowedFiles) {
		FileListFragment fragment = new FileListFragment();
		Bundle args = new Bundle();
		args.putString(FileChooserActivity.PATH, path);
		args.putString(FileChooserActivity.ALLOWED_FILE_EXTENSION, allowedFiles);
		fragment.setArguments(args);

		return fragment;
	}
FileChooserActivity fileChooserActivity;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new FileListAdapter(getActivity(),new FilesHolder());
		mPath = getArguments() != null ? getArguments().getString(
				FileChooserActivity.PATH) : Environment
				.getExternalStorageDirectory().getAbsolutePath();

		mAllowedFileExtension = getArguments() != null ? getArguments().getString(
                        FileChooserActivity.ALLOWED_FILE_EXTENSION) : "";
                    	
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterContextMenuInfo info=(AdapterContextMenuInfo) item.getMenuInfo();
		FileListAdapter adapter = (FileListAdapter) getListView().getAdapter();
		File file = (File) adapter.getItem(info.position);
		
		switch (item.getItemId()) {
			case 0:
				if (adapter != null) {
					FilesHolder fh=adapter.getFH();
					mPath = file.getAbsolutePath();
					((FileChooserActivity) getActivity()).onFileSelected(file);
					
					
					
				}
					
				break;

			case 1:
				
				Intent intent123 = new Intent(Intent.ACTION_VIEW);
				intent123.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				
				Uri uri123 = Uri.fromFile(file);
				intent123.setDataAndType(uri123,Utils.getMimeType(file.getAbsolutePath()));
				try
				{
				getActivity().startActivity(intent123);
				}
				catch (Exception e)
				{
					Toast.makeText(getActivity(), R.string.file_type_not_supported, Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
			}
			return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
FileListAdapter adapter = (FileListAdapter) getListView().getAdapter();
if (adapter != null) {
	File file = (File) adapter.getItem(info.position);
	if(!file.isDirectory())
	{menu.setHeaderTitle(R.string.select_action);
    menu.add(Menu.NONE, 0, 0, "Send file to "+fileChooserActivity.hostname);
    menu.add(Menu.NONE, 1, 1, "Open");
	
	}
	
	
}

	
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setEmptyText(getString(R.string.empty_directory));
		setListAdapter(mAdapter);
		setListShown(false);
		fileChooserActivity=(FileChooserActivity)getActivity();
    	
        registerForContextMenu(getListView());

		getLoaderManager().initLoader(LOADER_ID, null, this);

		super.onActivityCreated(savedInstanceState);
	}
 class FilesHolder{
	ArrayList<Uri> holder=new ArrayList<Uri>();
	public void addFile(File file,boolean b){
		if(b)
		{Toast.makeText(getActivity(), file.getName()+" selected", Toast.LENGTH_SHORT).show();
		Uri pathuri=Uri.fromFile(file);
		
		holder.add(pathuri);}
		else{
			Toast.makeText(getActivity(), file.getName()+" deselected", Toast.LENGTH_SHORT).show();
			Uri pathuri=Uri.fromFile(file);
			
			holder.remove(pathuri);
		}
			
	}
}
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		FileListAdapter adapter = (FileListAdapter) l.getAdapter();
		if (adapter != null) {
			File file = (File) adapter.getItem(position);
			FilesHolder fh=adapter.getFH();
			mPath = file.getAbsolutePath();
			((FileChooserActivity) getActivity()).onFileSelected(file,fh);
			
			
			
		}
	}

	@Override
	public Loader<List<File>> onCreateLoader(int id, Bundle args) {
		return new FileLoader(getActivity(), mPath, mAllowedFileExtension);
	}

	@Override
	public void onLoadFinished(Loader<List<File>> loader, List<File> data) {
		mAdapter.setListItems(data);

		if (isResumed())
			setListShown(true);
		else
			setListShownNoAnimation(true);
	}

	@Override
	public void onLoaderReset(Loader<List<File>> loader) {
		mAdapter.clear();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		File f=(File)mAdapter.getItem(arg2);
		if(f.isDirectory()){
			
		}
		else
		{
			
		}
		return true;
	}
}