package basicGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import graphics.DistributionsPanel;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import visualised.VisualDistribution;

public class NetCDFFrame extends JFrame implements WindowListener{

	String filename; 
	NetcdfFile ncfile = null;
	DistributionsPanel dPanel= null;
//	MainFxApp fxApp= null;
	
	//
	JList<String> list;
	private DefaultListModel<String> listModel;
	private ArrayList<String> names;
	private int selectedIndex;
	private JButton btApply;
	private JPanel panel;
	private JFrame frame;
	private JLabel labelFile;
	private String pathRemember="C:/Users/a1500/Documents/Eclipse Workspace/netCDF files/";
	protected JFileChooser fileChooser;
	
	

	/**
	 * Constructor
	 * 
	 * @param fileName
	 */
	public NetCDFFrame(String fileName,JFrame frame){
		this.frame= frame;
		setupMenu();

		this.filename= fileName;
		setTitle("-  NetCDF: "+filename.substring(0, 8)+"...."+filename.substring(fileName.length()-15));
		
		display(fileName);
	}


	/**
	 * @param fname
	 */
	public void display(String fname) {
		
		try {
			ncfile= NetcdfFile.open(fname);
			
			// The netCDF is okay
			if (ncfile!=null) {
				// loop through all variables
				List<Variable> variables= ncfile.getVariables();
				names= new ArrayList<String>();
				String vn= "";
				for (Variable var : variables) {
					if (var.getDimensions().size()>1 && var.getDimensionsString().contains("X")) {
						
					vn= var.getFullName();
					names.add(vn);
					}
				}
				// Setup GUI and add names of the variables
				setupList(names);
				this.validate();
				
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally { 
			if (null != ncfile)
				//				ncfile.close();
				System.out.println("\n netCDF file is closing ");
		}
	}
	
	
	/**
	 * 
	 */
	private void setupMenu(){
		JMenuBar menu= new JMenuBar();
		JMenu menuFile= new JMenu("File");
		menuFile.setFont(new Font("Arial", Font.PLAIN, 14));
		menuFile.setMnemonic(KeyEvent.VK_F);
		JMenuItem menuOpen= new  JMenuItem("Open");
		menuOpen.setMnemonic(KeyEvent.VK_O);

        JMenuItem menuExit = new JMenuItem("Exit");
        menuExit.setMnemonic(KeyEvent.VK_E);
		
        //Open file action
		menuOpen.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
        		//Initial directory
        		File p= new File(pathRemember);

				fileChooser = new JFileChooser(p);
				int result = fileChooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
				    File selectedFile = fileChooser.getSelectedFile();
				    filename= selectedFile.getAbsolutePath();
				    pathRemember= fileChooser.getCurrentDirectory().getPath();
				    display(filename);
				    System.out.println("Selected file: " + selectedFile.getAbsolutePath());
				}
			}
		});
		
		menuFile.add(menuOpen);
		menuFile.add(menuExit);
		menu.add(menuFile);
		setJMenuBar(menu);
	}

	/**
	 * 
	 * @param names
	 */
	private void setupList(ArrayList<String> names){
		
		// Use a list model to add the names of the variables of strings
		listModel = new DefaultListModel<String>();
		for (String vn : names) {
			listModel.addElement(vn);
		}

		list = new JList<String>(listModel);
		list.setBackground(Color.lightGray);
		list.updateUI();
		
		// Button to add distributions
		JButton btApply= new JButton("Add Distribution"); 
		btApply.setMaximumSize(new Dimension(50, 20));
		
		// Action listener for the JButton
		btApply.addActionListener(new ActionListener() {
			
			// 
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedIndex= list.getSelectedIndex();
				insertDistribution(names.get(selectedIndex));
			}

		});
		
			
		//Layout of GUI
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		//
		JPanel panel= new JPanel(new BorderLayout());
		panel.add(btApply, BorderLayout.SOUTH);
		panel.add(list,  BorderLayout.CENTER);
		
		String name= filename.substring(0, 8)+"...."+filename.substring(filename.length()-12);
		labelFile= new JLabel(name);
		labelFile.setFont(new Font("Arial", Font.ITALIC, 14));
		panel.add( labelFile ,BorderLayout.NORTH);
		
//				
//		if (contentPane. getComponents()!=null) {
//			contentPane.remove(0);
//		}
//		contentPane.removeAll();
		contentPane.add(panel);
	}
	
	/**
	 * Creates a new visual distribution and adds it to the panel
	 * 
	 * @param name The name of the variable we are going to look for in the netCDF file
	 */
	public void insertDistribution(String name) {
		VisualDistribution vd= new VisualDistribution(name, ncfile);
		//
		if (dPanel==null) {
			dPanel= new DistributionsPanel(vd,  true);
			dPanel.setFramePosition(new Point(this.getLocation().x+this.getWidth(),this.getLocation().y));
		}
		else {
			dPanel.addDistribution(vd);
		}
//
//		if (fxApp!=null) {
//			fxApp.setPanelDistr(dPanel);
//		}
	}


	/**
	 * The main,0
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		  EventQueue.invokeLater(new Runnable(){
			  public void run()
			  {
//				  String path="C:/Users/a1500/Documents/Eclipse Workspace/netCDF files/ibm2felt.nc";
				  String path="R:/alle/Rune/sildyear1.nc";
				  NetCDFFrame frame= new NetCDFFrame(path, null);
				  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				  frame.setSize(300, 350);
				  frame.setTitle("-  NetCDF: "+path.substring(0, 8)+"...."+path.substring(path.length()-15));
				  frame.setVisible(true) ;
			  }
		  });


	}

	

	// ==================================================

	/**
	 * @return the ncfile
	 */
	public NetcdfFile getNcfile() {
		return ncfile;
	}


	/**
	 * @param ncfile the ncfile to set
	 */
	public void setNcfile(NetcdfFile ncfile) {
		this.ncfile = ncfile;
	}

	

	/**
	 * @return the dPanel
	 */
	public DistributionsPanel getdPanel() {
		return dPanel;
	}


	/**
	 * @param dPanel the dPanel to set
	 */
	public void setdPanel(DistributionsPanel dPanel) {
		this.dPanel = dPanel;
	}


	/**
	 * @return the selectedIndex
	 */
	public int getSelectedIndex() {
		return selectedIndex;
	}


	/**
	 * @param selectedIndex the selectedIndex to set
	 */
	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}


	/**
	 * @return the names
	 */
	public ArrayList<String> getNames() {
		return names;
	}


	/**
	 * @param names the names to set
	 */
	public void setNames(ArrayList<String> names) {
		this.names = names;
	}


	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowClosed(WindowEvent e) {
		try {
			ncfile.close();
		} catch (IOException e1) {
			// 
			e1.printStackTrace();
		}
		
	}


	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}


}
