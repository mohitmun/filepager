package com.filepager.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.AttributeSet;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.filepager.main.MyApp;
import com.filepager.main.R;

public class FilePickerPreference extends Preference implements PropertyChangeListener, DialogInterface.OnClickListener {
        private static final String     TAG = "FilePickerPreference";
        AlertDialog                     dia;
        FilePicker                      fp;
Context c;
        public FilePickerPreference(Context context, AttributeSet attrs) {
                super(context, attrs);
                initialise(context, attrs);
       c=context;
        }

        public FilePickerPreference(Context context, AttributeSet attrs, int defStyle) {
                super(context, attrs, defStyle);
                initialise(context, attrs);
                c=context;;
        } 

        void initialise(Context context, AttributeSet attrs) {
                fp = new FilePicker(context, attrs);
                fp.setPropertyChangeListener(this);

                AlertDialog.Builder b = new AlertDialog.Builder(context);
                b.setPositiveButton(R.string.ok, this);
                b.setNegativeButton(R.string.cancel, this);
                b.setView(fp);
                b.setTitle(fp.getWorkingDir());

                dia = b.create();
        }

        public void propertyChange(PropertyChangeEvent event) {
                String propName = event.getPropertyName();
                if (propName.contentEquals("workingDir"))
                        dia.setTitle((String)event.getNewValue());
        }

        @Override protected void onClick() {
                fp.clearChoices();
                
                fp.setWorkingDIr(MyApp.DIRECTORY,c);
                dia.setTitle(fp.getWorkingDir());
                dia.show();
        }

        public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
/*                        String[] files = fp.getSelectedFiles();
                        int n = files.length;
*/						String dir=fp.getWorkingDir();
						
                        SharedPreferences.Editor ed = getEditor();
                        ed.putString(getKey(), dir);
                        ed.commit();
                }
        }
}