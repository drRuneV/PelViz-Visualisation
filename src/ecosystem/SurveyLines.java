package ecosystem;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import NetCdf.NetCDFInspector;
import pointDistribution.GeoPosition;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class SurveyLines {

	// Coordinates and clock of the survey
	Coordinate[] coordinates;
	Clock[] clock;
//	GeoPosition[] geoPositions= null;
	// Indexes to where the coordinates are in YX space
	int indexs[]= null;
	// The coordinate grid following the yx-space of the distribution
	// This is used to find out where the coordinates of the survey are
	private Coordinate[] coordinateGrid= null;
	private Dimension dim;
	// When : the current time state of the survey line, determines which position we are at
	// A global clock should tell survey lines  when, and the drawing routine should draw accordingly
	private Clock when;
	// 			
	// The times which are being read from the file, which can mean?
	double[] times;
	// The number of positions
	int length=0;
	
	// NetCDF file
	NetcdfFile ncfile = null;
	// Values as they are read from a underlying distribution
	float[] bioValues =null;
	// Interact
	public ArrayList<Coordinate> measureCoordinate= new ArrayList<>();
	// 
	private boolean isItFinished=false;

	// The image the survey lines are drawn upon
	BufferedImage image= null;
	// The background colour that fills the entire image when cleared
	Color background= new Color(255, 255, 255, 0);
	private int scale=1;
	ArrayList<Point> point= new ArrayList<>();
	private int time=0;
	//  The graphic drawing surface from the image
	private Graphics2D graphic;
	// SurveyLines color, colour2 is for large distances
	private Color color= new Color(60,50,50, 200);
	private Color color2= new Color(90,90,99, 80);
	// The colouring of values indicated
	private Color valueColour= new Color(200, 50, 50, 10);
	// The maximum value of the values used
	private float maxValue=0;
	// Should values be drawn?
	private boolean showValues= true ;
	//
	private boolean mirror=false;
	
	
	/**
	 * Constructor
	 * @param filename
	 */
	public SurveyLines( String filename, Coordinate[] coordinateGrid) {
		
		this.coordinateGrid= coordinateGrid; 
		
		try {
			
			ncfile =NetcdfFile.open(filename);
			if (ncfile!=null ){
				String[] latlon= {"lati","long"}; //  specific names starting with… 
			generateCoordinates(latlon);
			length= coordinates.length;
			indexs=generateCoordinateIndexs(coordinateGrid);
			

//			• displaySomeCoordinates();
			//  dates
			generateDates();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	

	
	private void displaySomeCoordinates(){
		for (int i = 0; i < length; i++) {
			if (i%1000==0) {
				System.out.println( i+": "+coordinates[i].toString());
			}
		}
	}


	/**
	 * Generates the dates
	 */
	private void generateDates(){
		Variable variableTime= ncfile.findVariable("time");
		Array dataDate;
		
		
		try {
			dataDate= variableTime.read();
			int size = (int) dataDate.getSize();
			times= new double[size];
			//			dateStrings= new String[size];
			// For each integer "hour since 1950", define a date
			// Assume that the date is based on 1970
			for (int i = 0; i < size; i++) {
				int hour1970 = dataDate.getInt(i);
				double d=dataDate.getDouble(i);
				int remainingHour= (int) ((d-hour1970)*24);
				times[i]= d;
				//				dateStrings[i]= Clock.createADate(hour1950);
				if (i%1000==0) {
//					System.out.println(i+" : "+d+"  maybe: "+ Clock.createADate(hour1970-Clock.daysBetween1950And1970*24));
//					System.out.println("Remaining hour: "+remainingHour);
				}
			}
			System.out.println("Read Survey line times from the file " + size);


		} catch (IOException e) {
			e.printStackTrace();
		}


	}
	
	
	
	/**
	 * Generates Coordinate's by reading from the netCDF file
	 * @param latlong 2 strings representing the names of latitude and longitude
	 */
	private void generateCoordinates(String[] latlong) {

		Variable varlat =null;		
		Variable varlon =null;
		List<Variable> variable= ncfile.getVariables();
		
		//  Any  pair of variable starting with lat & lon qualifies
		for (Variable var : variable) {
			int d=var.getShape().length; // expect one-dimensional variables!
			// latitude
			if (varlat==null && d==1 && var.getFullName().toLowerCase().startsWith(latlong[0])) {
				varlat= var;
				System.out.println("Found variable: "+var.getFullName()+"  size:"+var.getSize());
			}
			// longitude
			if (varlon==null &&   d==1 && var.getFullName().toLowerCase().startsWith(latlong[1])) {
				varlon= var;
				System.out.println("Found variable: "+var.getFullName()+"  size:"+var.getSize());
			}
			
			// System.out.println(var.getFullName()+"  size:"+var.getSize());
		}

		// Generate coordinates
		if (varlon!=null && varlat!=null) {
			
			// geoPositions.coordinate=
			coordinates=	readCoordinates(ncfile, varlat, varlon);
			System.out.println(coordinates.length+" Survey line coordinate generated");
		}
		
		
	}

	
	
	/**
	 * Read the coordinator variables from the netCDF file
	 * @param nc
	 * @param lat
	 * @param lon
	 * @return
	 */
	private Coordinate[] readCoordinates(NetcdfFile nc, Variable lat, Variable lon) {

		float scalef = 1;//findScaleFactor(lat);
		Coordinate[] coo= null;
		
		//NetCDF array
		Array datalat;
		Array datalon;
		try {
			// Get all the data of latitude and longitude
			datalat = lat.read();
			datalon=  lon.read();
			//Latitude and longitude's have the same length =width*height
			int dataSize = (int) datalon.getSize();
			coo = new Coordinate[dataSize]; //width*height
			
			for (int i = 0; i < dataSize; i++) {
				coo[i]= new Coordinate(datalat.getFloat(i)*scalef,datalon.getFloat(i)*scalef);
			}

			
			System.out.println("Generated "+dataSize+" Coordinates");
		}

		catch (IOException e) {
			e.printStackTrace();
		}
		return coo;
	}

	

	/**
	 * Generate the indexes to the XY positions of coordinates in the coordinate grid
	 * @param coordinateGrid
	 * @return
	 */
	private int[] generateCoordinateIndexs(Coordinate[] coordinateGrid){
		int[] idx= new int[length];
		
		for (int i = 0; i < idx.length; i++) {
			int ix= coordinates[i].findClosestIndex(coordinateGrid, 0.9f);
			idx[i]=ix;
		}
		return idx;
	}
			

	/**
	 * Inserts values from a distribution along the surveyLine
	 * @param data
	 * @param except
	 */
public void insertValues(float[] data,float except, int wh){
		int maxIndex = data.length ;
		bioValues= new float[indexs.length];
		int ix =0;
		maxValue=0;
		
		
		int chop= Math.max(indexs.length/(maxIndex/wh),1); //  Number of lines per timestep
		

		//  Later we have to synchronise this with time, to find the correct value in the data
		int t= (int) (maxIndex/wh)/2 ;// just test for half in the dataset
		int timeindex= t*wh;
//		System.out.println("timeindex: "+timeindex+" #index:"+indexs.length+"");
		
		// Fill values with the data corresponding to indexes 
		for (int i = 0; i < indexs.length; i++) {
			timeindex=(i/chop)*wh;
//			System.out.println("time="+(i/chop));
			ix = indexs[i]+  timeindex ;
			if (ix<maxIndex) {
				bioValues[i]= (data[ix] == except) ? 0 : data[ix];
				maxValue= (bioValues[i]>maxValue) ? bioValues[i]: maxValue ;
//				if (values[i]>0) {
//					System.out.println("ix:"+ix +" value:"+values[i]);
//				}
			}
		}	
		System.out.println("Maximum value found: "+maxValue);
	}

	
	/**
	 * Resets the graphic drawing surface, remove all points in graphic space, 
	 * Reconnect the points and plot again in the new image
	 */
	private void resetWhenRescaled() {
		
		// Clear the entire image to fully transparent
		graphic = image.createGraphics();
		graphic.setBackground(background);
		graphic.setColor(color);
		graphic.clearRect(0, 0, image.getWidth(), image.getHeight());
	
		// Recreate and put all the points in the new image
		point.clear();
		time=collectPoints(0,time);
		plotLine(1, time);
	}

	/**
	 * Triggers redrawing of the survey lines on its image when something has changed.
	 */
	public void redrawEverything(){
		if (image!=null) {
		resetWhenRescaled();
		}
	}


	/**
	 * Draws the survey line. This is the method called for drawing but no drawings actually done here.
	 * Drawing is done in plotline.
	 * @param sc the scale the scale of drawing needed for scaling the image right
	 * @param countExtra the extra time step we are going to draw for starting with the current time step
	 * @param width the width of the image
	 * @param height the height of the image
	 */
public void draw(float sc, int countExtra,int width,int height){

	boolean scaleChanged = (sc!=this.scale) ;

	// Create and recreate image and graphic both first time and when rescaling.
	// We are drawing in the real scale, therefore if the scale has changed we have to recreate the image.
	if (image==null || scaleChanged) {	
		redefine(sc, width, height);
	}
	
	// If animation has moved back in time, we need to clear the graphics, recollect and plot accordingly
	if (countExtra<time) {
		graphic.clearRect(0, 0, image.getWidth(), image.getHeight());
		point.clear();
		time=collectPoints(0,countExtra);
		plotLine(1, time);	
		System.out.println("Plotted surveyLines: "+countExtra);
	}

	//  When we start at time zero, we reset and clear the graphics; no need to plot anything
	if (countExtra==0) {
		time=0;
		point.clear();
		graphic.clearRect(0, 0, image.getWidth(), image.getHeight());
		System.out.println("clearing the points "+point.size());
	}

	// Restrict collecting points and plotting to the length of the data 
	int maxtime=Math.min(length, countExtra);
	//  Collect new points(n) from the current time step until…
	int n= collectPoints(time,maxtime);
	
//	System.out.println("Plotted surveyLines: "+n+" "+countExtra);

	
	//		if(time>0){
	//			System.out.println(" Added "+n +" points"+" Last point was: "+point.get(point.size()-1).toString()
	//					+" "+point.size());
	//		}

	//Loop all the positions, and plot a survey line
	if(time>0){		
		plotLine(time,n);
		//  «•» Special case: plot everything !	«•»!!«•»
		if (measureCoordinate.size()>1) {
//			plotLine(1, measureCoordinate.size());
		}
	}
	// The time counter is now at the end
	time= point.size();

}




	/**
	 * @param sc
	 * @param width
	 * @param height
	 */
	private void redefine(float sc, int width, int height) {
		this.scale= (int) sc;
		//  The image were going to draw upon
		image = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
		//  Dimension following distribution-yx and the coordinate grid
		dim= new Dimension((int)(height/sc), (int)(width/sc));
		// Reset graphics and the points collected 
		resetWhenRescaled();
	}




	/**
	 * Collect points between 2 time steps
	 * @param from beginning of time
	 * @param to end of time
	 * @return the number of points collected
	 */
	private int collectPoints(int from,int to) {
		int n=0;
		int nMissed=0;
		//For each position
		for (int i = from; i < to; i++) {
			int index= indexs[i];//coordinates[i].findClosestIndex(coordinateGrid, 0.9f);
//			System.out.println(" -> add points "+i+" index: "+index+" coordinate "+ coordinates[i]);
			// Do not collect points if missed
			if (index<0) {
				nMissed++;
				continue;
			}

			Point screen = findPoint(index);
			// Add points 
			point.add(screen);
			n++;
		}
//		System.out.println(" Missed:"+nMissed);
		
		return n;
	}




	/**
	 * Finds a screen pixel point corresponding to the index in a YX grid
	 * @param index the index within the distribution
	 * @return a graphical point within the image representing a distribution on screen
	 */
	private Point findPoint(int index) {
		
		Point screen= new Point();
		int y = index/dim.width;//the actual y value
		int x = index- y*dim.width; //the actual X value
		//  «•» Rotate: if the distribution is flipped we need to rotate xy here!!  «•»
		if (mirror) {
			x= dim.width-x-1;
			y= dim.height-y-1;
		}
		screen.x= (int) (y)*scale;
		screen.y= (int) (x)*scale;
		return screen;
	}


	/**
	 * 
	 * @param from
	 * @param n
	 */
	private void plotLine(int from,int n) {
		double distance=0;
				
		// Drawing time step from ->from+n	// «•» this will only enter if n>1
		for (int i = from; i < from+n-1;  i++) {
			int x= point.get(i-1).x;
			int x1=point.get(i).x;
			int y= point.get(i-1).y;
			int y1=point.get(i).y;

			// Make sure we are inside the image
//			if (x>0 && y>0  && x<image.getWidth() && y<image.getHeight()) {

				//  Indicate beginning
				if (i==0) {
					graphic.drawString("Start", x,	y);
				}
				// Line
				// Using more transparent colours if distance is large…
				distance= point.get(i).distance(point.get(i-1));
				if (distance>5*scale) {
					graphic.setColor(color2);
				}
				else{
					graphic.setColor(color);
				}		
				// Actual drawing here
				graphic.drawLine(x, y, x1, y1);
				
				//  •  Indicate density values along the survey line
				if (bioValues!=null && showValues) {
					if (i<bioValues.length) {
						double v=bioValues[i];	
						if (v>0) {
							int r= (int) ((v<1) ? 1: 5*scale*v/maxValue);// (int) Math.log(v*2)+scale;
							graphic.setColor(valueColour);
							graphic.fillOval(x-r, y-r, r*2, r*2);
						}
					}
				}
				
				// Last
				if (i==length-1) {
					graphic.drawString(String.format("%d", i), x-10, y);
				}
//				System.out.println("Drawing line p nr "+i);
			}
//			else 	System.out.println("x:y "+x+" "+y);

//		}
	}

	/**
	 * 
	 * @param c
	 */
	public void measure(Coordinate c){

		// 1st measure
		if (measureCoordinate.isEmpty()) {
			measureCoordinate.add(c);
			System.out.println(" Adding 1st point of survey line !");
			System.out.println("Inserted coordinate: "+c.toString()+" "+ length);
		}
		// Interpolating
		else{
			System.out.println(" Interpolating");
			interpolate(c,99);					
			setLength(measureCoordinate.size());
			System.out.println(" length of the survey line measurement is:"+length);
		}
		
		length= measureCoordinate.size();
		
		//•We also need to transfer the coordinates to the coordinate array!
		coordinates= new Coordinate[length];
		for (int i = 0; i < coordinates.length; i++) {
			coordinates[i]= new Coordinate( measureCoordinate.get(i) );
		}
		
		//
		indexs=generateCoordinateIndexs(coordinateGrid);
		
		
//		if (measureCoordinate.size()>0) {
//		surveyLine.drawInteractiveToScreen(panelGraphics);
//		}
		
	}

	  
	
	public void drawInteractiveToScreen(Graphics2D gPanel) {	
//		pFrom=screenPositionFromIndex(poly,  i);
//		pTo=  screenPositionFromIndex(poly,  k);
//		gPanel.drawLine(pFrom.x,pFrom.y,pTo.x,pTo.y);
//		gPanel.drawOval(pFrom.x-r, pFrom.y-r, r*2, r*2);
		
		
		Coordinate[] c= new Coordinate[measureCoordinate.size()];
		for (int i = 0; i < measureCoordinate.size(); i++) {
			Coordinate cm= measureCoordinate.get(i);
			c[i] = new Coordinate(cm);
		}
		coordinates=c;

	}

	/**
	 * Interpolate between the last 2 coordinates.
	 * 
	 * @param n
	 */
	public void interpolate(Coordinate cClicked,int n){

		if (length>0) {
			Coordinate c1= measureCoordinate.get(length-1);

			Coordinate[] c= Coordinate.interpolateBetween(c1, cClicked, n);

//			measureCoordinate.remove(cClicked);
			for (int i = 0; i < c.length; i++) {
				measureCoordinate.add(c[i]);
			}
//				System.out.println("Adding coordinate: "+c[ito].toString());
		}
	}

	
	/**
	 * @return the clock
	 */
	public Clock[] getClock() {
		return clock;
	}




	/**
	 * @param clock the clock to set
	 */
	public void setClock(Clock[] clock) {
		this.clock = clock;
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
	 * @return the length
	 */
	public int getLength() {
		return length;
	}




	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}




	/**
	 * @return the values
	 */
	public float[] getValues() {
		return bioValues;
	}




	/**
	 * @param values the values to set
	 */
	public void setValues(float[] values) {
		this.bioValues = values;
	}

	

	/**
	 * @return the isItFinished
	 */
	public boolean isItFinished() {
		return isItFinished;
	}




	/**
	 * @param isItFinished the isItFinished to set
	 */
	public void setItFinished(boolean isItFinished) {
		this.isItFinished = isItFinished;
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

