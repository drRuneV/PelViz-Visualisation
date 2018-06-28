package distribution;


import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import basicGUI.ProgressFrame;
import distribution.statistics.StatDistr;
import ecosystem.Clock;
import ecosystem.Coordinate;
import ecosystem.Ecosystem;
import interaction.AreaInformation;
import interaction.SelectionMask;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;



public class Distribution extends GridDistribution {

	private boolean hasTime;
	// Statistics for this distribution
	private StatDistr statistics;
	// Parameters from the netCDF file
	private NetCDFParameters parameters;
	// The mask used for selecting areas 
	protected SelectionMask selectedMask= null;
	// Information about the area this distributions covers 
	protected AreaInformation areaInfo=null;
	// Biomass
	protected BiomassMeasure biomass= null;
	// Tracker for the current time/count
	protected int count=0;

	//
	protected boolean useLeapYear=true;
	//
	protected ProgressFrame progressFrame= null;
	
	/**
	 * Inner class for parameters 
	 * @author a1500
	 *
	 */
	public class NetCDFParameters{
		
		public float scalefactor=1;
		public float fillV;
		public float missingV;
		public String fullName;
		
	}
	
	
	// ===============================================
	// 				Constructors					//
	
	/**
	 * Constructor 
	 * @param ncfile
	 */ // only used in the netCDF access «•»
	public Distribution(NetcdfFile ncfile) {
		super(ncfile);
	}

	

	/**
	 * Constructs a new distribution given the name of a netCDF viable and the netCDF file.
	 * 
	 * @param name The name of a netCDF variable
	 * @param ncfile the netCDF file where we look for the variable
	 */
	public Distribution(String name,NetcdfFile ncfile ) {

		super(ncfile);
		
		long lastTime= (long) System.currentTimeMillis();
		
		// The netCDF is okay
		if (ncfile!=null) {

			Variable var= ncfile.findVariable(name);
			
			//If the variable exists
			if (var!=null) {
				setName(name);
				
				progressFrame= new ProgressFrame();
				progressFrame.setTitle(name);
				progressFrame.setVisible(true);
				
				// Find the attributes of scale and unit
				parameters= new NetCDFParameters();
				defineAttributes(var);
				progressFrame.update(message);
				
				// Generate coordinate objects
				generateCoordinateLatLong();
				progressFrame.update(message);

				// Generate area
				generateArea();
				progressFrame.update(message);
				
				
				//  Time/date
				hasTime = containsTime(var);
				if (hasTime) {
					Variable variableTime= ncfile.findVariable("T");
					useLeapYear =   isLeapYear(variableTime);
					generateDates(ncfile, variableTime );
					if (!useLeapYear) {
						Clock clock= new Clock(dates[0], useLeapYear);
						int numberOfLeapYears= clock.numberOfLeapYears();
						String noleapYearString="";
						for (int d = 0; d < dates.length; d++) {
							noleapYearString=dateStrings[d];
							dates[d]+= numberOfLeapYears*24;
							dateStrings[d]= Clock.createADate(dates[d]);
							System.out.println(noleapYearString+" --> Adjusted date:  "+ dateStrings[d]);
						}
					}
				}
				
				//Only one date 1970
				else{
					dates=new int[1];
					dates[0]=Clock.daysBetween1950And1970*24;
					dateStrings= new String[1];
					dateStrings[0]= Clock.createADate(dates[0]);
				}
				
				progressFrame.update("Generated times/dates…");

				//Create the full dataset
				progressFrame.update("	->Loading the dataset…");
				createFullDataSet(var);
				progressFrame.update("Done loading the dataset…"+ getValues().length);
				
				// Statistics
				if (getValues().length<899000000) {//
					progressFrame.update("Generating statistics…");
					statistics= new StatDistr(this, new Point(0,this.getValues().length),true);
				}
				areaInfo= new AreaInformation(this);
				if (hasTime) {
					biomass= new BiomassMeasure(this);
//					System.out.println(biomass.displayBiomassInformation());
				}
				progressFrame.update("ok statistics, area information and biomass…");
			}
			//Final message
			long timeTookms= (long) ((System.currentTimeMillis()-lastTime));
			message= String.format("Generating all data took:"+timeTookms+"  ms");
			System.out.println(message);
			progressFrame.update(message);
			
		}
		
	}



