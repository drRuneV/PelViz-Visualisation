package graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import distribution.Topography;
import ecosystem.Coordinate;
import ecosystem.Ecosystem;
import graphicsGL.Vector2D;
import interaction.Interact;
import testing.ImageFrame;
import visualised.VisualDistribution;
import visualised.VisualTopography;

/**
 * 
 * @author Admin
 *
 */
public class DistPanel extends JPanel implements KeyListener, ActionListener{
	
	//
	private static final long serialVersionUID = 1L;
	// The active selected Visual Distribution
	protected VisualDistribution distribution= null;
	// 
	protected VisualTopography topography=null;
	// The ColourGradient of the distribution
	protected ColorGradient gradient;
	// The background colour of the panel
	protected Color background= Color.GRAY;
	// If the panel is launched on a Jframe
	protected boolean useFrame=true;

	protected float fps=0;
	// Timer for updating graphics in the paint method
	protected Timer T;
	//This is the image we draw every frame
	protected BufferedImage image;
	// The current frame or time of the animation
	protected int count=0;	
	// Duration
	protected long startTime=0;
	protected long duration=0;
	
	// Mouse
	protected Point mouseDownAt = new Point();
	protected boolean mDown=false;

	protected boolean wait;
	protected Point off= new Point(5,5);
	protected float scale= 2f;
	protected boolean mode=false;
	protected Interact interactDistribution=null;
	
	protected PixelInfo pixelInfo =null;
	
	// ==================================================
	

	/**
	 * Constructor
	 * Constructs a panel with a distribution visualised
	 * @param distribution The distribution to be visualised
	 * @param useFrame If a frame is to be constructed to but the panel on
	 * @paramcolorType 
	 */
	public DistPanel(VisualDistribution distribution,boolean useFrame) {
		
		this.useFrame = useFrame;

		 //  Create and start the timer
		 T = new Timer(40, this);
		 T.setInitialDelay(2000);
		 T.start(); 
		 startTime= System.currentTimeMillis();

		 // Add listeners and other components
//		 this.addMouseWheelListener(this);
		 this.addKeyListener(this);

		// Exit if distribution is undefined
		// <-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><-><->  	
		if (distribution!=null) {
			
		 this.distribution= distribution;
		 gradient= distribution.getGradient();
		 pixelInfo=new PixelInfo(this,distribution);
		 //
		 addListenersDistribution(distribution);
		 
		 // Show the panel on a JFrame if requested
		 Dimension dimension= new Dimension((int) (distribution.getHeight()*scale+off.x), distribution.getWidth());
		 Rectangle rec= new Rectangle( (0)*dimension.width, 50, dimension.width,dimension.height);
		 if (useFrame) {
			 JFrame frame= new ImageFrame(distribution.getFullName(),this, rec);	
		 }
		 
		 //  
		 defineSize();

		}
		else {
//			System.out.println("Distribution "+ distribution.getName()+" Is undefined !");
		}
	}

	/**
	 * @param distribution
	 */
	public void addListenersDistribution(VisualDistribution distribution) {
		// Add mouse and keyboard interaction dealing with distribution
		 interactDistribution= new Interact(distribution,this);
		 this.addMouseWheelListener(interactDistribution);
		 this.addKeyListener(interactDistribution);
		 this.addMouseListener(interactDistribution);
		 this.addMouseMotionListener(interactDistribution);
		 interactDistribution.setActive(true);
//		 System.out.println("••• Added listeners! ");
	}
	
	/**
	 * Defines the size of the panel and a BufferedImage to draw upon with opposite dimensions
	 * If a frame is being used,  rescale this.  
	 */
private void defineSize(){
		 int width= distribution.getWidth();
		 int height= distribution.getHeight();
		 // Define it with opposite dimensionality
		 int dWidth= (int) (height*1.8*scale+off.x*2);
		 int dHeight= (int) (width*1.5*scale+off.y*2); //
		 Dimension dimension= new Dimension(dWidth, dHeight);
		 setPreferredSize(dimension);
		 this.setFocusable(true);
		 this.requestFocusInWindow();
		 this.requestFocus(true);

		 // Create the image we are drawing, but opposite !!!
		 image = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
		
		 
		 // Resize the frame to fit the panel
		 if (useFrame) {
			 JFrame frame = (JFrame) this.getTopLevelAncestor();
			 frame.setSize(dimension);
		 }
	}

