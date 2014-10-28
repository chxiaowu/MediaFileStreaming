package com.exemple.clientRTSP;
 

/* ------------------
Client
usage: java Client [Server hostname] [Server RTSP listening port] [Video file requested]
---------------------- */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Client{

//GUI
//----
JFrame f = new JFrame("Client");
JButton startButton = new JButton("Start");
JButton pauseButton = new JButton("Play/Pause");
JButton nextButton = new JButton("Next");
JButton prevButton = new JButton("Previous");
JButton closeButton = new JButton("Close");
JPanel mainPanel = new JPanel();
JPanel buttonPanel = new JPanel();
JLabel iconLabel = new JLabel();
ImageIcon icon;


//RTP variables:
//----------------
DatagramPacket rcvdp; //UDP packet received from the server
DatagramSocket RTPsocket; //socket to be used to send and receive UDP packets
static int RTP_RCV_PORT = 25000; //port where the client will receive the RTP packets

Timer timer; //timer used to receive data from the UDP socket
byte[] buf; //buffer used to store data received from the server 

//RTSP variables
//----------------
//rtsp states 
final static int INIT = 0;
final static int READY = 1;
final static int PLAYING = 2;
static int state; //RTSP state == INIT or READY or PLAYING
Socket RTSPsocket; //socket used to send/receive RTSP messages
//input and output stream filters
static BufferedReader RTSPBufferedReader;
static BufferedWriter RTSPBufferedWriter;
static String VideoFileName; //video file to request to the server
int RTSPSeqNb = 0; //Sequence number of RTSP messages within the session
int RTSPid = 0; //ID of the RTSP session (given by the RTSP Server)

final static String CRLF = "\r\n";

//Video constants:
//------------------
static int MJPEG_TYPE = 97; //RTP payload type for MJPEG video

//--------------------------
//Constructor
//--------------------------
public Client() {

 //build GUI
 //--------------------------

 //Frame
 f.addWindowListener(new WindowAdapter() {
    public void windowClosing(WindowEvent e) {
	 System.exit(0);
    }
 });

 //Buttons
 buttonPanel.setLayout(new GridLayout(1,0));
 buttonPanel.add(startButton);
 buttonPanel.add(pauseButton);
 buttonPanel.add(nextButton);
 buttonPanel.add(prevButton);
 buttonPanel.add(closeButton);
 startButton.addActionListener(new setupButtonListener());
 nextButton.addActionListener(new playButtonListener());
 pauseButton.addActionListener(new pauseButtonListener());
 prevButton.addActionListener(new prevButtonListener());
 closeButton.addActionListener(new tearButtonListener());

 //Image display label
 iconLabel.setIcon(null);
 
 //frame layout
 mainPanel.setLayout(null);
 mainPanel.add(iconLabel);
 mainPanel.add(buttonPanel);
 iconLabel.setBounds(0,0,720,430);
 buttonPanel.setBounds(0,430,720,50);

 f.getContentPane().add(mainPanel, BorderLayout.CENTER);
 f.setSize(new Dimension(740,520));
 f.setVisible(true);

 //init timer
 //--------------------------
 timer = new Timer(20, new timerListener());
 timer.setInitialDelay(0);
 timer.setCoalesce(true);

 //allocate enough memory for the buffer used to receive data from the server
 buf = new byte[15000000];    
}

public static String[] str;

//------------------------------------
//main
//------------------------------------
public static void main(String argv[]) throws Exception
{
 //Create a Client object
 Client theClient = new Client();
 
 //get server RTSP port and IP address from the command line
 //------------------
 int RTSP_server_port = 8554; //Integer.parseInt(argv[1]); //
 String ServerHost = "localhost"; //argv[0]; //
// InetAddress ServerIPAddr = InetAddress.getByName(ServerHost);

 //get video filename to request:
 VideoFileName = "media/images_1.jpg"  ; //argv[2]+"images_1.jpg"; //
 str = VideoFileName.split("_");

 //Establish a TCP connection with the server to exchange RTSP messages
 //------------------
// theClient.RTSPsocket = new Socket(ServerIPAddr, RTSP_server_port);
 
 
 theClient.RTSPsocket = new Socket(ServerHost, RTSP_server_port);

 //Set input and output stream filters:
 RTSPBufferedReader = new BufferedReader(new InputStreamReader(theClient.RTSPsocket.getInputStream()) );
 RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(theClient.RTSPsocket.getOutputStream()) );

 //init RTSP state:
 state = INIT;
}


