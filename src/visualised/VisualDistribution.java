package visualised;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import distribution.Distribution;
import ecosystem.Coordinate;
import graphics.ColorGradient;
import graphics.ColorInt;
import graphics.ColorSet;
import graphics.DistPanel;
import graphics.DistributionsPanel;
import graphics.GraphicsUtil;
import interaction.SelectionMask;
import ucar.nc2.NetcdfFile;



public class VisualDistribution extends Distribution implements Visualised{

	private BufferedImage image= null;
	//
	private ColorGradient gradient;
	private ColorGradient gradientCombined;
	
	// Flag indicating graphics to show
	public boolean showGradient=true;
	public boolean showHistogram=false;
	public boolean showTail=true;
	public boolean showCm=false;
	public boolean showAreaInfo=false;
	public boolean showBiomass=false;
	//
	ArrayList<VisualDistribution> guestList = new ArrayList<>();
	private VisualDistribution guest= null;
	// Flag indicates whether we should merge colours together explicitly
	private boolean usemergeColor= true;
	// Flag indicates whether we should visualise the accumulated distribution
	private boolean useAccumulated= false;
	// If we are only drawing within the mask
	private boolean useMaskDrawing=false;
	// The Accumulated distribution
	private float accumulated[]= null;
	// 
	private boolean useAGuestAsMask=false;
	// 
	public boolean useSlope =false;
	// Flag indicates whether we should visualise the difference between various time steps
	public boolean useDifference=false;
	private int diffInterval=7;
	
	// Whether topography is being drawn beneath
	private boolean useTopography=false;// true;
	// This is the fill colour used for land when no topography data are present.
	private Color fillColor= new ColorInt(90, 90, 50, 200).makeColor();
	
	// The pixels this distributions occupies within the component where the image is placed
	private Rectangle pixelRegion= new Rectangle();
	// Absolute pixel region on screen
	private Point absoluteLocation= new Point();
	//  Selected region
	private Rectangle selectedRegion= new Rectangle();
	//
	private boolean mirror=false;
	

	

	// ===============================================
	
	/**
	 * Constructor
	 * 
	 * Creates a new visual distribution
	 * @param name The name of the variable we are going to look for in the netCDF file
	 * @param ncfile The netCDF files where we are going to look for the variable
	 */
	public VisualDistribution(String name, NetcdfFile ncfile) {
		super(name, ncfile);

		// Create the image we are drawing, but opposite !!!
		image = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
		progressFrame.update("Generated image.."+image.getWidth()+"×"+image.getHeight());
		
		createGradient();
		createGradientCombine();
		//
		selectedMask=new SelectionMask(width, height);
		//
		accumulated= new float[width*height];
		
		//
		progressFrame.dispose();
		
		
	}
	
	
	
	public void recalculate(int t){
		biomass.calculate();
	}

	
	/**
	 * Updates the distribution. This should be called before drawing.
	 * @param count this is the time step/day date
	 */
	public void update(int count){
		
		if (useAGuestAsMask && getGuest()!=null) {
			selectedMask.maskFromDistribution(guest, count);
		}
	}
	
	
	/**
	 * 
	 */
	private void createGradientCombine(){
		gradientCombined= new ColorGradient(1, 0);
		ColorSet  cs= new ColorSet("orange");
		
		GraphicsUtil.produceGradientDiscreetFrom(gradientCombined, cs.getColors());
		gradientCombined.interpolate(false);
		gradientCombined.setTheOpacityLinear();
	}
	

	/**
	 * Create the Gradient this distribution is going to use
	 */
	public void createGradient(){
		float maxM[]= new float[]{(float) getStatistics().getAverage()*2,  (float) getStatistics().getMin()};
		String na= getFullName();
		int colorIndex=  GraphicsUtil.mappingGradientToName(na.toLowerCase());
		gradient= new ColorGradient(maxM[0], maxM[1]);
		gradient= GraphicsUtil.produceGradient( colorIndex,maxM);//(int) (Math.random()*8-1), maxM );//maxMin[1]);
		gradient.define();		
	}

		

	
	public void show(){
		DistPanel pa= new DistPanel(this, true);
	}
	
		
	public void showOn(DistributionsPanel dpa){
		if (dpa==null) {
			dpa= new DistributionsPanel(this, true);
		}
		else{
			dpa.addDistribution(this);
		}
	}
	
	
	/**
	 * Accumulates the values
	 * @param t
	 */
	public void accumulate(int t){

		float value=0;
		int index=0;
		
		//Clear at time step 0
		if (t==0) {
			for (int i = 0; i < accumulated.length; i++) {
				accumulated[i]=0;			
			}
		}
		
		//Please set the accumulated values here
		for (int y = 0; y < height ;y++) {
			for (int x = 0; x < width; x++) {
				index =  x+y*width+t*this.getWH();
				accumulated[x+y*width]+=getValues()[index];
			}
		}
	}

	
	