	/**
	 * This method is overridden in distributions panel
	 * we cannot call define size directly because there is no such method in distributions panel
	 *  //rememberx 
	 */
	protected void redefine() {
		// 
		defineSize();
		
	}


	/**
	 * Draws the values of a distribution on an BufferedImage 
	 * @param t The current time step
	 */
	public void drawDistributionValues(int  t) {
		t= (distribution.isHasTime())?  t:0;

		if (mode) {
			insertValuesProjection(image, count,5);//count/2);
		}
		else  {
			distribution.drawBufferedImage(count);
		}
	}


	/**
	 * «•» ! «•» This should be moved to visual distribution.
	 * @param im
	 * @param t
	 */
	public void insertValuesProjection(BufferedImage im,int  t, float lon) {
		int width= distribution.getWidth();
		int height= distribution.getHeight();
		int wh= width*height;
		int hit=0;
		float value=0;
		int  colorf=   Color.black.getRGB(); //Colors.SILVER_Green.darker().darker().getRGB();//

		Coordinate co= null;
		Vector2D vec= null; 
		// Please set the values here
		for (int y = 0; y < height ;y++) {
			for (int x = 0; x < width; x++) {
				value= distribution.getValues()[x+y*width+t*wh];
//				int color=  gradient.retrieveColorInt(value);
				int color=  (value== distribution.getFillV()) ? colorf :gradient.retrieveColorInt(value);
				co= distribution.getCoordinates()[x+y*width];
//				System.out.println("coordinate lat "+co.getLon());
				vec= co.projectionCircle(lon+count); //??? «•»
				vec.scale(150f);
				// We need to involve some origins at some point
				vec.plus(new Vector2D(im.getWidth()/2.0f, 100));
				int ix= (int) vec.getX();
				int iy= (int) vec.getY();
//				System.out.println("x:"+ix+" iy:"+iy);
				// Fill the pixels
				if (ix>0 && ix<im.getWidth() && iy>0 && iy<im.getHeight()) {
					im.setRGB(ix, iy,color); 	
					hit++;
				}
				
			}
		}
	}
	

	/**
	 * Paint the component, continuously using a timer linked to actionPerformed
	 */
	@ Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		//do not do anything if there is no distribution
		if (distribution==null) {
			return;
		}


		// Clear graphics of the panel
		Graphics2D gPanel = (Graphics2D) g;
		gPanel.setBackground(background);
		gPanel.clearRect(0, 0, this.getWidth(), this.getHeight());
		// Clear the entire image 
		Graphics2D g3 = image.createGraphics();
		g3.setBackground(background);
		g3.clearRect(0, 0, image.getWidth(), image.getHeight());
		

		// We repaint the image every time				
		if (!wait) {
			count++;
			checkCount();
		}
		//
		// Finally draw the image
		if (mode) {
			insertValuesProjection(image, count,5);//count/2);
		}
		else {
			distribution.drawBufferedImage(count);
			g3.drawImage(distribution.getImage(), 0, 0,null, null);
		}

		//Create another scaled BufferedImage
		BufferedImage imScaled=GraphicsUtil.resizeImage(image, (int)(image.getWidth()*scale),
				(int)(image.getHeight()*scale)); 
		
		//Redraw the image every time on the components Graphics
		gPanel.drawImage(imScaled,(int) (off.x*scale),off.y,null,null);
		
