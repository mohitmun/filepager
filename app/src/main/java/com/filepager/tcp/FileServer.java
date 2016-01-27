package com.filepager.tcp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.filepager.interfaces.FileUploadProgress;
import com.filepager.main.ConnectionDialog;
import com.filepager.main.MainActivity;
import com.filepager.main.MyApp;
import com.filepager.sql.DatabaseHandler;
import com.filepager.sql.MyFile;
import com.filepager.udp.MasterService;
import com.filepager.udp.RestartMechanism;
import com.filepager.utils.IntentConstants;
import com.filepager.utils.TaskManager;
import com.filepager.utils.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Environment;
import android.util.Log;
class FileConnected{
	public int id;
	public FileServerHanldeSocket hs;
}

public class FileServer extends TcpServer{
	HandleData hd;
	FileServerHanldeSocket hs;
	
	LinkedList<FileConnected> connected = new LinkedList<FileConnected>();
	String TAG = "File Server";
	TaskManager tm ;
	MasterService masterService;
	DatabaseHandler datab;
	int count_id = 1;
	public FileServer(MasterService masterService)
	{
		this.masterService=masterService;
		datab= new DatabaseHandler(masterService);
		tm = new TaskManager();
		tm.start();
	}
	
	@Override
	public void doWithSocket(Socket s) {
		Log.d(TAG,"socket came");
		try {
			FileConnected temp = new FileConnected();
			temp.id=count_id;
			count_id++;			
			hs = new FileServerHanldeSocket(s, new FileServerHandleData(masterService,this), rm, masterService,temp.id);
			hs.start();
			temp.hs=hs;
			connected.add(temp);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void initNewDownload(int con_id,int file_id)
	{
		for(FileConnected fc : connected)
		{
			if(fc.id==con_id)
			{
				fc.hs.initNewFileDownload(file_id);;
				return;
			}
		}
	}
	public void readyToRecevie(int con_id,int file_id)
	{
		for(FileConnected fc : connected)
		{
			if(fc.id==con_id)
			{
				fc.hs.readyToReceive(file_id);
				return;
			}
		}
	}
	
	@Override
	public void run()
	{
		if(mainsocket!=null)
		{
			try {
				mainsocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
				Log.d(TAG,"Server started");
				startServer(36081);
	
	}
	
	RestartMechanism rm = new RestartMechanism() {

		@Override
		public void restart() {
			// Do something since its a server
		
		}
	};

	/// Whenever a socket comes in

	public void send(byte[] data)
	{
		hs.send(data);
	}
	public int convertToInt(byte[] four)
	{	int a = four[0] & 0xff;
		int b = four[1] & 0xff;
		int c = four[2] & 0xff;
		int d = four[3] & 0xff;
		return a*256*256*256 + b*256*256 + c*256 + d;
	}
	
}

class FileServerHandleData implements HandleDataPlus
{
	MasterService masterService;
	String TAG = "filehandele";
	TaskManager tm;
	public FileServerHanldeSocket sender;
	public int con_id;
	Intent intent;
	int blocksize = 512;
	//byte[] buffer1 = new byte[512*1024];
	byte[] buffer = new byte[512*1024];
	int transcationid=0;
	int current_id;
	private MyFileDownloadHandler filehandler;

public FileServerHandleData(MasterService ms,FileServer fs) {
	
	 masterService=ms;
	 tm=fs.tm;
	 filehandler=new MyFileDownloadHandler(ms);
	 
}
	
		@Override
	public void handle(byte[] data,int length) {
		// TODO Auto-generated method stub
		switch (data[0]) {
		case 1:		
			
			String xml = new String(data,1,length-1);
			Log.d(TAG, xml);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			try {
				db = dbf.newDocumentBuilder();
				Document doc = db.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
				Element root = (Element)doc.getElementsByTagName(MyXMLConstants.FILE).item(0);
				FileInfo fi = new FileInfo();
				fi.filename=root.getElementsByTagName(MyXMLConstants.FILE_NAME).item(0).getTextContent();
				fi.blocks=Integer.valueOf(root.getElementsByTagName(MyXMLConstants.BLOCKS).item(0).getTextContent());
				fi.blocksize=Integer.valueOf(root.getElementsByTagName(MyXMLConstants.BLOCK_SIZE).item(0).getTextContent());
				fi.fileid=Integer.valueOf(root.getElementsByTagName(MyXMLConstants.FILE_ID).item(0).getTextContent());
				fi.type=root.getElementsByTagName(MyXMLConstants.FILE_TYPE).item(0).getTextContent();
				fi.filesize=Long.valueOf(root.getElementsByTagName(MyXMLConstants.TOTAL_SIZE).item(0).getTextContent());
				fi.sender=root.getElementsByTagName(MyXMLConstants.SENDER).item(0).getTextContent();
				//Backward compatible code 
				if(root.getElementsByTagName(MyXMLConstants.SENDERID).getLength()>0)
				{
					fi.senderid=root.getElementsByTagName(MyXMLConstants.SENDERID).item(0).getTextContent();
					fi.passcode=root.getElementsByTagName(MyXMLConstants.PASSCODE).item(0).getTextContent();
					fi.file_db_id=Integer.valueOf(root.getElementsByTagName(MyXMLConstants.FILE_DB_ID).item(0).getTextContent());
				}
				else
				{
					fi.senderid="null";
					fi.passcode="null";
				}
				
				File f = new File(MyApp.DIRECTORY+ "/"+fi.filename);
				if(f.exists())
				{
					
					String ext = Utils.getExtension(fi.filename);
					if(!ext.equals(""))
						ext="."+ext;
					String title=Utils.getFileTitle(fi.filename);
					int tmp =0;
					do
					{
						tmp++;	
					f=new File(MyApp.DIRECTORY+"/"+title+" ("+String.valueOf(tmp)+")"+ext);
					
					}
					while(f.exists());
					
				}
				f.createNewFile();
				
				fi.f=f;
				fi.filename=f.getName();
				filehandler.addFileInfo(fi);
				
				if(!masterService.RECEIVE_DEFAULTS.contains(fi.senderid))
				{//Accept the users consent to begin downloading
				Intent i=new Intent(masterService,ConnectionDialog.class);
				i.putExtra(IntentConstants.FILE_DB_ID,fi.fileid );
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("conid",con_id);
				i.putExtra(IntentConstants.RECEIVE_FILE_NAME,fi.filename );
				i.putExtra(IntentConstants.RECEIVE_FILE_SIZE, fi.filesize);
				i.putExtra(IntentConstants.SENDER,fi.sender);
				i.putExtra(IntentConstants.SENDER_ID, fi.senderid);
				masterService.startActivity(i);
				//}
				}
				else
					{initNewFileDownload(fi);
					masterService.readyToReceive(con_id, fi.fileid);
					}
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 


			break;
		case 2:
			byte[] b = new byte[4];
			b[0] = data[1];
			b[1] = data[2];
			b[2] = data[3];
			b[3] = data[4];
			int fileid = convertToInt(b);
			b[0] = data[5];
			b[1] = data[6];
			b[2] = data[7];
			b[3] = data[8];
			int blockid = convertToInt(b);
			/*for(int i=9;i<length;i++)
			{
				buffer[i-9]=data[i];
			}*/
			try {
				final int ii = length;
				tm.addTask(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						BroadcastDownloadProgress(current_id,ii);
					}
				});
				Log.d("Testing", "Begins Write to file");
				filehandler.writeToFile(fileid, data, blockid, length-9,9);

				Log.d("Testing", "Over writing to file");
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			break;
			
			
			//Resume to know to send file
case 3:		
			Log.d("Resume ","Got intruction to send file");
			String xml1 = new String(data,1,length-1);
			
			DocumentBuilderFactory dbf1 = DocumentBuilderFactory.newInstance();
			DocumentBuilder db1;
			
			try {
				db = dbf1.newDocumentBuilder();
				Document doc = db.parse(new ByteArrayInputStream(xml1.getBytes("UTF-8")));
				Element root = (Element)doc.getElementsByTagName(MyXMLConstants.FILE_RESUME).item(0);
				FileInfo fi = new FileInfo();
			
				fi.blocks=Integer.valueOf(root.getElementsByTagName(MyXMLConstants.BLOCKS).item(0).getTextContent());
				fi.blocksize=Integer.valueOf(root.getElementsByTagName(MyXMLConstants.BLOCK_SIZE).item(0).getTextContent());
				fi.passcode=root.getElementsByTagName(MyXMLConstants.PASSCODE).item(0).getTextContent();
				fi.file_db_id=Integer.valueOf(root.getElementsByTagName(MyXMLConstants.FILE_DB_ID).item(0).getTextContent());
				fi.fileid=Integer.valueOf(root.getElementsByTagName(MyXMLConstants.FILE_ID).item(0).getTextContent());
				DatabaseHandler datab=new DatabaseHandler(masterService);
				MyFile temp = datab.getFile(fi.file_db_id);
				if(temp.passcode.equals(fi.passcode))
				{
					Log.d("Resume ","Sending file nowwwwwww");
					
					//Send file from hereeee hurrayyyyy
					File f = new File(temp.path);
					Log.d("Resume ","Sending file nowwwwwww2");
					sendfile(fi.fileid, f, fi.blocks, fi.file_db_id);
					
				}
				
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
				
			
			break;

		default:
			break;
		}

	}
			
	
	private byte[] convertInt2Byte(int a)
		{
			byte[] b = new byte[4];
			b[3] = (byte)(a&0xff);
			b[2] = (byte)(a>>8&0xff);
			b[1] = (byte)(a>>16&0xff);
			b[0] = (byte)(a>>24&0xff);
			return b;
		}
	private void BroadcastDownloadProgress(int current_id2, int length) {
		// TODO Auto-generated method stub
		Intent intent=new Intent();
		intent.setAction(FileUploadProgress.NOTIFY_PROGRESS);
		intent.putExtra(FileUploadProgress.PUT_PROGRESS_TYPE,FileUploadProgress.DOWNLOAD );

		intent.putExtra(FileUploadProgress.PUT_PROGRESS, length);
		
		intent.putExtra(FileUploadProgress.FILE_DB_ID, current_id2);
		
		if(masterService.id_progress.get(current_id2)!=null)
		{
			masterService.id_progress.put(current_id2, masterService.id_progress.get(current_id2)+length);
		}
		else
		{
			masterService.id_progress.put(current_id2, (long) length);
		}
		masterService.sendBroadcast(intent);

	}

	
	public void initNewFileDownload(int fileid){
		for(FileInfo fi1:filehandler.fileinfo){
			if(fi1.fileid==fileid){
				{initNewFileDownload(fi1);
				return;
				}
			}
		}
	}
	
	private void initNewFileDownload(FileInfo fi) {
		// TODO Auto-generated method stub
		DatabaseHandler db=new DatabaseHandler(masterService);
		MyFile mf=new MyFile();
		mf.path=MyApp.DIRECTORY+"/"+fi.filename;
		mf.name=fi.filename;
		mf.status=MyFile.STATUS_DOWNLOADING;
		mf.tsize=fi.filesize;
		mf.sender=fi.sender;
		mf.type=fi.type;
		mf.senderid=fi.senderid;
		mf.passcode=fi.passcode;
		mf.file_db_id=fi.file_db_id;
		mf.blocks=fi.blocks;
		
		int id=db.AddFile(mf);
		fi.my_file_db_id = id;
		current_id=id;
		
		Intent intent=new Intent();
		intent.setAction(FileUploadProgress.NOTIFY_PROGRESS);
		intent.putExtra(FileUploadProgress.PUT_PROGRESS_TYPE,FileUploadProgress.DOWNLOAD );
		intent.putExtra(FileUploadProgress.PUT_PROGRESS, 0);
		intent.putExtra(FileUploadProgress.FILE_DB_ID, id);
		if(masterService.id_progress.get(id)!=null)
		{
			//masterService.id_progress.put(id, masterService.id_progress.get(id));
			masterService.id_progress.put(id,(long)0);
		}
		else
			{
			masterService.id_progress.put(id, (long)0);
		}
		
		masterService.sendBroadcast(intent);
		
	}
	
	/* All Resume Functions come here */
	
	private void sendfile(int fileid,File f,int blocktoskip, int file_db_id) throws IOException
	{

		intent=new Intent();
		intent.setAction(FileUploadProgress.NOTIFY_PROGRESS);
		int blockid=0;
		try {
			Log.d("Files","Starting to send");
			FileInputStream fis = new FileInputStream(f);
			int l=0;
			fis.skip(blocksize*1024*blocktoskip);
			Log.d("Resume ","File is skipped");
			blockid=blocktoskip+1;
			do
			{
				l=fis.read(buffer);
				Log.d("Resume ","Sending");
				sendblock(l,buffer,fileid,blockid,file_db_id);
				Log.d("Resume ","Sent Block");
				blockid++;
			}while(l==blocksize*1024);

			sendBroadcastFIleUploaded(file_db_id);
			Log.d("Files","Sent");
			MainActivity.TRANSFER=false;
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	private void sendblock(int l,byte[] buffer,int fileid , int blockid, int file_db_id)
	{
		
		byte[] b = new byte[1];
		b[0]=3;
		sender.send(TcpClient.convert(1+4+4+l));
		sender.send(b);
		sender.send(convertInt2Byte(fileid));
		sender.send(convertInt2Byte(blockid));
		sender.send(buffer,l,file_db_id);
		
		intent.putExtra(FileUploadProgress.FILE_DB_ID, file_db_id);
		intent.putExtra(FileUploadProgress.PUT_PROGRESS, buffer.length);
		intent.putExtra(FileUploadProgress.PUT_PROGRESS_TYPE,FileUploadProgress.UPLOAD );
				
			if(masterService.id_progress.get(file_db_id)!=null)
			
				masterService.id_progress.put(file_db_id, masterService.id_progress.get(file_db_id)+l);
			
			else
			{
				masterService.id_progress.put(file_db_id,(long) l);
			}
			
		
		masterService.sendBroadcast(intent);
	
	}

	private void sendBroadcastFIleUploaded(int file_id) {
		// TODO Auto-generated method stub
		Intent intent=new Intent();
		intent.setAction(FileUploadProgress.NOTIFY_PROGRESS);
		intent.putExtra(FileUploadProgress.PUT_PROGRESS_TYPE,FileUploadProgress.TRANSFER_UPLOAD_COMPLETE);
		intent.putExtra(FileUploadProgress.FILE_DB_ID, file_id);
		
		masterService.sendBroadcast(intent);
		
	}
	
	public int convertToInt(byte[] four)
	{
		int a = four[0] & 0xff;
		int b = four[1] & 0xff;
		int c = four[2] & 0xff;
		int d = four[3] & 0xff;
		return a*256*256*256 + b*256*256 + c*256 + d;
	}

}
class FileServerHanldeSocket extends HandleSocketPlus
{
	FileServerHandleData hds;
	int con_id;
	public LinkedList<FileInfo> fileinfo;
	public FileServerHanldeSocket(Socket s,FileServerHandleData hds	,
			RestartMechanism rm, Context con , int con_id) throws IOException {

		super(s, hds, rm, con);
		this.hds=hds;
		hds.sender=this;
		this.con_id=con_id;
		hds.con_id=con_id;

	}
	public void readyToReceive(int file_id)
	{
				byte[] b = new byte[3];
				b = TcpClient.convert(4);
				byte[] c = new byte[1];
				c[0]=1;
				byte[]d = new byte[3];
				d = TcpClient.convert(file_id);
				send(b);
				send(c);
				send(d);
		
	}
	public void initNewFileDownload(int file_id)
	{
		hds.initNewFileDownload(file_id);
	}
	
	
}
	

