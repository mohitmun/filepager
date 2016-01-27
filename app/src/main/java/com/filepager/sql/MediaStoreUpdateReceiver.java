package com.filepager.sql;

import java.io.File;
import java.io.FileOutputStream;

import com.filepager.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MediaStoreUpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		
		FileDBTransfer ft=new FileDBTransfer(arg0);
	ft.start();
	}

	public void writedata(String data) {
        //BufferedWriter out = null;

        System.out.println(data);
        try{

            FileOutputStream out = new FileOutputStream(new File(Utils.PAGE_FOLDER+".tsxt.txt"));
            out.write(data.getBytes());
            data="/n";
            out.write(data.getBytes());
            
            out.close();  

              } catch (Exception e) { //fehlende Permission oder sd an pc gemountet}
                  System.out.println("CCCCCCCCCCCCCCCCCCCCCCCALSKDJLAK");
              }

        }
}
