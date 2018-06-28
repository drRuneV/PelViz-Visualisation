package basicGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import graphics.DistributionsPanel;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import visualised.VisualDistribution;

public class NetCDFPanel extends JPanel{

	String fileName;

	JList<String> list;
	private DefaultListModel<String> listModel;
	private ArrayList<String> names;
	private int selectedIndex;
	//
	// JFrame
	JFrame frame=null;
	// JPanel
	DistributionsPanel panel =null;
	// Current distribution selected
	private VisualDistribution distribution=null;

	private NetcdfFile ncfile;

	/**
	 * Constructor
	 * @param fileName
	 */
	public NetCDFPanel(String fileName,DistributionsPanel panel){

		this.fileName= fileName;
		this.panel= panel;

			try {
				ncfile = NetcdfFile.open(fileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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
				setup(names);


			}

	}
	
	
	private void setup(ArrayList<String> names){

		// Use a list model to add the names of the variables of strings
		listModel = new DefaultListModel<String>();
		for (String vn : names) {
			listModel.addElement(vn);
		}

		list = new JList<String>(listModel);
		list.setBackground(Color.lightGray);

		// Button to add distributions
		JButton btApply= new JButton("Add Distribution"); 
		btApply.setMaximumSize(new Dimension(50, 20));

		// Action for the apply button
		btApply.addActionListener(new ActionListener() {
			

			// 
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedIndex= list.getSelectedIndex();

				VisualDistribution vd= new VisualDistribution(names.get(selectedIndex), ncfile);
//				distribution=vd;
				if (panel!=null) {
					panel.addDistribution(vd);
				}
				else {
					vd.show();
				}
				
				//					if (dPanel==null) {
				//						dPanel= new DistributionsPanel(vd, 0, false);
				//					}
				//					else {
				//						dPanel.addDistribution(vd);
				//					}

				//					DistPanel p= new DistPanel(vd, 2,true);
			}
		});

		//Layout of GUI
		//			Container contentPane = this.getContentPane();
		//			contentPane.setLayout(new FlowLayout(FlowLayout.LEFT));

		//
		JPanel panel= new JPanel(new BorderLayout());
		panel.add(btApply, BorderLayout.SOUTH);
		panel.add(list,  BorderLayout.CENTER);

		String name= fileName.substring(0, 8)+"...."+fileName.substring(fileName.length()-12);
		JLabel label= new JLabel(name);
		label.setFont(new Font("Arial", Font.ITALIC, 14));
		panel.add( label ,BorderLayout.NORTH);
		//
		this.add(panel);

		//			contentPane.add(panel);
	}
		
		
	
	public void show(){

	  frame= new JFrame();
	  frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	  frame.setSize(300, 350);
	  frame.setTitle("-  NetCDF: "+fileName.substring(0, 8)+"...."+fileName.substring(fileName.length()-15));
	  frame.setVisible(true);
	  frame.add(this);

		
	}

	
	public static void main(String[] args) {

		  String path="C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/sildyear1.nc";
		  NetCDFPanel p= new NetCDFPanel(path, new DistributionsPanel(null, false));
//		  
		  p.show();
//		  JFrame frame= new JFrame();
//		  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		  frame.setSize(300, 350);
//		  frame.setTitle("-  NetCDF: "+path.substring(0, 8)+"...."+path.substring(path.length()-15));
//		  frame.setVisible(true);
//		  frame.add(p);
//		  frame.
	
	}

}
