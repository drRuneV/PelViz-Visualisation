package graphics;

import java.awt.Point;

import com.sleepycat.je.rep.elections.Protocol.Accept;

import ecosystem.Coordinate;
import visualised.VisualDistribution;
import visualised.VisualTopography;

/**
 * An assistant class for retrieving information at a given pixel for a visual distribution
 * 
 * @author Admin
 *°||° Remember that when you are using public datatypes it is almost impossible to track where they are being accessed, changed and used.
	°When they are private we can track them being accessed through the getters and setters.
 */
public class PixelInfo {

	private VisualDistribution distribution;
	private DistPanel panel =null;

	// The XY position within the distribution
	public int x;
	public int y;
	public Point yxLocation= new Point();
	
	// The XY index within the area independent of time
	public int indexYX=0;
	// The index of the current location/pixel
	public int indexYXT=0;
	// Whether the current pixel position is out of range for the given distribution
	public boolean outofRange;
	// The value of the distribution 
	public float value=0;
	// The coordinate of the given position
	public Coordinate coord=null;
	// Area in square metres of the given cell position
	public float area =0;
	// Volume of the given cell position
	public float volume=0;
	// The sum of all values in this pixel position including guests if they have the same unit
	public float sumValue=0;
	// Biomass, if BiomassDistribution
	public float biomass=0;
	// A string reporting the biomass
	private String biomassString="";

	// If the current pixel position is land
	public boolean	isLand=false;
	//  Was values calculated
	public boolean wasCalculated;
	// Just to be sure
	private static float million=1000000f;
	
	
	/**
	 * Constructor
	 * @param distribution The distribution to operate on
	 */
	public PixelInfo(DistPanel panel,VisualDistribution distribution) {
		this.distribution=distribution;
		this.panel= panel;
	}
	


	/**
	 * Gives the index within the distribution at the given position.
	 * The screen x position corresponds to y axis within the distribution
	 * @param p the position on the screen
	 * @return a point representing the index location within the distribution
	 */
	public Point yxLocationAtPoint(Point p ) {
		float scale= panel.getScale();
		// Offset for the start of the distribution on the screen
		int dx= (int) distribution.getPixelRegion().x ;
		int dy= (int) distribution.getPixelRegion().y ;
		// This is the positions within the distribution!
		int actualY= (int) (1.0f*((p.x-dx)/scale) ); 
		int actualX= (int) (1.0f*(p.y-dy)/scale);
		
		actualY= Math.max(0,Math.min(actualY, distribution.getHeight()-1));
		actualX= Math.max(0,Math.min(actualX, distribution.getWidth()-1));
		
		if (distribution.isMirror()) {
			actualY = distribution.getHeight()-actualY-1 ;
			actualX = distribution.getWidth()-actualX-1 ;
		}
		
//		System.out.println("Point calculated:"+actualX+" y:"+actualY);
		
		return new Point(actualX,actualY);
	}


	
	/**
	 * Explorers the current pixel position and calculates values
	 * @param p the pixel position to explore
	 * @param panel the panel we are operating on
	 */
	public void explore(Point p,boolean mirror){
		// Make sure that static distribution like topography does not use time count
		int count= 	 (distribution.isHasTime()) ? panel.getCount(): 0;
		// Offset for the start of the distribution on the screen
		int dx= (int) distribution.getPixelRegion().x ;
		int dy= (int) distribution.getPixelRegion().y ;
		// x is simply along the height of the distribution // A bug fixed here at Mars 26
		// This is the screen! positions within the distribution! //I think screen!!•
		// we find this screen X position which correspond to y position in distribution…
		x= (int) (1.0f*((p.x-dx)/panel.getScale()) ); 
		y= (int) (1.0f*(p.y-dy)/panel.getScale());

		//••«•»• offset is already taken care of in the pixel region?
		// it should be: y= (int) (1.0f*(p.y-dy)/scale, dy= distribution.getPixelRegion().x ; ?
		
		//«•» Rotate: This is where we have to rotate distribution on screen if it is being flipped
		if (mirror) { //We are still in screen space!!!!!!  
			x=distribution.getHeight()-x-1;
			y=distribution.getWidth()-y-1;
		}
		
		// This is the XY location in the distribution, opposite of screen
		yxLocation.setLocation(y, x);

		int wh= distribution.getWH();
		int width=distribution.getWidth(); 
		int height=distribution.getHeight();
		
		// This is a bit confusing, especially the indexing! •y is opposite along the  width of distribution
		// We want the index now within the distribution as well as the fixed geographical index
		 indexYXT= y +(x)*(width)+count*wh;
		 indexYX=  y +(x)*(width);	// this index is fixed regardless of time
		 //
		 indexYXT= Math.min(distribution.getValues().length-1 , indexYXT);
		 indexYXT= Math.max(indexYXT, 0);
		 indexYX= Math.min(wh-1, indexYX);
		 indexYX= Math.max(indexYX, 0);
		 
//		 System.out.println("indexYX: "+indexYX);
		
		 wasCalculated=false;
		 
		 outofRange  = (x<0 || y<0 || x>=height || y>=width);
		
		 // Calculations only makes sense within valid ranges
		 if (!outofRange &&  indexYXT>0) {
			 calculateValues();
			 wasCalculated=true;
		 }
		 
	}


	/**
	 * @param panel
	 */
	private void calculateValues() {
		// First calculate the total value in this location
		 value= distribution.getValues()[indexYXT];
		 sumValue=value;
		 for (VisualDistribution guest: distribution.getGuestList()) {
			 if (distribution.getUnit().matches(guest.getUnit())) {
				 sumValue+= guest.getValues()[(guest.isHasTime()) ? indexYXT: indexYX];
	 }
		 }
		 // Calculate values
		coord=   distribution.getCoordinates()[indexYX];
		area= distribution.getArea()[indexYX]/million ; //1 km^2 =million m^2: result in km^2
		isLand= (value==distribution.getFillV());
		//Volume
		VisualTopography topo= panel.getTopography();
		if (topo!=null) {
		volume= area * topo.getValueAtCoordinate(coord)*(-0.001f); //cubic kilometres km3
		}
		// Biomass : 
		String unit=  distribution.getUnit();
		biomassString="";
		biomass=0;
		if (!isLand && area>0  && unit.contains("C m-2")){
			float factor= (unit.matches("ug C m-2") ) ? 0.001f : 1f;
			biomass= area*million*value * 0.001f*factor; //gram to kg, area back to square metres
			biomassString= String.format("Biomass: %.1f kgC",  biomass);
			biomassString= (biomass>10000) ? String.format("Biomass: %.1e kgC", biomass) : biomassString;
		}
	}

	
	/*
	 * @param gPanel
	 * @param coo
	 * @param area
	 * @return
	 */
	public String generateCoordinateDepthString( VisualTopography topography) {
		// Coordinate
		String		strCoo =coord.toString();
		// Depth from topography
		if (topography!=null) {
		if (topography.isOk) {
		String  depth= String.format("  |  Depth: %.1f m", topography.getValueAtCoordinate(coord));
		strCoo+=depth;
		}
		}
				
		return strCoo;
	}

	/**
	 * @param strCoo
	 * @return
	 */
	public String generateAreaVolumeString( ) {
		// Area
		String areaInfo= String.format("Area: %.1f Km^2", area);
		// Volume
		String volumeInfo= String.format("  Volume: %.1f Km^3", volume);
		areaInfo+=volumeInfo;
		return areaInfo;
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
	 * @return the biomassString
	 */
	public String getBiomassString() {
		return biomassString;
	}

	

}
