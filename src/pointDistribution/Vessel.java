package pointDistribution;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import NetCdf.NetCDFInspector;
import ecosystem.Clock;
import ecosystem.Coordinate;
import graphics.GraphicsUtil;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;


/**
 * The vessel class is a vessel/boat/ship with a location in latitude longitude and date/time
 * and a list of positions with coordinates and dates and possible variables. 
 * A fishing vessel may have several associated variables for each position.
 * A survey vessel for an acoustic survey may have different variables.
 * 
 * @author Rune Vabø
 *
 */
public class Vessel {
	//
	private NetcdfFile ncfile = null;
	// The name of the vessel
	private String name="Ship";
	// Vessel number
	int id=0;
	// The number of positions/locations; corresponding to the number of time steps
	private int number;
	// The current position/count
	private int count=0;
	//
	private Coordinate  at;
	// When
	private Clock when;
	// The geographical positions with both coordinates and clock. Should follow the time dimension
	private GeoPosition positions[]= null;

	// a list of variables 
	private ArrayList<GeoVariable> variables= new ArrayList<>();
	// Dimension of the underlying grid
	Dimension dim;
	//
	BufferedImage image= null;
	Color background= new Color(255, 255, 255, 0);
	private Color color= Color.black;
	private Coordinate[] coordinateGrid= null;
	private int scale=1;
	
	// ================================================================================
	
	/**
	 * 
	 */
	class GeoVariable{
		
		private float[] values;
		private String name;
		private String unit;
		
		
		/**
		 * Constructor
		 * @param values
		 * @param name
		 * @param unit
		 */
		public GeoVariable(float[] values, String name, String unit) {
			this.values = values;
			this.name = name;
			this.unit = unit;
		}

		
		/**
		 * 
		 */
		public void  display(){
			String st= String.format(name+" Unit: "+unit );
			if (values!=null) {
				for (float f : values) {
					st+= " "+String.format("%.2f", f);
				}
			}
			System.out.println(st);
		}
		
		
		
		// ==================================================
		
		public float getValueNumber(int n){
			return values[n];
		}

		/**
		 * @return the values
		 */
		public float[] getValues() {
			return values;
		}

		/**
		 * @param values the values to set
		 */
		public void setValues(float[] values) {
			this.values = values;
		}
		
		
	}
	
	// ================================================================================
	
	/**
	 * Constructor for a vessel
	 * @param name The name of the vessel/boat
	 */
	public Vessel(String name,int id,NetcdfFile ncfile) {
		this.name= name ;
		this.id= id;
		this.ncfile = ncfile;
		if (id< GraphicsUtil.fixedColours.length) {
			color=  GraphicsUtil.fixedColours[id];
		}
		else {
			color= new Color(0, 250, 0,150);//green;}
		}
	}
	
	

	

