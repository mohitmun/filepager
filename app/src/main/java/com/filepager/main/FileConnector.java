package com.filepager.main;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.filepager.interfaces.FileUploadProgress;
import com.filepager.tcp.FileInfo;
import com.filepager.tcp.HandleDataPlus;
import com.filepager.tcp.MyFileDownloadHandler;
import com.filepager.tcp.MyXMLConstants;
import com.filepager.tcp.TcpClient;
import com.filepager.udp.MasterService;
import com.filepager.utils.TaskManager;
import com.filepager.utils.Utils;

public class FileConnector {
	HandleDataPlus hd;
	TcpClient tcpclient;
	int count = 1;
	int blocksize = 512; // in KB
	byte[] buffer = new byte[512 * 1024];
	private MasterService context;
	private LinkedList<FileHolder> filestosend = new LinkedList<FileHolder>();
	int noOfFilesToSend=0;
	Intent intent;
	MyFileDownloadHandler filehandler;
	public FileConnector(MasterService c) {
		context = c;
		filehandler = new MyFileDownloadHandler(c);
		hd = new HandleDataPlus() {
			@Override
			public void handle(byte[] data, int length) {
				// TODO Auto-generated method stub
				switch (data[0]) {
				case 1:
					// Send data
					int id = data[1] * 256 * 256 + data[2] * 256 + data[3];
					sendFileFromToBeSent(id);
					break;
				// Resume feature , here the block comes from the file server of
				// the sender
				case 3:
					Log.d("Resume", "Block Rcvd");
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
					for (int i = 9; i < length; i++) {
						buffer[i - 9] = data[i];
					}
					try {
						final int ii = length;
						
									 
						filehandler.writeToFileRandom(fileid, buffer, blockid,
								length - 9);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					break;
				default:
					break;
				}
			}
		};
		intent = new Intent();
		intent.setAction(FileUploadProgress.NOTIFY_PROGRESS);

	}
//Generally the port is 36081
	public void connect(String ip, int port) {
		tcpclient = new TcpClient(hd, ip, port, context);
		tcpclient.restart = false;
		MainActivity.TRANSFER = true;
		
		try {
			tcpclient.connect();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public int sendFileInfo(File f, String sender, int passcode,
			String senderid, int file_db_id)
			throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		Document doc = documentBuilder.newDocument();
		Element root = doc.createElement(MyXMLConstants.FILE);
		Element fileid = doc.createElement(MyXMLConstants.FILE_ID);
		fileid.appendChild(doc.createTextNode(String.valueOf(count)));
		int i = count;
		count++;

		Element blocksize = doc.createElement(MyXMLConstants.BLOCK_SIZE);
		blocksize.appendChild(doc.createTextNode("512"));

		Element blocks = doc.createElement(MyXMLConstants.BLOCKS);
		blocks.appendChild(doc.createTextNode(String.valueOf(f.length() / 512 / 1024)));

		Log.d("FileINfo", String.valueOf(f.length() / (512 * 1024)));

		Element totalsize = doc.createElement(MyXMLConstants.TOTAL_SIZE);
		totalsize.appendChild(doc.createTextNode(String.valueOf(f.length())));

		Element typefile = doc.createElement(MyXMLConstants.FILE_TYPE);
		typefile.appendChild(doc.createTextNode(Utils.getMimeType(f.getName())));

		Element sender1 = doc.createElement(MyXMLConstants.SENDER);
		sender1.appendChild(doc.createTextNode(sender));

		Element filename = doc.createElement(MyXMLConstants.FILE_NAME);
		filename.appendChild(doc.createTextNode(f.getName()));

		Element senderid1 = doc.createElement(MyXMLConstants.SENDERID);
		senderid1.appendChild(doc.createTextNode(senderid));

		Element passcode1 = doc.createElement(MyXMLConstants.PASSCODE);
		passcode1.appendChild(doc.createTextNode(String.valueOf(passcode)));

	
		Element db_id = doc.createElement(MyXMLConstants.FILE_DB_ID);
		db_id.appendChild(doc.createTextNode(String.valueOf(file_db_id)));

		doc.appendChild(root);
		root.appendChild(fileid);
		root.appendChild(blocks);
		root.appendChild(blocksize);
		root.appendChild(totalsize);
		root.appendChild(typefile);
		root.appendChild(filename);
		root.appendChild(sender1);
		root.appendChild(senderid1);
		root.appendChild(passcode1);
		root.appendChild(db_id);

		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		Properties outFormat = new Properties();
		outFormat.setProperty(OutputKeys.INDENT, "yes");
		outFormat.setProperty(OutputKeys.METHOD, "xml");
		outFormat.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		outFormat.setProperty(OutputKeys.VERSION, "1.0");
		outFormat.setProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperties(outFormat);
		DOMSource domSource = new DOMSource(doc.getDocumentElement());
		OutputStream output = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(output);
		transformer.transform(domSource, result);
		String xmlString = output.toString();
		byte[] b = new byte[1];
		b[0] = 1;
		
		try {
			byte[] xml1 =xmlString.getBytes("UTF-8");
			send(tcpclient.convert(xml1.length + 1));
			send(b);
			send(xml1);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return i;
	}

	int current_file_db_id;
	boolean sending;
	public void sendfile(int fileid, File f, int blocktoskip, int file_db_id)
			throws IOException {
		int blockid = 0;
		sending=false;
		try {
			FileInputStream fis = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(fis);
			int l = 0;
			fis.skip(blocksize * 1024 * blocktoskip);
			blockid = blocktoskip + 1;
			do {
				l = bis.read(buffer);
				
				if(l==-1)
					l=0;
				sending=sendblock(l, buffer, fileid, blockid, file_db_id);
				blockid++;
			Log.e("value l", "L:"+l);
			} while (l == blocksize * 1024 && sending);
			
			if(sending)
			{	
			sendBroadcastFIleUploaded(file_db_id);
			Log.d("Files", "Sent");
			}
			MainActivity.TRANSFER = false;
			
			fis.close();
			noOfFilesToSend--;
			if(noOfFilesToSend<=0)
			{
				tcpclient.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendBroadcastFIleUploaded(int file_id) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setAction(FileUploadProgress.NOTIFY_PROGRESS);
		intent.putExtra(FileUploadProgress.PUT_PROGRESS_TYPE,
				FileUploadProgress.TRANSFER_UPLOAD_COMPLETE);
		intent.putExtra(FileUploadProgress.FILE_DB_ID, file_id);

		context.sendBroadcast(intent);

	}

	public void send(byte[] data) {
		tcpclient.send(data);
	}

	private boolean sendblock(int l, byte[] buffer, int fileid, int blockid,
			int file_db_id) {

		boolean temp=false;
		byte[] b = new byte[1];
		b[0] = 2;
		Log.e("val", "converted s: "+String.valueOf(1 + 4 + 4 + l));
		temp=tcpclient.send(tcpclient.convert(1 + 4 + 4 + l));
		temp=tcpclient.send(b);
		temp=tcpclient.send(convertInt2Byte(fileid));
		temp=tcpclient.send(convertInt2Byte(blockid));
		temp=tcpclient.send(buffer, l, file_db_id);

		if(temp)
		{
		intent.putExtra(FileUploadProgress.FILE_DB_ID, file_db_id);
		intent.putExtra(FileUploadProgress.PUT_PROGRESS, buffer.length);
		intent.putExtra(FileUploadProgress.PUT_PROGRESS_TYPE,
				FileUploadProgress.UPLOAD);

		MasterService masterService = ((MasterService) context);

		if (masterService.id_progress.get(file_db_id) != null) {
			masterService.id_progress.put(file_db_id,
					masterService.id_progress.get(file_db_id) + l);
		} else {
			masterService.id_progress.put(file_db_id, (long) l);
		}

		context.sendBroadcast(intent);
		Log.d("Analyse", "Block sent");
		}
		return temp;
	}

	private byte[] convertInt2Byte(int a) {
		byte[] b = new byte[4];
		b[3] = (byte) (a & 0xff);
		b[2] = (byte) (a >> 8 & 0xff);
		b[1] = (byte) (a >> 16 & 0xff);
		b[0] = (byte) (a >> 24 & 0xff);
		return b;
	}

	public void addFiletoSend(FileHolder fs) {
		filestosend.add(fs);
		noOfFilesToSend++;
	}


	public void sendFileFromToBeSent(int file_id) {
		int fileid = 0;
		int file_db = 0;
		File f = null;
		boolean a = false;
		for (FileHolder h : filestosend) {
			if (h.fileid == file_id) {
				file_db = h.file_db_id;
				fileid = h.fileid;
				f = h.f;
				a = true;
				break;
			}
		}
		if (a)
			try {
				sendfile(fileid, f, 0, file_db);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	public void stopSending() {
		// TODO Auto-generated method stub
		tcpclient.close();
		sending=false;
	}

	public int convertToInt(byte[] four) {
		int a = four[0] & 0xff;
		int b = four[1] & 0xff;
		int c = four[2] & 0xff;
		int d = four[3] & 0xff;
		return a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;
	}

	/* All Resume function are below */

	int resume_count = 1;

	// For resume feature
	public int beginResumingDownload(int file_db_id, String passcode,
			int blockstoskip) throws ParserConfigurationException,
			TransformerException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory
				.newDocumentBuilder();
		Document doc = documentBuilder.newDocument();

		Element root = doc.createElement(MyXMLConstants.FILE_RESUME);
		doc.appendChild(root);

		Element blocksize = doc.createElement(MyXMLConstants.BLOCK_SIZE);
		blocksize.appendChild(doc.createTextNode("512"));

		Element fileid = doc.createElement(MyXMLConstants.FILE_ID);
		fileid.appendChild(doc.createTextNode(String.valueOf(resume_count)));
		int i = resume_count;
		resume_count++;

		Element db_id = doc.createElement(MyXMLConstants.FILE_DB_ID);
		db_id.appendChild(doc.createTextNode(String.valueOf(file_db_id)));

		Element passcode1 = doc.createElement(MyXMLConstants.PASSCODE);
		passcode1.appendChild(doc.createTextNode(passcode));

		Element blosk_to_skip = doc.createElement(MyXMLConstants.BLOCKS);
		blosk_to_skip.appendChild(doc.createTextNode(String
				.valueOf(blockstoskip)));

		root.appendChild(blocksize);
		root.appendChild(db_id);
		root.appendChild(passcode1);
		root.appendChild(blosk_to_skip);
		root.appendChild(fileid);

		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		Properties outFormat = new Properties();
		outFormat.setProperty(OutputKeys.INDENT, "yes");
		outFormat.setProperty(OutputKeys.METHOD, "xml");
		outFormat.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		outFormat.setProperty(OutputKeys.VERSION, "1.0");
		outFormat.setProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperties(outFormat);
		DOMSource domSource = new DOMSource(doc.getDocumentElement());
		OutputStream output = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(output);
		transformer.transform(domSource, result);
		String xmlString = output.toString();
		byte[] b = new byte[1];
		b[0] = 3;
		send(tcpclient.convert(xmlString.length() + 1));
		send(b);
		try {
			send(xmlString.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;

	}

	public void addFiletoResume(FileInfo fs) {

		filehandler.addFileInfoResume(fs);
		
	}

	
}
