package com.exemple.rtspserver;

import java.io.FileInputStream;



import java.io.File;

public class VideoStream {
	 FileInputStream fis; //video file
	 File file;
	  int frame_nb; //current frame nb

	  //-----------------------------------
	  //constructor
	  //-----------------------------------
	  public VideoStream(String filename) throws Exception{

	    //init variables
	    fis = new FileInputStream(filename);
	    file = new File(filename);
	    frame_nb = 0;
	  }

	  //-----------------------------------
	  // getnextframe
	  //returns the next frame as an array of byte and the size of the frame
	  //-----------------------------------
	  public void set(String filename) throws Exception{

		    //init variables
		    fis = new FileInputStream(filename);
		    file = new File(filename);
		    frame_nb = 0;
		  }

	  public int getnextframe(byte[] frame) throws Exception
	  {
//	    int length = 0;
//	    String length_string;
//	    byte[] frame_length = new byte[5];
//
//	    //read current frame length
//	    fis.read(frame_length,0,5);
		
	    //transform frame_length to integer
//	    length_string = new String(frame_length);
//	    length = Integer.parseInt(length_string);
		  
//		
	    return(fis.read(frame,0,(int)file.length()));
	  }
	  public String getnextImage(String filename, int i){
		  System.out.println(filename);
		  String s[] = filename.split("\\.");
		  System.out.println(s[0]);
		  String[] t = s[0].split("_");
		  t[1]=Integer.toString(Integer.parseInt(t[1])+1);
		  return file.getParent()+"/"+t[0]+"_"+i+"."+s[1];
		  //return "media\images_2.jpg";
	  }
	  public int getImageNo(String filename){
		  System.out.println(filename);
		  String s[] = filename.split("\\.");
		  System.out.println(s[0]);
		  String[] t = s[0].split("_");
		  t[1]=Integer.toString(Integer.parseInt(t[1])+1);
		  return Integer.parseInt(t[1]);
		  //return "media\images_2.jpg";
	  }
	}