	/**
	 * 
	 */
	private boolean isLeapYear(Variable var) {
		boolean  useLeap =true;

		List<Attribute> at= var.getAttributes();
		for (Attribute attribute : at) {
			String na= attribute.getFullName().toLowerCase();
			if(na.contains("calendar")   && attribute.isString()){
				String calendarType= attribute.getStringValue();

				useLeap  = (calendarType.contains("noleap")) ? false : true;
			}
		}

		return useLeap;
	}



	@Override
	public void copyAllFrom(GridDistribution gd) {
		this.copyFrom(gd);
	}

	/**
	 * Copies data from another distribution
	 * @param d The distribution to copy from
	 */
	public void copyFromAnother(Distribution d){
		copyAllFrom(d);
		this.setStatistics(d.getStatistics());
		this.setParameters(d.getParameters());
	}

	

	/**
	 * Main class for testing
	 * @param args
	 */
	public static void main(String[] args) {
		String s =" August-20 :as well as July 19";
		s ="1995-01-01:01 ";
		
		String p="C:/Users/a1500/Documents/Eclipse Workspace/netCDF files/";
		String 		filename  =  "2Dherring2015_rev.nc";
//		
//		String sp[]= s.toLowerCase().split("-");
//		for (String string : sp) {
//			System.out.println(string);
//		}
//		String month= sp[0];
//		String day=   sp[1];
//		
		NetcdfFile ncfile = null;
		try {
			ncfile = NetcdfFile.open(p+filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		NetCDFAccess netCDFAcess= new NetCDFAccess(p+ filename);
		Distribution distribution= new Distribution("HERbiom", ncfile);
		
	}


	/**
	 * Calculate the volume for the entire area the distribution covers
	 * @param topography
	 * @return
	 */
	public float[] calculateTheVolume(Topography topography){
		float[] volume= new float[area.length];
		
		for (int i = 0; i < volume.length; i++) {
			Coordinate co= getCoordinates()[i];
			if (getValues()[i] != getFillV()) {
			volume[i]=area[i]* topography.getValueAtCoordinate(co)*(-1);
			}
		}
//		System.out.println(" Total water volume:");
		return volume;
	}
	
	
	//volume= area * topo.getValueAtCoordinate(coord)*(-0.001f); //cubic kilometres km3
	
	/**
	 * 
	 * @param var
	 */
	private void defineAttributes(Variable var) {
		List<Attribute> at= var.getAttributes();
		for (Attribute attribute : at) {
			String na= attribute.getFullName().toLowerCase();
			if(na.contains("scale")){
				parameters.scalefactor= attribute.getNumericValue().floatValue();
			}
			if(na.contains("unit")){
				unit= attribute.getStringValue();
			}

			if(na.contains("fill")){
				parameters.fillV= attribute.getNumericValue().floatValue();
			}

			if(na.contains("missing")){
				parameters.missingV= attribute.getNumericValue().floatValue();
			}//
			//long_name
			if(na.contains("name") && na.length()> 4){
				parameters.fullName= attribute.getStringValue();
			}
			
		}
//		System.out.println("scalefactor="+parameters.scalefactor+" Unit:"+unit);
		message = "Defined attributes…this" ;
	}



	/**
	 * Create the full dataset with all dimensions
	 * @param var
	 */
	private void createFullDataSet(Variable var) {
		
		// We find the number of dimensions from the shape or dimensions
		// The whole dataset is used so origin is 0,0,...
		int origin[];
		origin= new int[var.getShape().length];
		for (int i = 0; i < origin.length; i++) origin[i]= 0;	//so origin is 0,0,...
		
		// Since we are taking the whole dataset the size is simply the shape.
		// Think about the shape as the "shape of an object" 2×5×6 a box et cetera
		// In the case of the NORWECOM.E2E model the shape is typically: T:364 Y:123 X:168
		// The size is a array of 3 integers then, 364, 123, 168
		int size[]=  var.getShape();

		// Here we actually creating the data,this can be tricky because the sequence can vary
		// It is ASSUMED! that the dataset is arranged like this: Time Y X
		// the last one, x, is defined to be the width but keep in mind that this is more 
		// like the latitude 
		createData(var, origin, size);

		// Set the right values for the with/height/time dimension of the dataset
		// «•» Here we could actually look more directly for the corresponding dimensions!?
		
		// «•» If there is another dimension like depth, we have to take care of this here!
		// «•» The name of the dimensions will ultimately matter!!! 
		
		// Use a index depending on the length of the shape to set the with/height/time
		int shapeix= var.getShape().length;
		
		// ASSUMES that width is the last variable
		setWidth(var.getShape(shapeix-1)); //2 if 3 dimensions
		
		// ASSUMES that height is the 2nd last variable
		setHeight(var.getShape(shapeix-2)); // 1
		
		// ASSUMES that time is the 3rd last variable if it is there
		if ((shapeix-3)==0) {
			setTime(var.getShape(0));	
		}
		// If there is no time, time is still one because there is one dataset
		else setTime(1);
	}


	/**
	 * Creates the Data set
	 * @param variable The netCDF variable we are creating the dataset for
	 * @param origin The starting point of the data we want to select
	 * @param size The size of the selection
	 * @return
	 */
	public boolean createData(Variable variable,int[] origin,int[] size){
		boolean isOkay=true;
		
		// access the attribute to get the missing value and replace this with -1
		// !!
		
		//Create the data
		// <•><•><•><•>  //Label1
		Array data;
		try {
			// Get the portion of data requested
			data = variable.read(origin ,size);
	
			// Create the float array where we put the data
			// The Array is two-dimensional with latitudes along the x-axis ?? !
			int length = (int) data.getSize();
			values =   new float[length];
			for (int i = 0; i < length; i++) {
				values[i]=  data.getFloat(i)*parameters.scalefactor;
			}
	
		}
		catch (InvalidRangeException | IOException e) {
			// TODO Auto-generated catch block
			isOkay=false;
			e.printStackTrace();
		}
		return isOkay;
	
	}



	/**
	 * 
	 * @param var
	 */
	private void generateCoordinateLatLong() {
		//  Deal with associated dimensions which we expect
		Variable varlat= ncfile.findVariable("Latt");
		Variable varlon= ncfile.findVariable("Long");

		// Generate coordinates
		if (varlon!=null && varlat!=null) {
			generateCoordinates(ncfile, varlat, varlon);
			message= "Distribution coordinates generated";
			System.out.println(message);
		}
	}
	
	/**
	 * Generates the area for each grid cell
	 */
	private void generateArea(){
		//  Deal with associated dimensions which we expect
		Variable varPM= ncfile.findVariable("PM");
		Variable varPN= ncfile.findVariable("PN");
		
		float scalef = findScaleFactor(varPM); //assume the same scaling for PN

		long lastFrame = System.currentTimeMillis();



		// Generate 
		if (varPM!=null && varPN!=null) {
			Array dataPM;
			Array dataPN;
			try {
				// Get all the data of 
				dataPM = varPM.read();
				dataPN = varPN.read();
				//Variables have the same length =width*height
				int length = (int) dataPM.getSize();
				area= new float[length]; //width*height
				float a=0;
				for (int i = 0; i < length; i++) {
					a=  dataPM.getFloat(i)*scalef *  dataPN.getFloat(i)*scalef;
					area[i]=1/a;								
//					System.out.println("Generating area!");
				}
				long timeTookms= (long) ((System.currentTimeMillis()- lastFrame ));
				message = "Area generated…# "+area.length+" ms:" +timeTookms;
				
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

	
	/**
	 * Try to find out if this variable contains time dimension
	 * 
	 * @param var - The variable we are checking
	 * @return - true if this variable contains a dimension starting with the specific name "T"
	 * and the number of dimension is greater than 2
	 */
	private boolean containsTime(Variable var) {
//		String allnames="";
		boolean is3D= false;
		ArrayList <Dimension> dimensions=  (ArrayList<Dimension>) var.getDimensions();
		for(int i=0; i< dimensions.size() ;i++){
			is3D= (is3D || dimensions.get(i).getFullName().startsWith("T"));
//			allnames +=dimensions.get(i).getFullName()+" ";
		}
//		is3D= (allnames.contains("T")  && dimensions.size()>2);
		is3D= (is3D && dimensions.size()>2);
		String s= (is3D) ?"contains time!":"DOES NOT contain TIME!";
		System.out.println(this.getFullName()+" " +s);
		return is3D;
	}

	/**
	 * Gives a the name  with year
	 * @return the full name followed by the year
	 */
	public String giveNameWithYear(){
		Clock clock= new Clock(getDates()[0],false);
		int year=clock.getCalendar().get(Calendar.YEAR);
		String name= getFullName() +String.format(" %d",year);
		return name;
	}

	
	/*
	 * Find the corresponding index for a given date string
	 */
	public int findDateIndexFrom(String s,String splitter){
		int index= -1;
		String sp[]= s.toLowerCase().split(splitter);

		// 
		if (sp.length>1) {
			String month= String.format(sp[0]);
			String day=   sp[1];
			int m= Ecosystem.indexOfMonth(month)+1;
			if (m<10) {
				month= "0"+m;
			}
			else month= ""+m;

			s = String.format(month+"-"+day);
			//			System.out.println("Correctly string:"+s);

			for (int i = 0; i < dateStrings.length; i++) {

				if (dateStrings[i].contains(s)){
//					System.out.println(dateStrings[i]+" "+s);
					index=i;
				}
			}

		}
		return index;
	}


	// ==================================================
	// Getters and setters
	// ==================================================

	
	
	/**
	 * @return the hasTime
	 */
	public boolean isHasTime() {
		return hasTime;
	}



	/**
	 * @param hasTime the hasTime to set
	 */
	public void setHasTime(boolean hasTime) {
		this.hasTime = hasTime;
	}



	/**
	 * @return the scalefactor
	 */
	public float getScalefactor() {
		return parameters.scalefactor;
	}



	/**
	 * @param scalefactor the scalefactor to set
	 */
		public void setScalefactor(float scalefactor) {
		this.parameters.scalefactor = scalefactor;
	}




	/**
	 * @return the fillV
	 */
	public float getFillV() {
		return parameters.fillV;
	}



	/**
	 * @param fillV the fillV to set
	 */
	public void setFillV(float fillV) {
		this.parameters.fillV = fillV;
	}



	/**
	 * @return the missingV
	 */
	public float getMissingV() {
		return parameters.missingV;
	}



	/**
	 * @param missingV the missingV to set
	 */
	public void setMissingV(float missingV) {
		this.parameters.missingV = missingV;
	}
	


	public String getFullName() {
		return parameters.fullName;
	}



	public void setFullName(String fullName) {
		this.parameters.fullName = fullName;
	}




	/**
	 * @return the parameters
	 */
	public NetCDFParameters getParameters() {
		return parameters;
	}



	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(NetCDFParameters parameters) {
		this.parameters = parameters;
	}



	/**
	 * @return the statistics
	 */
	public StatDistr getStatistics() {
		return statistics;
	}



	/**
	 * @param statistics the statistics to set
	 */
	public void setStatistics(StatDistr statistics) {
		this.statistics = statistics;
	}



	

	
	// ==================================================
	// Implemented interface methods
	// ==================================================
	
	@Override
	public String analyseNetCDF(NetcdfFile ncfile) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Variable[] getAllVariables(NetcdfFile nc) {
		// TODO Auto-generated method stub
		return null;
	}





	/**
	 * @return the selectedMask
	 */
	public SelectionMask getSelectedMask() {
		return selectedMask;
	}



	/**
	 * @param selectedMask the selectedMask to set
	 */
	public void setSelectedMask(SelectionMask selectedMask) {
		this.selectedMask = selectedMask;
	}



	/**
	 * @return the biomass
	 */
	public BiomassMeasure getBiomass() {
		return biomass;
	}



	/**
	 * @param biomass the biomass to set
	 */
	public void setBiomass(BiomassMeasure biomass) {
		this.biomass = biomass;
	}



	/**
	 * @param areaInfo the areaInfo to set
	 */
	public void setAreaInfo(AreaInformation areaInfo) {
		this.areaInfo = areaInfo;
	}



	/**
	 * @return the areaInfo
	 */
	public AreaInformation getAreaInfo() {
		return areaInfo;
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
	 * @return the progressFrame
	 */
	public ProgressFrame getProgressFrame() {
		return progressFrame;
	}



	public int length() {
		return this.getValues().length;
	}



}
