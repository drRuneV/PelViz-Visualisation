package ecosystem;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.jfree.data.xy.XYSeries;

import interaction.LineGraph;
import visualised.VisualDistribution;

public class TransectLine {


	// Coordinates 
	Coordinate[] coordinates= null;
	// Interact
	public ArrayList<Coordinate> measureCoordinate= new ArrayList<>();
	
	// Values as they are read from a underlying distribution
	float[] bioValues =null;
	
	// The coordinate grid following the yx-space of the distribution
	// This is used to find out where the coordinates of the survey are
	private Coordinate[] coordinateGrid= null;
	private Dimension dim= null;

	// Indexes to where the coordinates are in YX space
	int indexs[]= null;


	// The maximum value of the values used
	private float maxValue=0;

	//
	LineGraph graph=null;
	//
	boolean wasCreated=false;
	// The image the survey lines are drawn upon
	BufferedImage image= null;
	// The graphic drawing surface from the image 
	private Graphics2D graphic= null;
	
	private int scale=1;
	ArrayList<Point> point= new ArrayList<>();
	
	
	//VisualDistribution
	VisualDistribution distribution= null;
	//
	private boolean mirror=false;
	//
	private String xTitle="";
	


	// // // 
	// Could take the average across all time steps, as an option ?
	//
	//
	
	/**
	 * 
	 * Constructor
	 * @param cFrom
	 * @param cTo
	 * @param distribution
	 */
	public TransectLine(Coordinate  cFrom, Coordinate cTo, VisualDistribution distribution) {
		this.distribution= distribution;
		this.coordinateGrid= distribution.getCoordinates();


		// 1st measure
		measureCoordinate.add(cFrom);
		//Define the transect
		stretchToCoordinate(cTo);
		
		//
		xTitle= " "+cFrom.toString()+"  ->  "+ cTo.toString();
		
		// Create the graph first time
		createGraph();
		//
		redefineImage(1);
		
//		for (Coordinate coordinate : coordinates) {
//			System.out.println(coordinate.toString());
//		}
//		
	}




	/**
	 * @param cFrom
	 * @param cTo
	 */
	public void stretchToCoordinate(Coordinate cTo) {
		interpolate(cTo,99);
		xTitle= " "+measureCoordinate.get(0).toString()+"  ->  "+ cTo.toString();
		
		//•We also need to transfer the coordinates to the coordinate array!
		transferCoordinates();
		indexs=generateCoordinateIndexs(coordinateGrid);
	}




	/**
	 * Receives a coordinates to put into the array
	 * @param c The received coordinate
	 */
	public void receiveCoordinate(Coordinate c) {
		int size= measureCoordinate.size();


		// Insert several coordinates by interpolating
		if ( (size==1  )) { //))||  size%100==0) && size>0){
			stretchToCoordinate(c);
			System.out.println(" Interpolating: -> "+measureCoordinate.size());
			// Redefine
			redefineImage(scale);
			//Generate all the points
			generatePoints(0, coordinates.length);
			plotLine(1, coordinates.length);

			createGraph();
		}
		// When there are several coordinates we start all over 
		// by clearing all coordinates and inserting the first
		else {
			measureCoordinate.clear();
			measureCoordinate.add(c);
			
			System.out.println("transectLine length:  "+measureCoordinate.size());
		}
		
//		System.out.println("transectLine received a coordinate: "+c.toString());
	}




	/**
	 * Updates the graph/line plot with a given distribution and time step.
	 * Guest distributions are used as well, which results in a multiple line plot updating with time.
	 * Each time we have to check if new guests has been added to the graph.
	 * The frame which the graph is drawn upon will be created the first time when update is called.
	 * This method ensures that the first time the graphics created the location of the graph/frame is defined.
	 * 
	 * @param dis the distribution to use
	 * @param t the current timestamp
	 */
	public void updateGraph(VisualDistribution dis,int t) {

		// If the current distribution has changed
		if (dis!=null) {
			this.distribution=dis;
		}

		// Title
		int h= distribution.getDates()[t];
		String title=distribution.getFullName()+" "+ Clock.dayMonthYearString(h);//distribution.getDateStrings()[t];
		// Updates the values given the current time
		bioValues=insertBioValues(distribution.getValues(),t);
		
		
		// Check if guests has been added
		if (distribution.getGuestList().size()> graph.getSeries().getSeriesCount()-1) {
			addGuestAsSeries();
		}
		
		
		// Update the graph dynamically			
		graph.update(bioValues, title,0);
		// 
		// Position the frame nicely 
		if (graph.getFrame()!=null   && wasCreated) {
			graph.getFrame().setLocation(distribution.pixelAbsoluteUpperLeft());//
			if (wasCreated) {
				graph.getFrame().setVisible(true);
			}
			wasCreated=false;
		}

		// Guest distribution
		for (int i = 0; i < distribution.getGuestList().size(); i++) {
			VisualDistribution  vd= distribution.getGuestList().get(i);
			graph.update(insertBioValues(vd.getValues(), t), "", i+1);
		}
		
	}




