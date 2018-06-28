package interaction;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import distribution.Distribution;
import visualised.VisualDistribution;


/**
 * 
 * @author Admin
 *
 */
public class SelectionMask  implements MouseListener,MouseMotionListener {
	
	private static JPanel panel=null;
	// The selection mask
	int [] mask= null;
	// The image we are doing the selection at
	private BufferedImage image;
	// The visualised image of the mask
	private BufferedImage screenImage;
	
	private Dimension dimMask;
	//
	public boolean isActive=false;
	//  The mask colour and the unmasked colour: pure black-and-white
	private Color maskColor= new Color(0,0,0 , 255); //Color.BLACK;
	private Color clearColor= new Color(255, 255, 255, 255);
	// The colour representing the mask as visualised
	private Color cBlue= new Color(90, 110, 110, 150); 
	private String message = "" ;
	//The percentage of pixels covering the area
	private float pixelCoverage=0;
	private boolean mirror=false;
	
	/**
	 * Constructs a selection mask with a given width and height
	 * @param w The width corresponding to the with of a distribution
	 * @param h the height corresponding to the height of a distribution
	 */
	public SelectionMask(int w,int h) {
		dimMask= new Dimension(w, h);
		
		//An image with opposite dimensionality as the distribution,
		//with the Width=d.height and v versa
		image = new BufferedImage(h,w, BufferedImage.TYPE_INT_ARGB);
		clearImage();
		// This is the image drawn on the screen  as a representation of the mask
		screenImage= new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		//Mask follows the distribution yx
		mask= new int[w*h];
		clearMask();
		
		setupPanel();
	}
	
	/**
	 * Set all values in the mask  to 0, i.e. false
	 */
	public void clearMask(){
		for (int i = 0; i < mask.length; i++) {
			mask[i]=0;
		}
	}

	
	/**
	 * Invert the mask to the opposite
	 */
	public void invert(){
		int rgb=0;
		int i=0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				i=screenToIndex(0, y, x);
//				i= y+x*image.getHeight();
				mask[i]= (mask[i]==1) ? 0 : 1 ;
				rgb= (mask[i]==1) ? maskColor.getRGB() : clearColor.getRGB();
				
				image.setRGB(x, y, rgb);
			}

		}
		createMaskImage();
	}


	/**
	 * Finds the mask by checking the pixels in the image 
	 * as they are either a black (mask) or a white (no mask).
	 * The mask following the indexing of a distribution is set accordingly.
	 * When done.. The mask image is created by calling createMaskImage().
	 */
	public void findMask() {
		int w= image.getWidth();
		int h= image.getHeight() ;
		int indexDistribution=0;
		int color=0;
		int count=0;
		
		// Clear the mask before we try to find out where the mask is
		clearMask();

		// Loop through all the pixels in the image and define the mask
		// to be the pixels where the colour equals the mask colour
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				//«•» but if distribution is rotated? Mirror
				indexDistribution=screenToIndex(0, y, x);// y+x*h ;//okay-mask indexing following distribution
				color=image.getRGB(x, y);
				if (color== maskColor.getRGB()) {
					mask[indexDistribution]=1;
					count++;					
				}
			}
		}
		//Always create the new mask image after setting the mask
		createMaskImage();
		//Then calculate how many pixels/points in the distribution is covered by the mask
		pixelCoverage = 100.0f*count/(w*h) ; 
//		System.out.println("Mask covers: "+count+" out of "+w*h+ "= "+v+" %");
		
	}


	/**
	 * Sets all the pixels in the mask image by going through each index in the mask
	 * which has been defined in the findMask() method
	 */
	private void createMaskImage(){
		
		Color clearColor= new  Color(0,0,0,0);
		int indexDistribution =0;
		int color=0;

		// Loop through all of the indexes in the mask which are opposite the pixels in the image
		for (int y = 0; y < screenImage.getHeight(); y++) {
			for (int x = 0; x < screenImage.getWidth(); x++) {
				// This is where the mapping from a distribution YXspace to screen image space occurs:
				// The mask follows the distribution, but the mask image is on the screen
				indexDistribution = screenToIndex(0, y, x);//y+x*dimMask.width ;
				// Colour fully transparent or with a soft blue depending on the mask
				color= (mask[indexDistribution]>=1) ? cBlue.getRGB(): clearColor.getRGB() ;
				screenImage.setRGB(x, y,color);
			}
		}

	}


	/**
	 * Clears the entire image to white before we do any selection.
	 * This indirectly has the effect of resetting the mask and the mask image 
	 * since finding the mask depends on this image and the mask image depends on the mask.
	 * 
	 */
	public void clearImage() {
		// Clear the entire image 
		Graphics2D g3 = image.createGraphics();
		g3.setBackground(clearColor);
		g3.clearRect(0, 0, image.getWidth(), image.getHeight());
	}
	

	/**
	 * Adds another mask to this 
	 * @param anotherMask the mask to add
	 */
	public void add(SelectionMask anotherMask){
		int  i = 0 ;
		int rgb=0;

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				i= y+x*image.getHeight();
				mask[i]+= anotherMask.getMask()[i];
				rgb= (mask[i]==1) ? maskColor.getRGB() : clearColor.getRGB();
				image.setRGB(x, y, rgb);
			}
		}

		createMaskImage();

	}
	
	
	/**
	 * Remove land areas from the selection.
	 * This is done by setting the colours of each pixel in the image used for defining the mask
	 * @param distribution the distribution where we can find the land
	 */
	public void removeLand(VisualDistribution distribution){

		int index=0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {

				//«•» mirror here «•»
				index=screenToIndex(0, y, x);//y+x*image.getHeight() ;//okay-mask indexing following distribution
				float v= distribution.getValues()[index];
				// Criteria for land
				if (v==distribution.getFillV() || v==distribution.getMissingV()) {
					image.setRGB(x, y, clearColor.getRGB());
				}
			}
		}
		findMask();

	}

	
	/**
	 * Defines the mask from a given distribution
	 * @param distribution the distribution to use as a mask 
	 * @param time the current time step for the distribution
	 */
	public void maskFromDistribution(Distribution distribution, int time){
		
		int indexDistribution=0;
		int color;
		float value=0; 
		// 
		clearImage();
		// Loop through all of the indexes in the mask which are opposite the pixels in the image
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				// «•» Corresponding index for the screen position
				indexDistribution = screenToIndex(time, y, x);
				value= distribution.getValues()[indexDistribution];
				// color= (value>0) ? maskColor.getRGB(): clearColor.getRGB() ;
				if (value>0) {
				image.setRGB(x, y,maskColor.getRGB());
				}
//				System.out.println("guest value:"+value);
			}
		}
