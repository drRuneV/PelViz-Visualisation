package NetCdf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import visualised.VisualDistribution;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NCdumpW;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import distribution.Distribution;
import distribution.Topography;
import ecosystem.Coordinate;
import graphics.DistPanel;
import testing.TestTopography;


public class NetCDFAccess {

	
	String filename; 
	NetcdfFile ncfile = null;
	
	
	
	/**
	 * @param filename
	 * @param ncfile
	 */
	public NetCDFAccess(String filename) {
		this.filename = filename;

		try {
			ncfile = NetcdfFile.open(filename);
//			 The netCDF is okay
			if (ncfile!=null) {
				outputGeneral();
//				accessTopo();
				
				
				// ==================================================
				// •|•|•|•|•|•|•|•|•|•|
				// PRESENTATION •|•|•|•|•|•|•|•|•|•|
				// ==================================================
				//  comment line 52 and 55
				// Try distribution
//				VisualDistribution di= new VisualDistribution("CFbiom", ncfile);
//				DistPanel pa= new DistPanel(di, 0);
				//
//				VisualDistribution di2= new VisualDistribution("CFprod", ncfile);
			//	DistPanel pa2= new DistPanel(di2, 2);


//				Distribution dimask= new Distribution("RMask", ncfile);
//				DistPanel pa3= new DistPanel(dimask, 4);
				
//				VisualDistribution ditop= new VisualDistribution("Topo", ncfile);
//				DistPanel pa4= new DistPanel((VisualDistribution) ditop, 3);
				
			}
			else System.out.println("cannot find:"+filename);
 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally { 
			if (null != ncfile) try {
				ncfile.close();
				System.out.println("\n netCDF file is closing "); 
			} catch (IOException ioe) {
				System.out.println("trying to close " + filename + ioe.getMessage());
			}
		}
	}

			


	/**  	 */
private void outputGeneral(){
		
		System.out.println("\n netCDF filename:  "+filename);
		System.out.println(ncfile.getDetailInfo());
		System.out.println("Type description:  "+ncfile.getFileTypeDescription());
		System.out.println("Data: " + ncfile.getVariables() );

	}



	/**
	 * @throws IOException   */   //Label1
	public void accessTopo() throws IOException{

		
		// Test topography data 
		String name="btdata"; //<•><•><•><•>  
		Variable variable = ncfile.findVariable(name);		
		Coordinate coo[]  = {new Coordinate(89, -180), new Coordinate(-70, 180)};

		Topography topography= new Topography(ncfile);
		boolean wasFound= topography.lookForVariable(ncfile, name);

		if ( wasFound ){
			System.out.println("The variable "+name+" was found ");

			//We have to define the topography
			topography.defineRegular(coo, new float[]{Etopo2Data.res,Etopo2Data.res});
			// Create the values from the variable
			topography.createData(variable); //createValues(variable, coo);
			//Let us test the drawing
			 topography.drawPanel();
			//topography.drawPanel(); 
		}
		else {  

			System.out.println("we are NOT able to find the variable  "+ name); 
		}


	}


	//Label3

	private Distribution createDistribution(String name,NetcdfFile ncfile ) {
		Distribution distribution= null;
		boolean  is3D=false;
		
		
		
		Variable var= ncfile.findVariable(name);
		//If the variable exists
		if (var!=null) {
			ArrayList <Dimension> dimensions=  (ArrayList<Dimension>) var.getDimensions();

			//Look for a specific dimensional name like time
			String allnames="";
			for(int i=0; i< dimensions.size() ;i++){
				allnames +=dimensions.get(i).getFullName()+" ";
			}
			is3D= (allnames.contains("T")  && dimensions.size()>2);
			//

			//  
			distribution = new Distribution(ncfile);
			Variable varlat= ncfile.findVariable("Latt");
			Variable varlon= ncfile.findVariable("Long");
			Variable vT=  ncfile.findVariable("T");
			//		readVariable("T");

			//remember

			// Generate coordinates
			if (varlon!=null && varlat!=null) {
				distribution.generateCoordinates(ncfile, varlat, varlon);
				System.out.println("Distribution coordinate generated");
			} 

			// We find the number of dimensions from the shape or dimensions
			// The whole dataset is used so origin is 0,0,...
			int origin[];
			origin= new int[var.getShape().length];
			for (int i = 0; i < origin.length; i++) origin[i]= 0;
			// Since we are taking the whole dataset the size is simply the shape.
			// Think about the shape as the "shape"of an object 2×5×6 a box et cetera
			int size[]=  var.getShape();

			// Here we actually creating the data,this can be tricky because the sequence can vary
			distribution.createData(var, origin, size);
			// Use a index depending on the length of the shape to set the with/height/time
			int si= var.getShape().length;
			distribution.setWidth(var.getShape(si-1)); //2 if 3 dimensions
			distribution.setHeight(var.getShape(si-2)); // 1 
			if ((si-3)==0) {
				distribution.setTime(var.getShape(0));
			}
			distribution.setName(name);

			// •try the new distribution panel
			DistPanel pa= new DistPanel((VisualDistribution) distribution, true);

		}

		return distribution;

	}



	// Here we try to create a distribution giving it a variable
	// The variable contains information about dimensions and attributes
	// But the variables of those dimensions is not yet provided
	/**
	 * 
	 *///Label2
	private void tryDistribution(String name) {

		Variable var= ncfile.findVariable(name);
		// dimensionsc
		ArrayList <Dimension> dimensions=  (ArrayList<Dimension>) var.getDimensions();// ncfile.getDimensions();
		System.out.println("dimensions: "+dimensions.toString());
		int n=dimensions.size();
		String namev;
		for(int i=0; i< n ;i++){
			namev= dimensions.get(i).getFullName();
			Variable vard = ncfile.findVariable(namev);
			System.out.println("name:"+	namev+"  vname="+vard.getFullName());
		}
		//Try one specific variable
		// 
//		String variableName="Latt";
//		Variable var = ncfile.findVariable(variableName);
//		if(var!=null){
//		for (int i = 0; i < var.getDimensions().size(); i++) {
//		System.out.println(var.getDimensionsString()+" | Attr"+i+" "+ var.getAttributes().get(i));
//		System.out.println(var.getDimension(i).getName()+" : "+var.getDimension(i).getLength());
//		}
//		//		readVariable(variableName);
//		
	
		// Try to get the data the same manner as with the topography using origin and size
		Distribution distribution = new Distribution(ncfile);
		Variable varlat= ncfile.findVariable("Latt");
//		Variable vY= 	 ncfile.findVariable("Y");
		Variable varlon= ncfile.findVariable("Long");
//		Variable vX= 	 ncfile.findVariable("X");
//		Variable var= ncfile.findVariable(name);
		Variable vT=  ncfile.findVariable("T");
		readVariable("T");
		
		//remember
		
		//
		if (varlon!=null && varlat!=null) {
			distribution.generateCoordinates(ncfile, varlat, varlon);
			System.out.println("Distribution coordinate generated");

			//look at the values
//			for (int i = 0; i < distribution.getCoordinates().length; i++) {
//				System.out.println("c:"+distribution.getCoordinates()[i].getLat()+" °N "+
//						distribution.getCoordinates()[i].getLon());
//			}
//			// here we assume that the dimensions are time, latitude and longitude
			int origin[];//= new int[]{0,0,0};
			origin= new int[var.getShape().length];
			 for (int i = 0; i < origin.length; i++) {
				 origin[i]= 0;
			}
//			int size[]= new int[]{1, (int) vY.getSize(),(int) vX.getSize()};
			//int size[]= new int[]{ (int) vY.getSize(),(int) vX.getSize(),(int) vT.getSize()};
			int size[]=  var.getShape(); //new int[]{ (int) var.getShape(0),(int) var.getShape(1),(int) var.getShape(2)};
			
			
			// «•»  one solution would be to have the values in the distribution 
			//  as a two-dimensional array : values[y][x] …
			
			distribution.createData(var, origin, size);
			// this can be tricky because the sequence can vary !!«•»
			int si= var.getShape().length;
			distribution.setWidth(var.getShape(si-1)); //2 if 3 dimensions
			distribution.setHeight(var.getShape(si-2)); // 1 
			if ((si-3)==0) {
			distribution.setTime(var.getShape(0));
			}
			distribution.setName(name);
			
			// •try the new distribution panel
			DistPanel pa= new DistPanel((VisualDistribution) distribution, true);
			
			
			//there is an issue: the dimensions of distributions are organised different than 
			// the dimension of topography.
			
	}
	}



	/**
	 * @param size
	 */
	public void testingDrawing(int[] size,float[] data) {
		// We want longitude to be along the x-axis, size1 !
		TestTopography values = new TestTopography(size[1], size[0]);
		values.setValues(data);
		values.test(false);
		// pv.test2();  //use the older test panel
		// Draw the gradient in a separate frame as well
//		values.getGradient().test();
		
		
	}

	

	

	// How to Dump the Data from One Variable
	// <><><><><><><><><><><><><><><>
	public void readVariable(String name){

		Variable v = ncfile.findVariable(name);

		if (null == v) return;
		try {
			Array data = v.read();
			NCdumpW.printArray(data, name, System.out, null);
		} catch (IOException ioe) {
			System.out.println("trying to read " + name+ ioe);

		}

	}

	
}
//-•-//