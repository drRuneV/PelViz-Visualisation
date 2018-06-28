/**
 * 
 */
package visualised;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import distribution.Distribution;
import distribution.Topography;
import ecosystem.Coordinate;
import graphics.ColorGradient;
import graphics.ColorInt;
import graphics.GraphicsUtil;
import graphics.TopoPanel;
import testing.ImageFrame;
import ucar.nc2.NetcdfFile;

/**
 * @author a1500
 *
 */
public class VisualTopography extends Topography implements Visualised{

	
	private BufferedImage image= null;
	//
	int scale=1;
	//
	private ColorGradient gradient;
	
	// 
	public boolean showGradient=true;
//	public boolean showHistogram=false;
	// How is the topography going to be displayed
	public int displayMode=1;
	//
	public boolean isOk=false;
	// this should be a enum ! type
	private int projection=0;
	//
	private VisualDistribution distribution= null;
	//
	private boolean mirror=false;
	
	/**
	 * 
	 * @param ncfile
	 */ //not used «•» !
	public VisualTopography(NetcdfFile ncfile) {
		super(ncfile);
		
		createGradient();				
	}
	
	/**
	 * Constructs a VisualTopography by first creating a topography with a full dataset
	 * @param filename The name of the netCDF file
	 * @param name The name of the dataset/variable
	 */
	public VisualTopography(String filename, String name) {
		super(filename, name);
		// This topography is okay only when we were able to open something
		isOk= (this.getNcfile()!=null);
		
		if(this.getNcfile()!=null){
			createGradient();
			createImage();
		}
		else {
//			JOptionPane.showMessageDialog(null, message);
		}
	}
	
	
	
	
	/**
	 * Creates a VisualTopography by copying all variables from a topography
	 * 
	 * @param topo The topography to copy from
	 * @return a new VisualTopography
	 */
	static public VisualTopography createFrom(Topography topo){
	
		VisualTopography vd= new VisualTopography(topo.getNcfile());
//		vd.copyAllFrom(topo);
		vd.copyFromAnother(topo);
		return vd;
	}

	/**
	 * 
	 */
	public void createGradient() {
		// Set a specific ColourGradient for topography
		gradient=new ColorGradient(2300.0f, -4200.0f);
		GraphicsUtil.produceDefaultTopoGradientGrey(gradient, 255);
		gradient.setUseOpacityMask(false);
		gradient.setUseLogarithmic(false);
		gradient.drawGradient();
	}



	/**
	 * Create the background image we are drawing on
	 */
	public void createImage() {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}


	
	/**
	 * This method should be moved to another class:VisualTopo
	 * @param l
	 * @param i
	 * @return
	 */
	protected float darken(float l,int i){
		float factor=1;
		float d=1;	  // The diagonal
		float h=1;	  // The difference in values between 2 neighbours

		h= getValues()[i+1]-getValues()[i];
		//If values increase make it darker
		d= (float) Math.sqrt(h*h+ l*l);
		factor= Math.max(0.4f, l/d);
//		if (h<0)factor= Math.min(1.5f, 1/factor);
		
		return factor;
	}






	/**
	 * @param args
//	 */
//	public static void main(String[] args) {
//	}

	

	@Override
	/**
	 * Draws the topography
	 * @param t The current time
	 */
	public void drawBufferedImage(int t) {
		if (projection==0) {
			drawLatLonBufferedImage(t);
		}
			

	}

	

	@Override
	/**
	 * Draws the topography on a BufferedImage in straightforward latitude longitude projection
	 * @param t The current time 
	 */
	public void drawLatLonBufferedImage(int t) {
		//
		int x;		//Label2
		int y;
		boolean useRelief=true;
		float darkLength=60; 

		// Please set the color here
		for (int i = 0; i < getValues().length-1; i++) {
			y= i/width;
			x= i- y*width;

			// Must create a completely new colour each time
			ColorInt color= new ColorInt( gradient.retrieveColorInt(getValues()[i]));

			if (useRelief) {
				color.darker(darken(darkLength, i));
			}
			if (x< width && y<height) {
				image.setRGB(x, y ,  color.toInt()); //image.getHeight()-y-1,  color);
			}
			else System.out.println("x"+x+" y"+y);
		}


	}

	
	
