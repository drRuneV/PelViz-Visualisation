package interaction;


import distribution.Distribution;

public class AreaInformation {

	float totalArea=0;
	float seaArea=0;
	float waterVolume=0;
	float coverage=0;
	float masked=0;
//	float biomass;
	Distribution distribution =null;
	

	/**
	 * Constructor
	 * @param distribution The distribution used for calculations to areas
	 */
	public AreaInformation(Distribution distribution) {
		this.distribution= distribution;
		calculateTotal();
		calculate(0);
	}

	/**
	 * 
	 * @return
	 */
	private void calculateTotal(){
		float fillValue= distribution.getFillV();

		totalArea=0;
		seaArea=0;
		waterVolume=0;
		float value=0;
		float area=0;
		float km=0.000001f; // converter variable  

		// Calculates all fixed areas in square kilometres
		for (int i = 0; i < distribution.getArea().length; i++) {
			area=  distribution.getArea()[i]*km;
			value= distribution.getValues()[i]; 
			totalArea+=area;
			// 
			if (value != fillValue ) {
				seaArea += area;
			}
		}
	
}
	/**
	 * 
	 */
	public void calculateWaterVolume(){
		waterVolume=0;
		float km3=0.000000001f; // cubic metres to cubic kilometres-> divide by 1 billion 

		for (int i = 0; i < distribution.getArea().length; i++) {
			// Volume
			if (distribution.getWaterVolume()!=null ) {
				waterVolume+= distribution.getWaterVolume()[i]*km3;
//				System.out.println("seeVolume: "+waterVolume);
			}
			else System.out.println("Water volume not calculated yet ! "+distribution.getWaterVolume());
		}
	}
	
	/**
	 * 
	 * @param time
	 * @return
	 */
	public String[] calculate(int time){
		coverage=0;
		masked=0;
		float value=0;
		float area=0;
		float km=0.000001f;
		int   dIndex= (distribution.isHasTime()) ? time*distribution.getWH() : 0;

		// Calculates value/mask for the current time step areas in square kilometres
		for (int i = 0; i < distribution.getArea().length; i++) {
			area=  distribution.getArea()[i]*km;
			value= distribution.getValues()[i+dIndex]; 
			// Calculate area covered
			if (value>0) {
				coverage+=area;
				//  biomass 
			}
			// Calculator area which is masked
			if (distribution.getSelectedMask()!=null) {
				if (distribution.getSelectedMask().getMask()[i]==1) {
					masked+=area;
				}
			}
		}

		return createInformationString();	
	}


	/**
	 * Creates 3 strings of information about calculated area
	 * @return 
	 */
	private String[] createInformationString(){
		String information[]= new String[3];
		information[0]=  String.format("Tot: %.0f | Sea: %.0f  Km^2 "
				+ "Vol: %.0f Km^3",totalArea,seaArea, waterVolume);
//		information[1] = String.format("Covered: %.1f Km^2 = %.1f", coverage,100*coverage/seaArea)+"% ";
		information[1] = String.format("Covered: %.1f", 100*coverage/seaArea)+"% ";
		
		// Mask Information
		String infoMask="";
		if(distribution.getSelectedMask()!=null){ 
			if(distribution.getSelectedMask().getPixelCoverage()>0)
				infoMask+= String.format(", Mask:%.1f", 100*masked/seaArea)+"%";
		}

		information[2] = infoMask;
		return information;
	} 


	
}
