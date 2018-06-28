package interaction;

import java.awt.Component;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Map;

import org.jfree.data.xy.XYSeries;

import basicGUI.BiomassFrame;
import ecosystem.SurveyLines;
import graphics.ColorGradient;
import graphics.GraphicsUtil;
import visualised.VisualDistribution;



/**
 * Interact is a class which takes care of interaction in the distribution panel with regard 
 * to the selected distribution and its gradient. 
 * It has information about things like interaction mode, and control charts displayed 
 * interactively. Since MouseAdapter implements all the mouse listeners,
 * interact  by extending MouseAdapter is itself a mouse listener mouse motion listener, 
 * mouse wheel listener.It also takes care of much of the Keyboard interaction.
 * 
 * @author Rune Vabø
 *
 */
public class Interact extends MouseAdapter implements KeyListener{


	protected  VisualDistribution distribution= null;
	protected  ColorGradient	gradient= null;
	protected  Component component= null;
	private	   Rectangle areaSelected=new Rectangle();
	
	protected PositionChart pChart= null; 	
	//
	boolean active=true;
	//The mode of interaction
	public EMode mode= EMode.NORMAL; 
	
	//The type of mode, i.e. in selection mode we enable the selection of polygon, circles and rectangles
	protected int modeType=0;
	protected final String[] modeTypeNames=new String[]{"rectangle","polygon","circle"};
	//
	// Maybe the modeType should be two-dimensional with mode of being one dimension
	// 
//	Map<EMode,Integer> modeTypeMap = new Map();
	
	// The polygon drawn in selection mode 
	//!«•»  we could have several polygons and draw them nicely like we do in distribution panels
	Polygon polygon= new Polygon();
//	ArrayList<MaskPolygon> maskPolygon= new ArrayList<>();
	
	//Mouse
	public Point mouseAt= new Point();
	

	/**
	 * Constructor
	 * @param distribution
	 * @param component
	 */
	public Interact(VisualDistribution distribution, Component component) {
//		super();
		this.component =component;
		if (distribution!=null) {
			this.distribution = distribution;
			gradient= distribution.getGradient() ;
			active=true;
		}
		else {
			active=false;
		}
	} 
	
	
	/**
	 * Create a new chart with reference to distribution
	 */
	public void createChart(){
		pChart= new PositionChart(distribution);
	}

	/**
	 * Remove the chart and its series. Set it to invisible.
	 */
	public void removeChart() {
		if (pChart!=null) {
			pChart.serie.clear();
			pChart.setVisible(false);
			pChart= null;
		}
	}

	/**
	 * Gives the string of the selection type, i.e. rectangle, polygon, circle
	 * @return a string representation of the selection type
	 */
	public String selectionTypeString(){
		return modeTypeNames[modeType];
	}


	@Override
	public void mouseMoved(MouseEvent me) {
		mouseAt.x = me.getX();
		mouseAt.y = me.getY();
	}
	
	@Override
	public void mousePressed(MouseEvent me) {
		
		// Selection Rectangle
		Rectangle r=distribution.getPixelRegion();
		if (mode==EMode.SELECT && r.contains(me.getPoint())) {
			areaSelected.setLocation(me.getPoint());	
		}
	}


	@Override
	public void mouseDragged(MouseEvent me) {
		Rectangle r=distribution.getPixelRegion();
		
		//Selection Rectangle
		if (mode==EMode.SELECT && r.contains(me.getPoint())) {
			int w= me.getX()- areaSelected.x;
			int h= me.getY()- areaSelected.y;
			areaSelected.setSize(w, h);	
		}

	}

	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

		boolean controlDown= e.isControlDown();
		boolean altDown= e.isAltDown();
		boolean shiftDown= e.isShiftDown();
		boolean only= !controlDown && !altDown && !shiftDown; 
		
		 



		//Number of rotation of the wheel
		int n= e.getWheelRotation()*(-1);

		// not yet defined
		if (altDown  && controlDown){
		}
		// change the maximum/minimum of the gradient
//		else if (shiftDown) {
//			float dm= (n<0) ? 0.5f: 2.0f ;
//			gradient.setMax(gradient.getMax()*dm);
//			gradient.define();
			//		defineGradient(true);			
//		}
		// Change difference interval 
		else if (altDown){
			distribution.setDiffInterval(distribution.getDiffInterval()+n);
			System.out.println("difference : " + distribution.getDiffInterval());
		}

		gradient.reDefine();
		component.repaint();
		
		System.out.println("-> The mouse wheel was used !"+e.getWheelRotation());
	}

	
	
	@Override
