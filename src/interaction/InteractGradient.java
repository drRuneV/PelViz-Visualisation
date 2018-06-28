package interaction;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.plaf.ColorUIResource;

import graphics.ColorInt;
import graphics.GradientPanel;
import graphics.GraphicsUtil;
import visualised.VisualDistribution;



public class InteractGradient   extends Interact{

	Point mouseAt= new  Point();
	private Point mouseDownAt = new  Point();
	private Color colorRemember= new Color(0); 
	int indexNode=0;
	private boolean mDown=false;
	private int selectedIndex;
	private Color colorSelected;

	/**
	 * Constructor
	 * @param distribution 
	 * @param component
	 */
	public InteractGradient(VisualDistribution distribution,Component component) {
		super(distribution,component);
	}


	@Override
	/**
	 * Action to take place when a key is pressed
	 */
	public void keyPressed(KeyEvent e) {
		boolean controlDown= e.isControlDown();
		boolean altDown= e.isAltDown();
		boolean shiftDown= e.isShiftDown();
		boolean only= !controlDown && !altDown &&  !shiftDown;
			
		//boolean mustRedefineGradient=false;
		int key= e.getKeyCode();
		
		//Cleaning
		if(key == KeyEvent.VK_SPACE){
			gradient.clear(Color.BLACK);
			gradient.insert(Color.white, 0);
			gradient.insert(Color.black, 255);
			gradient.reDefine();
		}
		//Saving gradient to a file
		else if(key == KeyEvent.VK_S  &&  controlDown){
			GradientPanel panel= (GradientPanel) component;
			panel.saveTheGradient();
		}
		// Opening a gradient from a file
		else if(key == KeyEvent.VK_O  &&  controlDown){
			GradientPanel panel= (GradientPanel) component;
			File file= panel.accessGradientFileDialogue(null, "open");
			panel.openGradientInformation(file,gradient);
		}
		// delete the current node
		else if(key == KeyEvent.VK_DELETE){
			int index = gradient.nearbyNodeIndexAround(indexAtMouse(), 4);
			if (index>0 && index<255) {
				gradient.getColorIndex().remove(index);
			}
		}
		// Export
		else if(key == KeyEvent.VK_E && controlDown){
			File file= GraphicsUtil.saveImagePath(component,".png");
			try {
				if(file!=null){
				ImageIO.write(gradient.drawGradientOnly(), "png", file);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
		//Import
		else if(key == KeyEvent.VK_I && controlDown){
			File file= GraphicsUtil.openImagePath(component);
			gradient.importFrom(file);
		}
		//Label1
		// Insert colour at the mouse index
		else if(key == KeyEvent.VK_I && only){
			int index= indexAtMouse();
			if (index>=0  && index<256) {
				Color color= gradient.getColors()[index].makeColor();
				gradient.insert(color, index);
			}
		}
		//Create a new colour node, like when clicking
		else if(key == KeyEvent.VK_C  && only){
			int index= indexAtMouse();
			if (index>=0  && index<256) {
				insertColour(index);
			}
			
		}
		// copy the colour
		else if(key == KeyEvent.VK_C  && controlDown){
			int index= indexAtMouse();
			if (index>=0  && index<256) {
			colorRemember= gradient.getColors()[index].makeColor();
			}
		}
		// Paste the colour
		else if(key == KeyEvent.VK_V  && controlDown){
			int index= indexAtMouse();
			if (index>=0  && index<256 && colorRemember!=null) {
				gradient.insert(colorRemember, index);
			}
		}
		// Move the colour nodes
		else if( (key == KeyEvent.VK_UP  || key==KeyEvent.VK_DOWN )&& only ){
			int index = gradient.nearbyNodeIndexAround(indexAtMouse(), 18);
			if (index>0 && index< 255 ){
				int dn= (key==KeyEvent.VK_UP) ? 1: -1;
				colorRemember= gradient.getColors()[index].makeColor();
				gradient.getColorIndex().remove(index);
				gradient.insert(colorRemember, index+dn);
			}

		}
		// Shifting darkness/lightness
		else if( (key == KeyEvent.VK_D  || key == KeyEvent.VK_L) && only){
			int index = gradient.nearbyNodeIndexAround(indexAtMouse(), 4);
			if (index>-1) {
				ColorInt c= gradient.getColorIndex().get(index);
				float f= (key==KeyEvent.VK_D) ? 0.85f: 1.2f ;
				c.darker(f);
			}
		}
		// Shifting colours and transparency
		else if(key == KeyEvent.VK_Q && shiftDown){
			ColorInt c= findNearbyColor(4);
			if (c!=null) {
				c.shiftRGB(5, 0, 0);
			}
		}
		else if(key == KeyEvent.VK_W && shiftDown){
			ColorInt c= findNearbyColor(4);
			if (c!=null) {
				c.shiftRGB(0, 5, 0);
			}
		}
		else if(key == KeyEvent.VK_E && shiftDown){
			ColorInt c= findNearbyColor(4);
			if (c!=null) {
				c.shiftRGB(0, 0, 5);
			}
		}
		// Shifting transparency
		else if( (key == KeyEvent.VK_T || key == KeyEvent.VK_O) && shiftDown){
			ColorInt c= findNearbyColor(4);
			int n= (key== KeyEvent.VK_T)? -10: 10 ;
			if (c!=null) {
				int a= c.getAlpha()+n;
				a=Math.max(Math.min(255,a),0);
				c.setAlpha(a);
			}
		}
		// toggle use background in the gradient image
		else if(key == KeyEvent.VK_B && shiftDown){
			gradient.setUseBackground(!gradient.isUseBackground());
		}
		
		//
		gradient.reDefine();
		component.repaint();
//		System.out.println("key was pressed in interact gradient… Signal");

	}


	/**
	 * @param i 
	 * 
	 */
	private ColorInt findNearbyColor(int i) {
		int index = gradient.nearbyNodeIndexAround(indexAtMouse(), 4);
		ColorInt  c=null;
		if (index>-1) {
			c= gradient.getColorIndex().get(index);
		}
		return c;
	}


	
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

		boolean controlDown= e.isControlDown();
		boolean altDown= e.isAltDown();
		boolean shiftDown= e.isShiftDown();
		boolean only= !controlDown && !altDown && !shiftDown; 

		//Number of rotation of the wheel
		int n= e.getWheelRotation()*(-1);

		
		// change the maximum/minimum of the gradient
		if (only) {
			float dm= (n<0) ? 0.5f: 2.0f ;
			gradient.setMax(gradient.getMax()*dm);
			gradient.define();
			gradient.wasChanged= true;			
		}
		else if(shiftDown){
			gradient.shiftDark(n*5);
		}
		
		// not yet defined
		if (altDown  && controlDown){
		}
		
		gradient.reDefine();
		component.repaint();
	}
	

	@Override
	public void mouseClicked(MouseEvent me) {
		int index= indexAtMouse();
		int node= gradient.nearbyNodeIndexAround(index, 4);
		
		// Index is either a node or a new one 					
		index=  (node>=0 && me.getX()>gradient.getImage().getWidth()/2) ? node: index ;
		//
		if (index>=0  && index<256) {
			insertColour(index);
		}
	}


	/**
	 * @param index
	 */
	private void insertColour(int index) {
		Color color= gradient.getColors()[index].makeColor();
		Color c=JColorChooser.showDialog(null, "Choose a colour to insert",color);
		if (c!=null) {
			gradient.insert(c, index);
			gradient.reDefine();
			component.repaint();
		}
	}
	

	/**
	 * When the mouse is pressed we try to select a colour node and remember the colour
	 */
	@Override
	public void mousePressed(MouseEvent me) {
		mouseDownAt.setLocation(me.getPoint());
		mDown= true;
		int index= gradient.nearbyNodeIndexAround(indexAtMouse(), 4);
		if (index>0 && index<255) {
			selectedIndex= index; 
			colorSelected= gradient.getColors()[index].makeColor();
		}
	}


	/**
	 * Release the mouse button
	 */
	@Override
	public void mouseReleased(MouseEvent me) {
		mDown= false;
		selectedIndex=-1;
	}



	@Override
	public void mouseDragged(MouseEvent me) {
		mouseAt.x = me.getX();
		mouseAt.y = me.getY();

		int index= indexAtMouse();
		boolean  hasColor=gradient.getColorIndex().containsKey(index);
		Color color = (index>-1 && index<256) ? gradient.getColors()[index].makeColor(): Color.black;

		//Try to drag the colour node
		if(colorSelected!=null)
		if(index!=-1 && selectedIndex!=-1 && selectedIndex!=index && color.getRGB()!=colorSelected.getRGB()){
			
			//Shift the colour node to the current index if there is no node there already
			if (!hasColor) {
				gradient.getColorIndex().remove(selectedIndex);
				gradient.insert(colorSelected, index);
				selectedIndex=  index;
				gradient.reDefine();
				component.repaint();
			}
		}
		
	}


	

	@Override
	public void mouseMoved(MouseEvent me) {
		mouseAt= me.getPoint();
		int index= indexAtMouse(); 
		int indexNodeHere = gradient.nearbyNodeIndexAround(index, 4);
		
		
		if (!mDown) {
			//		System.out.println("mouseMoved in InteractionGradient ");
			if (indexNode != indexNodeHere) {
				component.repaint();
			}
			indexNode = indexNodeHere;
		}
		
	}

	/**
	 * 
	 * @return
	 */
	public int indexAtMouse(){
		int positiony=    mouseAt.y- (gradient.getOffy()+1);
		int p= (gradient.getColors().length -positiony);
//		System.out.println(positiony+" "+p);
		// Do not allowed outside
		p= (p<0 || p>255) ? 1:p;
		// Interpret as border values if close to border!
		p= (255-p<5) ? 255 : p ;
		p= (p<5) ? 0: p ;
		return p;
		
		//miny= Math.max(0, image.getHeight()-j-1-offy);
	}

	/**
	 * @return the mouseAt
	 */
	public Point getMouseAt() {
		return mouseAt;
	}



	
	
}
