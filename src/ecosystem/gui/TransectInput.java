package ecosystem.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import ecosystem.Coordinate;
import ecosystem.PolygonCoordinate;
import ecosystem.TransectLine;
import net.miginfocom.swing.MigLayout;

public class TransectInput extends JFrame{

	private String line;
	
	private String title = "Transect Line Coordinates";
	private JLabel labelTitle= new JLabel(title);
	private JLabel label= new JLabel("Enter coordinates as Lat Lon");
	private JTextArea coordinateText= new JTextArea("64.0 0.0 \n"+"78.0 -10");
	private JButton btApply= new JButton("Apply");  
	
	private TransectLine transectLine=null;

	
	
	
	/**
	 * Constructor
	 * @param transectLine Transit line to operate on
	 */
	public TransectInput(TransectLine transectLine, Point location) {

		//Set border layout for the frame
		setLayout(new BorderLayout());
		JPanel panel= new JPanel(new MigLayout());
		// 
		labelTitle.setForeground(Color.red);
		panel.add(labelTitle,"wrap");
		panel.add(label, "wrap");
		panel.add(coordinateText, "wmin 150 ,hmin 60, wrap");
		panel.add(btApply);
		//
		// Add the panel with the components to the centre
		add(panel, BorderLayout.CENTER);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(260, 190);
		setLocation(location.x, location.y);
		setVisible(true);
		setTitle(title );
		

		//  Insert coordinates from the transect line
		if (transectLine!=null) {
			if (transectLine.measureCoordinate.size()>1) {
				int size= transectLine.measureCoordinate.size();
				Coordinate c1=  transectLine.measureCoordinate.get(0);
				Coordinate c2=  transectLine.measureCoordinate.get(size-1);
				String sp  = String.format("%.2f %.2f \n%.2f  %.2f ", c1.getLat(), c1.getLon() ,c2.getLat(),c2.getLon()) ;
				coordinateText.setText(sp.replaceAll(",", "."));
			}
		}
		

		// Action Listener for button
		btApply.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String text=coordinateText.getText();
				Scanner sc = new Scanner(text);
				Coordinate c=null;
				int n=0;
				PolygonCoordinate pco= new PolygonCoordinate(text);
				Coordinate co[] = pco.getPolygon();
				//
				for (Coordinate coordinate : co) {
					System.out.println(coordinate.toString());
				}
//				
				if (transectLine!=null) {
					transectLine.measureCoordinate.clear();
					transectLine.measureCoordinate.add(co[0]);
					transectLine.receiveCoordinate(co[1]);
				}
				
				
//				while(sc.hasNextLine()){
//					line= sc.nextLine();
//					c=parseCoordinate(line, n);
//					System.out.println(c.toString());
//
//					if (transectLine!=null) {
//						if (n==0) {
//							transectLine.measureCoordinate.clear();
//							transectLine.measureCoordinate.add(c);
//						}
//						else {
//							transectLine.receiveCoordinate(c);
						}
//					}
//					n++;
//				}
//				sc.close();
//
//			}
		});

	}
	
	/**
	 * 
	 * @param line The line to parse
	 * @param n the line number
	 * 		//do not really need this if we use polygon coordinate object
	 */
	private Coordinate parseCoordinate(String line,int n) {
		
		Coordinate c= new Coordinate(0, 0);
		Scanner sc= new Scanner(line);
		String field="";
			//		while(sc.hasNext()){
		// First latitude
		field= sc.next().replaceAll(",", ".");
		float  lat= Float.parseFloat(field);
		c.setLat(lat);
		// Then Longitude
		field= sc.next();
		float  lon= Float.parseFloat(field);
		c.setLon(lon);

		return c;
	}
	
	
	
	public static void main(String[] args) {
		TransectInput t= new TransectInput(null, new Point(500, 400));
		
	}

}
