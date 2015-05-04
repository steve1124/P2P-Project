import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.UIManager;
import javax.swing.JCheckBoxMenuItem;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class clientGUI {

	JFrame frame;
	JTextField txtDirectoryServerIP;
	JButton btnChange = new JButton("Change...");
	JButton btnBrowse = new JButton("Browse...");
	JButton btnUpdate = new JButton("Inform and Update");
	JButton btnQuery = new JButton("Query");
	JTextField txtSharedDirectory = new JTextField();
	JTextField txtQueryFileName = new JTextField();
	JTextArea txtShowDirectoryContents = new JTextArea();
	JTextArea txtShowQueryResults = new JTextArea();
	JButton btnExit = new JButton("Exit From Directory Server");
	JButton btnRequestFile = new JButton("Request File");
	JCheckBoxMenuItem chckbxmntmSlowMode = new JCheckBoxMenuItem("Slow Mode");
	JScrollPane showQueryResults = new JScrollPane(txtShowQueryResults);
	JMenuItem mntmExit = new JMenuItem("Exit");

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					clientGUI window = new clientGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public clientGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		Client.readXML(Client.xml);
		txtShowQueryResults.setEditable(false);
		frame = new JFrame();
		frame.setBounds(Client.xCoordinate, Client.yCoordinate, 750, 650);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Client");
		frame.setResizable(false);
		
		frame.addWindowListener( new WindowAdapter()
		{
		    public void windowClosing(WindowEvent e)
		    {
		        Client.saveToXML(Client.xml);
		        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    }
		});
		
		JLabel lblDirectoryServerIp = new JLabel("Directory Server IP Address:");
		lblDirectoryServerIp.setBounds(120, 37, 172, 14);
		frame.getContentPane().add(lblDirectoryServerIp);
		
		txtDirectoryServerIP = new JTextField();
		txtDirectoryServerIP.setEditable(false);
		txtDirectoryServerIP.setBounds(302, 34, 158, 20);
		frame.getContentPane().add(txtDirectoryServerIP);
		txtDirectoryServerIP.setColumns(10);
		
		
		
		btnChange.setBounds(470, 33, 89, 23);
		frame.getContentPane().add(btnChange);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 84, 714, 2);
		frame.getContentPane().add(separator);
		
		txtShowDirectoryContents.setEditable(false);
		JScrollPane showDirectoryContents = new JScrollPane(txtShowDirectoryContents);
		showDirectoryContents.setBounds(292, 139, 255, 188);
		frame.getContentPane().add(showDirectoryContents);
		
		btnUpdate.setBounds(557, 218, 167, 23);
		frame.getContentPane().add(btnUpdate);
		
		JLabel lblSharedDirectory = new JLabel("Shared Directory:");
		lblSharedDirectory.setHorizontalAlignment(SwingConstants.CENTER);
		lblSharedDirectory.setBounds(19, 188, 263, 20);
		frame.getContentPane().add(lblSharedDirectory);
		
		txtSharedDirectory.setEditable(false);
		txtSharedDirectory.setBounds(15, 219, 258, 20);
		frame.getContentPane().add(txtSharedDirectory);
		txtSharedDirectory.setColumns(10);
				
		btnBrowse.setBounds(99, 250, 89, 23);
		frame.getContentPane().add(btnBrowse);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 364, 714, 2);
		frame.getContentPane().add(separator_1);
		
		btnQuery.setBounds(99, 473, 89, 23);
		frame.getContentPane().add(btnQuery);
		
		showQueryResults.setBounds(292, 395, 255, 113);
		frame.getContentPane().add(showQueryResults);		
		
		txtQueryFileName.setBounds(10, 442, 263, 20);
		frame.getContentPane().add(txtQueryFileName);
		txtQueryFileName.setColumns(10);
		
		JLabel lblEnterFileName = new JLabel("Enter File Name:");
		lblEnterFileName.setHorizontalAlignment(SwingConstants.CENTER);
		lblEnterFileName.setBounds(10, 417, 263, 14);
		frame.getContentPane().add(lblEnterFileName);
		btnExit.setBounds(10, 542, 263, 23);
		
		frame.getContentPane().add(btnExit);
			
		btnRequestFile.setBounds(557, 441, 167, 23);
		
		frame.getContentPane().add(btnRequestFile);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		
		mnFile.add(mntmExit);
		
		JMenu mnSettings = new JMenu("Settings");
		menuBar.add(mnSettings);
		mnSettings.add(chckbxmntmSlowMode);
	}
	
	public JFrame getFrame(){
		return frame;
	}
}