	/**
	 * Interpolates a coordinate grid to a larger coordinate grid
	 * @param w the width of the original grid
	 * @param h the height of the original grid
	 * @param coOr the original coordinate grid
	 * @return a new interpolated coordinate grid
	 */
	public Coordinate[] interpolate(int w,int h,Coordinate[] coOr){
		int w2= w*2-1;
		int h2= h*2-1;

		int expanded= (w2)*(h2);  //E.g.: a 3 x 4 grid becomes 5 x 7
		Coordinate[] c= new Coordinate[expanded];
		int ix=0;
		int iy=0;
		int x2=0;
		
//		System.out.println(coOr.toString());
//		for (Coordinate coordinate : coOr) {
//			System.out.println(coordinate.toString());
//		}

		// Go through the original data
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				iy=y*2;		//every 2nd row, even number row
				//every 2nd is the same
				ix=x*2; 	// twice the column
				c[ix+iy*w2] = new Coordinate( coOr[x+y*w]);
				// interpolate between x and x-1 to fill in the columns between
				if (x>0) {
					x2=ix-1;
					//column between x-1
					c[x2+iy*w2] = new Coordinate( coOr[x+y*w].interpolate(coOr[x-1+y*w]));
				}
			}
		}
		
		int yabove=0;
		int ybelow=0;
		//now fill in the missing rows : 1,3,5,7..
		for (int y = 1; y < h2-1; y+=2) {
			for (int x = 0; x < w2; x++) {
				yabove=y-1;
				ybelow=y+1;
				c[x+y*w2]= new Coordinate( c[x+yabove*w2].interpolate(c[x+ybelow*w2]) ); //was w2
			}
			
		}
			
			
		return c;
	} 
	
	
	/**
	 * Draws the visual distribution on a BufferedImage XY projected
	 * @param im The image to draw on
	 * @param dis The distribution we are drawing together with.
	 * The distribution is used in order to have a reference to latitude and longitude 
	 */
	public void drawOnBufferedImageXY(BufferedImage im, Coordinate[] coordinateGrid) {
		
		int height=  im.getWidth();
		int width=   im.getHeight();
		float value=0;
		int x1= 0;
		boolean useSlope=false;
		ColorInt landcolor= new ColorInt( gradient.retrieveColorInt(500));
		ColorInt seacolor= new ColorInt( gradient.retrieveColorInt(-500));


		// Please color the values here
		for (int y = 0; y < height;y++) {
			for (int x = 0; x < width; x++) {
				if (x+y*width< coordinateGrid.length) {
					
				// Find the corresponding coordinate and the value at this coordinate
				Coordinate co= new Coordinate(coordinateGrid[x+y*width]);
				value= getValueAtCoordinate(co);//getValues()[x+y*width];
				ColorInt color= new ColorInt( gradient.retrieveColorInt(value));
				// Check for mode
				switch(displayMode){
				case 1: color=(value>0) ?  landcolor: color ;
				break;
				case 2: color=(value<0) ?  seacolor: color ;
				break;
				}
				//
				if (useSlope) {
				x1	= Math.min(x+y*width+1, width*height-1);
				Coordinate co2=new Coordinate(coordinateGrid[x1]);
				float value1= getValueAtCoordinate(co2);
				if (value1> value) {
					color.darker( 0.7f );
				}
//				System.out.println("darker"); 
				}	

				if (x< width && y<height) {
					
					if (mirror) {
						int xm=width-x-1;// Math.min(image.getHeight(), height-x-1);
						int ym=height-y-1;//  Math.min(image.getWidth(),width-y-1);
						im.setRGB(ym,xm,color.toInt()); // XY in distribution Opposite than image!!
					}
					else {
						im.setRGB(y,x,color.toInt()); // XY in distribution Opposite than image!!
					}
				} 
				}

			}
		}
	}
	
	
	@Override
	/**
	 * 
	 *///This is not used! 
	public void drawOnBufferedImage(BufferedImage im, int t) {
		
		int width=  im.getHeight(); //opposite!!
		int height= im.getWidth();
		float value=0;

		
		// Please color the values here
		for (int y = 0; y < height ;y++) {
			for (int x = 0; x < width; x++) {
				if (getCoordinates() ==null)  {
					System.out.println("Coordinates is undefined");
				}
				Coordinate co= new Coordinate(getCoordinates()[x+y*width]);
				//
					value= getValueAtCoordinate(co);//getValues()[x+y*width];
//				System.out.println("Coordinate:"+ co.toString()+" v= "+value);
//				else  System.out.println("topography is undefined");
					
				int color=  gradient.retrieveColorInt(value);
				
				if (x< width && y<height) {
					if (mirror) {
						y=width-y-1;
						x=height-x-1;
					}
					im.setRGB(y,x,color); //Opposite!!
				}
			}
		}
		
	}


	/**
	 * Shows the topography in a separate frame
	 */
	public void showTopoInWindow(){

		//  Test Drawing on a panel to be shown
		JPanel p= new TopoPanel(this);
		// Show the panel on a JFrame
		JFrame frame= new ImageFrame("Topography",p, new  Rectangle());
	}
	



	/**
	 * Changes the topographically gradient
	 * @param n number of changes
	 * @return a new ColourGradient 
	 */
	public ColorGradient changeMode(int  n) {
		ColorGradient cg = getGradient();
		//
		
		displayMode+= n;
		if ( displayMode>6 || displayMode<0) {
			 displayMode=0;
		}
		//
		if ( displayMode==3) {
			GraphicsUtil.produceDefaultTopoGradient(cg, 255);
			cg.shiftDark(50);
		}
		else if ( displayMode==4) {
			GraphicsUtil.produceDefaultTopoGradient(cg, 80);
		}
	
		else if ( displayMode==5) {
			GraphicsUtil.produceDefaultTopoGradientGrey(cg, 255);
		}
		else if ( displayMode==6) {
			GraphicsUtil.produceDefaultTopoGradientGrey(cg, 80);
		}
	
		
		return cg;
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
	 * @return the gradient
	 */
	public ColorGradient getGradient() {
		return gradient;
	}




	/**
	 * @param gradient the gradient to set
	 */
	public void setGradient(ColorGradient gradient) {
		this.gradient = gradient;
	}





	/**
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
