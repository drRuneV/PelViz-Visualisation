package NetCdf;

import java.io.IOException;

import distribution.Topography;
import ecosystem.Coordinate;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import visualised.VisualTopography;

public class Etopo2Data {

	NetcdfFile ncfile = null;
//	private Topography topography;

	//the resolution of the file is 2 minutes which corresponds to 2°/60
	//2/60= 0,03333333333333 
	public static float res= 2.0f/60.0f;
	

	/**
	 * @param ncfile
	 */
	public Etopo2Data(String filename,Coordinate coo[]) {
		

		try {
			ncfile = NetcdfFile.open(filename);
//			 The netCDF is okay
			if (ncfile!=null) {
				generate(coo);
			}
		} 
		catch (IOException e) {
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

	/**
	 * Generates topography according to coordinates
	 * @param coo
	 * @return
	 */
	public Topography generate(Coordinate coo[]){
		String name="btdata"; //<•><•><•><•>  
		Topography topography = new Topography(ncfile);
		
		boolean wasFound= topography.lookForVariable(ncfile, name);
		if ( wasFound ){
			System.out.println("The Etopo2 "+name+" was found ");
			//We have to define the topography
			topography.defineRegular(coo, new float[]{res,res});
			// Create the values from the variable
			Variable variable = ncfile.findVariable(name);		
			topography.createData(variable); 
		}
		
		return topography;
	}
	
}