	/**
	 * Draws the topography on a given BufferedImage.
	 * ! «•» This method should be moved into a VisualTopography later on
	 * 
	 * @param topography
	 * @param cg
	 * @param timage
	 */
	/*
	public void drawTopography(Topography topography,ColorGradient cg,BufferedImage timage){
		int width=  getWidth();
		int height= getHeight();
		float value=0;

		// Please set the values here
		for (int y = 0; y < height ;y++) {
			for (int x = 0; x < width; x++) {
				Coordinate co= new Coordinate(getCoordinates()[x+y*width]);
				//
				if (topography!=null) {
					value= topography.getValueAtCoordinate(co);//getValues()[x+y*width];
//				System.out.println("Coordinate:"+ co.toString()+" v= "+value);
				}				
				else  System.out.println("topography is undefined");
					
				int color=  cg.retrieveColorInt(value);
				
				
				if (x< width && y<height) {
					timage.setRGB(y,x,color); //Opposite!!
				}
			}
		}
		
	}
	*/
	
	
	// ==================================================
	// Implemented methods
	// ==================================================
	
	//«•» Not really used  any more. 
	//«•» It was used to draw pixels on a already existing image
	//«•» 
	@Override
	public void drawOnBufferedImage(BufferedImage im, int t) {
		t= (isHasTime())?  t:0;
		
		int width=  getWidth();
		int height= getHeight();
		int wh= 	width*height;
		float value=0;
		float relvalue;
		int  colorf= Color.black.getRGB();  

		// Please set the values here
		for (int y = 0; y < height ;y++) {
			for (int x = 0; x < width; x++) {
				//
				value= getValues()[x+y*width+t*wh];
				int color=  (value==getFillV()) ? colorf :gradient.retrieveColorInt(value);
//				ColorInt ci= new ColorInt(color);
//				Color c= ci.makeColor();
				relvalue=(float) (value/gradient.getMax());//getStatistics().getMax());
				
				int current= im.getRGB(y, x); 
				int combined = (value==0 || value==getFillV()) ? current: 
					ColorInt.mergeWeight(color,current,relvalue);//ColorInt.merge(color, current);//color+=current;
				
				if (x< width && y<height) {
					im.setRGB(y,x,combined); //Opposite!!
				}
				else System.out.println("Unable to set the values: x"+x+" y"+y);
			}
		}

	}

	
	/**
	 * 
	 */
	public void drawBufferedImage(int t) {
		
		t= (isHasTime()) ?  t:0;
		t= Math.min( Math.max(0,t),getTime()-1 );
		// 
		if (t<0) {
			System.out.println("absurd value : " +  t); 
		}

//		int width= getWidth();
//		int height= getHeight();
		int wh= width*height;
		ColorInt c= new ColorInt(fillColor);
		int  colorf=   c.toInt();
		
		float value=0;
		int index =0;
		boolean sea=true;
		boolean masking=  (useMaskDrawing && getSelectedMask().getPixelCoverage()>0 );
		
		// Please set the values here
		for (int y = 0; y < height ;y++) {
		for (int x = 0; x < width; x++) {
				index= x+y*width;
				value= getValues()[index+t*wh];
			
				//Criteria for land versus sea
				sea= (value!=getFillV());
				
				// Skip land/filling values if we are using topography
				if (useTopography && !sea ) {
					continue;
				}
				// Mask : Another Skip drawing criteria
				if (masking && sea  && getSelectedMask().getMask()[index]==0) {
						//continue;//
						value=0;
					}
				
				
				// Use the difference between the current time and some future time - t2
				if ( sea &&   useDifference  && isHasTime()) {
					int t2=	 Math.min( Math.max(0,t+diffInterval),getTime()-1 );
					value=  getValues()[index+t2*wh] - value;
				}
				// Value is the accumulated value
				else if(useAccumulated && sea){
					value= accumulated[index];
				}
				// Set the final colour
				int color=  (value==getFillV()) ? colorf :gradient.retrieveColorInt(value);
				
				// Mirror !
				if (mirror) {
					int xm=width-x-1;// Math.min(image.getHeight(), height-x-1);
					int ym=height-y-1;//  Math.min(image.getWidth(),width-y-1);
					image.setRGB(ym,xm,color); // XY in distribution Opposite than image!!
				}
				else {
					image.setRGB(y,x,color); // XY in distribution Opposite than image!!
				}
				
			}
		}

	}


