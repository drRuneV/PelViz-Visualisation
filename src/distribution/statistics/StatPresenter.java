package distribution.statistics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.util.DoubleSummaryStatistics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import interaction.LineGraph;


/**
 * 
 * @author a1500
 *
 */
public class StatPresenter extends JFrame{
	
	private StatDistr statDistribution= null;

	//
//	private JFrame frame =null;
	private JPanel panel;
	private JTextArea areaDay=null;
	private JTextArea areaTotal=null;
	private JPanel panelWithImage=null;
	private boolean isPresenting=false;
	private Histogram histogram= null;
	//
	BufferedImage image =null;
	private int graphNumber=0;

	//
	private LineGraph graph;
	
	
	
	
	/**
	 * Constructor
	 * @param statDistribution
	 */
	public StatPresenter(StatDistr statDistribution) {
		this.statDistribution= statDistribution;
		setup(300);
		setPresenting(true);
		setVisible(true);
	}
	
	
	
	
	/**
		 * @param dx
		 */
		private void setup(int dx) {
			// main panel
			panel= new JPanel();
			panel.setLayout(new BorderLayout());
	
			add(panel);
			pack();
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setLocation(dx, 300);
			setSize(550, 250);
//			setAlwaysOnTop(true);
	
			// Listen to the frame
			createWindowsListener();
			// MouseListener
			createMouseListener();
	
			areaTotal=displayValues(-1,areaTotal);
//			System.out.println(areaTotal);
			panel.add(areaTotal, BorderLayout.WEST);
			areaDay=displayValues(0, areaDay);
			areaDay.setBackground(Color.lightGray);
			areaDay.setForeground(Color.blue);
			
			panel.add( createPanelWithImage(),BorderLayout.EAST);
			panel.add(areaDay,BorderLayout.CENTER);
	//			panel.setLayout(new BorderLayout());
	
		}




	/**
	 * Presents the statistics on a Jframe
	 * @param dx X location on screen
	 */
	public void update(int dx, int time){

//		isPresenting= true;

//		if (!isVisible()) {
//			setVisible(true);
//		}
		
		// Continuous updating
		if (isPresenting) {
			displayValues(time, areaDay);
			if (time!=-1) {
				histogram=statDistribution.histograms[time];
				updatePanelHistogram(time);
			}
			panelWithImage.repaint();
			panel.repaint();
			repaint();
		}

	}




	/**
	 * Create and add a MouseListener to the frame
	 */
	private void createMouseListener(){
		
		addMouseMotionListener(new MouseMotionListener() {
			
			

			

			@Override
			public void mouseMoved(MouseEvent e) {
				repaint();
				panelWithImage.repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				String title= "Histogram #"+ histogram.count +" max:"+histogram.max;
				if (graph==null) {
					Histogram h= statDistribution.histograms[0];
					title= "H #"+ h.count +" max:"+h.max;
					graph= new LineGraph(LineGraph.convertFromFloat(h.columns), title, "Interval", "number",true);
					graph.show();
					graph.defineTheAxis(200);
					graphNumber++;
					for (int i = 1; i < statDistribution.histograms.length; i++) {
						title= i+" H #"+ h.count +" max:"+h.max;
						Histogram hh= statDistribution.histograms[i];
						graph.addSerie(LineGraph.convertFromFloat(hh.columns), title);
						
					}
				}
//				else if (graphNumber<statDistribution.histograms.length) {
//					graph.addSerie(LineGraph.convertFromFloat(histogram.columns), title);
//					graphNumber++;
//				}
			}
		});
	}

	/**
	 * 
	 */
	private void createWindowsListener() {
		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent arg0) {					
			}			
			
			public void windowIconified(WindowEvent arg0) {					
			}			
			@Override
			public void windowDeiconified(WindowEvent arg0) {					
			}			
			@Override
			public void windowDeactivated(WindowEvent arg0) {					
//				setPresenting(false);
			}
			
			@Override
			public void windowClosing(WindowEvent arg0) {
				setPresenting(false);
				dispose();//				=null;
				System.out.println("closing the statistics window");
			}
			
			@Override
			public void windowClosed(WindowEvent arg0) {
			}			
			@Override
			public void windowActivated(WindowEvent arg0) {
				statDistribution.setPresenting(true);
				setPresenting(true);
			}
		});
	}

			
		
	/**
	 * Displays values in a text area
	 * @param time the current time step
	 * @param area the text area to display on
	 * @return the text area filled out
	 */
	private JTextArea displayValues(int time, JTextArea area ) {
		
		StatDistr statistics[] = statDistribution.getStatistics();		
		StatDistr stat=  (time<0) ?  statDistribution: statistics[time];
		
		DoubleSummaryStatistics dss= stat.ss;//(time<0) ? statDistribution.ss : statistics[time].ss ;
		String title= (time<0) ? "Total":  stat.getDistribution().getDateStrings()[time];
		String format1= (dss.getMax()> 10000  ) ?  "%.1e"  :"%.2f";
		
		// Create or clean the area
		if (area== null) {
		area= new JTextArea();
		area.setBackground(Color.lightGray.brighter() );
		area.setFont(new Font("Serif", Font.ITALIC, 15));
		area.setEditable(false);
		area.setPreferredSize(new Dimension(150, 120));
//		System.out.println("Area created! "+area);
		}
		else{
			area.setText("");
		}

		area.append(title);
		area.append(String.format("\nMax   =  "+ format1+"\n", dss.getMax()));
		area.append(String.format("Min   = %.4f \n", dss.getMin()));
		area.append(String.format("Average= "+format1+"\n", dss.getAverage()));
		area.append(String.format("Median = "+format1+"\n", stat.getMedian()));
		area.append(String.format("Std   = "+format1+"\n",stat.getStd()));
		area.append(String.format("Count:%d \n", dss.getCount()));
		String infoZeros= (statDistribution.acceptZeros) ? "Accepting zeros": "No zeros accepted";
		area.append(String.format(infoZeros+"\n"));
		
		return area;
	}

	
	
	
	/**
	 * Draws the histogram  for the current time step on an image and repaint 
	 * the panel whether image is located
	 * @param t the current time step
	 */
	private void updatePanelHistogram(int t){
		image = histogram.drawHistogram();//statDistribution.histograms[t].drawHistogram() ;
		panelWithImage.repaint();
	}
	

	
	
	
	/**
	 * Creates the panel where the image with the histogram will be showed
	 * @return
	 */
	private JPanel createPanelWithImage(){

		// Create a panel where image can be put
		if (panelWithImage==null) {
			panelWithImage= new JPanel(){

				@ Override
				/**
				 * Repaint method for panel
				 */
				public void paintComponent(Graphics g) {
					// Clear graphics of the panel
					Graphics2D gPanel = (Graphics2D) g;
					gPanel.setBackground(Color.black);
					if(image!=null){
						gPanel.clearRect(0, 0, image.getWidth(), image.getHeight());
						gPanel.drawImage(image,0, 0,null,null);
						int  dy= image.getHeight()+5;
						gPanel.drawImage(statDistribution.getTotalHistogram().drawHistogram(),0, dy,null,null);
//						gPanel.drawImage(statDistribution.getDistribution());
					}
				}

			};

		}
		panelWithImage.setPreferredSize(new Dimension(180, 120));


		return panelWithImage;
	}




	/**
	 * @return the isPresenting
	 */
	public boolean isPresenting() {
		return isPresenting;
	}




	/**
	 * @param isPresenting the isPresenting to set
	 */
	public void setPresenting(boolean isPresenting) {
		this.isPresenting = isPresenting;
	}


	
	
	
	
}
