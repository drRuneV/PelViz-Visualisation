package graphics;


import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.BoxLayout;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import basicGUI.KeyboardHelp;
import basicGUI.NetCDFPanel;
import ecosystem.Clock;
import ecosystem.Coordinate;
import ecosystem.SurveyLines;
import ecosystem.TransectLine;
import interaction.EMode;
import interaction.ImageSaver;
import interaction.LineGraph;
import interaction.PopupHandler;
import interaction.SelectionMask;
import javafx.stage.Stage;
import pointDistribution.Vessel;
import ucar.nc2.util.reflect.PublicInterfaceGenerator;
//import testing.AudioItem;
import visualised.VisualDistribution;
import visualised.VisualTopography;



/**
 * 
 * @author a1500
 *
 */
public class DistributionsPanel extends DistPanel implements KeyListener,
								MouseListener, MouseMotionListener{

	private static final long serialVersionUID = 1L;
	private ArrayList<VisualDistribution>  distributions= new ArrayList<>();
	private int ixd=0;
	private int ixguest=0;
	
	// Various flags
	private boolean combine=false;
	private boolean join=true;
	
	private boolean showVessel=false;
	private boolean showSurveyLine=false;
	
	//  BufferedImage to draw upon
	private BufferedImage imScaled= null;
	// The image representing the topography
	private BufferedImage topoImage=null;
	private BufferedImage scaleTopographyImage=null;
	
	private Graphics2D panelGraphics =null;
	
	private String strInformation="";
	// This object takes care of saving images
	private ImageSaver imageSave= null;

	// Sound interaction
//	static String soundPath= "C:/Users/Admin/Music/Sound effects/Underwater.wav";
//	private AudioItem audioItem= null;
	
	// «•» The path to the topographic data is hardcoded here:
	 //"R:/alle/Rune/etopo2.nc";
	private String topoFile= 
//			"C:/Users/a1500/Documents/Eclipse Workspace/netCDF files/etopo2.nc";
	"C:/Users/a1500/Documents/Eclipse Workspace/netCDF files/etopo2.nc"; 
			//"R:/alle/Rune/etopo2.nc";
	private ColorGradient topogradient;
	private boolean useTopo= true;
	//When was the mouse down and how long has it been down
	private long whenMouseDown;
	private long durationDown;
	private boolean mirror=false;
	// The image containing the chart
	private BufferedImage imageChart;
	private JLabel statusLabelMode=new JLabel("status");
	private JLabel statusLabel=new JLabel("status");
	private JPanel statusPanel;
	//
	PopupHandler popMenu= null;
	//
	private Stage fxStage=null;
	//Vessel
	ArrayList<Vessel> vessels=null;
	// SurveyLines 
	private SurveyLines surveyLine= null;
	//
	private TransectLine transectLine=null;
	
	// 
	private int fontIndex=0;
	private boolean showTransect=false;
	//
	private  static String[] fontNames= new String[]{"Tahoma","Aerial","Calibri","Lucida bright",
			"System",			"Bell MT",		"Veranda",		"Times new Roman",
			"Bookman Old Style","Bodoni",		"Castellar",	"HP simplified",
			"Modern No 20",		"Lucida fax",   "Microsoft sans serif"};

	
	// ===================================================================================
	
	
	
	// ==================================================
	// 				Constructors
	// ==================================================
	public DistributionsPanel(VisualDistribution distribution,boolean useFrame) {
		super(distribution, useFrame);

		// Do nothing if there is no distribution
		if (distribution==null) {
			return;
		}		
		
		//	Add mouse Listeners
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
			
		
		// Handle to the gradient
		gradient= distribution.getGradient();
		// 

		// Make a topography	
		createTopography();
		distribution.setUseTopography(useTopo);

		scale = (distribution.getWidth()>300) ? (float)1.0: (float) 3;//(1.25*2);
		addDistribution(distribution);
		
		setupStatusBar();
		//
		popMenu= new PopupHandler(this);
		//Vessel
		// vessels=Vessel.testing();
		
		//Audio
//		try {
//			audioItem= new AudioItem(soundPath);
//			audioItem.gainControl.setValue(-20);
//		} catch (LineUnavailableException e) {
//			e.printStackTrace();
//		}
		
	}


	
	
	
	/**
	 * Creates the topography.
	 * This method is only called once, either by the constructor or by the user. 
	 * @param distribution a visual distribution
	 */
	public void createTopography() {
		topography= new VisualTopography(topoFile,"btdata");

		boolean isFileOkay =topography.getNcfile()!=null;

		if (topography!=null  && isFileOkay){
			if (topography.isOk) {

				topogradient= topography.getGradient(); 
				rescaleTopography(distribution);
				System.out.println(topography+" isFileOkay: "+isFileOkay);
			}
			else {
				strInformation=" -> Could not find the topography file";
				System.out.println(strInformation);
				useTopo=false;
				setFlagForUsingTopography();
				topography= null;
			}
		}
		else{
//			JOptionPane.showMessageDialog(null, "Unable to find the Topography file!");
			topography= null;
			useTopo=false;
		}
	}


	/**
	* Set the flag of using topography for each distribution for consistency
	 */
	private void setFlagForUsingTopography() {
		for (VisualDistribution vd : distributions) {
			vd.setUseTopography(useTopo);
		}
	}


	/**
	 * Rescale is the topography to double size
	 * @param dis distribution where we get the coordinate grid from
	 */
	private void rescaleTopography(VisualDistribution dis) {
		int sc=  2;
		int n=2;
		//testing
		Coordinate[] c= dis.getCoordinates();
		// Create a new interpolated coordinate grid
		Coordinate[] coo= topography.interpolate(dis.getWidth(),dis.getHeight(), c );
		System.out.println("Coordinate length: "+coo.length+" old:"+c.length);
		// Try another round
		coo= topography.interpolate(dis.getWidth()*2-1,dis.getHeight()*2-1, coo );
		sc=4;
		
		System.out.println("Coordinate length: "+coo.length+" old:"+c.length);
//				for (Coordinate coordinate : coo) {
//					System.out.println(coordinate.toString());
//				}
		
		n=3; // //  ? • ? •? •? • this actually works but why? 5->10-1=9 : 9->18-1= 17 !! •
		//  we should track the scaling in the topography itself !•
		
		// This image follows the size of the XY Distribution
		topoImage = new BufferedImage(dis.getHeight()*sc-n,dis.getWidth()*sc-n, BufferedImage.TYPE_INT_ARGB);
//				topography.drawOnBufferedImageXY(topoImage, distribution.getCoordinates());
		topography.drawOnBufferedImageXY(topoImage, coo);
	}

	

	/**
	 * 
	 */
	private void redrawTopography() {
		if (topography.isOk) {
			Coordinate[] c= topography.interpolate(distribution.getWidth(),
					distribution.getHeight(), distribution.getCoordinates() );
			// again
			c= topography.interpolate(distribution.getWidth()*2-1,
					distribution.getHeight()*2-1, c);
			//  
			topography.drawOnBufferedImageXY(topoImage, c);
		}
	}





	/**
	 * Add a distribution to the list of distributions
	 * @param dist the distribution to be added
	 */
	public void addDistribution(VisualDistribution dist){

		// Adds the interaction listener with regards to distribution
		if ( interactDistribution==null) {
			this.addListenersDistribution(dist);
		}
		//  Adding the distribution to the array list
		distributions.add((VisualDistribution) dist);
		setFlagForUsingTopography();
		
		// Calculate volume 
		if (topography != null) {
			dist.setWaterVolume(dist.calculateTheVolume(topography));
			//! AreaInformation is calculated in distribution.Java, before volume exists: must calculate Volume
			if (dist!=null) {
				dist.getAreaInfo().calculateWaterVolume();
			}
		}
		
//		System.out.println("Added Distribution has the size: "+distributions.size());
		// redefine both the size of the panel and the image
		redefine();
		
		
	}
	
	/**
	 * Set the location of the parent frame
	 * @param p -  The location where we want to place the frame
	 */
	public void setFramePosition(Point p){
		if (useFrame) {
			JFrame frame = (JFrame) this.getTopLevelAncestor();
			frame.setLocation(p);
		}
	}
	
	/**
	 * Redefines the size of the panel to fit the number of distributions and the scale.
	 */
	public void redefine(){
		if (distributions !=null  && !distributions.isEmpty()) {

			int width= distributions.get(0).getWidth();
			int height= distributions.get(0).getHeight();
			int n= distributions.size();
			// Define it with opposite dimensionality
			int dWidth= (int)( (height+2*off.x)*n*scale  + off.x*2);
			dWidth+= distribution.getGradient().getImage().getWidth();
			int dHeight= (int) (width*scale+120+ 10*n*scale); //arbitrary!!
			Dimension dimension= new Dimension(dWidth, dHeight);
			setPreferredSize(dimension);
			this.setSize(dimension);
			this.setFocusable(true);
			this.requestFocusInWindow();
			this.requestFocus(true);
			

			// Create the image we are drawing, but opposite !!!
			image.flush();
			image = new BufferedImage((height+off.x)*n, width+off.y, BufferedImage.TYPE_INT_ARGB);

			// Resize the frame to fit the panel
			if (useFrame) {
				JFrame frame = (JFrame) this.getTopLevelAncestor();
				frame.setSize(dimension);
				frame.setMinimumSize(new Dimension((int) (height*1.5f), (int) (width*1.5f)));
			}
			if (fxStage!=null) {
//				int w = fxStage.getScene().getRoot().getChildrenUnmodifiable().get(1).get ;
				int w= (int) Math.max(500, image.getWidth()*scale+ 180);
				fxStage.setWidth(w);
				int h  = (int) Math.max(500, image.getHeight()*scale+ 220) ;
				fxStage.setHeight(h);
//				fxStage.requestFocus();
			}

//			System.out.println("Everything redefined panelImage ! "+ image.getWidth()+"×"+image.getHeight());
//			System.out.println("Dimension: " +dimension.width+"×"+dimension.height+" Scale:"+scale);

			//Define the pixel region
			defineRegions();
		}	
	}

	
	
	private void testlatlon(Graphics2D g, Coordinate c){
		int index=c.findClosestIndex(distribution.getCoordinates(), 0.5f);

		if (index!=-1) {
			
//		System.out.println("closest indexed to "+c.toString()+" is "+index+" : "+
//		distribution.getCoordinates()[index].toString());
		// Coordinates is just a list of values organised as a y-x 
		int y = index/distribution.getWidth();//the actual y value
		int x = index- y*distribution.getWidth(); //the actual X value
		Point screen= new Point();
		screen.x= (int) ((off.x)*scale*(ixd+1)+ distribution.getHeight()*scale*ixd +y*scale);
		screen.y= (int) ( off.y+ x*scale);
		g.drawString("•", screen.x, screen.y);
		g.drawString(c.toString(), screen.x, screen.y+20);
		}
	}
	
	
	private void setupStatusBar(){
		
	this.setLayout(new BorderLayout());
	//
	Font font= new Font("Bodoni MT", Font.PLAIN, 16);
			

	// create the status bar panel and show it down at the bottom of the frame
	statusPanel = new JPanel();
//	statusPanel.setBackground(background);
//	statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
	statusPanel.setPreferredSize(new Dimension(getWidth(), 18));
	statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
	this.add(statusPanel, BorderLayout.SOUTH);
	//
	statusLabelMode = new JLabel("…");
	statusLabelMode.setFont(font);
	statusLabelMode.setForeground(Color.red);
//	statusLabel.setBackground(background);
	statusLabelMode.setHorizontalAlignment(SwingConstants.LEFT);
	statusPanel.add(statusLabelMode);
	//Another label

	statusLabel.setForeground(Color.black);
	statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
	statusPanel.add(statusLabel);
}

	
	private void hideProgressFrames(){
		
		for (VisualDistribution vd : distributions) {
			vd.getProgressFrame().dispose();
		}
	}
	
	
	/**
	 * Paint the component, continuously using a timer linked to actionPerformed
	 * Every distribution draws itself on a BufferedImage and these images are arranged together
	 * and scaled on another image placed on the graphics of the panel.
	 */
	@ Override
	public void paintComponent(Graphics g) {
		
		long lastFrame= System.nanoTime();
		
		// Clear graphics of the panel
		Graphics2D gPanel = (Graphics2D) g;
		gPanel.setBackground(background);
		gPanel.clearRect(0, 0, this.getWidth(), this.getHeight()-20);//-statusLabelMode.getHeight());
		panelGraphics= gPanel;

		// rendering hint
		gPanel.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gPanel.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		

		//Do not do anything if there is no distribution
		if (distribution==null || distributions==null) {
			return;
		}
		else if (distributions.isEmpty()){
			return;
		}
		
		// Make sure we do not use a topography which does not exist
		useTopo= (topography==null) ?  false : (topography.isOk)? useTopo: false;

		// Information in status bar
		if (count> 100 && count%1==0) {
			displayStatusBar();
		}

		// We repaint the image every time				
		if( image!=null){
			
			if (!wait) {
				count++;
				checkCount();
				distribution.update(count);
				drawDistributions();
			}

		// 
		// Create another scaled BufferedImage first: optimised March 25 •
		if ( imScaled==null || image.getWidth()*scale != imScaled.getWidth()) {
			imScaled=GraphicsUtil.resizeImage(image, (int)(image.getWidth()*scale),
					(int)(image.getHeight()*scale)); 
		}
	
		// 
		// Topography
		if (useTopo) {
		drawTopography(gPanel);
		}
		
		// Finally draw the image
		// Draw DISTRIBUTIONS as the scaled image with distributions on the panel ��� 
		gPanel.drawImage(imScaled, 0,0,null,null);


		// Vessels 
		if (vessels != null && showVessel) {
			drawVessels(gPanel);
		}
		// Survey lines
		if (showSurveyLine && surveyLine!=null){
			drawSurveyLines(gPanel);
		}
		// Transect
		if (transectLine==null) {
			transectLine= new TransectLine(new Coordinate(67, -10), new  Coordinate(67, 5), distribution);
		}
		else if (showTransect && transectLine.measureCoordinate.size()>0) {
			drawTransect(gPanel);
		}

		
		// Draw the associated gradient also
		if ( (scale>1  || distribution.getWidth()>300) && distributions!= null) {
		drawGradients(gPanel);
		}
		
		
		//Draw the name and centre of mass and the histogram
		int n=0;
		for (VisualDistribution vd : distributions) {
			displayName(gPanel, n);
			//centre of mass
			if (vd.showCm) {
			indicateCenterMass(gPanel,n,vd);
			}
			//Histogram
			if (vd.showHistogram)
				drawHistogram(gPanel, n, vd);
		n++;	
		}

		}
		else System.out.println(" The image is undefined! ");

		// Draw image during a drag-and-drop operation
		drawImageDuringADrag(gPanel);
		

		// Display information
		displayInformation(gPanel,ixd);
		indicateWhich(gPanel);
//		displayTime(gPanel,ixd); //removed for presentation

		// Display pixel information interactively; coordinate, depth, area, volume, value
		pixelInfo.explore(interactDistribution.mouseAt,mirror );
		
		// Show AreaInformation
		if ( (scale>1  || distribution.getWidth()>300)  && distribution.isShowAreaInfo()) {
		displayAreaInformation(gPanel);
		}
		// Show biomass interactively
		if (distribution.isShowBiomass()  && !pixelInfo.outofRange)  {
			displayBiomass(gPanel);
		}
		// Continuously showing statistics if presenting
		if (distribution.getStatistics().isPresenting()) {
			distribution.getStatistics().presenting(distribution.getPixelRegion().x+5,count); // //Label4
		}
		
			
		
		// Selection 
		if (interactDistribution.mode==EMode.SELECT ) {
			displaySelection(gPanel);	
		}
		
		
		// Draw a line chart as a image
		if (imageChart!=null) {
			int x = Math.min(interactDistribution.mouseAt.x+10, this.getWidth()-imageChart.getWidth()) ;
			int y = Math.min(interactDistribution.mouseAt.y+10, this.getHeight()- imageChart.getHeight()) ;
			Rectangle r= new Rectangle(x, y, imageChart.getWidth(),imageChart.getHeight());
			
			// Transparency 
			if (r.contains(interactDistribution.mouseAt)) {
				imageChart= GraphicsUtil.createTransparentImage(imageChart, 255,100, Color.lightGray);
			}
			else{
				imageChart= GraphicsUtil.createTransparentImage(imageChart, 255, 200, Color.lightGray);
			}
			gPanel.drawImage(imageChart,x , y ,null,null);
		}
		
		// 
//		for (int i = 0; i < 50; i++) {
//		testlatlon(gPanel, new Coordinate(70, -25+i) );
//		}
		
		//		if(joinImage||combine) displayCombine(gPanel);
		//
		// Save all images
		if (imageSave!=null) {

			if (imageSave.isActive()) {
				imageSave.save(count, this);
			}
			else {
				wait=false;
			}			
		}
		// 
		imScaled.flush();
		imScaled=null;
		gPanel.dispose();
		
		// this is a testing area
		//
		//	soundFeedback();

		//
		if(duration>4){
			hideProgressFrames();
		}
		
		//
		String st="ma 20" ;
//		System.out.println("Date "+st+" corresponds to: "+distribution.findDateIndexFrom(st," "));
		
		// Calculate frames/s
		if (count%20==0) {
			framesPerS(lastFrame);
		}
		
		// |«•»| Force garbage collection!, because something is leaking memory, 
		//please note that the memory is leaking even when animation is waiting!
		if (count%10==0) {
			System.gc();
			System.runFinalization();
		}
		//gPanel.scale(0.5f, 0.5f); another way to scale the graphics!
	}





	/**
	 * @param gPanel
	 */
	private void drawTransect(Graphics2D gPanel) {
		// Dynamically update the graph � line plot
		transectLine.updateGraph(distribution, count);
		
		Rectangle pRegion=  distribution.getPixelRegion();
		Dimension dim= pRegion.getSize();
		dim.height= dim.width;
		// Make an image of the graph
		BufferedImage bi= GraphicsUtil.createTransparentImage(
				transectLine.getGraph().createImage(dim), 255, 200, Color.lightGray);
		int x= pRegion.x+pRegion.width+2;
		int y= pRegion.y;
		// Place the image on the graphics panel
//			gPanel.drawImage(bi,x , y ,null,null);
		
		//Draw the transectLine dynamically updated
		transectLine.draw(scale);
		// Place the transect line image on the graphic
		Point pos= new Point(pRegion.x, pRegion.y);
		gPanel.drawImage(transectLine.getImage(),pos.x, pos.y,null,null);
	}





	/**
	 * @param gPanel
	 */
	private void drawSurveyLines(Graphics2D gPanel) {
		{
			int w= (int) (distribution.getImage().getWidth()*scale); 
			int h= (int) (distribution.getImage().getHeight()*scale);
			
			// This is arbitrary now �|�|�Handling date is necessary 
			int nPrStep= surveyLine.getLength()/distribution.getTime()-1;
			// Values from current distribution 
			if (surveyLine.getValues()==null) {
				surveyLine.insertValues(distribution.getValues(), distribution.getFillV(), 
							distribution.getWH());
			}
			// Drawing			
			int npoints= Math.max(nPrStep*count, surveyLine.measureCoordinate.size()) ;
			//			System.out.println("Max points to draw is "+npoints);
			
			surveyLine.draw(scale, npoints, w,h);
			Point pos= new Point(distribution.getPixelRegion().x, distribution.getPixelRegion().y);
			
			// Place the survey line image on the graphic
			gPanel.drawImage(surveyLine.getImage(),pos.x, pos.y,null,null);
		}
	}





	/**
		 * Draws all the vessels in each distribution visualisation
		 * @param g the graphics to draw upon
		 */
	private void drawVessels(Graphics2D g){

		if (vessels!=null) {
			// For each distribution
			for (VisualDistribution vd : distributions) {
				Rectangle r= vd.getPixelRegion();
				//				 List of vessels 
				for (Vessel vessel : vessels) {
					vessel.draw(count, scale);
					g.drawImage(vessel.getImage(),r.x ,r.y ,null,null);
				}
				//				Vessel v= vessels.get(0);
				//				v.draw(count, scale);
				//				g.drawImage(v.getImage(),r.x ,r.y ,null,null);
			}
		}

	}





	/**
	 * @param gPanel
	 */
	private void drawTopography(Graphics2D gPanel) {
		float sc = distribution.getImage().getWidth()/topoImage.getWidth() ;

		float scale2= scale*0.25f;// sc;// 0.5f;
		scaleTopographyImage= 
				GraphicsUtil.resizeImage(topoImage, (int)(topoImage.getWidth()*scale2),
				(int)(topoImage.getHeight()*scale2)); 
		// Draw the topography for each distribution
		for (int n = 0; n < distributions.size(); n++) {
			VisualDistribution dist= distributions.get(n);
//			int dx=(int) (dist.getHeight()*n +off.x*(n+1)); // pixelregion?
			int dx= dist.getPixelRegion().x;
			int dy= dist.getPixelRegion().y;
			gPanel.drawImage(scaleTopographyImage, dx,dy,null,null);//(int) (dx*scale), (int)(off.y/scale),null,null);
		}
	}


	/**
	 * @param gPanel
	 * @param n
	 * @param vd
	 */
private void drawHistogram(Graphics2D gPanel, int n, VisualDistribution vd) {
	
		int t= (vd.isHasTime())? count : 0;
		BufferedImage bs=vd.getStatistics().drawHistogram(t);
		
		// Please change this to use pixel regions��� �|�|� �|�|� �|�|�
		int x= (int) (vd.getPixelRegion().x+vd.getPixelRegion().width -vd.getStatistics().gethIntervals()-2 );
		gPanel.drawImage(bs,x, vd.getPixelRegion().y, null,null);
		
		// Guest Histograms
		int dy= bs.getHeight();
		if (join && vd.getGuestList()!=null) {
			for (VisualDistribution vdg : vd.getGuestList()) {
				t=(vdg.isHasTime())? count : 0;
				BufferedImage biGuest=vdg.getStatistics().drawHistogram(t);
				gPanel.drawImage(biGuest,x, 2+dy, null,null);
				dy+=bs.getHeight();
			}
					
			}
	
}


	/**
	 * Draws a one-to-one scale image of the current distribution where the mouse is, 
	 * when the mouse has been down for more than 250 ms and we are not in selection mode
	 * 
	 * @param gPanel the graphic drawing surface of the panel
	 */
	private void drawImageDuringADrag(Graphics2D gPanel) {
		if (mDown && durationDown>250 && interactDistribution.getMode()!=EMode.SELECT ) {
			BufferedImage bi = distributions.get(distributionIndexAt(mouseDownAt.x)).getImage();
			Point p= interactDistribution.mouseAt ;
			//
			if(useTopo) {
				float s = 0.25f ;
				BufferedImage bDrag= GraphicsUtil.resizeImage(topoImage, (int)(topoImage.getWidth()*s),
						(int)(topoImage.getHeight()*s));
				gPanel.drawImage(bDrag,(int)p.getX(), (int)p.getY() ,null,null);
			}
			gPanel.drawImage(bi,(int)p.getX(), (int)p.getY() ,null,null);
			bi.flush();
		}
	}


	/**
	 * @param gPanel
	 */
	private void drawGradients(Graphics2D gPanel) {
		Font font= new Font("Times", Font.BOLD, 12);
		
		for (VisualDistribution vd : distributions) {
			if (vd.showGradient) {
				BufferedImage bi = vd.getGradient().getImage();
				Rectangle r= vd.getPixelRegion();
				int x=(int) r.x+ r.width -bi.getWidth();
				int y=(int) r.y+ r.height-bi.getHeight(); 
				gPanel.setFont(font);
				if (distributions.size()==1) {
					x+=bi.getWidth();
				}
				gPanel.setColor(Color.BLACK);
				gPanel.drawString(vd.getUnit(), x+15, y+8); //causes a thread problem crash
				gPanel.drawImage(bi,x-2,y,null,null);
			}
		}
	}
	

	

	/**
	 * Displays biomass interactively
	 * @param gPanel 
	 */
	private void displayBiomass(Graphics2D gPanel) {
		//  Placement on screen
//		int dx=   distribution.getHeight()/2-yxLocationAtMouse().y;
		int x=  interactDistribution.mouseAt.x+10;//(int) ((dx<0) ? 2*dx: 0.2*dx);  
		x+=  (x-distribution.getPixelRegion().x<10) ? 20: -5 ;
		Point p=  new Point(x , interactDistribution.mouseAt.y-25);
		// Colour and font
		Color c= ColorInt.blackOrWhite(background, Color.lightGray);
		gPanel.setColor(c);
		gPanel.setFont(new Font(fontNames[4], Font.PLAIN, 12+(int)scale));
		// Display the strings
		String split=  pixelInfo.getBiomassString();
		if (!split.isEmpty()) {
			String[] str= split.split(":");
			gPanel.drawString(str[0] ,p.x ,p.y);
			gPanel.drawString(str[1] ,p.x ,p.y+20);
		}
		// Display the total
		String st = distribution.getBiomass().totalMetricTonnString(count);
		gPanel.drawString(st, p.x, p.y+40);
//		String s2=distribution.getBiomass().displayBiomassInformation();
//		gPanel.drawString(s2, p.x, p.y+60);
		
	}



	/** Display area information at the bottom of the distribution image
	 * @param gPanel
	 */
	private void displayAreaInformation(Graphics2D gPanel) {
		int y= distribution.getPixelRegion().height-5;
		int dx= distribution.getPixelRegion().x+1;
		int w =   (int) Math.min(distribution.getPixelRegion().width, (200+scale*50) );
		// Rectangle
		gPanel.setColor(ColorInt.transparencyOf(Color.white, 100));
		
		gPanel.fillRect(dx, y-40,w, 45);
		// Color Font
		Color c= new Color( ColorInt.merge(Color.black.getRGB(), ColorInt.invert(background).darker().getRGB()));
		gPanel.setColor(ColorInt.makeShiftRGB(c, 0, 0, 10));
		gPanel.setFont(new Font("Bodoni MT", Font.PLAIN, 14+(int)scale));
		
		// This gives information about the total, see, volume, covered, mask in three different strings
		String[] areaInfo= distribution.getAreaInfo().calculate(count);
		// Total area
		String  areaString[]= areaInfo[0].split("V");
		if (w>=distribution.getPixelRegion().width) {
			gPanel.drawString(areaString[0],dx, y-0);
		}
		else {
			gPanel.drawString(areaInfo[0],dx, y-0);
		}
		// Coverage
		gPanel.setFont(new Font(fontNames[4], Font.PLAIN, 14+(int)scale));
		gPanel.drawString(areaInfo[2],dx+130, y-20);
		// Mask		
		gPanel.drawString(areaInfo[1],dx, y-20);
	}


	/**
	 * Display the name of a given distribution with the current date information
	 * @param g The graphics to draw upon
	 * @param i The distribution number
	 */
	private void displayName(Graphics2D g,int i) {
		VisualDistribution dist= distributions.get(i);
		boolean hasTime= distribution.isHasTime();
		String dateString=""; 
		
		// Finds the day month and year from the distributions date array
		if (hasTime) {
			int hourSince1950= distribution.getDates()[count];
			Clock clock= new Clock(hourSince1950);
			dateString =clock.dayMonthYearString() ;
		}
		String display= String.format(dist.getFullName()+" ");
		// Extra information
		if (distribution.useDifference) {
			display+= String.format(" as difference "+distribution.getDiffInterval()+" days ahead" );
		}

		//
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// Font Size
		int fontSize= (int) Math.max(12, Math.min(26, 6*scale));
		fontSize+= (distribution.getWidth()>300) ? 4: 0;
//		g.setFont(new Font("Bodoni MT", Font.PLAIN, fontSize));
		
		// Colour and position
		Color textColour = (ColorInt.isLighterThan(background, Color.blue)||useTopo) ? Color.black: Color.white ;
		g.setColor(textColour);
		Rectangle r=dist.getPixelRegion();
		Point p= new Point(r.x+r.width/4,  (int) (r.y+6+5*scale));
		// Display Date
		g.setFont(new Font("Bodoni MT", Font.BOLD, Math.max(12,fontSize-1)));
		g.setColor(textColour.brighter());
		g.drawString(dateString,p.x,p.y+scale/4);
		int dy=(int) (16+scale*2);
			
		// Display the full name
		g.drawString(display, p.x,p.y+dy);
		//Guest
		VisualDistribution guest= dist.getGuest();
		if (guest!=null &&  (combine || join) ) {
			int n=0;
			for (VisualDistribution vdg : dist.getGuestList()) {
				n++;
				g.setFont(new Font("Arial", Font.PLAIN, fontSize-5));
				g.setColor(Color.red.darker().darker());
				g.drawString("+ "+vdg.getFullName(), p.x, p.y+dy+20*n);
			}
		} 
	}


	/**
	 * Displays various information on the status bar/panel, at the bottom
	 */
	private void displayStatusBar() {
		String s  = " Mode:"+interactDistribution.mode.toString();
		if (interactDistribution.mode== EMode.SELECT){
			String type= interactDistribution.getModeTypeNames()[interactDistribution.getModeType()];
			s+= "  Type: "+type.toUpperCase()+
					String.format(" PixelCoverage: %.1f",distribution.getSelectedMask().getPixelCoverage())+" %";
		}
		statusLabelMode.setText(s+"  ");
//		statusLabelMode.updateUI();
		statusLabelMode.repaint();
		String sfps= String.format(" fps=%.2f", fps);
		statusLabel.setText(" Scale:"+scale+ "  Delay:"+T.getDelay()+" ms"+sfps );
//		statusLabel.updateUI();
		statusLabel.repaint(); 
		// 
		statusPanel.repaint();
//		statusPanel.updateUI();
	}


	/**
	 * Displays information of which distributions are being combined
	 * @param g
	 */ //Not use any more! 
	public void displayCombine(Graphics2D g) {
		int fontSize= (int) Math.max(8, Math.min(16, 10*scale));
		g.setFont(new Font("Arial", Font.PLAIN, fontSize));
		g.setColor(Color.blue.darker().darker());
		int x= (int) (off.x*scale+5);
		int y= (int) (off.y+ distribution.getWidth()*scale + 120);
		if (!distributions.isEmpty()) {
	
			g.drawString(distributions.get(ixguest).getFullName()+" onto "+
					distributions.get(ixd).getFullName() , x,y); 
		}
	
	}


	/**
	 * Displays a  selection area if we are in selection mode
	 * @param gPanel
	 */
	private void displaySelection(Graphics2D gPanel) {
		Point  p= yxLocationAtMouse();
		int w= distribution.getWidth();
		int h= distribution.getHeight();
		int index= p.x+ p.y*(w);
		index= Math.max(0, index);
		index= Math.min(w*h, index);
		
		boolean outofRange  = (p.x<0 || p.y<0 || p.x>=w || p.y>=h);
		Rectangle area= interactDistribution.getAreaSelected();
		int c= interactDistribution.getModeType();
				
		int fill= (mDown==true) ? 50:0;
		gPanel.setColor(new Color(50+50*(c-1), 50, 50, 150+fill));

		//•
		if (!outofRange) {
			if (mDown) {
				// Rectangle
				if (c==0) {
					gPanel.drawRect( area.x, area.y,  (int)area.getWidth(), (int)area.getHeight() );
				}//  Circle
				else if(c==2){ // 
					gPanel.drawOval(area.x, area.y, area.width, area.height);
				}
			}//  Polygon
			if(c==1){
				drawPolygonToScreen(gPanel);
			}

			//Just for testing:Indicate the geographical location
			/*
			if(!pixelInfo.outofRange){
			Coordinate  coo= distribution.getCoordinates()[index];
			String		strCoo =coo.toString();
			gPanel.setColor(new Color(250, 250, 150, 200));
			gPanel.setFont(new Font("Times", Font.ITALIC, 10));
			gPanel.drawString(strCoo,  interactDistribution.mouseAt.x, interactDistribution.mouseAt.y-20);
			}*/
			}

	}

	

	/**
	 * @param gPanel
	 */ // This method could be moved to the mask ���
	private void drawPolygonToScreen(Graphics2D gPanel) {
		
		// Polygons are an image space XY, opposite of distribution space 
		Polygon poly= interactDistribution.getPolygon();
		Point pFrom= new Point();
		Point pTo= new Point();
		int r=4;
		// Converting from polygon to screen positions
		if (poly.npoints>1) {
			int k= 0;
			for (int i = 0; i < poly.npoints; i++) {
				k= (i==poly.npoints-1) ? 0: i+1 ;
				pFrom=screenPositionFromIndex(poly,  i);
				pTo=  screenPositionFromIndex(poly,  k);
				gPanel.drawLine(pFrom.x,pFrom.y,pTo.x,pTo.y);
				gPanel.drawOval(pFrom.x-r, pFrom.y-r, r*2, r*2);
//				gPanel.drawOval(pTo.x-2, pTo.y-2, 4, 4);
			}
		}

		// Indicate the first polygon point
		pFrom=screenPositionFromIndex(poly,  0);
		gPanel.drawOval(pFrom.x-2, pFrom.y-2, 4, 4);


		// Draw potential next point
		int n  = poly.npoints;
		Point p=interactDistribution.mouseAt;
		if (n>0) {
			pFrom=screenPositionFromIndex(poly,  n-1);
			Point  first= screenPositionFromIndex(poly,0); 
			gPanel.drawLine(pFrom.x,pFrom.y,p.x,p.y);
			gPanel.drawLine(first.x,first.y,p.x,p.y);
		}

		// Use the mouse as the last point in the polygon interactively
		//  and draw a filled polygon which is not used as a mask!
//		Point p= yxLocationAtMouse();
		Polygon polygon= new Polygon();//poly.xpoints, poly.ypoints, poly.npoints);
		for (int j = 0; j < poly.npoints; j++) {
			Point pPoly= screenPositionFromIndex(poly, j);//new Point(poly.xpoints[j], poly.ypoints[j]);
			polygon.addPoint(pPoly.x,pPoly.y);
		}
		polygon.addPoint(p.x, p.y);
		if (polygon.npoints>1) {
			gPanel.setColor(distribution.getSelectedMask().getcBlue());
			gPanel.fillPolygon(polygon);
		}

	}


	/**
		 * 
		 */
		private void drawDistributions() {
			// Clear the entire image 
			Graphics2D graphic = clearImage();
		
	
			// Draw each distribution on its own image , 
			int n = 0;
			for (VisualDistribution dist : distributions) {
	//			if (useTopo) {				
	//				dist.drawTopography(topography, cgradient,  dist.getImage());
	//				dist.drawOnBufferedImage(dist.getImage(), count);
	//			}
				if(dist.isUseAccumulated()){
					dist.accumulate(count);
				}
				// Draw the distribution
				dist.drawBufferedImage(count);
				n++;
			}
	
			//Try combination 	
			if(combine &&  distribution.getGuest()!=null  && distribution.getGuest().isHasTime()){
				distribution.drawWithGuestBufferedImage(count);
			} 
	
			//After drawing each distribution draw them together on a image, not on the screen!
			//Draw all images of distributions on the Panel graphics
			for (n = 0; n < distributions.size(); n++) {
				VisualDistribution dist= distributions.get(n);
				int dx= (dist.getHeight()*n +off.x*(n+1)); // Drawing internally nothing to do with pixel region!
				int dy=off.y;//(int) (off.y/scale);
			
				// Draw the Topography as well if requested
//				if (useTopo && topoImage!=null) {
//				graphic.drawImage(topoImage,dx, (int)(off.y/scale),null,null);
//				}

				// «•» If we are in selection mode draw the mask as well «•»////Label3
				if (interactDistribution.getMode()==EMode.SELECT) {
					//dist.getSelectedMask().drawOnTheImage(dist.getImage());
					graphic.drawImage(dist.getSelectedMask().getMaskImage(),dx,dy,null,null);

				}

				// Draw the images aligned horizontally separated by an offset.x
				graphic.drawImage(dist.getImage(),dx, (int) dy,null,null);

				// Draw all guests together as images stacked on top of each other 
				if (join  && dist.getGuest()!=null) {
					if (dist.getGuestList()!=null) {
						for (VisualDistribution vdg : dist.getGuestList()) {
							dx=(int) (vdg.getHeight()*n +off.x*(n+1));
							graphic.drawImage(vdg.getImage(), dx, dy,null,null);
						}
					}
				}
			}
			graphic.dispose();
		}


	@ Override
	/**
	 * Draw all the distributions
	 * We need to override the method from DistPanel because we want to draw something different here
	 */
	public void drawDistributionValues(int  t) {
		if(!distributions.isEmpty()){
			drawDistributions();
		}
	}


	/**
	 * Converts to screen position from a given pair of XY within a distribution.
	 * The polygon are in screen space within the image of the distribution.
	 * @param poly the polygon containing the positions
	 * @param i the index within the polygon of the positions
	 * @return a point representing screen position
	 */
	private Point screenPositionFromIndex(Polygon poly, int i) {
		Point pFrom= new Point();
		int dx= distribution.getPixelRegion().x ;
		int dy= distribution.getPixelRegion().y;
		
		pFrom.x= (int) (dx+  poly.xpoints[i]*scale);//w*scale+ (ixd+1)*off.x*scale);
		pFrom.y= (int) (dy+	 poly.ypoints[i]*scale); //fixed April 14
		
//		if (mirror) { // not necessary here
//			pFrom.x= distribution.getPixelRegion().width-pFrom.x- 1 ;
//			pFrom.y= distribution.getPixelRegion().height-pFrom.y-1 ;
//		}
		
		return pFrom;
	}
	
	private Point screenPositionFromPoint(Point p) {
		Point pFrom= new Point();
		pFrom.x= (int) ( (p.x*scale) + (ixd+1)*off.x*scale);
		pFrom.y= (int) (p.y*scale  +off.y/1); //fixed
		return pFrom;
	}



	/**
	 * Indicates which distribution is selected by a Rectangle
	 * @param g
	 */
	private void indicateWhich(Graphics2D g){
		
		Color c=   g.getColor();
		ColorInt color= new ColorInt( Color.lightGray.brighter() );
//		if (count%20==0) {
//			color.invert();
//		}
		g.setColor(color.makeColor());
		Rectangle r=distribution.getPixelRegion();
		g.drawRect(r.x, r.y, r.width-1, r.height-1);
		g.setColor(c);
	}

	/**
	 * Clears the image to fully transparent
	 * @return a graphics to draw on
	 */
	private Graphics2D clearImage() {
		Graphics2D g= image.createGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));

		g.fillRect(0,0,image.getWidth(),image.getHeight());
		//reset composite
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		return g;
	}



	/**
	 * Defines the pixel region that a distribution covers visually
	 */
	private void defineRegions(){
		int n=0;
		int x=0;
		int y=0;
		int h=0;
		int w=0;
		
		for (VisualDistribution vd : distributions) {
			x= (int) ((vd.getHeight()*n +off.x*(n+1))*scale);
			y= (int) (off.y*scale);	//was: off.y/scale	?
			w= (int) (vd.getHeight()*scale);
			h= (int) (vd.getWidth()*scale) ;
			vd.setPixelRegion(new Rectangle(x, y, w, h));
			Point onScreen= new Point (this.getLocationOnScreen().x+x, this.getLocationOnScreen().y +y); 
			vd.setAbsoluteLocation(onScreen);//
//			System.out.println("PixelRegion: "+ n+" "+vd.getPixelRegion().toString());
			n++;
		}
		
	}

	
	
	/**
		 * @param lastFrame
		 */
		private void framesPerS(long lastFrame) {
			long timeTookms= (long) ((System.nanoTime()- lastFrame)*0.000001);
	//		System.out.println(lastFrame+" ");
			fps= 1000/(timeTookms+0.01f);
	//		System.out.println("frams/s: "+fps);
		}


	/**
	 * Sets the current distribution to the distribution selected.
	 * Sets the corresponding gradient , interaction and pixelinfo as well.
	 */
	private void currentDistributionIs(){
		if(!distributions.isEmpty()){
		distribution= distributions.get(ixd);
		gradient= distributions.get(ixd).getGradient();
		// The interaction has to know which distribution to interact on as well
		interactDistribution.setDistribution(distribution);
		interactDistribution.setGradient(gradient);
		// PixelInformation
		pixelInfo.setDistribution(distribution);
		// 
		if (surveyLine!=null){
			surveyLine.insertValues(distribution.getValues(), distribution.getFillV(), distribution.getWH());
			surveyLine.redrawEverything();
		}
		if (transectLine!=null) {
			transectLine.setDistribution(distribution);
			transectLine.recalculate(distribution,scale);
		}
		
//		System.out.println("Current distribution is:"+ixd);
		}
		
	}
	

	/**
		 * Find which distribution the mouse location is currently above
		 * @param atx The mouse location in x-coordinate
		 * @return The index of the distribution 	
		 */
		public int distributionIndexAt(int atx) {
	//		int x= (int) (1.0f*(atx/scale ) );
			int w= (int) ((distribution.getHeight()+off.x)*scale);
		
			int ix= (atx/w);
	//		System.out.println("w="+w+ " me.x="+atx+" Distribution =x/w="+ixd);
			ix= Math.max(0, ix);
			ix= Math.min(ix, distributions.size()-1);
			return ix;
		}


	/**
	 * Gives the index within the distribution at the current mouse position
	 * @return the index
	  Wednesday, ‎4 ‎April ‎2018:: Not necessarily any more, taken care of by pixelInfo
	public int  indexAtMouse(int useTime) {
		Point p = yxLocationAtMouse();
		int w= distribution.getWidth();
		int h= distribution.getHeight();

		// Make sure that static distribution like topography does not use time count
		int t= (distribution.isHasTime()) ? count: 0;
		//
		int index= p.x+(p.y)*w+ t*w*h*useTime;

		index= Math.min(h*w*distribution.getTime(), index);

		return index;
	}
	*/	




	/**
	 * Gives the location within the distribution at the current mouse position
	 * The screen x position corresponds to y axis within the distribution
	 * @return 
	 */
	private Point yxLocationAtMouse() {
		return pixelInfo.yxLocationAtPoint(interactDistribution.mouseAt); 
				// yxLocationAtPoint(interactDistribution.mouseAt);
	}

	/**
	 * Give us the location within the distribution at the given position
	 * The screen x position corresponds to y axis within the distribution
	 * @param at the position within the component
	 * @return a position corresponding to the XY within the distribution
	 	// Wednesday, ‎4 ‎April ‎2018: not needed any more, taking care of by pixelInfo
	private Point yxLocationAtPoint(Point at) {
		
		/*
		int h= distribution.getHeight();
		// This is the horizontal offset given the distribution number
		int dx= (int)  ( ( ixd*h+off.x*(ixd+1) )*scale);
		// This is the position within the distribution!
		int actualY= (int) (1.0f*((at.x-dx)/scale) ); 
		int actualX= (int) (1.0f*(at.y-off.y)/scale);

		actualY= Math.max(0,Math.min(actualY, h-1));
		actualX= Math.max(0,Math.min(actualX, distribution.getWidth()-1));
//		return new Point(actualX,actualY);
		return pixelInfo.yxLocationAtPoint(at);
	}
*/
		
		/**
	 * @param controlDown
	 * @param screen
	 */
	private void plotLineChart(boolean controlDown, Point screen) {
		float value= distribution.getValues()[pixelInfo.indexYXT] ;
		
		if (distribution!=null ) {
			Point pos  = yxLocationAtMouse();
			boolean isSea= (value!= distribution.getFillV());
			
			//Create the chart 1st time
			if (interactDistribution.getpChart()==null ) {
				interactDistribution.createChart();
			}
			// Update the chart according to position
			else if (distribution.getPixelRegion().contains(interactDistribution.mouseAt) && isSea) {
				int size = distribution.getWidth() ;
				int dxSize=(int)  (0.8*distribution.getPixelRegion().width);//(int) Math.min(scale*150,this.getWidth()- mouseAt.x); 
				int dySize=(int)  (0.4*distribution.getPixelRegion().height);// (int) Math.min(scale*100,this.getHeight()- mouseAt.y);
				
				System.out.println("PixelRegion: "+distribution.getPixelRegion().width);
				
//				dxSize= (int) Math.min(400, dxSize);
//				dySize= (int) Math.min(300,dySize);	
				
				dxSize= (int) Math.max(dxSize,250);
				dySize= (int) Math.max(dySize,200);
				// Update the chart
				interactDistribution.getpChart().update(screen, pos,(int) scale);
				// Create a image of the chart to display
		    	imageChart= interactDistribution.getpChart().
		    			getChart().createBufferedImage(dxSize, dySize);
		    	//
			}
			// Remove the chart If we move the mouse outside the region
			else{
				interactDistribution.removeChart();
				imageChart= null;
			}
	
		}
		//
	}


		@Override
		/**
		 * Action to take place when a key has been pressed
		 */
		public void keyPressed(KeyEvent e) {
			int  key = e.getKeyCode();
			boolean ctrlDown= e.isControlDown();
			boolean altDown= e.isAltDown();
			boolean shiftDown= e.isShiftDown();
			// Only is true if none of the modifier keys are pressed
			boolean only= !ctrlDown && !altDown && !shiftDown;

			// Trigger the key event in the parent class DistPanel
			super.keyPressed(e);
			
			//show topography in separate window
			if(key == KeyEvent.VK_T && ctrlDown){
				if (topography!=null  && topography.isOk) {//  Error,if topography is null?
					topography.setDistribution(distribution);
					topography.showTopoInWindow(); 
				}
				//If there is no topography try to open the file
				else {
					System.out.println("No topography !- !- !- !- !- !-");
					JFileChooser fcd = new JFileChooser();
					fcd.setDialogTitle(" Please locate the Etopo2.nc file");
//					FileNameExtensionFilter filter = new FileNameExtensionFilter("nc");
//					fcd.setFileFilter(filter);
					// 
					int  result= fcd.showOpenDialog(this);
					if (result==JFileChooser.APPROVE_OPTION) {
						File f=fcd.getSelectedFile();
						topoFile=f.getAbsoluteFile().getAbsolutePath();
						createTopography();	
						useTopo= topography.isOk;
					}
				}
			}

			//Help!
			else if(key == KeyEvent.VK_F1){
				
				KeyboardHelp k=new KeyboardHelp(null,this.getMousePosition());//defaultPath);
			}
			
			// Mirror everything
			else if(key == KeyEvent.VK_F5){
				System.out.println("Flipping the distribution image");
				mirror=!mirror;
						
				for (VisualDistribution vd : distributions) {
					vd.setMirror(mirror);
					
				}
				if (topography!=null) {
					topography.setMirror(mirror);
					redrawTopography();//topography.drawBufferedImage(count);
				}
				//		
				if (surveyLine!=null) {
					surveyLine.setMirror(mirror);
				}
				distribution.getSelectedMask().setMirror(mirror);
			}

			else if(key == KeyEvent.VK_O && ctrlDown){
				String initialDirectory="./res/"; //relative path to current directory
				JFileChooser fcd = new JFileChooser(initialDirectory);
				fcd.setDialogTitle("Open netCDF file...");
				fcd.showOpenDialog(this);
				
				// Return the file
				if (fcd.getSelectedFile()!=null) {
					String filename= fcd.getSelectedFile().getAbsoluteFile().getAbsolutePath()+"" ;
					//  
					NetCDFPanel netCDFPanel = new NetCDFPanel(filename,this);
					netCDFPanel.show();
					
				}
			}

			
			// Show line plot along the survey line //Label7
			else if(key == KeyEvent.VK_B  && shiftDown && altDown){

				// display plot of the values along survey lines
			if (surveyLine!=null) {
				// Make sure we have values
				surveyLine.insertValues(distribution.getValues(), distribution.getFillV(), distribution.getWH());
				// Create the plot
				LineGraph surveyPlot= new LineGraph(surveyLine.getValues(),
						distribution.getFullName()+ " Along survey", "Position", 
						distribution.getUnit());
				// Include the guests
				for (VisualDistribution vdguest : distribution.getGuestList()) {
					surveyLine.insertValues(vdguest.getValues(), vdguest.getFillV(), vdguest.getWH());
					surveyPlot.addSerie( surveyLine.getValues() ,vdguest.getFullName());
				}
				// Redefine back to current distribution
				surveyLine.insertValues(distribution.getValues(), distribution.getFillV(), distribution.getWH());
				// Show the plot
				Point p = theRightLocation();
				surveyPlot.show(p);
			}
			}


			//	Save the image
			else if(key == KeyEvent.VK_S  && ctrlDown &&  shiftDown){
				GraphicsUtil.saveImageOfComponent(this);
			}
			// Save all the images
			else if(key == KeyEvent.VK_S  && ctrlDown){
				T.stop();
				wait=true;
				// Let the users choose a file location and name
				File file =  GraphicsUtil.saveImagePath(this);
				if (file!=null) {
					//Using either the distributions pixel region or the entire component(null)
					Rectangle r= (altDown) ? distribution.getPixelRegion(): null ;
					imageSave= new ImageSaver(file, r);
				}
				// Start the process of saving images
				wait=false;
				count=0;
				T.restart();
			}
			
			// Show vessels	
			else if(key == KeyEvent.VK_F  && only){
				showVessel= ! showVessel;
			}
			// Show surveyLine
			else if(key == KeyEvent.VK_V  && only){
				showSurveyLine= ! showSurveyLine;
			}

			// Show transect
			else if(key == KeyEvent.VK_V  && shiftDown){
				showTransect= ! showTransect;
			}


			
			// Gradient Window, shift key enables changing the topography ColorGradient
			else if(key == KeyEvent.VK_H  && ctrlDown){
				boolean isTopo= shiftDown  && topogradient!=null;
				ColorGradient g= (isTopo) ? topogradient: gradient ;
				GradientPanel p= new GradientPanel(g);
				p.show();
				//
			}

			// The colour gradients of all the guests are set to have the same maximum as the current distribution
			else if(key == KeyEvent.VK_G && ctrlDown){
				distribution.normaliseGradients();
			}
			// Change distribution next 
			else if(key == KeyEvent.VK_COMMA){	
				ixd--;
				ixd= Math.max(0, ixd);
				currentDistributionIs();
			}
			// Change distribution  to previous
			else if(key == KeyEvent.VK_PERIOD && only){
				ixd++;
				ixd= Math.min(ixd, distributions.size()-1);
				currentDistributionIs();
			}
			//Remove the distribution, remember the guest list as well
			else if(key == KeyEvent.VK_DELETE){

				// Removing this distribution as guest in other distributions first
				for (VisualDistribution vd : distributions) {
					if (vd!=distribution && !vd.getGuestList().isEmpty() ){
						for (VisualDistribution vdguest : vd.getGuestList()) {
							if (vdguest.equals(distribution)) {
								vd.getGuestList().remove(distribution);
								System.out.println("Removed it as guests as well:"+vd.getGuestList().size());
								break; // This is essential to avoid crashing but multiple of the same will not be resolved
							}
						}
					}
				}
				// Remove the guests
				distribution.getGuestList().clear();

				// Removing distribution itself
				distributions.remove(ixd);
				ixd= Math.min(ixd, distributions.size()-1);
				ixguest= Math.min(ixguest,Math.max(0, distributions.size()-1));
				currentDistributionIs();
				defineRegions();
			}

			// Remove all the guest
			else if(key == KeyEvent.VK_BACK_SPACE && only){
				// say goodbye to all the guests
				distribution.getGuestList().clear();
				distribution.setGuest(null);
				currentDistributionIs();
			}
			// Remove the last guest
			else if(key == KeyEvent.VK_BACK_SPACE && ctrlDown){
				// say goodbye to the last guest
				distribution.getGuestList().remove(distribution.getGuest());
				if (distribution.getGuestList().size()>0) {
					distribution.setGuest(distribution.getGuestList().get(distribution.getGuestList().size()-1));
				}
				currentDistributionIs();
			}


			// Merging colours
			else if(key == KeyEvent.VK_X){
				distribution.setUsemergeColor(!distribution.isUsemergeColor());
			}

			//
			else if(key == KeyEvent.VK_C  && only ){
				combine= !combine;
			}
			// Remember the current distribution so that we can paste it on another distribution
			else if(key == KeyEvent.VK_C  && ctrlDown ){
				ixguest= ixd;
			}
			// Pasting distribution onto another
			else if(key == KeyEvent.VK_V  && ctrlDown){
				//Drop one distribution onto another
				if ( ixguest!=ixd){
					//					currentDistributionIs();
					distribution.setGuest(distributions.get(ixguest));
					distribution.getGuestList().add(distributions.get(ixguest));
					System.out.println("Dropping distribution "+ixguest +" at "+ixd);
				}
			}
			// set the flag for joining images
			else if(key == KeyEvent.VK_J){
				join= !join;
			}
			// Set the flag for using topography
			else if(key == KeyEvent.VK_U  && topography!=null){
				useTopo= (topography.isOk) ? !useTopo : false;
				//Set the flag of each distribution for consistency
				for (VisualDistribution vd : distributions) {
					vd.setUseTopography(useTopo);
					vd.redrawImage();
				}
			}
			//  Finalise survey line
			else if(key == KeyEvent.VK_ENTER && interactDistribution.mode==EMode.MEASURE){
				if (showSurveyLine) {
					// Finalise
					if ( !surveyLine.isItFinished()) {
						surveyLine.setItFinished(true);
					}
					else {
						surveyLine.measureCoordinate.clear();
						surveyLine.setItFinished(false);
					}
				}

			}


			// Change the display mode of topography
			else if(key == KeyEvent.VK_T && only && useTopo){
				topography.displayMode+= 1;
				if (topography.displayMode>6) {
					topography.displayMode=0;
				}
				//
				if (topography.displayMode==3) {
					GraphicsUtil.produceDefaultTopoGradient(topogradient, 255);
					topogradient.shiftDark(50);
				}
				else if (topography.displayMode==4) {
					GraphicsUtil.produceDefaultTopoGradient(topogradient, 80);
				}

				else if (topography.displayMode==5) {
					GraphicsUtil.produceDefaultTopoGradientGrey(topogradient, 255);
				}
				else if (topography.displayMode==6) {
					GraphicsUtil.produceDefaultTopoGradientGrey(topogradient, 80);
				}

				// Redraw the topography if it is okay
				redrawTopography();
			}
	
			drawDistributionValues(count);
			defineRegions();
		}





		/**
		 * @return
		 */
		private Point theRightLocation() {
			Point p= new Point(this.getLocationOnScreen().x+distribution.getPixelRegion().x+distribution.getPixelRegion().width, 
					this.getLocationOnScreen().y+distribution.getPixelRegion().y);
			return p;
		}


		/**
	 * 
	 */
