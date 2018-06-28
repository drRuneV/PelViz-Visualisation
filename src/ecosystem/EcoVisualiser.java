package ecosystem;

import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JPanel;

import visualised.VisualDistribution;

/**
 * EcoVisualiser visualises content for the ecosystem.
 * Primarily visualises distributions, survey lines and transects on a swing panel (JPanel) 
 * using the graphics from the distribution's panel object.
 * We may implement other ways of visualising the content of the ecosystem.
 * @author a1500
 *
 */
public class EcoVisualiser {

	private Ecosystem ecosystem=null;
	private Graphics2D graphics =null;

	//
	private boolean showSurveyLine=false;


	
	
	/**
	 * Constructor
	 * @param ecosystem
	 */
	public EcoVisualiser(Ecosystem ecosystem) {
		this.ecosystem = ecosystem;
//		this.graphics= ecosystem.getGraphics();
	}
	

	public void displayGraphics(Graphics2D g){
		graphics=g;
		
		
		
		if (showSurveyLine && ecosystem.getSurveyLine()!=null){
			drawSurveyLine();
//			System.out.println("Drawing ecosystem visualiser surveyLine");
		}

	}


	/**
	 * 
	 */
	private void drawSurveyLine() {
		//
		VisualDistribution distribution= ecosystem.getPanel().getDistribution();
		float scale= ecosystem.getPanel().getScale();
		int count=  ecosystem.getPanel().getCount();   
		
		SurveyLines surveyLine= ecosystem.getSurveyLine();
		
		int w= (int) (distribution.getImage().getWidth()*scale); 
		int h= (int) (distribution.getImage().getHeight()*scale);
		
		// This is arbitrary now �|�|�Handling date is necessary 
		int nPrStep= surveyLine.getLength()/distribution.getTime()-1;
		// Values from current distribution 
		if (surveyLine.getValues()==null) {
			surveyLine.insertValues(distribution.getValues(), distribution.getFillV(), 
						distribution.getWH());
		}
		// Drawing			
		int npoints= Math.max(nPrStep*count, surveyLine.measureCoordinate.size()) ;
		//			System.out.println("Max points to draw is "+npoints);
		
		surveyLine.draw(scale, npoints, w,h);
		Point pos= new Point(distribution.getPixelRegion().x, distribution.getPixelRegion().y);
		// 
		
		// Place the survey line image on the graphic
		graphics.drawImage(surveyLine.getImage(),pos.x, pos.y,null,null);
	}


	
	/**
	 * @return the ecosystem
	 */
	public Ecosystem getEcosystem() {
		return ecosystem;
	}


	/**
	 * @param ecosystem the ecosystem to set
	 */
	public void setEcosystem(Ecosystem ecosystem) {
		this.ecosystem = ecosystem;
	}


	/**
	 * @return the showSurveyLine
	 */
	public boolean isShowSurveyLine() {
		return showSurveyLine;
	}


	/**
	 * @param showSurveyLine the showSurveyLine to set
	 */
	public void setShowSurveyLine(boolean showSurveyLine) {
		this.showSurveyLine = showSurveyLine;
	}
	
	
	
	

}