	/**
	 * Creates the line graph.
	 *  This graph is meant to represent biomass values along the transect line
	 * @return a title string for the graph.
	 */
	private String createGraph() {

		// Updates the values given the current time
		bioValues=insertBioValues(distribution.getValues(),0);

		String title=distribution.getFullName();

		// Create new frame for the graph
		if(graph!=null){
			if (graph.getFrame()!=null) {
				if (graph.getFrame().isVisible()) {
					graph.getFrame().dispose();
				}
			}
		}
		
		graph= new LineGraph(bioValues, title,xTitle, distribution.getUnit());
		graph.defineTheAxis(distribution.getStatistics().getAverage()*5);
		addGuestAsSeries();
		wasCreated=true;

		return title;
	}




	/**
	 * Recalculate when distribution or the scale changes
	 * @param dis The new current distribution
	 * @param scale the new scale
	 */
	public void recalculate(VisualDistribution dis,float scale){
		distribution= dis;
		
		redefineImage(scale);
					
		//Generate all the points
		generatePoints(0, coordinates.length);
		plotLine(1, coordinates.length);
		
		// Reset all series
//		createGraph();
		// Main series	
		//.setKey(distribution.getFullName());
		graph.serie= new XYSeries(distribution.getFullName());

		graph.getSeries().removeAllSeries();
		graph.getSeries().addSeries(graph.getSerie());
		addGuestAsSeries();
		
		
	}




	/**
	 * 
	 */
	private void addGuestAsSeries() {
		// Guest distribution
		for (int i = 0; i < distribution.getGuestList().size(); i++) {
			VisualDistribution  vd= distribution.getGuestList().get(i);
			graph.addSerie(insertBioValues(vd.getValues(), 0), vd.getFullName()+"_g"+i);
		}
	}


	

	/**
	 * Interpolate the last coordinate to a new coordinate by inserting several coordinates in between
	 * @param cClicked
	 * @param n
	 */	//maybe we should only interpolate the coordinate and keep the measure as to simple coordinates
	public void interpolate(Coordinate cClicked,int n){
		int length= measureCoordinate.size();

		if (length>0) {
			Coordinate c1= measureCoordinate.get(length-1);
			Coordinate[] c= Coordinate.interpolateBetween(c1, cClicked, n);
			
			// Need to clear the list 1st in order to get the interpolated 
//			measureCoordinate.clear();
			
			// Add the interpolated coordinates
			for (int i = 0; i < c.length; i++) {
				measureCoordinate.add(c[i]);
			}
		}
		
	}




	/**
	 * Transfer the measured coordinates to the coordinate list
	 */
	private void transferCoordinates() {
		int length= measureCoordinate.size();
		
		coordinates= new Coordinate[length];
		for (int i = 0; i < coordinates.length; i++) {
			coordinates[i]= new Coordinate( measureCoordinate.get(i) );
		}
	}


	/**
	 * Generate the indexes to the XY positions of coordinates in the coordinate grid
	 * @param coordinateGrid
	 * @return
	 */
	private int[] generateCoordinateIndexs(Coordinate[] coordinateGrid){
		int[] idx= new int[coordinates.length];
		
		for (int i = 0; i < idx.length; i++) {
			int ix= coordinates[i].findClosestIndex(coordinateGrid, 0.9f);
			idx[i]=ix;
		}
		return idx;
	}
	

	/**
	 * 
	 * @param data
	 * @param t
	 */
	public float[] insertBioValues(float[] data,int  t){
		int maxIndex = data.length ;
		float[] values= new float[indexs.length];
		int ix =0;
//		maxValue=0;

		float except=distribution.getFillV();
		int timeindex= t*distribution.getWH();

		// Fill values with the data corresponding to indexes 
		for (int i = 0; i < indexs.length; i++) {
			ix = indexs[i]+  timeindex ;
			if ( ix>-1 && ix<maxIndex) {
				values[i]= (data[ix] == except) ? 0 : data[ix];
//				maxValue= (values[i]>maxValue) ? values[i]: maxValue ;
			}
		}	
//				System.out.println("Maximum bio value found: "+maxValue);
		return values;
	}
	
	
	/**
	 * Draw the transect line on the given scale on a image
	 * @param sc
	 */
	public void draw(float sc){
	
		boolean scaleChanged = (sc!=this.scale) ;

		// Create and recreate image and graphic both first time and when rescaling.
		// We are drawing in the real scale, therefore if the scale has changed we have to recreate the image.
		if (image==null || scaleChanged) {
			redefineImage(sc);
			//Generate all the points
			generatePoints(0, coordinates.length);

//			System.out.println("Redefining transectLine…"+coordinates.length+" coordinates");
		}

		clearGraphics();
		// 
		if (measureCoordinate.size()>0) {
			plotLine(1, coordinates.length);
		}

	}

