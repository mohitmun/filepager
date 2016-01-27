package com.filepager.tcp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class HttpSender implements Runnable
{
  //static constants
  //HttpServer root is the current directory
  static final int PORT = 8080; //default port

  //instance variables
  Socket connect;
String filepath;

  //constructor
  public HttpSender(Socket connect,String path)
  {
    this.connect = connect;
    filepath=path;
  }

  /**
   * run method services each request in a separate thread.
   */
  public void run()
  {
    BufferedReader in = null;
    PrintWriter out = null;
    BufferedOutputStream dataOut = null;
    String fileRequested = null;

    try
    {
      //get character input stream from client
      in = new BufferedReader(new InputStreamReader(
        connect.getInputStream()));
      //get character output stream to client (for headers)
      out = new PrintWriter(connect.getOutputStream());
      //get binary output stream to client (for requested data)
      dataOut = new BufferedOutputStream(
        connect.getOutputStream());

      File file = new File(filepath);
      
      int fileLength = (int)file.length();

      String content = getContentType(file.getName());


      
        FileInputStream fileIn = null;

        byte[] fileData = new byte[fileLength];

        try
        {
          //open input stream from file
          fileIn = new FileInputStream(file);
          //read file into byte array
          fileIn.read(fileData);
        }
        finally
        {
          close(fileIn); //close file input stream
        }

        //send HTTP headers
        out.println("HTTP/1.0 200 OK");
        out.println("Server: Java HTTP Server 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: application/vnd.android.package-archive");
        out.println("Content-length: " + file.length());
        out.println("Content-Disposition: attachment; filename= filepager.apk");
        out.println("Content-Transfer-Encoding: binary");
        out.println(); //blank line between headers and content
        out.flush(); //flush character output stream buffer
       connect.getOutputStream().write(fileData, 0, fileLength); 
       connect.getOutputStream().flush();
       try {
		Thread.sleep(100);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
    catch (FileNotFoundException fnfe)
    {
      //inform client file doesn't exist
      fileNotFound(out, fileRequested);
    }
    catch (IOException ioe)
    {
      System.err.println("Server Error: " + ioe);
    }
    finally
    {
      close(in); //close character input stream
      close(out); //close character output stream
      close(dataOut); //close binary output stream
      close(connect); //close socket connection

    }
  }


  /**
   * fileNotFound informs client that requested file does not
   * exist.
   *
   * @param out Client output stream
   * @param file File requested by client
   */
  private void fileNotFound(PrintWriter out, String file)
  {
    //send file not found HTTP headers
    out.println("HTTP/1.0 404 File Not Found");
    out.println("Server: Java HTTP Server 1.0");
    out.println("Date: " + new Date());
    out.println("Content-Type: text/html");
    out.println();
    out.println("<HTML>");
    out.println("<HEAD><TITLE>File Not Found</TITLE>" +
      "</HEAD>");
    out.println("<BODY>");
    out.println("<H2>404 File Not Found: " + file + "</H2>");
    out.println("</BODY>");
    out.println("</HTML>");
    out.flush();

  }


  /**
   * getContentType returns the proper MIME content type
   * according to the requested file's extension.
   *
   * @param fileRequested File requested by client
   */
  private String getContentType(String fileRequested)
  {
    if (fileRequested.endsWith(".htm") ||
      fileRequested.endsWith(".html"))
    {
      return "text/html";
    }
    else if (fileRequested.endsWith(".gif"))
    {
      return "image/gif";
    }
    else if (fileRequested.endsWith(".jpg") ||
      fileRequested.endsWith(".jpeg"))
    {
      return "image/jpeg";
    }
    else if (fileRequested.endsWith(".class") ||
      fileRequested.endsWith(".jar")||fileRequested.endsWith(".apk"))
    {
      return "applicaton/octet-stream";
    }
    else
    {
      return "text/plain";
    }
  }


  /**
   * close method closes the given stream.
   *
   * @param stream
   */
  public void close(Object stream)
  {
    if (stream == null)
      return;

    try
    {
      if (stream instanceof Reader)
      {
        ((Reader)stream).close();
      }
      else if (stream instanceof Writer)
      {
        ((Writer)stream).close();
      }
      else if (stream instanceof InputStream)
      {
        ((InputStream)stream).close();
      }
      else if (stream instanceof OutputStream)
      {
        ((OutputStream)stream).close();
      }
      else if (stream instanceof Socket)
      {
        ((Socket)stream).close();
      }
      else
      {
        System.err.println("Unable to close object: " + stream);
      }
    }
    catch (Exception e)
    {
      System.err.println("Error closing stream: " + e);
    }
  }
}