//		System.out.println("maskFromDistribution ! ! !");
		findMask();
	}

	/** Maps/finds the mask index corresponding to the given pixel position within image
	 * @param time
	 * @param y screen position y
	 * @param x screen position x	
	 * @return 
	 */
	private int screenToIndex(int time, int y, int x) {
		int indexDistribution;
		//indexDistribution = y+x*dimMask.width +distribution.getWH()*time ;
		if (mirror) {
			x=  image.getWidth()-x-1;
			y = image.getHeight()-y-1;
		}
		indexDistribution = y+x*dimMask.width + time*dimMask.height*dimMask.width;//   distribution.getWH()*time ;
		return indexDistribution;
	}

	/**
	 * Calculates the area coverage of the mask
	 * @param distribution 
	 */
//	public void calculateAreaCoverage(VisualDistribution distribution){
//		areaCoverage=0;
//		for (int i = 0; i < mask.length; i++) {
//			if (mask[i]==1) {
//				areaCoverage+= distribution.getArea()[i];
//			}
//		}
////		areaCoverage/=distribution.get
//	}

	
	/**
	 * Save the mask as an image
	 * @param file the file to save to
	 */
	public void saveMask(File file){

		if (file==null){
			JFileChooser fcd = fromFileChooser(" Set a location and filename for saving image of mask");
			fcd.showSaveDialog(null);
			if(fcd.getSelectedFile()!=null) {
				file =  new File( fcd.getSelectedFile().getAbsoluteFile()+".png");
			}
		}

		if (file!=null){
			try {
				ImageIO.write(image, "png", file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	

	/**
	 * Opens a mask image file
	 * @param file the image file to open as a mask
	 */
	public void openMaskImage(File file){

		if (file==null){
			JFileChooser fcd = fromFileChooser("Open image mask");
			fcd.showOpenDialog(null);
			if(fcd.getSelectedFile()!=null) {
				file =  new File( fcd.getSelectedFile().getAbsoluteFile().getAbsolutePath() );//+".png");
			}
		}
		

		// If we have a file, open it and create the mask
		if (file!=null){
			System.out.println(file.getAbsolutePath());
			try {
				image= ImageIO.read(file);
				findMask();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	/**
	 * @return
	 */
	private JFileChooser fromFileChooser(String st) {
		String initialDirectory="./res/Gradients/"; //relative path to current directory
		JFileChooser fcd = new JFileChooser(initialDirectory);
		fcd.setDialogTitle(st);
		fcd.setFont(new Font("Times", Font.PLAIN, 14));
		return fcd;
	}


	/**
	 * Draws a filled polygon representing a selection on the image and updates the mask by
	 * calling the function findMask()
	 * @param poly The polygon we want to draw
	 * @param add Whether we are going to add or subtract from the mask
	 */
	public void drawPolygon(Polygon poly, boolean add){
		Graphics2D graphic = image.createGraphics();
		Color color= (add) ? maskColor: clearColor;
		graphic.setColor(color);
		graphic.fillPolygon(poly);
		findMask();
	}
	
	/**
	 * Draws a filled circle on the image as a way to select (or deselect) on the selection mask
	 * @param p The point on the image where the circle is drawn
	 * @param radiusX The radius of the circle drawn
	 * @param add Whether we are going to add or subtract from the mask
	 */
	public void drawCircle(Point p,int radiusX,int radiusY, boolean add){
		Graphics2D graphic = image.createGraphics();
		Color color= (add) ? maskColor: clearColor;
		graphic.setColor(color);
		graphic.fillOval(p.x-radiusX/2, p.y-radiusY/2, radiusX, radiusY);
		findMask();
	}
	

	
	/**
	 * Draws a filled rectangle on the image as a way to select  (or deselect) on the selection mask
	 * @param area The rectangular area we want to draw
	 * @param add Whether we are going to add or subtract from the mask
	 */
	public void drawRectangle(Rectangle area, boolean add){
		Graphics2D graphic = image.createGraphics();
		Color color= (add) ? maskColor: clearColor;
		graphic.setColor(color);
		graphic.fillRect(area.x, area.y, area.width, area.height);
		findMask();
	}

	/**
	 * Draws the mask on the incoming image:   not used
	 * @param bi
	 */
	public void drawOnTheImage(BufferedImage bi){
		Graphics2D graphic = bi.createGraphics();
		graphic.drawImage(screenImage, 0, 0, null);
	}



	/**
	 * @param mask
	 */
	private void setupPanel() {
		panel= new JPanel(){
			
			@ Override
			public void paintComponent(Graphics g) {
				g.clearRect(0, 0, image.getWidth()*2, image.getHeight()+20);
				g.drawImage(image, 0, 0, null);
				g.drawImage(screenImage, image.getWidth()+2, 0, null);
				g.drawString(message, 20, image.getHeight()+20);
			}
		};
		panel.addMouseListener(this);
		panel.addMouseMotionListener(this);
	}




/*
	public static void main(String[] args) {
	
			Polygon poly=  new Polygon();
	
			poly.addPoint(20, 20);
			poly.addPoint(140, 20);
			poly.addPoint(90, 40);
	//		poly.addPoint(40, 140);
			poly.addPoint(10, 140);
	
			SelectionMask mask= new SelectionMask(200, 150);
			mask.drawPolygon(poly,true);
			mask.findMask();
			//
			Rectangle r= new Rectangle(50,50,50, 50);
			mask.drawRectangle(r, true);
			
			//
			
	//		panel.seta
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(450, 370);
			frame.add(panel);
			frame.addMouseListener(mask);
			frame.setLocation(600, 300);
			frame.setTitle("title");
			frame.setVisible(true);
		}
		*/

	@Override
	public void mouseClicked(MouseEvent me) {
		boolean controlDown= !me.isControlDown();
		boolean shiftDown= me.isShiftDown();

		if (shiftDown) {
			invert();
		}

		drawCircle(me.getPoint(), 50,50,controlDown);
		panel.repaint();
	}

	@Override
	public void mouseEntered(MouseEvent me) {		
	}

	@Override
	public void mouseExited(MouseEvent me) {
	}

	@Override
	public void mousePressed(MouseEvent me) {
	}

	@Override
	public void mouseReleased(MouseEvent me) {		
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		boolean controlDown= !me.isControlDown();

		drawCircle(me.getPoint(), 20,20,controlDown);
		panel.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		boolean controlDown= me.isControlDown();
		int x= me.getX();
		int y= me.getY();			


		if(controlDown){
			drawCircle(me.getPoint(),10, 10,controlDown);
		}
		panel.repaint();

		int dindex= y+x*dimMask.width;
		int ixScreen= x+y*image.getWidth();
		dindex= Math.max(0, Math.min(dimMask.width*dimMask.height-1, dindex));
		//
		int value= mask[dindex];
		if (x<image.getWidth() && y<image.getHeight()) {
			String color= (image.getRGB(x, y) ==Color.black.getRGB()) ? "black":"white" ;
			System.out.println(x+":"+y+"->color:"+color+" Index: "+ixScreen+
					" in distribution "+dindex+ " mask:"+value);
			message= String.format("Index: "+ixScreen+
					" in distribution "+dindex+ " mask:"+value );
		}
	}
	
	// ==================================================
	// Getters and setters
	// ==================================================

	/**
	 * @return the mask
	 */
	public int[] getMask() {
		return mask;
	}

	/**
	 * @param mask the mask to set
	 */
	public void setMask(int[] mask) {
		this.mask = mask;
	}

	/**
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
	}

	
	/**
	 * @return the maskColor
	 */
	public Color getMaskColor() {
		return maskColor;
	}

	/**
	 * @param maskColor the maskColor to set
	 */
	public void setMaskColor(Color maskColor) {
		this.maskColor = maskColor;
	}

	/**
	 * @return the maskImage
	 */
	public BufferedImage getMaskImage() {
		return screenImage;
	}

	/**
	 * @return the pixelCoverage
	 */
	public float getPixelCoverage() {
		return pixelCoverage;
	}

	/**
	 * @return the cBlue
	 */
	public Color getcBlue() {
		return cBlue;
	}

	/**
	 * @param cBlue the cBlue to set
	 */
	public void setcBlue(Color cBlue) {
		this.cBlue = cBlue;
	}

	/**
	 * @return the mirror
	 */
	public boolean isMirror() {
		return mirror;
	}

	/**
	 * @param mirror the mirror to set
	 */
	public void setMirror(boolean mirror) {
		this.mirror = mirror;
	}

	
	

}
