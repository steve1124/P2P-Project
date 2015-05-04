	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * Authors: Aneil Younis
	 * 			Matthew Shafran
	 * 			Steven Lesiczka
	 * 
	 * CS 490 - Computer Networks
	 * 
	 * Final Project 
	 * 
	 * Client.java - runs the client and sends a message to server and waits for an ACK before sending the rest of the message.
	 * 
	 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
	import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.xml.sax.*;
import org.w3c.dom.*;

	//add comments.  directions on how to run it
	public class Client {
		//Keeps track of the current sequence number the packets should be
		static String sequenceNumber = "0"; 
		
		//If slowMode is true, there is a one second pause before sending. otherwise there is no pause
		static boolean slowMode = false;
		//Commands to be sent to server
		static final String INFORM_AND_UPDATE = "INFORM_AND_UPDATE";
		static final String QUERY = "QUERY";
		static final String EXIT = "EXIT";
		
		static final double ALPHA = .125;
		static final double BETA = .25;
		
		static long estimatedRTT = 10;
		static long devRTT;
		static int timeoutInterval = 1000;
		//Port numbers for sender and receiver
		static int sendPortNumber = 2007;
		static int receivePortNumber = 3007;
		static int clientReceiverPortNumber = 4007;
		 
		//IP Address of server
		static String serverIP = "";
		static String otherClientIP = "192.168.1.12";
		
		static String[] message;
				
		static String path = "";
		
		static String xml = "settings.xml";
		
		static int xCoordinate;
		static int yCoordinate;
		
		
		public static void main(String[] args) throws IOException, UnknownHostException, InterruptedException{		
			//saveToXML(xml);
			InetAddress address = InetAddress.getLocalHost();
			final String hostIP = address.getHostAddress();
		
			final clientGUI window = new clientGUI();
			window.getFrame().setVisible(true);
			final JFrame frame = window.getFrame();
			
			//readXML(xml);
			Initialize(window);		
			
			window.btnChange.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					serverIP = JOptionPane.showInputDialog(frame,"Enter IP Address of Directory Server", serverIP);
					if(serverIP != null){
						window.txtDirectoryServerIP.setText(serverIP);
						saveToXML(xml);
					}
				}
			});
			
			window.btnBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final JFileChooser chooser = new JFileChooser(){
						public void approveSelection(){
							if(getSelectedFile().isFile())
								return;
							else
								super.approveSelection();
						}
					};
					
					File currentPath = new File(path);
					chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					chooser.setCurrentDirectory(currentPath);
					
					String prevPath = path;
					
					int returnVal = chooser.showOpenDialog(frame);
					if(returnVal == JFileChooser.APPROVE_OPTION){
						path = chooser.getSelectedFile().getPath();
						window.txtSharedDirectory.setText(chooser.getSelectedFile().getPath());
					}
					
					else if(returnVal == JFileChooser.CANCEL_OPTION){
						path = prevPath;
					}
					File files = new File(path);
					File[] listOfFiles = files.listFiles();
					
						
					if(listOfFiles != null){
						window.txtShowDirectoryContents.setText("");
						for(int i=0; i<listOfFiles.length; i++){
							String[] name = listOfFiles[i].getName().split("\\\\");
							window.txtShowDirectoryContents.append(name[name.length-1] + "\n");
						}
					}
					
					saveToXML(xml);
				}
			});
			
			window.btnUpdate.addActionListener(new ActionListener(){
				
			public void actionPerformed(ActionEvent e) {
				if (path != null) {
					try {
						Update(serverIP, path, hostIP);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
				
			});
			
			window.btnQuery.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					try {
						Query(serverIP, window.txtQueryFileName.getText(), hostIP);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					window.txtShowQueryResults.setText("IP Address\tFile Name\tFile Size\n");
					for(int i=1; i<message.length; i++){
						String[] temp = message[i].split(":");
						if(temp.length > 1)
							window.txtShowQueryResults.append(temp[0] + "\t" + temp[1] + "\t" + temp[2] + "\n");
						else{
							window.txtShowQueryResults.setText("");
							window.txtShowQueryResults.append(temp[0]);
						}
					}

				}
			});
			
			window.btnExit.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					try {
						Exit(serverIP, hostIP);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}
				
			});
			
			
			window.chckbxmntmSlowMode.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(window.chckbxmntmSlowMode.getState())
						slowMode = true;
					else
						slowMode = false;
				}
			});
			
			window.txtQueryFileName.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					char c = e.getKeyChar();
					if(c == KeyEvent.VK_ENTER){
						try {
							Query(serverIP, window.txtQueryFileName.getText(), hostIP);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						window.txtShowQueryResults.setText("IP Address\tFile Name\tFile Size\n");
						for(int i=1; i<message.length; i++){
							String[] temp = message[i].split(":");
							if(temp.length > 1)
								window.txtShowQueryResults.append(temp[0] + "\t" + temp[1] + "\t" + temp[2] + "\n");
							else{
								window.txtShowQueryResults.setText("");
								window.txtShowQueryResults.append(temp[0]);
							}
						}
						
					}
				}
			});
			
			window.btnRequestFile.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					
				}
			});
			
			window.mntmExit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					xCoordinate = window.frame.getX();
					yCoordinate = window.frame.getY();
					saveToXML(xml);
					System.exit(0);
				}
			});
			
			Client.waitForRequest WaitForRequest = new Client().new waitForRequest();
			new Thread(WaitForRequest).start();
			
			/*while(true){
				Scanner scan = new Scanner(System.in);
				System.out.print("Enter a command: ");
				String input = scan.nextLine();
				parseInput(input);
			}*/
			
			//connectToClient(otherClientIP, "filename.txt");
			//Thread.sleep(3000);
			//connectToClient(otherClientIP, "filename2.txt");
			
		}
		static void deliverMessage(String message, String serverIP) throws IOException, InterruptedException{	
			//Resets the sequence number
			sequenceNumber = "0";
			
			//Gets the server's address
			InetAddress serverAddress = InetAddress.getByName(serverIP);
			
			//Initialize empty socket and socket to receive data on.
			DatagramSocket socket = new DatagramSocket();
			DatagramSocket rcvSocket = new DatagramSocket(receivePortNumber);
			
			//Splits the message into separate bytes and make an input stream of those bytes.
			byte[] data = message.getBytes();
			ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
			
			//Calculates total number of packets for the message
			int numPackets = data.length / 127 + 1;
			int packetCount = 1;
			
			//Iterates through the above bytestream, while some bytes remain.
			while (byteStream.available()>0){
				byte[] packetData = new byte[126];
				int bytesRead = byteStream.read(packetData);
				
				//Creates a byte array to send to server. The first byte is the sequence number.
				byte[] fullPacket = new byte[bytesRead+2];	
				fullPacket[0] = sequenceNumber.getBytes()[0];	
				
				//The second byte is the final packet flag
				if(packetCount == numPackets)
					fullPacket[1] = "1".getBytes()[0];
				else
					fullPacket[1] = "0".getBytes()[0];
				
				//Iterates over packetData and puts its' values into the fullPacket.
				for(int i = 2; i<fullPacket.length; i++){
					fullPacket[i] = packetData[i-2];
				}
				
				
				//Pads out the byte array if the length is too short.
				if (bytesRead<fullPacket.length)
					fullPacket = Arrays.copyOf(fullPacket, bytesRead+2);
				String s = new String(fullPacket);
				System.out.println(s);
				//Creates a packet to be sent to the servers address and port number.
				DatagramPacket packet = new DatagramPacket(fullPacket, fullPacket.length, serverAddress, sendPortNumber);
				
				System.out.println("Packet with sequence number " + sequenceNumber + " being sent,"
						+ " current timeout: " + timeoutInterval);
				
				//Pauses for one second if slowStart is enabled.
				if(slowMode)
					Thread.sleep(1000);
				
				//Sends the packet
				sendPacket(socket,rcvSocket,packet);
				
				packetCount ++;
			}
			//Closes the socket and prints EOF.
			socket.close();
			rcvSocket.close();
		}
		static void sendPacket(DatagramSocket socket, DatagramSocket rcvSocket, DatagramPacket packet) throws IOException{
			//Causes the client to keep sending the packet until the server ACK's the packet.
			boolean sending = true;
			
			long startTime = System.nanoTime();
			//Sends the packet and starts a timer that timesout after 3 seconds.
			socket.send(packet);

			rcvSocket.setSoTimeout(timeoutInterval);
			
			//Prepares to receive the ACK packet from the server.
			byte[] ackData = new byte[128];
			DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length);
			
			//Send until server ACK's packet
			while(sending == true){
				try{
					
					//Receive the packet and break it down to get the ACK number sent back.
					rcvSocket.receive(ackPacket);
					long sampleRTT = (System.nanoTime() - startTime) / 1000000;
					estimatedRTT = (long) ((1-ALPHA) * estimatedRTT + ALPHA * sampleRTT);
					devRTT = (long) ((1-BETA) * devRTT + BETA* Math.abs(sampleRTT-estimatedRTT));
					timeoutInterval = (int) (estimatedRTT + 4 * devRTT);
					System.out.println("estimatedRTT: " + estimatedRTT + " devRTT: " + devRTT + " timeoutinterval: " + timeoutInterval);
					
					byte[] packetData = Arrays.copyOf(ackPacket.getData(), ackPacket.getLength());
					String data = new String(packetData);
					//System.out.println(data);
					message = data.split("[\r\n]+");
					String[] ACKNum = message[0].split("[:]");
					System.out.println("ACK received with sequence number " + ACKNum[1]);

					if(message.length > 1){
						for(int i=1; i<message.length; i++){
							System.out.println(message[i]);
						}
					}
					//System.out.println("seq: " + sequenceNumber);
					
					//If the sequence number matches what was sent (meaning the right packet was sent)
					//		then change the current sequence number and move onto the next packet.
					if(ACKNum[1].equals(sequenceNumber)){
						changeSeqNum();
						return;
					}
					//If ACK message was returned, then stop trying to send the packet.
					else if(!(ACKNum[1].isEmpty())){
						sending = false;
					}
					
				}
				catch(SocketTimeoutException e){
					timeoutInterval += 10;
					socket.setSoTimeout(timeoutInterval);
					//If the 3 second timeout is hit, attempt to send the packet again.
					System.out.println("Timeout occurred, resending\n");
					System.out.println("Packet with sequence number " + sequenceNumber + " being sent,"
							+ " current timeout: " + timeoutInterval);
					socket.send(packet);
					continue;
				}
			}
		}
		
		//Swap the sequence number
		static void changeSeqNum(){
			if(sequenceNumber.equals("0"))
				sequenceNumber = "1";
			else 
				sequenceNumber = "0";
		}
		
		public static void connectToClient(String ip, String fileName, int fileSize){
			try {
				Socket tcpSocket = new Socket(ip, clientReceiverPortNumber);
				
				byte[] nameOfFile = new byte[fileName.length()];
				nameOfFile = fileName.getBytes();
				DataOutputStream outputStream = new DataOutputStream(tcpSocket.getOutputStream());
				outputStream.write(nameOfFile);
				
				byte[] fileData = new byte[fileSize];
				    InputStream input = tcpSocket.getInputStream();
				    FileOutputStream fileOutput = new FileOutputStream(fileName);
				    BufferedOutputStream bufferOutput = new BufferedOutputStream(fileOutput);
				    int bytesRead = input.read(fileData, 0, fileData.length);
				    bufferOutput.write(fileData, 0, bytesRead);
				    bufferOutput.close();
				    tcpSocket.close();
				
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		private class waitForRequest implements Runnable{

			public void run() {
				try {
					receiveConnection(otherClientIP);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
				
				
			}			
		}
		
		public static void receiveConnection(String ip) throws IOException, InterruptedException{		
		    ServerSocket serverSocket = new ServerSocket(clientReceiverPortNumber);
		    File myFile;
		    
		    while (true) {
		      
		      Socket tcpSocket = serverSocket.accept();
		      System.out.println("Connection established");
		        
		      String fileName = "";
		      byte[] nameOfFile = new byte[128];
		      DataInputStream inputStream = new DataInputStream(tcpSocket.getInputStream());
		      inputStream.read(nameOfFile);
		      fileName = new String(nameOfFile);
		      fileName = fileName.trim();
		      String filePath = path + "/" + fileName;
		      filePath = filePath.trim();
		      
		      myFile = new File(filePath);

		      byte[] fileData = new byte[(int) myFile.length()];
		      BufferedInputStream bufferedInput = new BufferedInputStream(new FileInputStream(myFile));
		      bufferedInput.read(fileData, 0, fileData.length);
		      OutputStream output = tcpSocket.getOutputStream();
		      
		      output.write(fileData, 0, fileData.length);
		      output.flush();		      
		      tcpSocket.close();
		    }
		  }	
		
		
		public static void Update(String serverIP, String directory, String hostIP) throws IOException, InterruptedException{
			
			File files = new File(directory);
			File[] listOfFiles = files.listFiles();
			
			String[] fileNames = new String[listOfFiles.length];
			long[] fileSizes = new long[listOfFiles.length];
			
			for(int i=0; i<listOfFiles.length; i++){
				String[] name = listOfFiles[i].getName().split("\\\\");
				fileSizes[i] = listOfFiles[i].length();
				fileNames[i] = name[name.length-1]+ ":" + fileSizes[i];
			}

			String message = INFORM_AND_UPDATE + " " + hostIP + "\r\n";
			for(String s: fileNames){
				message += s + "\r\n";
			}
			System.out.println(serverIP);
			
			deliverMessage(message,serverIP);
			
		}
		
		
		public static void Query(String serverIP, String fileName, String hostIP) throws IOException, InterruptedException{
			String message = QUERY + " " + hostIP + "\r\n"
					+ fileName + "\r\n";
			
			deliverMessage(message,serverIP);
		}
		
		public static void Exit(String serverIP, String hostIP) throws IOException, InterruptedException{
			String message = EXIT + " " + hostIP + "\r\n";
			
			deliverMessage(message,serverIP);
		}
		
		public static void getFile(String ip, String fileName, int fileSize){
			connectToClient(ip, fileName, fileSize);
		}
		
		public static void saveToXML(String xmlFile){
			Document dom;
			Element e = null;
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try{
				DocumentBuilder db = dbf.newDocumentBuilder();
				dom = db.newDocument();
				
				Element rootElement = dom.createElement("Settings");
				
				e = dom.createElement("serverIP");
				e.appendChild(dom.createTextNode(serverIP));
				rootElement.appendChild(e);
				
				e = dom.createElement("path");
				e.appendChild(dom.createTextNode(path));
				rootElement.appendChild(e);
				
				e = dom.createElement("window");
				e.appendChild(dom.createTextNode(xCoordinate + "," + yCoordinate));
				rootElement.appendChild(e);
				
				dom.appendChild(rootElement);
				
				try{
					Transformer tr = TransformerFactory.newInstance().newTransformer();
		            tr.setOutputProperty(OutputKeys.INDENT, "yes");
		            tr.setOutputProperty(OutputKeys.METHOD, "xml");
		            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		            //tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
		            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		            
		            tr.transform(new DOMSource(dom), 
                            new StreamResult(new FileOutputStream(xmlFile)));

				} catch (TransformerException te) {
					System.out.println("1 " + te.getMessage());
				} catch (IOException ioe) {
					System.out.println("2 " + ioe.getMessage());
				}
			} catch (ParserConfigurationException pce) {
				System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
				}
		}
		
		public static boolean readXML(String xmlFile){
			Document dom;
			String c = "";
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try{
				DocumentBuilder db = dbf.newDocumentBuilder();
				
				dom = db.parse(xmlFile);
				
				Element doc = dom.getDocumentElement();
				
				serverIP = getTextValue(serverIP, doc, "serverIP");
				path = getTextValue(path, doc, "path");
				c = getTextValue(c, doc, "window");
				String[] temp = c.split(",");
				xCoordinate = Integer.parseInt(temp[0]);
				yCoordinate = Integer.parseInt(temp[1]);
				
				return true;
				
			} catch (ParserConfigurationException pce) {
	            System.out.println(pce.getMessage());
	        } catch (SAXException se) {
	            System.out.println(se.getMessage());
	        } catch (IOException ioe) {
	            System.err.println(ioe.getMessage());
	        }

	        return false;
		}
		
		private static String getTextValue(String def, Element doc, String tag) {
		    String value = def;
		    NodeList nl;
		    nl = doc.getElementsByTagName(tag);
		    if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
		        value = nl.item(0).getFirstChild().getNodeValue();
		    }
		    return value;
		}
		
		private static void Initialize(clientGUI window){
			window.txtDirectoryServerIP.setText(serverIP);
			
			window.txtSharedDirectory.setText(path);
			
				File files = new File(path);
				File[] listOfFiles = files.listFiles();
				window.txtShowDirectoryContents.setText("");
				if(listOfFiles != null){
					for(int i=0; i<listOfFiles.length; i++){
						String[] name = listOfFiles[i].getName().split("\\\\");
						window.txtShowDirectoryContents.append(name[name.length-1] + "\n");
					}
				}
			
		}
		

	}

	