//	private void soundFeedback() {
//		float value=0;
//		
//		if (distribution!=null) {
//			float reference= gradient.getMax();//(float) distribution.getStatistics().getAverage();
//			value= (float) (distribution.getValues()[indexAtMouse()]/reference);
//					
//			if (value>0) {
//			float gain = Math.min(-30+36*value,5.5f); 
//			audioItem.gainControl.setValue(gain);
//			}
//			else {
//				audioItem.gainControl.setValue(-50);
//			}
//		}
//	}
//

	@Override
	public void mouseMoved(MouseEvent me) {
		boolean controlDown= me.isControlDown();
		Point screen= me.getLocationOnScreen();
		Point upperCorner=new Point(distribution.getPixelRegion().x, distribution.getPixelRegion().y);  
		Point p= yxLocationAtMouse();
		int ix= pixelInfo.indexYX;//   indexAtMouse(0);// 
		interactDistribution.mouseAt.x = me.getX();
		interactDistribution.mouseAt.y = me.getY();

		this.requestFocus();
//		soundFeedback();
		
		// Interact with the line chart
		if (interactDistribution.mode==EMode.MEASURE && controlDown)  {
			plotLineChart(controlDown, upperCorner);//screen);			
		}
		else {
			interactDistribution.removeChart();
			imageChart= null;
		}

		
		//Testing selection mask	////Label2
		if(distribution.getSelectedMask().getMask()[ix] ==1){
			Graphics2D graphic= (Graphics2D) this.getGraphics();
			graphic.drawString("Inside mask!", me.getX(), me.getY());
		}


		// Interaction with vessel
		if (vessels!=null) {
			
		}


		//		if(distribution.getSelectedRegion().contains(p)){
		//			System.out.println(" Contains Mask !");
	}



	


	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		super.mouseWheelMoved(e);
		
		boolean controlDown= e.isControlDown();
		boolean shiftDown= e.isShiftDown();
		int n= e.getWheelRotation()*(-1); 

		//experimental !
		if (controlDown) {
			float dm= (n<0) ? 0.8f: 1.25f ;
			topogradient.setMin(topogradient.getMin()*dm);
			topogradient.setMax(topogradient.getMax()*dm);
//			distribution.drawTopography(topography, cgradient,  topoImage);
//			1/8= 0,125
		}
		
		if (shiftDown) {
//			fontIndex+=n;
//			fontIndex= Math.max(0, Math.min(fontIndex, fontNames.length));
		}
	}
	
	
	@Override
	public void mouseClicked(MouseEvent me) {
		boolean controlDown= me.isControlDown();
		boolean doublec= (me.getClickCount()>1);
		boolean rclick= SwingUtilities.isRightMouseButton(me);  //me.getButton();
		int ix = distributionIndexAt(me.getX());
		// The location within the distribution
		Point p= yxLocationAtMouse();// pixelInfo.yxLocation;//yxLocationAtMouse();
		// Corresponding location in image
		Point pImage= new Point(p.y, p.x);
		
		//���••//���
		if (mirror) {
		// 	  April 14: this Was wrong, cannot use image because it is wider for several distributions
			pImage.x= distribution.getImage().getWidth()- pImage.x-1 ;//image.getWidth()-pImage.x-1  ;
			pImage.y = distribution.getImage().getHeight()-pImage.y-1;//  image.getHeight()-pImage.y-1 ;
		}


		// This is the place to use a pop-up menu !«•»
		if (rclick && ix==ixd) { //Label2
			popMenu.showPopup(me);
			System.out.println("triggering pop-up");
		}
		
		//Single click
		else if(!doublec){
			
			// Change distribution if we click on another
			if (ix!=ixd) {
				ixd=ix;
				currentDistributionIs();
				interactDistribution.getPolygon().reset();
			}

			
			// Interactively making survey lines //Label1          
			else if (interactDistribution.mode==EMode.MEASURE && !pixelInfo.outofRange){
				Coordinate c= new Coordinate(pixelInfo.coord);
				// Interact with a survey line by defining new positions
				if (showSurveyLine  && !surveyLine.isItFinished() ) {
					surveyLine.measure(c);
					//Update values
					if (surveyLine.getLength()>100) {
						surveyLine.insertValues(distribution.getValues(), distribution.getFillV(), distribution.getWH());
					}
				}
				// Transect line interaction
				else if(showTransect && transectLine!=null){
					transectLine.receiveCoordinate(c);
				}					
			}
			
			
			//Polygon
			else if (interactDistribution.mode==EMode.SELECT  && 
					interactDistribution.getModeType()==1) {
				Polygon poly= interactDistribution.getPolygon();
				poly.addPoint(pImage.x, pImage.y);
				System.out.println(" Adding point "+poly.npoints +" to polygon: "+pImage.toString());
				//
			}
			
		}
		// Double-click
		else {
			//Are we double-clicking a mask in selection mode?
			boolean maskClick= interactDistribution.getMode()==EMode.SELECT && 
					distribution.getSelectedMask().getMask()[pixelInfo.indexYX]==1;
						
			// Changing colour of land if we are double-clicking on land
			if (pixelInfo.isLand ) {
				Color c=JColorChooser.showDialog(null, "Choose land colour", distribution.getFillColor());
				if (c!=null) {
					distribution.setFillColor(c);
				}
			}
			// Control double-click the mask to open one
			else if(maskClick && controlDown){
				distribution.getSelectedMask().openMaskImage(null);
			}
			// Double-click selection mask to save it 
			else if(maskClick){
				distribution.getSelectedMask().saveMask(null);				
			}
			else {
//				DistPanel pa= new DistPanel(distribution, true);	
			}
		}

	}


	@Override
	public void mousePressed(MouseEvent me) {
		whenMouseDown= System.nanoTime(); 
		durationDown=0;
		mDown= true;
		mouseDownAt.setLocation( interactDistribution.mouseAt);
		
		popMenu.showPopup(me);
	}


	/**
	 * 
	 */
	@Override
	public void mouseReleased(MouseEvent me) {
		boolean controlDown= me.isControlDown();
		boolean altDown= me.isAltDown();
		boolean shiftDown= me.isShiftDown();
		boolean only= !controlDown && !altDown && !shiftDown; 

		int ix = 		distributionIndexAt(me.getX());
		int fromIndex=	distributionIndexAt(mouseDownAt.x);
		mDown= false;

		//
		popMenu.showPopup(me);
		//

		//Drop one distribution onto another
		if ( fromIndex!=ix && interactDistribution.getMode()!=EMode.SELECT ){ 
			ixguest= fromIndex;
			//Identify the distribution we are dropping onto
			ixd= ix;
			currentDistributionIs();
			distribution.setGuest(distributions.get(ixguest));
			distribution.getGuestList().add(distributions.get(fromIndex));
			//				System.out.println("Dropping distribution "+fromIndex +" at "+ix);
		}
		// If we are in selection mode, set the selection range within the distribution
		// ! we need to check the boundaries 
		if (interactDistribution.mode == EMode.SELECT ) {
			//Rectangles are in index space
			Point from= pixelInfo.yxLocationAtPoint(mouseDownAt) ;// yxLocationAtPoint(mouseDownAt);
			Point to=	pixelInfo.yxLocation;//yxLocationAtPoint(interactDistribution.mouseAt);//yxLocationAtPoint(interactDistribution.mouseAt);	
			Rectangle r= new Rectangle(from.x, from.y, to.x-from.x,to.y-from.y);

			
			//! does not work !
			if (mirror) {				
				Rectangle rMirror= new Rectangle(to.x,to.y, from.x-to.x,from.y-to.y);
				r.setRect(rMirror);
			}

			//Label5 
			// Mask	-> �� Move this to another class, mask
			if (r.width>0){   //interactDistribution.mouseAt.x> mouseDownAt.x) {

				distribution.setSelectedRegion(r);
				//
					Rectangle rImage= new Rectangle(r.y, r.x, r.height, r.width);
					Point pCentre=new Point(rImage.x+rImage.width/2, rImage.y+rImage.height/2);
					SelectionMask mask=distribution.getSelectedMask();
					
					// Reset the mask first completely if none of the modifying keys are used  		
					if (only) {
						mask.clearImage();	
					}
					//  Either adding the mask or subtracting
					boolean addMask= (only||shiftDown);

					
					//Drawing a rectangle or a circle
					if (interactDistribution.getModeType()==0) { //Rectangle
						mask.drawRectangle(rImage, addMask);
						mask.removeLand(distribution);
					}
					else if (interactDistribution.getModeType()==2){ //Circle
						mask.drawCircle(pCentre, rImage.width, rImage.height, addMask);
						mask.removeLand(distribution);
					}
				}
			
//				mask.drawOnTheImage(distribution.getImage());
//				System.out.println("-> selected Region:"+ r.toString());
			}
		}


	@Override
	public void mouseDragged(MouseEvent me) {

		// Be sensitive to the time the mouse has been down
		durationDown= (long) ((System.nanoTime()- whenMouseDown)*0.000001);
		if (durationDown>300) {
		interactDistribution.mouseAt.x = me.getX();
		interactDistribution.mouseAt.y = me.getY();	
//		System.out.println("duration ms:"+durationDown);
		}
	}


	@Override
	public void mouseEntered(MouseEvent arg0) {
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
	}


	/**
	 * @return the imScaled
	 */
	public BufferedImage getImScaled() {
		return imScaled;
	}


	/**
	 * @param imScaled the imScaled to set
	 */
	public void setImScaled(BufferedImage imScaled) {
		this.imScaled = imScaled;
	}


	/**
	 * @return the distributions
	 */
	public ArrayList<VisualDistribution> getDistributions() {
		return distributions;
	}


	/**
	 * @param distributions the distributions to set
	 */
	public void setDistributions(ArrayList<VisualDistribution> distributions) {
		this.distributions = distributions;
	}


	/**
	 * @return the strInformation
	 */
	public String getStrInformation() {
		return strInformation;
	}


	/**
	 * @param strInformation the strInformation to set
	 */
	public void setStrInformation(String strInformation) {
		this.strInformation = strInformation;
	}


	/**
	 * @return the fxStage
	 */
	public Stage getFxStage() {
		return fxStage;
	}


	/**
	 * @param fxStage the fxStage to set
	 */
	public void setFxStage(Stage fxStage) {
		this.fxStage = fxStage;
	}


	/**
	 * @return the showVessel
	 */
	public boolean isShowVessel() {
		return showVessel;
	}


	/**
	 * @param showVessel the showVessel to set
	 */
	public void setShowVessel(boolean showVessel) {
		this.showVessel = showVessel;
	}





	/**
	 * @return the surveyLine
	 */
	public SurveyLines getSurveyLine() {
		return surveyLine;
	}





	/**
	 * @param surveyLine the surveyLine to set
	 */
	public void setSurveyLine(SurveyLines surveyLine) {
		this.surveyLine = surveyLine;
	}


	
}