//------------------------------------
//Handler for buttons
//------------------------------------

//.............
//TO COMPLETE
//.............

//Handler for Setup button
//-----------------------
class setupButtonListener implements ActionListener{
 public void actionPerformed(ActionEvent e){

   //System.out.println("Setup Button pressed !");      

   if (state == INIT) 
	{
	  //Init non-blocking RTPsocket that will be used to receive data
	   try{
	    //construct a new DatagramSocket to receive RTP packets from the server, on port RTP_RCV_PORT
	    //RTPsocket = ...
		    RTPsocket = new DatagramSocket(RTP_RCV_PORT);

		    
		 //   set TimeOut value of the socket to 5msec.
		    	

		    	    RTPsocket.setSoTimeout(5);
	    //set TimeOut value of the socket to 5msec.
	    //....

	  }
	  catch (SocketException se)
	    {
	      System.out.println("Socket exception: "+se);
	      System.exit(0);
	    }
	}
	  //init RTSP sequence number
	  RTSPSeqNb = 0;
	 
	  //Send SETUP message to the server
	  VideoFileName = Client.str[0]+"_"+"1"+".jpg"  ; 
	  send_RTSP_request("SETUP");

	  //Wait for the response 
	  if (parse_server_response() != 200)
	    System.out.println("Invalid Server Response");
	  else 
	    {
	      //change RTSP state and print new state 
		  state = READY;
	      System.out.println("New RTSP state: READY");
	      //System.out.println("New RTSP state: ....");
	      System.out.println(++RTSPSeqNb);
	      send_RTSP_request("PLAY");

	      timer.start();
	    }
	//else if state != INIT then do nothing
 }
}

//Handler for Play button
//-----------------------
class playButtonListener implements ActionListener {
 public void actionPerformed(ActionEvent e){

	 System.out.println("Play Button pressed !");

     if (RTSPSeqNb<=11)
	{
//    	 RTSPSeqNb++;
	   	//increase RTSP sequence number
		//send PLAY message to the server
	
		VideoFileName= Client.str[0]+ "_"+RTSPSeqNb+".jpg";
	   	System.out.println(RTSPSeqNb);
	  	
	  	send_RTSP_request("PLAY");
		
		//   wait for the response
	  	if (parse_server_response() != 200)
		  System.out.println("Invalid Server Response");
		else
	    {
			//   change RTSP state and print out new state
	      	state=PLAYING;
	      	System.out.println("New RTSP state: PLAYING");

			//   start the timer
	    	timer.start();
	    }
	}
	//else if state != READY then do nothing
   }
}


//Handler for Pause button
//-----------------------
class prevButtonListener implements ActionListener {
 public void actionPerformed(ActionEvent e){

   System.out.println("Prev Button pressed !");   

   if (RTSPSeqNb>2) 
	{
	   	//increase RTSP sequence number
		RTSPSeqNb--;
		RTSPSeqNb--;

		//send PLAY message to the server
		
		System.out.println(RTSPSeqNb);
		VideoFileName= Client.str[0]+ "_"+RTSPSeqNb+".jpg";
		send_RTSP_request("PLAY");

	//   wait for the response

		if (parse_server_response() != 200)
			System.out.println("Invalid Server Response");
		else
		{

			//   change RTSP state and print out new state
		    state=PLAYING;
			System.out.println("New RTSP state: PLAYING");

			//   start the timer
			timer.start();
		}
	}
   //else if state != PLAYING then do nothing
 }
}
class pauseButtonListener implements ActionListener {
	 public void actionPerformed(ActionEvent e){


		  //Send PAUSE message to the server
		  send_RTSP_request("PAUSE");
		
		  //Wait for the response 
		 if (parse_server_response() != 200)
			  System.out.println("Invalid Server Response");
		  else 
		    {
		      //change RTSP state and print out new state
		      //........
		      //System.out.println("New RTSP state: ...");
		      
		      //stop the timer
			  state=READY;
			  if(timer.isRunning())
				  timer.stop();
			  timer.start();
		    }
		}
	   //else if state != PLAYING then do nothing
	 
	}