	@Override
	public void drawLatLonBufferedImage(int t) {
		for (Coordinate co : getCoordinates()) {
			
		}
	}



	/**
	 * 
	 * @param t
	 */
	public void drawWithGuestBufferedImage(int t) {
		t= (isHasTime())?  t:0;
		int width= getWidth();
		int height= getHeight();
		int wh= width*height;
		float value=0;
		float valueG =0;
		int color= 0;
		int colorG= 0;
		int combined=0;
		int mixColor=0;
		boolean land=false;
		//colour for fill value
		int  colorf= Color.black.getRGB();  

		// Please set the values here
		for (int y = 0; y < height ;y++) {
			for (int x = 0; x < width; x++) {
				//
				value= getValues()[x+y*width+t*wh];
				valueG= getGuest().getValues()[x+y*width+t*wh];
				land= (value==getFillV() || valueG==getFillV() );
				// Use a fixed colours for fill values, typically land
				color=  (value==getFillV()) ? colorf :gradient.retrieveColorInt(value);
				colorG= (valueG==getFillV()) ? colorf : getGuest().gradient.retrieveColorInt(valueG);
				
				// Combine the values
				if (value*valueG == 0) {
					combined= (value==0)? colorG : color;
				}
				else {
					//The value of the guest relative to the total value
					float relative = valueG/(valueG+value);
					mixColor= gradientCombined.retrieveColorInt( relative);
					combined= (usemergeColor)? ColorInt.mergeWeight(color, colorG, 1- relative) : mixColor;
					combined= (land)? colorf: combined; 
					//	System.out.println("v= "+ valueG/(valueG+value));
				}

				// Set the pixels in the bitmap finally
				if (x< width && y<height) {
					image.setRGB(y,x,combined); //Opposite y:x !!
				}
				else System.out.println("Unable to set the values: x"+x+" y"+y);
			}
		}
	}