		// Draw the associated gradient also
		if(distribution.showGradient){
		gPanel.drawImage(gradient.getImage(), off.x+(int) (distribution.getHeight()*scale),
					(int) (distribution.getWidth()*0)+ off.y,null,null);
		} 
		//Histogram
		if(distribution.showHistogram){
			int t= (distribution.isHasTime())? count : 0;
		gPanel.drawImage(distribution.getStatistics().getHistograms()[t].drawHistogram() , 
				off.x+ image.getWidth()-30,50, null,null);
		}
		//Centre of mass
		if (distribution.showCm) {
		indicateCenterMass(gPanel,0 ,distribution);
		}
		// Display information
		displayInformation(gPanel,0);
		gPanel.dispose();
	}
	
	

	/**
	 * 
	 */
	protected void checkCount() {
		// 
		if (distribution.isHasTime()) {
	
			if (count>= distribution.getTime()) {
				count=0;		
			}
	
			if (count<0 ) {
				count=distribution.getTime()-1;
			}
		}
		// 
		else if ( distribution.getGuest()!=null) {
			for (VisualDistribution vdg : distribution.getGuestList()) {
				count= (count>= vdg.getTime()) ? 0: count;
				count= (count< 0 ) ? vdg.getTime()-1: count;
			}
		}
		else count=0;
			//We need else if here ! because if the distribution does not have time
			// And it does not have guest we need one way to stop the counter=0;
			//«•»«•» !!!!!!!!!!
	}

	/**
	 * @param gPanel
	 */
	protected void displayInformation(Graphics2D gPanel , int ixd) {

		//		displayTime(gPanel,0);
		int fontSize= (int) Math.max(12, Math.min(22, 10+2*scale));
		gPanel.setFont(new Font("Bodoni MT", Font.PLAIN, fontSize ));
		//
		// rendering hint
		gPanel.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gPanel.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON );


		// Placement on screen
		int yposition= (int)  distribution.getPixelRegion().y+
				distribution.getPixelRegion().height+0;//(off.y+20+width*scale);
		int xposition= (int) distribution.getPixelRegion().x+10;//(off.x+20+height*scale);

		// The value, coordinate, area and value colour
		if (pixelInfo.wasCalculated) {

			float 	value= pixelInfo.value;//distribution.getValues()[pixelInfo.indexYXT];
			boolean	isLand=pixelInfo.isLand;// (value==distribution.getFillV());

			// 1. draw coordinate and Depth values from Topography
			String strCoo = pixelInfo.generateCoordinateDepthString(topography);
			String strArea= pixelInfo.generateAreaVolumeString();
			int dy=20;
			dy-= ( distribution.getPixelRegion().height>800 ) ? 80: 0;

			//Color
			Color textColour = (ColorInt.isLighterThan(background, Color.blue)) ? Color.black: Color.white ;
			gPanel.setColor(textColour);

			//			gPanel.setColor(ColorInt.invert(background.darker()));
			gPanel.drawString(strCoo,xposition,yposition+dy);
			dy+=20;
			if (!isLand) {
				gPanel.drawString(strArea,xposition,yposition+dy);
			}

			// 2. draw value
			float w=value/pixelInfo.sumValue;
			String percent=String.format(" %.1f", 100*w)+" %";
			String format1= (value> 10000  ) ?  " %.1e"  :" %.2f";
			format1= (value>0 && value<0.1) ? " %.4f" : format1 ;
			String vtext = (isLand) ? "Land !" :	
				String.format(distribution.getName() +" "+format1+distribution.getUnit(),value)+percent;

			int cx= 10;	
			dy+= 20;
			int length= (int) (cx+w*(30+ scale*20));
			int dl= (isLand || value==0 || distribution.getGuestList().isEmpty() ) ? cx : length;
			gPanel.setColor(textColour.brighter().brighter());
			gPanel.drawString(vtext, xposition+dl,yposition+dy);

			//indicate colour value
			int color= gradient.retrieveColorInt(value);
			gPanel.setColor(new Color(color));
			gPanel.fillRect(xposition-cx, yposition+dy-cx, dl,cx); 

			// 3. draw the guest value
			if (!distribution.getGuestList().isEmpty() && !isLand) {
				displayGuestsInformation(gPanel,  yposition, xposition, cx, value, dy);
			}

		}
		else {
			//			gPanel.drawString("Undefined! ",off.x,  yposition );
		}
	}

/**
 * @param gPanel
 * @param indexYXT
 * @param indexYX
 * @param yposition
 * @param xposition
 * @param cx
 * @param value
 * @param dy
 */