public void keyPressed(KeyEvent e) {
		
		boolean controlDown= e.isControlDown();
		boolean altDown= e.isAltDown();
		boolean shiftDown= e.isShiftDown();
		boolean shiftDownOnly= e.isShiftDown() && !controlDown && !altDown;
		boolean only= !controlDown && !altDown &&  !shiftDown;

		boolean mustRedefineGradient=false;
		int key= e.getKeyCode();

		if (!active) {
			System.out.println("!-|-|-->This listener is not active!!!");
			return;
		}

		// Make sure distribution exists.Relevant for GradientPanel
		if (distribution!=null) {


			// Toggle show gradient
			if(key == KeyEvent.VK_G  && only){
				distribution.showGradient= !distribution.showGradient;
			}
			// Toggle show histogram
			else if(key == KeyEvent.VK_H && only){
				distribution.showHistogram= !distribution.showHistogram;
			}
			// Toggle show centre of mass
			else if(key == KeyEvent.VK_P && only){
				distribution.showCm=!distribution.showCm;
			}
			// Toggle show trail from centre of mass
			else if(key == KeyEvent.VK_I && only){
				distribution.showTail= !distribution.showTail;
			}
			// Toggles show difference
			else if(key == KeyEvent.VK_Z && only){
				distribution.useDifference=!distribution.useDifference; 
			}
			// Toggles use accumulated
			else if(key == KeyEvent.VK_A && shiftDownOnly){
				distribution.setUseAccumulated(! distribution.isUseAccumulated());
			}

			// Present the statistics
			else if(key == KeyEvent.VK_S  && shiftDownOnly){
				int dx= 50; //this.getTopLevelAncestor().getX();
				distribution.getStatistics().setUseMask(distribution.isUseMaskDrawing());
				distribution.getStatistics().generateStatistics();
				distribution.getStatistics().presenting(dx+50,-1); // //Label1
			}
			// Show area information
			else if(key == KeyEvent.VK_S  && only){
				distribution.setShowAreaInfo(!distribution.isShowAreaInfo());
			}
			// Biomass graph 
			else if(key == KeyEvent.VK_B  && shiftDown && controlDown){
				//Update distribution if changes has been done in the mask or otherwise
				distribution.recalculate(0);
				// BiomassFrame is only called here
				BiomassFrame biomassFrame= new BiomassFrame(distribution);
				
				//LineGraph line= new LineGraph(distribution.getBiomass().getByDay(), title, xtitle, unit);
				// Guest distributions
				int n=0;
				if (!distribution.getGuestList().isEmpty()) {
					for (VisualDistribution vd : distribution.getGuestList()) {
						biomassFrame.getGraph().addSerie(vd.getBiomass().getByDay(), vd.getFullName()+"_g"+n);
						n++;
					}
				}

			}
			//  Show biomass
			else if(key == KeyEvent.VK_B  && shiftDownOnly ){
				distribution.setShowBiomass(!distribution.isShowBiomass());
				distribution.recalculate(0); //In order to recalculate biomass inside mask
			}

		}//distribution
			
				
		// Automatically rescale
		if(key == KeyEvent.VK_R  && shiftDown && controlDown){
			RescaleFrame frame= new RescaleFrame(distribution);
			frame.rescaleAutomatically();
			frame.dispose();
		}

		// Rescale is the distribution of values
		else if(key == KeyEvent.VK_R && controlDown){
			RescaleFrame frame= new RescaleFrame(distribution);
		}
			
			// • Mask relevant •  
			
			//  Toggle use a guest as a mask
			else if(key == KeyEvent.VK_M && altDown){
				distribution.setUseAGuestAsMask(!distribution.isUseAGuestAsMask());
				distribution.recalculate(0); //In order to recalculate biomass inside mask
			}
			
			//Reset the polygon
			else if(key == KeyEvent.VK_K && shiftDownOnly ){
				getPolygon().reset();
			}
			// Finalise the polygon
			else if(key == KeyEvent.VK_ENTER  && only){
				Polygon poly= getPolygon();
				if (poly.npoints>1) {
					distribution.getSelectedMask().drawPolygon(poly, !controlDown);
					distribution.getSelectedMask().removeLand(distribution);
					getPolygon().reset();
				}
				distribution.recalculate(0); //In order to recalculate biomass inside mask
			}
			// Reset the mask completely   
			else if(key == KeyEvent.VK_K && shiftDown && controlDown){
				distribution.getSelectedMask().clearImage();
				distribution.getSelectedMask().findMask();
				getPolygon().reset();
				distribution.recalculate(0); //In order to recalculate biomass inside mask
			}
			// Toggle - drawn inside mask only
			else if(key == KeyEvent.VK_M && shiftDown){
				distribution.setUseMaskDrawing(!distribution.isUseMaskDrawing());
			}
			// Invert the mask
			else if(key == KeyEvent.VK_I  && shiftDown){
//				System.out.println("Trying to invert the mask");
				distribution.getSelectedMask().invert();
				distribution.getSelectedMask().removeLand(distribution);
			}
			// Opening and saving mask
			else if(key == KeyEvent.VK_M && controlDown && !shiftDown){
				distribution.getSelectedMask().saveMask(null);				
			}
			else if(key == KeyEvent.VK_M && controlDown && shiftDown){
			distribution.getSelectedMask().openMaskImage(null);
			}
			
		
		//Change the mode
		if(key == KeyEvent.VK_M && only){			
			mode= mode.next();
//			System.out.println("Mode is: "+modeNumber);
		}
		else if(key == KeyEvent.VK_K && only){
			modeType++;
			modeType= (modeType> modeTypeNames.length-1 ) ? 0: modeType;
			modeType= Math.min(modeType, modeTypeNames.length-1);
			System.out.println("ModeType is: "+modeTypeNames[modeType]);
		}


		// 	- Gradient -
		if(key == KeyEvent.VK_T && only){ //transparency
		}
		else if(key == KeyEvent.VK_O && only){ //transparency
		}
		// - darker/lighter -
		else if(key == KeyEvent.VK_D && shiftDown){
			gradient.shiftDark(1);	
			mustRedefineGradient= true;
		}
		else if(key == KeyEvent.VK_L && shiftDown){
			gradient.shiftDark(-1);	
			mustRedefineGradient= true;
		}
		
		// Manipulating the ColourGradient
		else if(key == KeyEvent.VK_N && only){
			gradient.setMax(gradient.getMax()*0.5f);
			mustRedefineGradient= true;
		}
		else if(key == KeyEvent.VK_B && only){
			gradient.setMax(gradient.getMax()*2);
			mustRedefineGradient= true;
		}
		else if(key == KeyEvent.VK_PERIOD && shiftDown ){
			gradient.setMin(0);
			gradient.setUseOpacityMask(true);
			mustRedefineGradient= true;
		}
			
		
		// Make the gradient stepwise
		else if(key == KeyEvent.VK_S  && only){
//			ColorGradient cg=gradient.makeStepwise(8);
//			gradient.setColorIndex(cg.getColorIndex());
//			gradient.setColors(cg.getColors());
//			cg=null;
//			mustRedefineGradient= true;
			
		}
		// Producing gradient for production/negative values on the lower scale
		else if(key == KeyEvent.VK_A && only){
			gradient.setMin(gradient.getMax()*(-1));
			ColorGradient cg= GraphicsUtil.produceProductionGradient(null, gradient.getMin(), gradient.getMax());
			gradient.setColorIndex(cg.getColorIndex());
			gradient.setColors(cg.getColors());
			gradient.setUseOpacityMask(false);
			mustRedefineGradient= true;
		}
		
		//colour shifting
		else if(key == KeyEvent.VK_Q && only){
			gradient.shiftRed();
			mustRedefineGradient= true;
		}
		else if(key == KeyEvent.VK_W && only){
			gradient.shiftGreen();
			mustRedefineGradient= true;
		}
		else if(key == KeyEvent.VK_E && only){
			gradient.shiftBlue();
			mustRedefineGradient= true;
		}
		// Logarithmic
		else if(key == KeyEvent.VK_Y && only){
			gradient.setUseLogarithmic(!gradient.isUseLogarithmic());
		}

		// Change the ColourGradient
		else if (key>=48 && key<=57 && only){
			int coloringix= key -48 ;//new Integer(s).intValue();
			float[] maxmin= new float[]{gradient.getMax(),gradient.getMin()};
			ColorGradient cg= GraphicsUtil.produceGradient(coloringix,maxmin  );
			gradient.setColors(cg.getColors());
			gradient.setColorIndex(cg.getColorIndex());
			gradient.setName(cg.getName());
			mustRedefineGradient= true;
		}
		
		//
		if (mustRedefineGradient) {
			gradient.reDefine();
		}

		component.repaint();
	}
	
	

	@Override
	public void keyReleased(KeyEvent e) {
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
	}


	/**
	 * @return the distribution
	 */
	public VisualDistribution getDistribution() {
		return distribution;
	}


	/**
	 * @param distribution the distribution to set
	 */
	public void setDistribution(VisualDistribution distribution) {
		this.distribution = distribution;
	}


	/**
	 * @return the gradient
	 */
	public ColorGradient getGradient() {
		return gradient;
	}


	/**
	 * @param gradient the gradient to set
	 */
	public void setGradient(ColorGradient gradient) {
		this.gradient = gradient;
	}


	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}


	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}


	
	/**
	 * @return the mode
	 */
	public EMode getMode() {
		return mode;
	}


	/**
	 * @param mode the mode to set
	 */
	public void setMode(EMode mode) {
		this.mode = mode;
	}


	/**
	 * @return the areaSelected
	 */
	public Rectangle getAreaSelected() {
		return areaSelected;
	}


	/**
	 * @param areaSelected the areaSelected to set
	 */
	public void setAreaSelected(Rectangle areaSelected) {
		this.areaSelected = areaSelected;
	}


	/**
	 * @return the pChart
	 */
	public PositionChart getpChart() {
		return pChart;
	}



	/**
	 * @return the modeType
	 */
	public int getModeType() {
		return modeType;
	}



	/**
	 * @return the modeTypeNames
	 */
	public String[] getModeTypeNames() {
		return modeTypeNames;
	}


	/**
	 * @return the polygon
	 */
	public Polygon getPolygon() {
		return polygon;
	}


	/**
	 * @param polygon the polygon to set
	 */
	public void setPolygon(Polygon polygon) {
		this.polygon = polygon;
	}
	

	
}
