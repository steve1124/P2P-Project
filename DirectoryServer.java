/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Authors: Aneil Younis
 * 			Matthew Shafran
 * 			Steven Lesiczka
 * 
 * CS 490 - Computer Networks
 * 
 * Final Project 
 * 
 * DirectoryServer.java - runs the server and waits to receive a message then ACK's back the proper sequence number.
 * 
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class DirectoryServer {
	//Commands to be sent to server
    static final String INFORM_AND_UPDATE = "INFORM_AND_UPDATE";
    static final String QUERY = "QUERY";
    static final String EXIT = "EXIT";
    
	//If slowMode is true, there is a one second pause before sending. otherwise there is no pause
    static boolean slowMode = false;
    
    //IP Address of client
    static String IP = "192.168.1.86"; 
    
    //Which command to run
    static String method = "";
    
    //Server host and IP address
    static String IPAddress;
    static String hostIP;
    static String name;
    
    //The message sent by user
    static String[] message = new String[5];
    
    //list of users or files later
    static ArrayList<Entry> directory = new ArrayList<Entry>();
    
    //Client and server port numbers.
    static int portNumber = 2007;
    static int receiverPortNumber = 3007;
    
    static String fullMessage = "";
    
    static String IPlist;
    
    static File directoryFile = new File("c:/users/slesi_000/documents/cs490/directory.txt");
    
    
    

    public static void main(String[] args) throws IOException, InterruptedException {
    	
    	GUI window = new GUI();
    	window.getFrame().setVisible(true);
    	 	
    	initializeDirectory();
    	window.updateListing(window.DServer, window.textArea);
    	
    	listenThread ListenThread = new DirectoryServer().new listenThread(window);
    	new Thread(ListenThread).start();
    	
    }
    
    private class listenThread implements Runnable{

    	GUI window;
    	public listenThread(GUI window){
    		this.window = window;
    	}
		@Override
		public void run() {
			try {
				startListening(window);
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
    	
    }
    	
    	  	
    public static void startListening(GUI window) throws IOException, InterruptedException{
        //Initialize an empty socket.
        DatagramSocket socket = null;
        
        //Set message to empty
        String m = "";
        
        //Set previously received ACK to 1, since the first message should have a sequence
        //		number of 0.
        char prevAck = '1';
        
        int packetCount = 0;
        
        InetAddress address = InetAddress.getLocalHost();
        hostIP = address.getHostAddress();
        name = address.getHostName();
        
        try{
        	//Open the socket as the specificed port number.
            socket = new DatagramSocket(portNumber);
            
            while(true){
            	//Get ready to receive a 128 bit packet and read it into m.
                byte[] buf = new byte[128];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                byte[] packetData = Arrays.copyOf(packet.getData(), packet.getLength());
                m = new String (packetData);
                //System.out.println(m);
                
                //Grab the sequence bit from m.
                char seq = m.charAt(0);
                
                //Grab the final packet flag
                char finalPacket = m.charAt(1);
                
                System.out.println("Packet received with sequence number " + seq);
                
                //If the sequence number is what we expect, print out the data and make the
                //		previously received sequence number the current one.
                if(prevAck != seq){
                    String data = m.substring(2);
                    if(packetCount == 0)
                    	parseMessage(data, window);
                    fullMessage += data;
                    if(finalPacket == '1'){
                    	prevAck = '1';
                        parseMessage(fullMessage, window);
                        extractData(window);
                        fullMessage = "";
                        packetCount = 0;
                    }
                    else
                    	prevAck = seq;
                }
                //Otherwise prepare to return an ACK of the opposite bit to client.
                else{
                    if(seq == '0')
                        seq = '1';
                    else
                        seq = '0';
                }
              
                String ackMessage = "ACK:" + seq;
                
                if(method.equals(QUERY)){
                	ackMessage += "\r\n" + IPlist;
                }
                
                //System.out.println(ackMessage);
               
                //Create the ACK packet.
                 byte[] ackPacketData = new byte[128];
                 ackPacketData =ackMessage.getBytes(); 
                 InetAddress clientAddress = InetAddress.getByName(IPAddress);
                 DatagramPacket ackPacket = new DatagramPacket(ackPacketData, ackPacketData.length, clientAddress, receiverPortNumber);
                 //Pause for one second if slowMode is enabled.
                 if(slowMode)
                	 Thread.sleep(1000);
                 
                 System.out.println("Sending ACK with sequence number " + seq);
                 //Send the packet back to the client.
                 socket.send(ackPacket);
                 ackMessage = "";
                 IPlist = "";
                 packetCount++;
                 

            }
        }finally{
        	//Close the socket
            if(socket != null)
                socket.close();
        }
        

    }
    
    public static void initializeDirectory() throws IOException{
    	if(directoryFile.length() > 0){
	    	FileReader fr = new FileReader(directoryFile);
	    	BufferedReader br = new BufferedReader(fr);
	    	String readFromFile = br.readLine();
	    	String[] directoryEntries = readFromFile.split(":");
	    	
	    	for(int i=0; i<directoryEntries.length; i++){
	    		String temp[] = directoryEntries[i].split(";");
	    		directory.add(new Entry(temp[0], temp[1], temp[2]));
	    	}
    	}
    	
    	/*for(Entry e:directory)
    		System.out.println(e);*/
    }
    
    public static void extractData(GUI window) throws IOException{
    	
    	if(method.equals(INFORM_AND_UPDATE))
    		Update(window);
    	else if(method.equals(QUERY))
    		IPlist = Query();
    	else if(method.equals(EXIT))
    		Exit(IPAddress, window);
    }

    public static void parseMessage(String s, GUI window){
        
        message = s.split("[\r\n]+");
        
        String[] info = message[0].split("[ ]");
        method = info[0];
        //hostName = info[1];
        IPAddress = info[1];
        
    }
    
    public static void Update(GUI window) throws IOException{
        for(int i=1; i<message.length; i++){  
            String[] temp = message[i].split("[:]");
            directory.add(new Entry(IPAddress, temp[0], temp[1]));           
        }
        
        window.updateListing(window.DServer, window.textArea);
        
        FileWriter fw = new FileWriter(directoryFile,false);
        
        for(Entry e : directory){
            //System.out.println(e);
            
            String fileString = e.IPAddress + ";" + e.fileName + ";" + e.fileSize + ":";
            fw.write(fileString,0,fileString.length());
        }
        
        fw.close();
                
    }
    
    public static String Query(){
    	String peerIP = "";
    	int count = 0;

    	for(int i=1; i<message.length; i++){
    		String[] temp = message[i].split("[:]");
    		for(Entry e: directory){
    			if(e.fileName.equals(temp[0])){
    				count++;
    				peerIP += e.IPAddress + ":" + e.fileName + ":" + e.fileSize + "\r\n";
    			}
    		}    	
    	}
    	
    	if(count == 0)
    		peerIP += "no peers found";
    	
    	return peerIP;
    }
    
    public static void Exit(String ip, GUI window) throws IOException{
    	int max = directory.size();
    	int i = 0;
    	while(i < max){
    		if(directory.get(i).IPAddress.equals(ip)){
    			directory.remove(i);
    			max--;
    		}
    		else
    			i++;
    	}
    	
    	window.textArea.setText("IP Address\t\tFile Name\t\tFile Size\n");
    	window.updateListing(window.DServer, window.textArea);
    	
    	 FileWriter fw = new FileWriter(directoryFile,false);
         
         for(Entry e : directory){
             //System.out.println(e);
             String fileString = e.IPAddress + " " + e.fileName + " " + e.fileSize + ":";
             fw.write(fileString,0,fileString.length());
         }
         
         fw.close();
    }
}