	/**
	 * Grabs the relevant data from the netCDF file. 
	 * @param filePath
	 */
	public void grabTheData(){
		
		NetCDFInspector inspector = new NetCDFInspector(ncfile);
		
		Variable vtime;
		String tString="time";
		// Check for the time variable specifically
		if (inspector.lookForVariable(ncfile,tString)==false) {
			System.out.println(" Cannot find the variable "+ tString+" in this file : ");
		}
		// If we have time continue…
		else{
		vtime= ncfile.findVariable(tString);
		number= (int) vtime.getSize();
		//ni.displayTime(time);// okay
		
		// Time variables as represented as hours since 1950
		int hours[]= inspector.generateDates(ncfile, vtime);

		// Coordinates
		coordinateGrid= generateCoordinateGrid();
		Coordinate coordinate[] =findCoordinates(inspector);
		// 
		// Generate Graphical positions with both coordinate and time
		int length= hours.length;
		positions=new GeoPosition[length];
		for (int i = 0; i < length; i++) {
			positions[i] =new GeoPosition(coordinate[i], hours[i]);
		}


		// Find all ship variables 
		ArrayList<Variable> shipVariable= findShipVariables();
		if (!shipVariable.isEmpty()) {
			

		//Then loop through each ship variable and  get the data using origin and size
		for (Variable shipv : shipVariable) {

			int shapeTime= (int) number ;//shipv.getShape(0);
			// The origin is zero in time and the current population in the population dimension
			// The size is the total time step but just one population
			int[] origin= new int[]{0,id}; //change the 2nd parameter, population, to get another boat
			int[] size= new int[]{shapeTime, 1} ;
			// Read the data and put it into one variable
			try {
				Array dataVariable = shipv.read(origin ,size);
				float[] values= new float[number];
				for (int j = 0; j < number; j++) {
					values[j] =dataVariable.getFloat(j);
				}
				//
				GeoVariable gv  = new GeoVariable(values, shipv.getFullName(), shipv.getUnitsString()) ;
				variables.add(gv);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			//			String result="";
			//			result= NCdumpW.toString(dataVariable);
			//			System.out.println(shipv.getFullName() +" "+result);
			catch (InvalidRangeException e) {
				e.printStackTrace();
			}

		}
		

		}


		}




	}



	/**
	 * 
	 */
	private void display() {
		for (GeoVariable geoVariable : variables) {
			geoVariable.display();
		}
	}
	
	
	/**
	 * Creates a dialogue for selecting specific variables
	 * @return
	 */
	public ArrayList<Variable> showVariableInFrame(){
		ArrayList<Variable> variables=null;
		
		
		
		return variables; 		
	}
	



	/**
	 * @param inspector
	 */
	private Coordinate[] findCoordinates(NetCDFInspector inspector) {
		Coordinate[] coo = null;

		// 
		int w= ncfile.findVariable("Latt").getShape()[1]; //assumes x-coordinate is number 1, y number 0
		Variable posx= ncfile.findVariable("xpos");
		Variable posy=ncfile.findVariable("ypos");
		// Both of the position variables need to exist

		// Create a coordinate grid first in order to find geographical coordinates
		if (posx !=null && posy !=null) {
			coo= inspector.findCoordinates(coordinateGrid, posx, posy,w,id);
			System.out.println("Distribution coordinate generated");
		}
		
		return coo;
	}



	/**
	 * Generates the coordinate grid
	 * @return an array of coordinates
	 */
	public Coordinate[] generateCoordinateGrid(){
		Coordinate[] coordinateGrid= null;
		
		// Specifically look for latitude longitude variables «•» , "lat" will not be found!
		Variable varlat= ncfile.findVariable("Latt");
		Variable varlon= ncfile.findVariable("Long");

		// Generate coordinates
		if (varlon!=null && varlat!=null) {
			coordinateGrid= NetCDFInspector.generateCoordinates(ncfile, varlat, varlon);
			dim= new Dimension(varlat.getShape(1), varlat.getShape(0)); // 
		}
	
	
		return coordinateGrid;
	}



	/**
	 * Look specifically for ship variables, that is variables which are two-dimensional in time and population
	 * 
	 * @return a array list of variables
	 */
	public ArrayList<Variable> findShipVariables(){
		ArrayList<Variable> variables= (ArrayList<Variable>) ncfile.getVariables();
		ArrayList<Variable> shipVariables= new ArrayList<>();
		int dimSize=0;
		
//		NetCDFInspector inspector= new NetCDFInspector(ncfile, filePath)
		String[] dimensionStrings= {"time","pop"}; 
		
		for (Variable variable : variables) {
			dimSize= variable.getDimensions().size();
			boolean ok = NetCDFInspector.hasDimension(dimensionStrings, variable);
			if (dimSize<3 && ok ) {
				shipVariables.add(variable);
			}
		}
		
		return shipVariables;
		
	}



	
	
	public void addPositions(GeoPosition pos[]){
		for (GeoPosition geoPosition : pos) {
			
		}
	}
	

	/**
	 * Adds a new variable
	 * @param variable
	 */
	public void insertVariable(GeoVariable variable){
		variables.add(variable); 
	}


	/**
	 * Draws the trajectory of a vessel until the current time counter 
	 * @param count the current time counter, index based
	 * @param sc the scale
	 */
	public void draw(int count, float sc){

		if (image==null || sc!=this.scale) {	//opposite	YX !
			this.scale= (int) sc;
			image = new BufferedImage(dim.height*scale,dim.width*scale, BufferedImage.TYPE_INT_ARGB);
		}
		//
		// Clear the entire image to fully transparent
		Graphics2D graphic = image.createGraphics();
		graphic.setBackground(background);
		graphic.clearRect(0, 0, image.getWidth(), image.getHeight());
		int n=0;

		ArrayList<Point> point= new ArrayList<>();
		//For each position
		for (GeoPosition geoPosition : positions) {
			Coordinate c= geoPosition.coordinate;
			int index=c.findClosestIndex(coordinateGrid, 0.05f);

			int y = index/dim.width;//the actual y value
			int x = index- y*dim.width; //the actual X value
			Point screen= new Point();
			screen.x= (int) (y)*scale;
			screen.y= (int) (x)*scale;
			n++;
			// Add points until count/time
			point.add(new Point(screen.x, screen.y));
			if (n>count) {
				break;
			}
		}
		//Trajectory
		graphic.setColor(color);
		graphic.setFont(new Font("Bodoni MT", Font.PLAIN, 10+scale));
		
		float ds=0;
		//		graphic.drawPolygon(polygon);
		int max= Math.min(point.size(),6);
		int r= (int) Math.random()*(20+scale) ;
		// Loop all the positions
		for (int i = 1; i < point.size(); i++) {
			int x=point.get(i-1).x;
			int x1=point.get(i).x;
			int y=point.get(i-1).y;
			int y1=point.get(i).y;
			if (i==1) {
				graphic.drawString("Start", x,	y);
			}
			ds= i*0.15f;
			int m  = Math.max(2, max-i) ;
			graphic.drawOval(x-m, y-m, m*2+(int)ds*1, m*2+(int)ds*1);
			graphic.drawOval(x-r,y-r,r*2,r*2);
			graphic.drawLine(x, y, x1, y1);
			if (i==point.size()-1) {
			graphic.drawString(String.format("%d", i), x-10, y);
			}
		}

	}
		
	
	
	public static ArrayList<Vessel> testing(){

		String p="C:/Users/Admin/Documents/Eclipse Workspace/netCDF files/";
//		String p="C:/Users/a1500/Documents/Eclipse Workspace/netCDF files/";
		String filetest= "fishboat1995_01.nc";
		String filePath= p+filetest;
		ArrayList<Vessel> vessels = openFile(filePath);

		return vessels;
	}
	
	// ======================================================================



	/**
	 * @param nv
	 * @param filePath
	 * @return
	 */
	private static ArrayList<Vessel> openFile(String filePath) {
		ArrayList<Vessel> vessels= new ArrayList<>();
		NetcdfFile ncfile;


		try {
			ncfile = NetcdfFile.open(filePath);
			if (ncfile!=null) {
				
				int nv=findPopulationSize(ncfile);
				//
				for (int i = 0; i < nv; i++) {
					String vesselName=  String.format("Vessel %d", i);
					Vessel v= new Vessel(vesselName,i,ncfile);
					v.grabTheData();
					vessels.add(v);
				}

			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return vessels;
	}



	private static int findPopulationSize(NetcdfFile ncfile) {
		int size=0;
		//Assumes population
		String tString="pop";
		Variable variable= ncfile.findVariable(tString);
		if (variable!=null) {
			try {
				Array data= variable.read();
				size= data.getInt(0);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}



		return size;
	}



	public static void main(String[] args) {


		// find population size
		// assume the population is fixed through time
//		System.out.println("Dimensionality of population:"+ np);
		
		ArrayList<Vessel> vessels= Vessel.testing();

		// display
		for (Vessel vessel : vessels) {
			System.out.println(" vessel : "+vessel.name);
			vessel.display();
			for (int i = 0; i < vessel.number; i++) {
				Coordinate c= vessel.getPositions()[i].coordinate;
				int index=c.findClosestIndex(vessel.coordinateGrid, 0.05f);

				int y = index/vessel.dim.width;//the actual y value
				int x = index- y*vessel.dim.width; //the actual X value

				System.out.println(c.toString()+" x "+x +" y "+y);

			}
			
		}


		//
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(450, 600);
		frame.setMinimumSize(new Dimension(450, 350));
		frame.setLocation(600, 300);
		frame.setTitle("title");
		frame.setVisible(true);
		JPanel panel= new JPanel() {

			@ Override
			public void paintComponent(Graphics g) {
				for (Vessel vessel : vessels) {
					vessel.draw(200,4);
					BufferedImage image= vessel.getImage();
					g.drawImage(image,0 ,0 ,null,null);
				}
			}

		} ;

		frame.add(panel);
	}







	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}


	/**
	 * @param number the number to set
	 */
	public void setNumber(int number) {
		this.number = number;
	}


	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}


	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}


	/**
	 * @return the at
	 */
	public Coordinate getAt() {
		return at;
	}


	/**
	 * @param at the at to set
	 */
	public void setAt(Coordinate at) {
		this.at = at;
	}


	/**
	 * @return the when
	 */
	public Clock getWhen() {
		return when;
	}


	/**
	 * @param when the when to set
	 */
	public void setWhen(Clock when) {
		this.when = when;
	}


	/**
	 * @return the positions
	 */
	public GeoPosition[] getPositions() {
		return positions;
	}


	/**
	 * @param positions the positions to set
	 */
	public void setPositions(GeoPosition[] positions) {
		this.positions = positions;
	}



	/**
	 * @return the variables
	 */
	public ArrayList<GeoVariable> getVariables() {
		return variables;
	}



	/**
	 * @param variables the variables to set
	 */
	public void setVariables(ArrayList<GeoVariable> variables) {
		this.variables = variables;
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
	
	

	
	
}
