package distribution;

import ecosystem.Coordinate;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * DataField is a general field of data which can be anything from 
 * a distribution, survey lines, particles or acoustics.
 * This is an interface towards accessing data in netCDF files.
 *
 */
public interface DataField {
	
	//
	String analyseNetCDF(NetcdfFile ncfile );
	
	// Look for the occurrence of this variable 
	boolean lookForVariable(NetcdfFile ncfile , String name);
		

	//Returns a list of all the variables in this netCDF file
	Variable[] getAllVariables(NetcdfFile  nc );

	// 
	Coordinate[] generateCoordinates(NetcdfFile  nc , Variable lat, Variable lon);



	
}
