package distribution;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class BiomassMeasure {
	
	//  The distribution to calculate from
	Distribution  distribution=null;
	// Biomass measures
	float total=0;
	float byDay[];
	float byDayMask[];
	float totalMask=0;
	String unit="";
	boolean  useMetricTons= true;
	// The image for drawing information and graphs
	BufferedImage image =null;
	// Frame to present the biomass information 
	JFrame frame =null;
	private static float million=1000000f;
	
	/**
	 * Constructor
	 * @param distribution
	 */
	public BiomassMeasure(Distribution distribution) {
		this.distribution=distribution;
		if (distribution.isHasTime()) {
			int t= distribution.getTime();
			byDay= new float[t];
			byDayMask= new float[t];
			calculate();
		}
		
	}

	
	

	/**
	 * Calculates everything.
	 * Biomasses are calculated by multiplying by area, giving kg C (ukg C in case of zooplankton)  
	 */
	public void calculate(){
		boolean ok=false;
		float area=0;
		float biomass=0;
		int t=0;
		int i=0;
		int index=0;
		float f=1;
		
		unit= String.format("%s",distribution.getUnit() );
		
		unit= unit.replaceAll("g C", "kg C"); ////gram to kg !
		unit= unit.replaceAll("m-2","");
		// Metric tons
		if (useMetricTons) {
			unit= unit.replaceAll("kg C", "MT C");
			f = million*1000;
		}
		// Reset every value
		reset();
		// 
		for (float value : distribution.getValues()) {

			ok = value>0 &&  value!=distribution.getFillV() && value!=distribution.getMissingV();

			if (ok) {
				t=i/distribution.getWH();
				// Index within grid
				index= (i>=distribution.getWH()) ? i-distribution.getWH()*t: i ;
				area= distribution.getArea()[index];
				// biomass 	->	area is in square metres!
				biomass= area*value/(1000*f);// 0.001f; //gram to kg !
				total+= biomass;
				byDay[t]+=biomass;
				// Mask
				if (distribution.getSelectedMask()!=null) {					
					if(distribution.getSelectedMask().getMask()[index]!=0){
						totalMask+=biomass;
						byDayMask[t]+= biomass;
					} 
				}
			}
			i++;
		}
	}



	/**
	 * 
	 */
	private void reset() {
		total=0;
		totalMask=0;
		for (int i = 0; i < byDay.length; i++) {
			byDay[i]=0;
			byDayMask[i]=0;
		}
	}

	
	
	public void displayOnPanel(){
		 
	}
	
	
	/**
	 * Creates a string of biomass information, average, total, masked, by day
	 * @return a string of biomass information
	 */
	public String displayBiomassInformation(){

		System.out.println("unit.contains µkg? "+unit.contains("ukg"));
		String information="";
		int t = byDay.length;
		
		information = distribution.getFullName()+
			String.format(" Average : %.2f  Masked : %.2f ", total/(t),totalMask/t)+unit +"\n";
		float metricTonn= (float) ((unit.contains("ukg")) ? total/(million*1000*t): total/(1000*t)); 
		information+=String.format("Total Mt: %.3f \n", metricTonn/million);
		//
		
		for (int i = 0; i < byDay.length; i++) {
			if (i%10==0) {
				information+= String.format("Day:%d   %.2f ", i,byDay[i])+unit+ "\n";
			}
		}

		return information;
	}



	/**
	 * Creates a string representing the total metric tons of biomass for the given day
	 * @param t the given day/time
	 * @return a string representing the total metric tons of biomass
	 */
	public String totalMetricTonnString(int t){
		//  kilograms or metric tons
		float biomass= (float) ((unit.contains("ukg")) ? byDay[t]/(million): byDay[t]);
		// Kilograms or metric tons
		String s= (useMetricTons) ? String.format("Total : %.3f M t ", biomass) :
									String.format("Total: %.3f kg", biomass);
		
		return s;
	}


	/**
	 * @return the total
	 */
	public float getTotal() {
		return total;
	}




	/**
	 * @return the totalMask
	 */
	public float getTotalMask() {
		return totalMask;
	}




	/**
	 * @return the byDay
	 */
	public float[] getByDay() {
		return byDay;
	}




	/**
	 * @param byDay the byDay to set
	 */
	public void setByDay(float[] byDay) {
		this.byDay = byDay;
	}




	/**
	 * @return the byDayMask
	 */
	public float[] getByDayMask() {
		return byDayMask;
	}




	/**
	 * @param byDayMask the byDayMask to set
	 */
	public void setByDayMask(float[] byDayMask) {
		this.byDayMask = byDayMask;
	}




	/**
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}




	/**
	 * @param unit the unit to set
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}




	

	
}
