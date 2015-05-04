import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JLabel;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JList;


public class serverGUI {
	DirectoryServer DServer = new DirectoryServer();
	JTextArea textArea = new JTextArea();
	
	int directoryCount;

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					serverGUI window = new serverGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
	}

	/**
	 * Create the application.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public serverGUI() throws IOException, InterruptedException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	private void initialize() throws IOException, InterruptedException {
		frame = new JFrame();
		frame.setBounds(0, 0, 650, 650);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Directory Server");
		
		
		textArea.setEditable(false);
		JScrollPane directoryListing = new JScrollPane(textArea);
		directoryListing.setBounds(50, 50, 500, 500);
		frame.getContentPane().add(directoryListing);
		textArea.setText("IP Address\t\tFile Name\t\tFile Size\n");
		
		
		
			
		directoryCount = DServer.directory.size();
		
		updateListing(DServer,textArea);
		
		//DirectoryListener DListener = new DirectoryListener(DServer, textArea);
		//new Thread(DListener).start();
		
	}
	
	public static void updateListing(DirectoryServer DServer, JTextArea textArea){
		for(Entry e:DServer.directory){
			textArea.append(e.IPAddress + "\t\t" + e.fileName + "\t\t" + e.fileSize + "\n");
			
		}
	}
	
	public JFrame getFrame(){
		return frame;
	}
	
	private class DirectoryListener implements Runnable{

		DirectoryServer DServer;
		JTextArea textArea;
		
		public DirectoryListener(DirectoryServer DServer, JTextArea textArea){
			this.DServer = DServer;
			this.textArea = textArea;
			
		}
		@Override
		public void run() {
			
			

		}
		
	}
}