	/**
	 * Redefines the image with the dimensionality where the transect line is being drawn.
	 * @param sc
	 */
	private void redefineImage(float sc) {
		this.scale= (int) sc;
		
		//Image dimension
		int width= (int) (distribution.getImage().getWidth()*scale); 
		int height= (int) (distribution.getImage().getHeight()*scale);

		//  The image were going to draw upon
		image = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
		//  Dimension following distribution-yx and the coordinate grid
		dim= new Dimension((int)(height/scale), (int)(width/scale));
		// Reset graphics and the points collected 
		
		clearGraphics();
	
	}




	/**
	 * Clears the graphics of the image, with a transparent colour.
	 */
	private void clearGraphics() {
		// Clear the entire image to fully transparent
		Color background= new Color(255, 255, 250, 0);
		graphic = image.createGraphics();
		graphic.setBackground(background);
//		graphic.setColor(background);
		graphic.clearRect(0, 0, image.getWidth(), image.getHeight());
		graphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphic.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		
	}



	
	/**
	 * Generate points within a range of indexes.
	 * The points are pixel points in the image the distribution is using.
	 * @param from index from
	 * @param to index to
	 * @return the number of points generated
	 */
	private int generatePoints(int from,int to) {
		int n=0;
		int nMissed=0;
		//
		point.clear();
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
	 * Plots the transect line on the graphic together with value indicator as filled circles. 
	 * @param from from index
	 * @param n number of points to plot
	 */
	private void plotLine(int from,int n) {
//		double distance=0;
				
		Color color= new Color(60,50,70, 250);
		Color valueColour= new Color(200, 50, 50, 10);
		
		maxValue= (float) graph.serie.getMaxY();

		//  Indicate first
		if (measureCoordinate.size()==1){
			drawFirstPoint(measureCoordinate.get(0));
		}
		
		//
		int to= Math.min(point.size()-1, from+n-1);
		
		
		// Drawing time step from ->from+n	// «•» this will only enter if n>1
		for (int i = from; i < to;  i++) {
			int x= point.get(i-1).x;
			int x1=point.get(i).x;
			int y= point.get(i-1).y;
			int y1=point.get(i).y;

				// Line
				graphic.setColor(color);
				// Actual drawing here
				graphic.drawLine(x, y, x1, y1);
 				// Indicate endpoints  
				if (i==from || i==(to-1)) {
					drawCircle(i);
//					System.out.println("Endpoints: :"+i);
				}
				
				//  •  Indicate density values along the survey line
				if (bioValues!=null ) {
					if (i<bioValues.length) {
						double v=bioValues[i];	
						if (v>0) {
							int r= (int) ((v<1) ? 1: 5*scale*v/maxValue);// (int) Math.log(v*2)+scale;
							r= Math.min(r, 50);
							graphic.setColor(valueColour);
							graphic.fillOval(x-r, y-r, r*2, r*2);
						}
					}
				}
//				System.out.println("Drawing line p nr "+i);
			}
//			else 	System.out.println("x:y "+x+" "+y);

//		}
	}




	/**
	 * Draws a circle indicating a specific point in the transect, typical start and end points
	 * @param i the index of the point to indicate with a circle
	 */
	public void drawCircle(int i) {
		int x1=point.get(i).x;
		int y1=point.get(i).y;
		
		int r=2*scale;
		r= Math.min(r, 9);
		graphic.drawOval(x1-r , y1-r , r *2, r *2);
		r=scale;
		r= Math.min(r, 5);
		graphic.drawOval(x1-r , y1-r , r *2, r *2);
		// 
//		System.out.println("Drawing circle to indicate.....,");
	}

	
	public void drawFirstPoint(Coordinate c){
		int ix= c.findClosestIndex(coordinateGrid, 0.9f);
		Point p= findPoint(ix);
		// 
		int r=2*scale;
		r= Math.min(r, 9);
		graphic.drawOval(p.x-r , p.y-r , r *2, r *2);
		
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
	 * @return the graph
	 */
	public LineGraph getGraph() {
		return graph;
	}




	/**
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
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




	/**
	 * @return the wasCreated
	 */
	public boolean isWasCreated() {
		return wasCreated;
	}




	/**
	 * @param wasCreated the wasCreated to set
	 */
	public void setWasCreated(boolean wasCreated) {
		this.wasCreated = wasCreated;
	}




}
