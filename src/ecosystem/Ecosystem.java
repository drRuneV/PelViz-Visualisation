/**
 * 0.1745329252*90= 1.5707964	: unit distance
 * 1,57*6,37= 10,0009 : thousand kilometres from north pole to equator  
 */
package ecosystem;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JPanel;

import graphics.DistributionsPanel;
import interaction.LineGraph;
import visualised.VisualDistribution;

/**
 * @author a1500
 *
 */
public class Ecosystem {
	
private DistributionsPanel panel= null;
private Graphics2D graphics =null;

//
private EcoVisualiser ecoVisualiser= null;
//
//SurveyLines 
private SurveyLines surveyLine= null;
//
	

//The Earth radius in metres
public static final int EARTHRADIUS= 6371000;
//1° in radians
public static final double TORAD= 0.01745329252;
//At about the centre of the North Sea
private static final Coordinate NORTHSEA= new Coordinate(70, 0);

//Origin  (Place the origin at the centre of the North Sea)
private Coordinate Origin= new Coordinate(70, 0); 

// 
public static String[] months= new String[]{
		"January","February","March","April","May","June",
		"July","August","September","October","November","December"};





public Ecosystem(DistributionsPanel panel) {
	// 
	this.panel= panel;
	ecoVisualiser= new EcoVisualiser(this);
}

/**
 * 
 * @param origin
 */
public Ecosystem(Coordinate origin) {
	Origin = origin;
}

public void display(Graphics2D graphics){
	this.graphics=graphics;
	ecoVisualiser.displayGraphics(graphics);
}

/**
 * 
 * @param s
 * @return
 */
public static int indexOfMonth(String s){
	int index= 0;
	for (int i = 0; i < months.length; i++) {
		if (months[i].toLowerCase().contains(s.toLowerCase())) {
			index=i;
			break;
		}
	}
	return index;
}

/**
 * @param origin
 */
public void setOrigin(Coordinate origin) {
	Origin = origin;
}



	/**
	 * 
	 * @return - The origin of the ecosystem
	 */
	public Coordinate getOrigin() {
	return Origin;
}

	

	/**
	 * @return the graphics
	 */
	public Graphics2D getGraphics() {
		return graphics;
	}


	/**
	 * @param graphics the graphics to set
	 */
	public void setGraphics(Graphics2D graphics) {
		this.graphics = graphics;
	}

	/**
	 * @return the ecoVisualiser
	 */
	public EcoVisualiser getEcoVisualiser() {
		return ecoVisualiser;
	}

	/**
	 * @param ecoVisualiser the ecoVisualiser to set
	 */
	public void setEcoVisualiser(EcoVisualiser ecoVisualiser) {
		this.ecoVisualiser = ecoVisualiser;
	}

	/**
	 * @return the surveyLine
	 */
	public SurveyLines getSurveyLine() {
		return surveyLine;
	}

	/**
	 * @param surveyLine the surveyLine to set
	 */
	public void setSurveyLine(SurveyLines surveyLine) {
		this.surveyLine = surveyLine;
	}

	/**
	 * @return the panel
	 */
	public DistributionsPanel getPanel() {
		return panel;
	}

	/**
	 * @param panel the panel to set
	 */
	public void setPanel(DistributionsPanel panel) {
		this.panel = panel;
	}

	/**
	 * Creates a plot showing biomass values along the survey
	 * @param distribution 
	 */
	public void thePlotAlongSurvey(VisualDistribution distribution) {
		if (surveyLine!=null) {
			// Make sure we have values
			surveyLine.insertValues(distribution.getValues(), distribution.getFillV(), distribution.getWH());
			// Create the plot
			LineGraph surveyPlot= new LineGraph(surveyLine.getValues(),
					distribution.getFullName()+ " Along survey", "Position", 
					distribution.getUnit());
			
			// Include the guests
			for (VisualDistribution vdguest : distribution.getGuestList()) {
				surveyLine.insertValues(vdguest.getValues(), vdguest.getFillV(), vdguest.getWH());
				surveyPlot.addSerie( surveyLine.getValues() ,vdguest.getFullName());
			}
			// Redefine back to current distribution
			surveyLine.insertValues(distribution.getValues(), distribution.getFillV(), distribution.getWH());
			// Show the plot
			Point p = getPanel().theRightLocation();
			surveyPlot.show(p);
		}

	}

	/**
	 * Defines a survey line from a incoming coordinate, typically from a mouse click in distribution's panel.
	 * @param c a coordinate 
	 */
	public void defineSurveyLine(Coordinate c) {
//		Interact with a survey line by defining new positions
		VisualDistribution distribution= getPanel().getDistribution() ;
		if (ecoVisualiser.isShowSurveyLine()){
			if(surveyLine!=null){

				if (!surveyLine.isItFinished() ) {				
					surveyLine.measure(c);
					//Update values
					if (surveyLine.getLength()>100) {
						surveyLine.insertValues(distribution.getValues(), distribution.getFillV(), distribution.getWH());
					}
				}
			}
			//we could create a survey line here?????
		}
		
	}

	
	/**
	 * Finalises a survey line . Typically after a user commands this.
	 * 
	 */
	public void finaliseSurveyLine() {

		if (getEcoVisualiser().isShowSurveyLine()) {
			// Finalise
			if ( !getSurveyLine().isItFinished()) {
				getSurveyLine().setItFinished(true);
			}
			// Otherwise 
			else {
				getSurveyLine().measureCoordinate.clear();
				getSurveyLine().setItFinished(false);
			}
		}

	}






}
