/**
 * 
 */
package NetCdf;

import java.io.IOException;
import java.util.ArrayList;

import ecosystem.Clock;
import ecosystem.Coordinate;
import pointDistribution.Vessel;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NCdumpW;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * @author Admin
 *
 */
public class NetCDFInspector {
	
	// This is a handle into a netCDF file
	protected NetcdfFile ncfile = null;
	protected String filePath="";
	private String message;

	
	
	
	
	
	/**
	 * @param ncfile
	 * @param filePath
	 */
	public NetCDFInspector(NetcdfFile ncfile) {
		this.ncfile = ncfile;
	}




	public boolean lookForVariable(NetcdfFile ncfile, String name) {
		boolean isPresent;
		Variable variable = ncfile.findVariable(name);
		isPresent= (variable!=null);
	
		if(!isPresent){
			System.out.println("we are NOT able to find the variable  "+ name); 
		}
		else {  System.out.println("The variable "+name+" was found ");
		}
	
		return isPresent;
	}




	/**
	 * 
	 * @param ncfile
	 * @return
	 */
	public String analyseNetCDF(NetcdfFile ncfile) {
		String information="Variables: "; 

		//All variables
		ArrayList<Variable> variables= (ArrayList<Variable>) ncfile.getVariables();
		//
		ArrayList<Variable> shipVariables= new ArrayList<>();
		//
//		Vessel vessel= new Vessel("ship"); 

		// loop through all variables
		for (Variable variable : variables) {
			information+= variable.getDescription();
			
			System.out.println("\n Variable : "+variable.getNameAndDimensions()+
					" "+variable.getDescription());
			 findDimensionOf(variable);
			 
		}
		
		
		
		return information;
	}



	/**
	 * 
	 * @param ncfile
	 * @param dimensionStrings 
	 * @return
	 */
	public static ArrayList<Variable> findShipVariables(NetcdfFile ncfile, String[] dimensionStrings){
		ArrayList<Variable> variables= (ArrayList<Variable>) ncfile.getVariables();
		ArrayList<Variable> shipVariables= new ArrayList<>();
		int dimSize=0;
						
		for (Variable variable : variables) {
			dimSize= variable.getDimensions().size();
			boolean ok = hasDimension(dimensionStrings,variable);
			if (dimSize==2 && ok ) {
				shipVariables.add(variable);
			}
		}
		
		return shipVariables;
		
	}


	/**
	 * Checks if the current variable has all the dimensions as they are represented by strings
	 * @param dimensionStrings  a list of strings representing the dimensions we want to check
	 * @param variable the variable we want to check
	 * @return
	 */
	public static boolean hasDimension(String[] dimensionStrings, Variable variable) {
		//
		ArrayList <Dimension> dimensions=  (ArrayList<Dimension>) variable.getDimensions(); 

		String totalString = "" ;
		// All the names of the dimensions for this variable into one string 
		for (Dimension dimension : dimensions) {
			totalString += dimension.getFullName();
		}

		boolean okay=true;
		// Check if all the desired dimensions are represented ,
		// if one of them is not, okay, turns out to be false
		for (String string : dimensionStrings) {
			okay= totalString.contains(string)  && okay;
		}

		return okay;
	}





	/**
	 * @param ncfile
	 */
	private void findDimensionOf(Variable var) {
		
		ArrayList <Dimension> dimensions=  (ArrayList<Dimension>) var.getDimensions();// ncfile.getDimensions();
		System.out.println("dimensions: "+dimensions.toString());
		int n=dimensions.size();
		String namev;
		for(int i=0; i< n ;i++){
			namev= dimensions.get(i).getFullName();
			Variable vard = ncfile.findVariable(namev);
			if (vard !=null) {
				System.out.println("Dimname:"+	namev+"  vname="+vard.getFullName());
			}
			else System.out.println(namev +"\n  Dimension has no corresponding variable");
		}
	}

	
	
	
	/**
	 * 
	 * @param fileName
	 */
	public void checkNetCDF(String fileName){

		try {
			ncfile = NetcdfFile.open(fileName);
			//				 The netCDF is okay
			if (ncfile!=null) {
				filePath= fileName;
			}
		} 
		catch (IOException e) {
//			e.printStackTrace();
			message= "The netCDF file: \n"+fileName +"\n  is not accessible or does not exist";
			System.out.println(message);
//			 JOptionPane.showMessageDialog(null, message);
		}
	}