//Handler for Teardown button
//-----------------------
class tearButtonListener implements ActionListener {
 public void actionPerformed(ActionEvent e){

   //System.out.println("Teardown Button pressed !");  

   //increase RTSP sequence number
   // ..........
   

   //Send TEARDOWN message to the server
   send_RTSP_request("TEARDOWN");

   //Wait for the response 
   if (parse_server_response() != 200)
	System.out.println("Invalid Server Response");
   else 
	{     
	  //change RTSP state and print out new state
	  //........
	  //System.out.println("New RTSP state: ...");

	  //stop the timer
	  timer.stop();

	  //exit
	  System.exit(0);
	}
 }
}


//------------------------------------
//Handler for timer
//------------------------------------

class timerListener implements ActionListener {
 public void actionPerformed(ActionEvent e) {
   
   //Construct a DatagramPacket to receive data from the UDP socket
   rcvdp = new DatagramPacket(buf, buf.length);

   try{
	//receive the DP from the socket:
	RTPsocket.receive(rcvdp);
	  
	//create an RTPpacket object from the DP
	RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());

	//print important header fields of the RTP packet received: 
	System.out.println("Got RTP packet with SeqNum # "+rtp_packet.getsequencenumber()+" TimeStamp "+rtp_packet.gettimestamp()+" ms, of type "+rtp_packet.getpayloadtype());
	RTSPSeqNb++;
	//print header bitstream:
	rtp_packet.printheader();

	//get the payload bitstream from the RTPpacket object
	int payload_length = rtp_packet.getpayload_length();
	byte [] payload = new byte[payload_length];
	rtp_packet.getpayload(payload);

	//get an Image object from the payload bitstream
	// Toolkit toolkit = Toolkit.getDefaultToolkit();
	// Image image = toolkit.createImage(payload, 0, payload_length);
	ImageIcon pic = null;
	if (payload != null) {
		pic = new ImageIcon(payload);
		System.out.println("GOT Payload with length " + payload_length);
	} 
//	else {
//		pic = new ImageIcon(getClass().getResource(""));
//	}
	Image scaled = pic.getImage().getScaledInstance(iconLabel.getWidth(), iconLabel.getHeight(), Image.SCALE_DEFAULT);
	
	//display the image as an ImageIcon object
	icon = new ImageIcon(scaled);
	iconLabel.setIcon(icon);

	
   }
   catch (InterruptedIOException iioe){
	//System.out.println("Nothing to read");
   }
   catch (IOException ioe) {
	System.out.println("Exception caught: "+ioe);
   }
 }
}

//------------------------------------
//Parse Server Response
//------------------------------------
private int parse_server_response() 
{
 int reply_code = 0;

 try{
   //parse status line and extract the reply_code:
   String StatusLine = RTSPBufferedReader.readLine();
   //System.out.println("RTSP Client - Received from Server:");
   System.out.println(StatusLine);
 
   StringTokenizer tokens = new StringTokenizer(StatusLine);
   tokens.nextToken(); //skip over the RTSP version
   reply_code = Integer.parseInt(tokens.nextToken());
   
   //if reply code is OK get and print the 2 other lines
   if (reply_code == 200)
	{
	  String SeqNumLine = RTSPBufferedReader.readLine();
	  System.out.println(SeqNumLine);
	  
	  String SessionLine = RTSPBufferedReader.readLine();
	  System.out.println(SessionLine);
	
	  //if state == INIT gets the Session Id from the SessionLine
	  tokens = new StringTokenizer(SessionLine);
	  tokens.nextToken(); //skip over the Session:
	  RTSPid = Integer.parseInt(tokens.nextToken());
	}
 }
 catch(Exception ex)
   {
	System.out.println("Exception caught: "+ex);
	System.exit(0);
   }
 
 return(reply_code);
}

//------------------------------------
//Send RTSP Request
//------------------------------------

//.............
//TO COMPLETE
//.............

private void send_RTSP_request(String request_type)
{
	try{

		 
			
 

		      RTSPBufferedWriter.write(request_type+" "+VideoFileName+" "+"RTSP/1.0"+CRLF);

	 
			

		      RTSPBufferedWriter.write("CSeq: "+RTSPSeqNb+CRLF);

		 		

		      if(request_type.equals("SETUP")) {
		          RTSPBufferedWriter.write("Transport: RTP/UDP; client_port= "+RTP_RCV_PORT+CRLF);
		      }

		 

		      else {
		          RTSPBufferedWriter.write("Session: "+RTSPid+CRLF);
		      }

		      RTSPBufferedWriter.flush();
		    }
		    catch(Exception ex)
		      {
			System.out.println("Exception caught: "+ex);
		      }
		  }

		}//end of Class Client


