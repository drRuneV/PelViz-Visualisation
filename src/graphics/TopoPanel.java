package graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
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
import java.io.File;
import java.io.IOException;
import java.time.Year;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import NetCdf.Etopo2Data;
import distribution.Topography;
import ecosystem.Coordinate;
import testing.ImageFrame;
import testing.TestValues;
import visualised.VisualDistribution;
import visualised.VisualTopography;

/**
 * A panel which uses a buffered image to draw topographic data from the etoop2. file
 *
 */
public class TopoPanel extends JPanel implements KeyListener, ActionListener,
										MouseListener, MouseMotionListener,MouseWheelListener{

	private JLabel infoJLabel;
	private static final long serialVersionUID = 1L;
	private BufferedImage image;
	private int count=0;
	private Point  offset= new Point(0,0);//new Point(4500, 200); // specific for topography  
	private static Timer T;
	
	//«•» !!!!!!!!!!
//	private TestTopography testValue;
	//later we want to pass in a grid distribution instead of the test values.!!
	// «•»
	private Point offspeed= new Point(0,0);
	private BufferedImage biSub=null;
	private BufferedImage biScreen;
	// Mouse
	private Point mouseDownAt = new Point();
	private Point mouseAt= new Point();
	private boolean mDown=false;
	private VisualTopography topography;
	private ColorGradient cgradient;
	private float scale= 2f;
	private Dimension dimension;
	private long lastTime=0;
//	private int  relief=50;
	

	
	
	// ==================================================
	
	/**
	 * Constructor 
	 */
public TopoPanel(TestValues testv) {
		
		int width= testv.getWidth();
		int height= testv.getHeight();

		// Create the image we are drawing
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		// Insert values into BufferedImage
		testv.insertValues(image);
		testv.darkenedValue(image,60);
		initiate();
	}

	


/**
 * Constructor for the panel using a topography object.
 * A buffered image is created with the same with and height as the data.
 * The complete data series is drawn on the BufferedImage.
 * A specific ColourGradient is created with predefined topography colours.
 * 
 * «•» ! Topography should be a visual topography object and then the gradient should be inside
 * that object.  This topography object should also be able to draw itself on a buffered image,
 * and thereby deprecating the method drawValues.
 * «•» | «•»
 * -> October 17: no, you are wrong, topography should be a topography!.
 * 
 * @param topography - The topography to draw on this panel 
 */
public TopoPanel(Topography topo) {
	
	// A VisualTopography is created from the data of the topography
	topography= VisualTopography.createFrom(topo);

	setupTopography();
	
	initiate();
}




public TopoPanel(VisualTopography topo) {
	this.topography= topo;
	setupTopography();	
	initiate();
}




/**
 * 
 */
private void setupTopography() {
	// Setup the VisualTopography
	topography.createGradient();
	topography.createImage();
	topography.displayMode=2;
	// The gradient
	cgradient= topography.getGradient();
	
	// Insert values into BufferedImage
//	drawValues(true, relief);
	topography.drawBufferedImage(0);
	image= topography.getImage();
}



public static void main(String[] args) {
	
	String topoFile= "C:/Users/a1500/Documents/Eclipse Workspace/netCDF files/etopo2.nc";
	Topography topography= new Topography(topoFile,"btdata");
	System.out.println("-> in main TopoPanel width :"+topography.getWidth());
		
	if(topography.getWidth()>0){
	TopoPanel pa= new TopoPanel(topography);
	JFrame frame= new ImageFrame("Value",pa, new  Rectangle());
	}
}



	/**
	 * Initiates the panel by setting preferred dimension, initiating and starting the timer
	 * and adding listeners.
	 */
private void initiate() {
		
		//Dimension dimension= new Dimension(width,height);
		dimension= new Dimension(1600,900);
		setPreferredSize(dimension);
		this.setFocusable(true);
		this.requestFocusInWindow();

		//  Create and start the timer
		T = new Timer(50, this);
		T.setInitialDelay(500);
	    T.start(); 
	    // Add listeners and other components
	    this.addMouseWheelListener(this);
	    this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.addKeyListener(this);
		infoJLabel= new JLabel("info");
		this.add(infoJLabel);
		
		lastTime= (long) (System.nanoTime()*0.0000001);
	}



	/**
	 * Draws the values in a BufferedImage
	 * @param useRelief - If relief is to be used
	 * @param l - The reference length used for darkening.
	 */
	/*
	public void drawValues(boolean useRelief,float l) {
		int x;		//Label2
		int y;
		int width=  topography.getWidth();
		int height= topography.getHeight();
		
		// Please set the values here
		for (int i = 0; i < topography.getValues().length-1; i++) {
			y= i/width;
			x= i- y*width;

			// Must create a completely new colour each time
			ColorInt color= new ColorInt( cgradient.retrieveColorInt(topography.getValues()[i]));

			if (useRelief) {
				color.darker(darken(l, i));
			}
			if (x< width && y<height) {
				image.setRGB(x, y ,  color.toInt()); //image.getHeight()-y-1,  color);
			}
			else System.out.println("x"+x+" y"+y);
		}
	}
*/

	/**
	 *  Darkens all the pixels in the buffered image depending on the slope/different from one value to the next.
	 *  «•» ! Not used ?
	 * @param l - The reference length used for darkening.
	 */
/*
	public void darkenedValue(float l){
		int x;		//Label2
		int y;
		float[] values= topography.getValues();
		int width=  topography.getWidth();
		int height= topography.getHeight();

		
		// Please change the values here
		for (int i = 0; i < values.length-1; i++) {
			y= i/width;
			x= i- y*width;
			// Must create a completely new colour each time
			ColorInt color= new ColorInt( cgradient.retrieveColor(values[i]));
			
			color.darker(darken(l, i));	
//			color.alpha*=factor;
			if (x< width && y<height) {
				image.setRGB(x, y ,  color.toInt());
			}
		}

	}
*/
	
	/**
	 * Darkens the value at the given index
	 * @param l - The reference length used for darkening.
	 * @param i - The index in the topography to darken 
	 * @return The factor of darkening.
	 */
	private float darken(float l,int i){
		float factor=1;
		float d=1;	  // The diagonal
		float h=1;	  // The difference in values between 2 neighbours

		h= topography.getValues()[i+1]-topography.getValues()[i];
		//If values increase make it darker
		d= (float) Math.sqrt(h*h+ l*l);
		factor= Math.max(0.4f, l/d);
//		if (h<0)factor= Math.min(1.5f, 1/factor);
		
		return factor;
	}

	


	/**
	 * Paint the component, continuously using a timer linked to actionPerformed
	 */
	@ Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		count++;
			
		if(biSub!=null)changeOffset();

		//
		if (cgradient.wasChanged) {
			topography.drawBufferedImage(0);
			cgradient.wasChanged=false;
		}
		
//		if (count%100==0) System.out.println("painting component "+count);

		//  Redraw the image every time on the components Graphics
		Graphics2D g2 = (Graphics2D) g;
		g2.setBackground(Color.BLACK);
//		g2.clearRect(offset.x, offset.y, image.getWidth(), image.getHeight());
		g2.clearRect(0, 0, image.getWidth(), image.getHeight());

		
		BufferedImage bis = createScaledImage(); 
		
		//Create a graphics drawing surface through another BufferedImage
		biScreen =new BufferedImage(bis.getWidth(),bis.getHeight() , BufferedImage.TYPE_INT_ARGB);
		Graphics2D gbi = biScreen.createGraphics();
		gbi.drawImage(bis, 0, 0,null, null);	
//		gbi.drawImage(bi, 0, 0,null, null);
		gbi.drawImage(cgradient.getImage(),5,5,null,null);
		//drawColourGradient(gbi);
		displayValueInfo(mouseAt.x, mouseAt.y);

		//VisualDistribution...
		
		if (topography.getDistribution()!=null) {
			drawDistribution(topography.getDistribution());
		}
		
		// Finally draw the image
		g2.drawImage(biScreen, 0, 0,null, null);

		g2.dispose();
		gbi.dispose();
	}


	/**
	 * Creates a scaled BufferedImage from the big image of the total topography
	 * based on the current scale and the offset. 
	 * The image is clipped from the offset.
	 * @return A new buffered image scaled and clipped.
	 */
	private BufferedImage createScaledImage() {
		int width=  this.getWidth();//
		int height= this.getHeight();//
		// This is the offset within the image we are clipping from
		int x= (int) Math.max(0,offset.x);//+(1-scale)*width);
		int y= (int) Math.max(0,offset.y);//+(1-scale)*height);
		width*=scale;
		height*=scale;
		width=   Math.min(width, image.getWidth()-x-1);
		height=	 Math.min(height,image.getHeight()-y-1);
//		Rectangle r= new Rectangle(x,y,width,height);
//		System.out.println("image width: :"+image.getWidth()); 
		try {
			biSub= image.getSubimage(x,y,width, height );
		} catch (Exception e) {
			
			e.printStackTrace();
		}
				
		// Create another scaled BufferedImage first
		BufferedImage ims=GraphicsUtil.resizeImage(biSub, (int)(biSub.getWidth()/scale),
				(int)(biSub.getHeight()/scale));
		return ims;
	}

	
	/*private void drawColourGradient(Graphics2D g){
//		values.drawGradientonImage(bi);
//		testValue.cgradient.drawGradient();
		g.drawImage(cgradient.getImage(),5,5,null,null);
	}
	*/


	/**
	 * Changes the offset continuously and smoothly. 
	 */
	private void changeOffset() {
		offset.x+= offspeed.x;
		offset.y+= offspeed.y;
		offspeed.x*= 0.96f;
		offspeed.y*= 0.96f;
		// Control the offset limit
		offset.x= (int) Math.min(image.getWidth()-biSub.getWidth()*1-1, offset.x); 
		offset.x= Math.max(0, offset.x);
		offset.y= (int) Math.min(image.getHeight()-biSub.getHeight()*1-1,offset.y);
		offset.y= Math.max(0,offset.y);
//		System.out.println("offset:"+offset.x+":"+offset.y);
	}
	
	
	/**
	 * Resizes a given BufferedImage according to a given width and height. 
	 * @param image - The image to copy from
	 * @param width - The new with of the new image
	 * @param height - The new height of the new image
	 * @return A new buffered image which is a copy of the given image with a new with and height.
	 */
	public  BufferedImage resizeImage(BufferedImage image, int width, int height) {
        int type=0;
       type = image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
       BufferedImage resizedImage = new BufferedImage(width, height,type);
       Graphics2D g = resizedImage.createGraphics();
       g.drawImage(image, 0, 0, width, height, null);
       g.dispose();
       return resizedImage;
    }

	
	@Override
	/**
	 * This action occurs when the timer triggers
	 */
	public void actionPerformed(ActionEvent arg0) {
		if (count> 5000) {
//			System.exit(0);
		}
		repaint();
		
	}

	
	
	/**
	 * Find out which position in the XY space a specific coordinate corresponds to
	 * @param co The coordinate to find position for
	 * @return The XY position within the dataset
	 */
	public Point whereXY(Coordinate co){
	
		// The position in the dataset, NB! Latitude decreases from 90°
		Point position= new Point();
		position.x = (int) ((co.getLon()- topography.getUpper().getLon())/topography.getResLon());
		position.y = (int) ((topography.getUpper().getLat()-co.getLat())/topography.getResLat());
		
		//
		position.x= Math.min(Math.max(0, position.x), topography.getWidth()-1);
		position.y= Math.min(Math.max(0, position.y), topography.getHeight()-1);
		return position;
	}
	
	
	/**
	 * 
	 * @param co
	 * @return
	 */
	public Point whereScreen(Coordinate co){
		// The position of the screen
		Point pscreen=new Point();
		Point pdata= whereXY(co);

		pscreen.x=(int) (( pdata.x- offset.x)/scale);
		pscreen.y=(int) (( pdata.y- offset.y)/scale);

		// Just for testing correctness and can be deleted later «•» ! |||| 
		//		System.out.println("topography->"+topography.getLatitude());
		//		int index= pdata.x+pdata.y*topography.getHeight()*topography.getWidth();
		Coordinate coat= new Coordinate(topography.getLatitude()[pdata.y], topography.getLongitude()[pdata.x]);	  
		//		System.out.println("position data and screen: "+pdata.x+" "+pdata.y+"|| " +
		//							pscreen.x+" "+pscreen.y+" c:"+coat);
		//int x = (int) (x1*scale+ offset.x);
		//«•»

		return pscreen;
	}


	/**
	 * 
	 * @param distribution
	 */
	public void drawDistribution(VisualDistribution distribution){
		int w= distribution.getWidth();
		int h= distribution.getHeight();
		int index=0;
		int i=0;
		float value=0;
		int color=0;
		count= (count>distribution.getTime()-1) ? 0: count ;
		int timeIndex= distribution.getWH()*count;
		//
		Graphics2D gr =  (Graphics2D) biScreen.createGraphics();//\graphics from image
		Color c= null;
		
		for (Coordinate co : distribution.getCoordinates()) {
			Point p = whereScreen(co) ;
			value= distribution.getValues()[index+timeIndex];
			boolean inRange =(  p.x>0  && p.x< biScreen.getWidth()-1 && p.y>0 && p.y< biScreen.getHeight()-1 );
			if (value>1  && value!= distribution.getFillV()  && inRange){
				color= distribution.getGradient().retrieveColorInt(value);
//				int  combined = ColorInt.mergeWeight(color, biScreen.getRGB(p.x, p.y), 0.5f);
				//biScreen.setRGB(p.x, p.y, color);
				//Color
				c= distribution.getGradient().retrieveColor(value).makeColorAlpha();
//				System.out.println(c.getAlpha());
				gr.setColor(c);
				gr.fillOval(p.x-3, p.y-3, 6,6);//(int)(5+1/scale), (int)(5+1/scale));				
			}
			index++;
		}
//		System.out.println(" Drawing distribution!!!…");
	}

	/**
	 * Displays information about the value where the mouse is.
	 * 
	 * @param x1 the X position of the mouse on the screen
	 * @param y1 the Y position of the mouse of the screen
	 */
	private void displayValueInfo( int x1,int y1){
		
		// Create a graphic drawing surface from the scaled image 
		Graphics2D g2 = (Graphics2D) biScreen.createGraphics();
		// The actual positions within the topographic dataset
		int x = (int) (x1*scale+ offset.x);
		int y = (int) (y1*scale +offset.y);
		// The index within the dataset of topography -> x+y*width 
		int index =  Math.min(topography.getWidth()*topography.getHeight()-1,x+ y*topography.getWidth());
		// Get the values from the current position
		float v= topography.getValues()[index];
		
		// Build the String we want to display,.. 85,-180 are specific? «•»
		//
		Coordinate cup= topography.getUpper();
		Coordinate co= new Coordinate(cup.getLat()-y*Etopo2Data.res,cup.getLon()+ x*Etopo2Data.res); 
		String str= String.format("Val= %.1f m",v )+" at :"+co.getLat()+"°N"+" : "+co.getLon()+"°";
//		String.format("%.1f",);
		int posX= 100;//+ offset.x;// values.getWidth()/2;
		int posY= 20;
		// Draw the text inside a filled rectangle 
		 {	g2.setBackground(new Color(0.3f,0.3f,0.3f,0.6f));
		  	g2.setColor(new Color(0.15f,0.15f,0.2f,0.5f));
		  	g2.fillRoundRect(posX, posY, 220+ str.length()*2, 22, 18,8);
		  	g2.setColor(Color.white);
		  	g2.setFont(new Font("Arial", Font.PLAIN, 14));
		  	g2.drawString(str, posX+10, posY+15);
		 }
		// Interactive drawing
		g2.drawString(str.substring(4, 13)+"m", x1,y1);
		
		//Indicate the colour with a small circle
		Color c= cgradient.retrieveColor(v).makeColor();
		g2.setColor(c);
		g2.fillOval(posX-9,  posY+6,12,12);
		
		
		// Indicated line  to edges
		Point p=whereScreen(co);//new Coordinate(60, 0));
		g2.setColor(new Color(50, 50, 60,20));//Color.red);
		g2.drawLine(p.x, p.y, p.x, 0);
		g2.drawLine(0, p.y, p.x, p.y);
		
		drawLatLines(p.x);
		
		// 
	}

	private void drawLatLines(int px){
		Graphics2D graphic = (Graphics2D) biScreen.createGraphics();

		long timeTookms= (long) ((System.nanoTime()*0.0000001- lastTime));
		int  fade= (int) Math.max( timeTookms / 4,0);
		fade= Math.min(fade, 180);
		
//		System.out.println(timeTookms+" fade:"+fade);

		// Zero longitude Median
		Point p=whereScreen(new Coordinate(60, 0));
		graphic.setColor(new Color(150, 90, 90, 100-fade/4));//Color.red);
		graphic.drawLine(p.x, p.y, p.x, 0);
//		graphic.drawLine(0, p.y, p.x, p.y);
		
		//
		graphic.setFont(new Font("Arial", Font.PLAIN, 12));
		for(int ix=40; ix< 91; ix+=5){
			graphic.setColor(new Color(180, 90, 90, 70+180-fade/1));//Color.red);
			Point pZero=whereScreen(new Coordinate(ix, 0));
			String str= String.format("%d °",ix);
			graphic.drawString(str, pZero.x-18, pZero.y);
			graphic.drawString(str, px, pZero.y);
			graphic.setColor(new Color(150, 90, 90, 180-fade/2));//Color.red);
			graphic.drawLine(pZero.x, pZero.y, biScreen.getWidth(),pZero.y);

		}

	}
	
	
	private void plotGreenwich(){
		Point p= whereXY(new Coordinate(60, 0));
		Graphics2D g2 = (Graphics2D) biScreen.createGraphics();
		
	}
	
	
	




	private void redefine(){
	//		dimension.width= bi2.getWidth()+50;
	//		dimension.height= bi2.getHeight()+50;
			JFrame frame = (JFrame) this.getTopLevelAncestor();
			frame.setSize(dimension);
		}




	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int n= e.getWheelRotation()*(-1);
		boolean controlDown= e.isControlDown();
		boolean altDown= e.isAltDown();
		boolean shiftDown= e.isShiftDown();
		boolean only= !controlDown && !altDown && !shiftDown; 



		
		// Change the maximum of the topography gradient
		if (only) {
			cgradient.setMax(cgradient.getMax()+n*100);
			cgradient.define();
			cgradient.clear(Color.black);
			GraphicsUtil.produceDefaultTopoGradientGrey(cgradient, 255);
			cgradient.reDefine();
			topography.drawBufferedImage(0);
			System.out.println("MouseWheelListener activated");
		}
		
		
	}


	
	@Override
	/**
	 * //Label1
	 */
	public void mouseClicked(MouseEvent me) {
		int x = me.getX();
		int y = me.getY();
		
		displayValueInfo(x,y);
		
		System.out.println("Mouse was clicked at "+x+" :"+y); 
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override 
	public void mousePressed(MouseEvent  me) {
		mouseDownAt.setLocation(me.getX() ,me.getY());
		mDown=true;
//		System.out.println("mousePressed! "+ mDown);
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		mouseAt.setLocation(me.getX() ,me.getY());
//		offset.x +=   mouseDownAt.x-mouseAt.x;:
		offspeed.x+= (mouseDownAt.x-mouseAt.x)/5;
		offspeed.y+= (mouseDownAt.y-mouseAt.y)/5;
//		offset.y += mouseDownAt.y-mouseAt.y;
		System.out.println("mouse released "+(mouseAt.x -mouseDownAt.x));
		offset.x = Math.max(0,offset.x);
		offset.x = Math.min(image.getWidth(),offset.x);
		mDown=false;
	}


	@Override
	public void mouseMoved(MouseEvent me) {
		displayValueInfo(me.getX(),me.getY());
		mouseAt.setLocation(me.getX() ,me.getY());
//		if(mDown){
//			offset.x += mouseAt.x -mouseDownAt.x;
//			System.out.println("-Mouse was moved!!");
//		}

		lastTime= (long) (System.nanoTime()*0.0000001);
	}




	@Override
	public void mouseDragged(MouseEvent arg0) {
	}




	@Override
	public void keyPressed(KeyEvent e) {
		char keyc = e.getKeyChar();
		int key = e.getKeyCode();
		
		boolean controlDown= e.isControlDown();
		boolean altDown= e.isAltDown();
		boolean shiftDown= e.isShiftDown();
		boolean only= !controlDown && !altDown &&  !shiftDown;

		
		if (key == KeyEvent.VK_ESCAPE) {
			System.exit(0);
			System.out.println("pressing the escape");
		}
		else if(key == KeyEvent.VK_H  && controlDown){
			GradientPanel p= new GradientPanel(cgradient);
			p.show(); //Label1
		}

		// Save the image
		else if(key == KeyEvent.VK_S  && controlDown){
			
			 //rememberx 
			BufferedImage scalebi = resizeImage(image, image.getWidth()/2, image.getHeight()/2);
			String imname="Image"+offset.x+".png";
			File outputfile = new File(imname);
//			JFileChooser fcd = new JFileChooser();
//			fcd.showSaveDialog(this);
			try {
				ImageIO.write(scalebi, "png", outputfile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		else if(key == KeyEvent.VK_HOME){
			offset.x=0;
			offset.y=0;
		}
		else if(key == KeyEvent.VK_LEFT){
			offset.x--;
			offspeed.x-=8;
			offset.x = Math.max(0,offset.x);
		}
		else if(key == KeyEvent.VK_RIGHT){
			offset.x++;
			offspeed.x+=8;
			offset.x = Math.min(image.getWidth(),offset.x);
		}
		else if (key ==KeyEvent.VK_DOWN){
			offset.y++;
			offspeed.y+=8;
			offset.y = Math.max(0,offset.y);
		}
		else if (key ==KeyEvent.VK_UP){
			offset.y--;
			offspeed.y-=8;
			offset.y = Math.min(image.getHeight(),offset.y);
		}
		else if (key ==KeyEvent.VK_N){
			offset.x= 4500;
			offset.y= 300;
		}
		else if (key ==KeyEvent.VK_A){
			offset.x= 2300;
			offset.y= 1400;
		}
		else if (key ==KeyEvent.VK_E){
			offset.x= 5000;
			offset.y= 800;
		}
		else if (key ==KeyEvent.VK_I){
			offset.x= 8200;
			offset.y= 2200;
		}
		else if (key ==KeyEvent.VK_MINUS){
			scale*=2;
//			dimension.height/=2;
//			dimension.width/=2;
			redefine();
		}
		else if (key ==KeyEvent.VK_PLUS){
			scale*=0.5f;
//			dimension.height*=2;
//			dimension.width*=2;
			redefine();
		}
		
		// Change the display mode of topography
		else if(key == KeyEvent.VK_M){
			topography.displayMode+= 1;
			cgradient.clear(Color.black);
			if (topography.displayMode>6) {
				topography.displayMode=2;
			}
			//
			if (topography.displayMode==3) {
				GraphicsUtil.produceDefaultTopoGradient(cgradient, 255);
				cgradient.shiftDark(50);
			}
			else if (topography.displayMode==4) {
				GraphicsUtil.produceDefaultTopoGradient(cgradient, 150);
			}
			
			else if (topography.displayMode==5) {
				GraphicsUtil.produceDefaultTopoGradientGrey(cgradient, 255);
			}
			else if (topography.displayMode==6) {
				GraphicsUtil.produceDefaultTopoGradientGrey(cgradient, 80);
				cgradient.shiftGreen();
				cgradient.shiftGreen();
				cgradient.shiftGreen();
			}
			
			// Redraw the topography if it is okay
//			if (topography.isOk) {
				topography.drawBufferedImage(0);
//				System.out.println("topography is okay");
				cgradient.drawGradient();
//			}
		}

//		System.out.println("Keystroke:"+key+" "+e.getKeyCode());

	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
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

	
}