private void displayGuestsInformation(Graphics2D gPanel, int yposition, int xposition,
		int cx, float value, int dy) {

		
		//  Information about all guest distributions
		for (VisualDistribution guest: distribution.getGuestList()) {
			int ix= (guest.isHasTime()) ? pixelInfo.indexYXT: pixelInfo.indexYX;
			float v= guest.getValues()[ix];
			float w=v/pixelInfo.sumValue;
			int dl= (int) (cx+w*(30+ scale*20));
			dy+=20;
			String percentage= (pixelInfo.sumValue!=0) ? String.format(" : %.1f", 100*w)+"%" : ".." ;
			String stv= String.format(" %.2f", v);

			gPanel.setColor(new Color(guest.getGradient().retrieveColorInt(v)));
			gPanel.fillOval(xposition-cx, yposition+dy-cx, dl,cx);
			// indicate colour of guest
//			gPanel.setColor(ColorInt.invert(background.brighter()));
			gPanel.setColor(ColorInt.blackOrWhite(background, Color.blue));
			gPanel.drawString(guest.getName()+stv+percentage, xposition+dl,yposition+dy);
		}
	}

	/**
	 * @param gPanel
	 */ //! Not used by May 11.
	protected void displayTime(Graphics2D gPanel, int ixd) {
		int fontSize= (int) Math.max(10, Math.min(14,8*scale));
		int x= (int) ((off.x*(ixd+1))*scale +distribution.getHeight()*scale*ixd)+2 ;
		int y=  (int) (off.y*scale+8+scale);
		gPanel.setFont(new Font("Arial", Font.PLAIN, fontSize));
		gPanel.setColor(ColorInt.invert(background).brighter()); //Color.LIGHT_GRAY);
		gPanel.drawString("Day "+count+" ",x, y);
		//+Ecosystem.months[Math.min(count/30,11)]
//		gPanel.drawString("Frames/s="+String.format("%.2f",fps ),5 , this.getHeight()-25);
	} 
	
	/**
	 * 
	 * @param gPanel
	 * @param ixd
	 */
	protected void indicateCenterMass(Graphics2D gPanel,int ixd, VisualDistribution vd) {
		
		Point cmPoint;
		if (vd.isHasTime()) {
			cmPoint=vd.getStatistics().getCenterMass()[count];	
		}
		else cmPoint=vd.getStatistics().getCenterMass()[0];
		
		// The offset from the left side of the screen
		int dx= vd.getPixelRegion().x;//(int) ( ( ixd*vd.getHeight()+off.x*(ixd+1) )*scale);
		// x is simply along the height of the distribution, opposite!
		Point at= new Point();
//		pixelInfo.y
		at.y= (int) (off.y+cmPoint.x*scale);
		at.x= (int) (dx+cmPoint.y*scale);
		
		// Mirror each of the centre of mass point in statistics instead!«•»
		if (vd.isMirror()) {
			at.x=vd.getHeight()-at.x-1;
			at.y= vd.getWidth()-at.y-1;
		}

		gPanel.setColor(Color.white);
		int w= (int) (8+Math.sin(count/2)*4) ;
		gPanel.drawOval(at.x-w/2, at.y-w/2, w,w);
		//draw a tale// 
		int tailLength= vd.getStatistics().getCenterMass().length ;//220;
		int f =1 ;
		boolean  future= false;
		Color color= new Color(255, 255, 255, 255);
		Color colorFuture= new Color(50, 50, 50, 90);
		
		// 
		if(vd.isHasTime() && vd.showTail){
			for (int i = 1; i < tailLength-1; i++) {		
				future= (i>count);
				f= (!future) ?  (int)(((count-i)*255f)/tailLength)  : 255;
//				System.out.println("factor: "+f);
				Point p= vd.getStatistics().getCenterMass()[i]; //count-i
				Point p2= vd.getStatistics().getCenterMass()[i+1]; //count-i
				color= (future) ? colorFuture: new Color(255, 255, 255, 255-f) ;
				gPanel.setColor(color);//
//				gPanel.drawLine((int) (dx+p.y*scale), (int) (off.y+p.x*scale),at.x, at.y);
				gPanel.drawLine((int) (dx+p.y*scale), (int) (off.y+p.x*scale),
								(int) (dx+p2.y*scale), (int) (off.y+p2.x*scale));
				if (!future) {
					gPanel.drawOval(at.x-2, at.y-2, 4,4);
				}
				at.y= (int) (off.y+p.x*scale);
				at.x= (int) (dx+p.y*scale);
			}
		}
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		repaint();
		duration=( System.currentTimeMillis()-startTime)/1000;
	}





	
	/**
	 * 
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		boolean controlDown= e.isControlDown();
		boolean altDown= e.isAltDown();
		boolean shiftDown= e.isShiftDown();
		boolean only= !controlDown && !altDown && !shiftDown;
					
		if (key == KeyEvent.VK_ESCAPE) {
			System.exit(0);
		}
		else if(key == KeyEvent.VK_LEFT){
			count+= (controlDown)? -7 :-1;
			checkCount();
		}
		else if(key == KeyEvent.VK_RIGHT){
			if (wait) {
				
			}
			count+= (controlDown)? 7 :1;
			checkCount();
		}
		else if(key == KeyEvent.VK_SPACE){
			if (only) {
			wait= !wait;
			}
			else if (controlDown){
				wait= true;
			}
		}
		else if(key == KeyEvent.VK_HOME){
			if(only){
				count=0;
			}
			else if (controlDown){
				count=0;
				wait=true;
			}
			
		}
		else if(key == KeyEvent.VK_END){
			count=distribution.getTime()-1;
		}
		
		else if(key == KeyEvent.VK_UP){
			T.setDelay(Math.min( 2000, T.getDelay()+5));
		}

		else if(key == KeyEvent.VK_DOWN){
			T.setDelay(Math.max(0, T.getDelay()-5));
		}
		// Change the scale
		else if(key == KeyEvent.VK_PLUS){
//			scale*= 1.25;
			scale+=1;
			scale= Math.min(8, scale);
			redefine();
		}
		else if(key == KeyEvent.VK_MINUS){
//			scale*= 0.8; //0.8;
			scale-=1;
			scale= Math.max(1, scale);
			redefine();
		}
		// Darken the background panel
		else if(key == KeyEvent.VK_D && only){
				background= background.darker();
		}
		// Lighten the background panel
		else if(key == KeyEvent.VK_L && only){
				background= background.brighter();
		}
//
//		gradient.reDefine();
		drawDistributionValues(count);
	}
	

	@Override
	public void keyReleased(KeyEvent e) {
	}
	@Override
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * Get the distribution
	 * @return the distribution
	 */
	public VisualDistribution getDistribution() {
		return distribution;
	}

	/**
	 * @param distribution the distribution to set
	 */
	public void setDistribution(VisualDistribution distribution) {
		this.distribution = distribution;
	}

	/**
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(BufferedImage image) {
		this.image = image;
	}

	/**
	 * @return the scale
	 */
	public float getScale() {
		return scale;
	}

	/**
	 * @param scale the scale to set
	 */
	public void setScale(float scale) {
		this.scale = scale;
	}

	/**
	 * @return the interactDistribution
	 */
	public Interact getInteractDistribution() {
		return interactDistribution;
	}

	/**
	 * @param interactDistribution the interactDistribution to set
	 */
	public void setInteractDistribution(Interact interactDistribution) {
		this.interactDistribution = interactDistribution;
	}

	/**
	 * @return the off
	 */
	public Point getOff() {
		return off;
	}

	/**
	 * @param off the off to set
	 */
	public void setOff(Point off) {
		this.off = off;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @return the topography
	 */
	public VisualTopography getTopography() {
		return topography;
	}

	/**
	 * @param topography the topography to set
	 */
	public void setTopography(VisualTopography topography) {
		this.topography = topography;
	}

	/**
	 * @return the pixelInfo
	 */
	public PixelInfo getPixelInfo() {
		return pixelInfo;
	}

	

	
}

//One can retrieve part of an image like this:
//int Array[]=image.getRGB({, startY, w, h, rgbArray, offset, scansize)()