	// How to Dump the Data from One Variable
	// <><><><><><><><><><><><><><><>
	/**
	 * Dumping the content of a variable
	 * @param name
	 * @return
	 */
	public String dumpingVariable(String name){
		String result="";
		Variable v = ncfile.findVariable(name);

		if (v==null) return result;
		//
		try {
			Array data = v.read();
			result= NCdumpW.toString(data);
			//printArray(data, name, System.out, null);
		} catch (IOException ioe) {
			System.out.println("trying to read " + name+ ioe);

		}
		
		return result;
	}


	public static void main(String[] args) {
		String p="C:/Users/a1500/Documents/Eclipse Workspace/netCDF files/";
		String filetest= "fishboat1995_01.nc";
		String filePath=p+ filetest;// "ibm2felt.nc";
		NetcdfFile ncfile;
		NetCDFInspector ni;

		String tString="time";

		try {
			ncfile = NetcdfFile.open(filePath);

			if (ncfile!=null) {

				ni=new NetCDFInspector(ncfile);
				//			System.out.println(ni.dumpingVariable(name));
//				System.out.println(ncfile.getVariables().toString());

				
				// If we have time
				Variable time;
				if (ni.lookForVariable(ncfile,tString)==true) {
				time= ncfile.findVariable(tString);	
//					ni.displayTime(time);// okay
				int hours[]= ni.generateDates(ncfile, time);
				//
				
				// Coordinates
				Variable varlat= ncfile.findVariable("Latt");
				Variable varlon= ncfile.findVariable("Long");
				// Generate coordinates
				if (varlon!=null && varlat!=null) {
					ni.generateCoordinates(ncfile, varlat, varlon);
					System.out.println("Distribution coordinate generated");
					// 
					int w= varlat.getShape()[1]; //assumes x-coordinate is number 1, y number 0
					Variable posx= ncfile.findVariable("xpos");

					Variable posy=ncfile.findVariable("ypos");
					if (posx !=null && posy !=null) {

						Coordinate[] coordinateGrid= ni.generateCoordinates(ncfile, varlat, varlon);
						Coordinate c[]= ni.findCoordinates(coordinateGrid, posx, posy,w,0);
						
						//print out for testing
						int n=0;
						for (Coordinate coordinate : c) {
							
							String date=  (n<hours.length) ? Clock.createADate(hours[n])  : "no";
							System.out.println(n+ "  Coordinate: "+ coordinate.toString()+
									" "+date);
							n++;
						}
					}
				} 

				// Find all ship variables 
				String[] dimensionStrings= {"time","pop"}; 
				ArrayList<Variable> shipVariable= ni.findShipVariables(ncfile,dimensionStrings);
				for (Variable variable : shipVariable) {
					System.out.println("ShipVariable: "+variable.getFullName());
				}
				
				// Then create corresponding number of vessels
				//
				ArrayList<Vessel> vessels= new ArrayList<>();
//				Variable vpop= ncfile.findVariable("pop");
				// find population size
				// assume the population is fixed through time
				int np=  (int) shipVariable.get(0).getDimension(1).getLength();
				System.out.println("Dimensionality of population:"+ np);

				for (int i = 0; i < np; i++) {
					String vesselName=  String.format("Vessel %d", i);
					Vessel v= new Vessel(vesselName,i,ncfile);
					v.grabTheData();
					
									}
				}


					
				}

			
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		
	}


	/**
	 * Finding the coordinates in the coordinate grid
	 * @param coordinateGrid
	 * @param posx
	 * @param posy
	 * @param width
	 * @return
	 */
	public Coordinate[] findCoordinates(Coordinate[] coordinateGrid,Variable posx,Variable posy,int width,int id){
		Coordinate[] coordinates = null ;
		int sizeTime= posx.getShape(0); //the number of time steps
//		int n = (int) posx.getSize();
		coordinates = new Coordinate[sizeTime];
		int index=0;
		
//		origin= new int[var.getShape().length];
//		for (int i = 0; i < origin.length; i++) origin[i]= 0;	//so origin is 0,0,...
//		
		//
		try {
			int[] origin= new int[]{0,id}; //change the 2nd parameter, population, to get another boat
			int[] size= new int[]{sizeTime, 1} ;
			Array dataposx = posx.read(origin ,size); //(origin ,size);
			Array dataposy = posy.read(origin ,size); //(origin ,size);
			//
			for (int i = 0; i < sizeTime; i++) {
				index= dataposx.getInt(i)+ dataposy.getInt(i)*width;
				coordinates[i]= new Coordinate(coordinateGrid[index]);
			}


		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}

		System.out.println("Number of coordinates was: "+sizeTime);

		return coordinates;
	}
	
	
	
	/**
	 * 
	 * @param nc
	 * @param lat
	 * @param lon
	 * @return
	 */
	public static Coordinate[] generateCoordinates(NetcdfFile nc, Variable lat, Variable lon) {

		float scalef = 0.01f;// 1.0f ;//findScaleFactor(lat);
		Coordinate[] coordinates= null;
		
		// Get all the data of latitude and longitude
		try {
			//NetCDF array
			Array datalat = lat.read();
			Array  datalon=  lon.read();
			//Latitude and longitude's have the same length =width*height
			int length = (int) datalon.getSize();
			coordinates = new Coordinate[length]; //width*height
			for (int i = 0; i < length; i++) {
				coordinates[i]= new Coordinate(datalat.getFloat(i)*scalef,datalon.getFloat(i)*scalef);
			}
		}

		catch (IOException e) {
			e.printStackTrace();
		}
		return coordinates;
	}
	
	

			/**
			 * Generate the dates from the netCDF variable T "Time"
			 * @param nc the netCDF file
			 * @param dateVar The date/time variable
			 * @return An array of integers as time is represented in the netCDF file
			 */
	public int[]generateDates(NetcdfFile nc, Variable dateVar) {
		Array dataDate;
		int[] dates= null;

		try {
			dataDate= dateVar.read();
			int length = (int) dataDate.getSize();
			dates= new int[length];
			String[] dateStrings = new String[length];
			//
			for (int i = 0; i < length; i++) {
				int hour1950 = dataDate.getInt(i);
				dates[i]= hour1950;
				dateStrings[i]= Clock.createADate(hour1950);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return dates;
	}




	/**
	 * @param time
	 */
	private void displayTime(Variable time) {
		Array data;
		try {
			// Get the portion of data requested
			int[] origin= new int[1];
			int[] size= new int[1];
			origin[0]=0;
			size[0]=time.getShape(0);
			data = time.read(origin ,size);
			//
			int length = (int) data.getSize();
			float[] values =   new float[length];
			for (int i = 0; i < length; i++) {
				values[i]=  data.getFloat(i);//*parameters.scalefactor;
			}


			//Trying to converted time to date
			Clock clock= new Clock(); 
			String st;
			for (int i = 0; i < data.getSize(); i++) {
				int hourSince1950= (int) values[i];
				st= Clock.createADate(hourSince1950) ;//String.format("  %d", i);
				System.out.println(i+ " Time:"+ values[i]+" -> "+st);
			}
		}
		catch (InvalidRangeException | IOException e) {
			e.printStackTrace();
		}
	}
	

}