	/**
	 * Redraws the image as a black transparent
	 */
	public void redrawImage(){
		Color c= new Color(0, 0, 0, 0);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
			image.setRGB(x, y, c.getRGB());	
			}
		}
	
	}
	
	/**
	 * Sets all the gradients of the guests to have the same maximum as this one
	 */
	public void normaliseGradients() {
		// 
		if (getGuestList()!=null) {
			for (VisualDistribution vdg : getGuestList()) { 
				ColorGradient g=vdg.getGradient();
				g.setMax(gradient.getMax());
				g.interpolate(false);
				g.reDefine();			
			}
		}
	}

	public Point pixelUpperLeft(){
		Point p=  new Point(pixelRegion.x+pixelRegion.width,pixelRegion.y);
		return p;
	}

	
	public Point pixelAbsoluteUpperLeft(){
		Point p= new Point(absoluteLocation.x+pixelRegion.width, absoluteLocation.y);
		return p;
	}
	
	
	// ==================================================
	// •	All the getters and setters goes below	•
	// ==================================================

	/**
	 * @return the usemergeColor
	 */
	public boolean isUsemergeColor() {
		return usemergeColor;
	}

	/**
	 * @param fillColor the fillColor to set
	 */
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	/**
	 * @return the fillColor
	 */
	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(BufferedImage image) {
		this.image = image;
	}

	/**
	 * @return the showAreaInfo
	 */
	public boolean isShowAreaInfo() {
		return showAreaInfo;
	}



	/**
	 * @param showAreaInfo the showAreaInfo to set
	 */
	public void setShowAreaInfo(boolean showAreaInfo) {
		this.showAreaInfo = showAreaInfo;
	}



	/**
	 * @return the showBiomass
	 */
	public boolean isShowBiomass() {
		return showBiomass;
	}



	/**
	 * @param showBiomass the showBiomass to set
	 */
	public void setShowBiomass(boolean showBiomass) {
		this.showBiomass = showBiomass;
	}



	/**
	 * @param gradient the gradient to set
	 */
	public void setGradient(ColorGradient gradient) {
		this.gradient = gradient;
	}

	/**
	 * @return the gradient
	 */
	public ColorGradient getGradient() {
		return gradient;
	}

	public ArrayList<VisualDistribution> getGuestList() {
		return guestList;
	}

	public void setGuestList(ArrayList<VisualDistribution> guestList) {
		this.guestList = guestList;
	}

	/**
	 * @param usemergeColor the usemergeColor to set
	 */
	public void setUsemergeColor(boolean usemergeColor) {
		this.usemergeColor = usemergeColor;
	}

	/**
	 * @return the guest
	 */
	public VisualDistribution getGuest() {
		return guest;
	}

	/**
	 * @param guest the guest to set
	 */
	public void setGuest(VisualDistribution guest) {
		this.guest = guest;
	}

	/**
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
	}
	

	/**
	 * @return the diffInterval
	 */
	public int getDiffInterval() {
		return diffInterval;
	}

	/**
	 * @param diffInterval the diffInterval to set
	 */
	public void setDiffInterval(int diffInterval) {
		this.diffInterval = diffInterval;
	}

	/**
	 * @return the useDifference
	 */
	public boolean isUseDifference() {
		return useDifference;
	}

	/**
	 * @param useDifference the useDifference to set
	 */
	public void setUseDifference(boolean useDifference) {
		this.useDifference = useDifference;
	}



	public boolean isUseAccumulated() {
		return useAccumulated;
	}



	public void setUseAccumulated(boolean useAccumulated) {
		this.useAccumulated = useAccumulated;
	}



	public boolean isUseSlope() {
		return useSlope;
	}



	public void setUseSlope(boolean useSlope) {
		this.useSlope = useSlope;
	}



	public boolean isUseTopography() {
		return useTopography;
	}



	public void setUseTopography(boolean useTopography) {
		this.useTopography = useTopography;
	}



	/**
	 * @return the pixelRegion
	 */
	public Rectangle getPixelRegion() {
		return pixelRegion;
	}



	/**
	 * @param pixelRegion the pixelRegion to set
	 */
	public void setPixelRegion(Rectangle pixelRegion) {
		this.pixelRegion = pixelRegion;
	}
	
	



	/**
	 * @return the absoluteLocation
	 */
	public Point getAbsoluteLocation() {
		return absoluteLocation;
	}



	/**
	 * @param absoluteLocation the absoluteLocation to set
	 */
	public void setAbsoluteLocation(Point absoluteLocation) {
		this.absoluteLocation = absoluteLocation;
	}



	/**
	 * @return the selectedRegion
	 */
	public Rectangle getSelectedRegion() {
		return selectedRegion;
	}



	/**
	 * @param selectedRegion the selectedRegion to set
	 */
	public void setSelectedRegion(Rectangle selectedRegion) {
		this.selectedRegion = selectedRegion;
	}

	/**
	 * @return the useMaskDrawing
	 */
	public boolean isUseMaskDrawing() {
		return useMaskDrawing;
	}



	/**
	 * @param useMaskDrawing the useMaskDrawing to set
	 */
	public void setUseMaskDrawing(boolean useMaskDrawing) {
		this.useMaskDrawing = useMaskDrawing;
	}



	/**
	 * @return the useAGuestAsMask
	 */
	public boolean isUseAGuestAsMask() {
		return useAGuestAsMask;
	}


	/**
	 * @param useAGuestAsMask the useAGuestAsMask to set
	 */
	public void setUseAGuestAsMask(boolean useAGuestAsMask) {
		this.useAGuestAsMask = useAGuestAsMask;
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

	