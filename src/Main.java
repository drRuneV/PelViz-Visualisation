import java.io.IOException;
import NetCdf.NetCDFAccess;
import ecosystem.Ecosystem;
import ecosystem.SurveyLines;
import graphics.DistributionsPanel;
import ucar.nc2.NetcdfFile;
import visualised.VisualDistribution;
					
public class Main {

	
// This is just a test class which should be removed later on when
// Each distribution class takes care of this by them self	
public static NetCDFAccess netCDFAcess;


	//==================================================	
	/**
	 * The main method of the Main class where it all starts
	 *  
	 * @param args
	 */
	public static void main(String[] args) {

		
		String filename ="etopo2.nc";	
		String p="C:/Users/a1500/Documents/Eclipse Workspace/netCDF files/";
		
		// «•» Opens up the topography window where we can scroll
		// ==================================================
		// PRESENTATION •|•|•|•|•|•|•|•|•|•|
		// ==================================================
		// COMMENT line below
//		NetCDFAccess netCDFTopography= new NetCDFAccess(p+filename);
		
		// This is the new way to make topography
		// The etopo2 object completely generates the topography data, by using the createdata method in topography.
		/*Coordinate coo[]  = {new Coordinate(85, -180), new Coordinate(-80, 180)};
		Etopo2Data topo= new Etopo2Data(p+filename, coo);
		Topography topography= topo.getTopography();
		topography.drawTopoPanel();
		*/
		

		//«•»//«•»//«•»  Testing «•»
		String filetest= "fishboat1995_01.nc";
//		NetCDFAccess nc= new NetCDFAccess(p+filetest);
//		="physics.nc"; //"survey_lines.nc";//
		

		// Opens up another netCDF excess for the individual-based modelfile
//		filename ="physics.nc"; //"survey_lines.nc";// 
		filename  =  "2Dherring2015_rev.nc";//".nc";		
		netCDFAcess= new NetCDFAccess(p+ filename);


		//
		// Open directly
		NetcdfFile ncfileher = null;
		NetcdfFile ncfileHerring= null;
		NetcdfFile ncfileHerring2= null;
		NetcdfFile ncfilemac = null;
		NetcdfFile ncfilekol= null;
		try {
//			ncfile2= NetcdfFile.open(p+"ibm2felt.nc"); //ibm2felt1
			ncfileHerring= NetcdfFile.open(p+"2Dherring2015_rev.nc"); //ibm2felt1
			ncfileHerring2= NetcdfFile.open(p+"2Dherring2055_rev.nc"); 
			String fHerring="sildyear1.nc";//->individual:"herring2004_01.nc"; //"sildyear1.nc"
			ncfileher = NetcdfFile.open(p+fHerring); //sildyear1
			ncfilemac = NetcdfFile.open(p+"macyear1.nc"); //mackrell
			ncfilekol = NetcdfFile.open(p+"kolmuleyear1.nc"); //
			
			// Panel
			DistributionsPanel panel= null;
			// zooplankton
			if (ncfileHerring!=null) {
				VisualDistribution vdHerring= new VisualDistribution("HERbiom", ncfileHerring);				
				// Show several distributions in one window
				panel = new DistributionsPanel(vdHerring, true);
				
				//
//				Ecosystem
				Ecosystem ecosystem= new Ecosystem(panel);
				panel.setEcosystem(ecosystem);
				
				//if we use a bigger grid, survey lines should follow this grid instead !
				filename="survey_lines.nc";  
				if (filename.contains("survey")) {
					SurveyLines surveyLine= new SurveyLines(p+ filename,vdHerring.getCoordinates());
					ecosystem.setSurveyLine(surveyLine);					
//					netCDFAcess= new NetCDFAccess(p+ filename);
				}
   
			}
			// herring 2055
			if (ncfileHerring2!=null){
				VisualDistribution vdherring2055= new VisualDistribution("HERbiom", ncfileHerring2);
				panel.addDistribution(vdherring2055);
			}
				
			// Mackrell
			if (ncfilemac!=null){
//				VisualDistribution vdmackrell= new VisualDistribution("MACbiom", ncfilemac );
//				pv1.addDistribution(vdmackrell);
				
			}
			// blue whiting
			if (ncfilekol!=null){
//				VisualDistribution vdmkol= new VisualDistribution("BLUbiom", ncfilekol);
//				pv1.addDistribution(vdmkol);
				
			}
				
//				netCDFAcess= new NetCDFAccess(p+ fHerring);
				
				// 
				
				// ==================================================
				// •|•|•|•|•|•|•|•|•|•|
				// PRESENTATION •|•|•|•|•|•|•|•|•|•|
				// ==================================================
				
				//another panel
//				DistributionsPanel panel = new DistributionsPanel(vdmackrell, true);
//				panel.addDistribution(vdherring); 
//
			
				
				
//
				
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally { 
			if (null != ncfileHerring) try {
				ncfileHerring.close();
				ncfileher.close();
				ncfilekol.close();
				ncfilemac.close();
			} 
			catch (IOException ioe) {
				System.out.println("trying to close " + filename + ioe.getMessage());
			}
		}
		

		//		HERbiom   995868    
//        HERabun  1161180  
//         HERfat  1326492   165312  true 
////        HERprod  1491804   165312  true
//
//        name____start_____size__unlim
//           T   995880        4  true 
//           X     2844      672  false 
//           Y     3516      492  false 
//        Topo     4008   165312  false 
//       RMask   169320   165312  false 
//          PM   334632   165312  false 
//          PN   499944   165312  false 
//        Long   665256   165312  false 
//        Latt   830568   165312  false 
//     BLUbiom   995884   165312  true 
//     BLUabun  1161196   165312  true 
//      BLUfat  1326508   165312  true 
//     BLUprod  1491820   165312  true 
//


		

		//		Stream.of(1,2,3,4)
		//		.filter(x-> x > 2)
		//		.forEach(System.out::println);

	}

}
