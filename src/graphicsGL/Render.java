package graphicsGL;



import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import ecosystem.Coordinate;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Canvas;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;



/**
 * 
 * @author Rune Vabø
 *
 */
public class Render {

	private Canvas canvas = null;
	private int count;
	private int frames;
	private boolean waiting=false;
	
	

	/**
	 * Constructor
	 * 
	 * @param canvas - The canvas for attaching the display
	 */
	public Render(Canvas canvas) {
		this.canvas=canvas;
		count=0;

	}

	/**
	 * 
	 */
	public void start(){

		setupOpenGL();
		visualisationLoop();
		// Only when the program exits do we need to clean up and destroy the 	display. 
		// We need to pause the visualisation loop, exit and start again without destroying the display.
		cleanUp();
		
	}


	//  We need to set up OpenGL code by initialising the display and OpenGL stuff.
	private void setupOpenGL(){
		initDisplay();
		initGL();
		
	}


	/**
	 * 
	 */
	private void initDisplay() {

		try {
			
		Display.setDisplayMode(new DisplayMode(800, 600  ));
        
        Display.setLocation(30, 20);
        Display.setResizable(true);
        Display.setSwapInterval(1);

        //  We will set the parent of the display to be the Canvas
        if (canvas.getParent() != null){
        	Display.setParent(getCanvas());
        }
        System.out.println(canvas.getParent());
     
        Display.create();
        System.out.println(" The display was created");
        
        // Create keyboard and mouse
        Keyboard.create();
//        Mouse.create();
        
        
    } catch (LWJGLException ex) {
        Logger.getLogger(Render.class.getName()).log(Level.SEVERE, null, ex);
    }
          
}

	


	private void initGL() {
		//         
	     glMatrixMode(GL_PROJECTION);   
	     glLoadIdentity();
	     glOrtho(0,Display.getWidth(),0,Display.getHeight(),-1,1);
	     
	     glClearColor(0.0f,0.1f,0.2f,0.0f );
	     glMatrixMode(GL_MODELVIEW);
	        
	     glDisable(GL_DEPTH_TEST);
	     //
	     glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
	     glEnable(GL_BLEND);     
	     glEnable(GL_POINT_SMOOTH);
	     glEnable(GL_LINE_SMOOTH);
//	     glEnable(GL_DEPTH_TEST);

		
	}




	

	private void visualisationLoop() {
		// the loop goes here
		//========================================
		while (!Display.isCloseRequested()) {

			glClear(GL_COLOR_BUFFER_BIT );
			draw();
			count++;
			handleMouse();
			Display.update();
			
			
//			System.out.println(" Drawing inside loop");
		}
	}

	/**
	 * Should handle all mouse input
	 */
	private void handleMouse(){

		boolean ok=  true;
		boolean leftMousedown= Mouse.isButtonDown(0);
		boolean rightMousedown= Mouse.isButtonDown(1);
		
		/**  left click Mouse */
		if (leftMousedown && ok){
		}	    
		
		if(ok){
			glRec(Mouse.getX(),Mouse.getY(),5);
		
		}
	}
	
	/**
	 * In OpenGL 2D the origo is at the lower left corner of the display.
		We translate this origo to the centre of the screen.

	 */
	public void draw(){
		float radius=  (float) Math.sin(0.05*count);
		float w=50;
		
		Vector2D v[]= Coordinate.testing((int) (1800+20*Math.cos(count)));
		
		glPushMatrix();
//			glRotatef((float) (10*count), 0, 0, 1);
			glTranslatef(Display.getWidth()/2f, Display.getHeight()/2f,0);
	
			drawPositions(v);
			Rotate();
			
			
			glColor4f( 0.0f, 0.2f, 1.0f, 1.0f);// (float) (1-radius*0.9f)); 
//			glRec(Display.getWidth()/2f+40, Display.getHeight()/2f+40, (float) (40*radius));
			glRec(0,0,50);
			glColor4f( 1f, 0, 0.0f,1.0f);
			glRec(0,50,50);
			glColor4f( 0f, 0.5f, 0.0f,1.0f);
			glRec(0,-50,50);
			
//			glColor4f(0.5f, radius, 0.0f,1.0f);
			glRec(0,0,20);
			
			glRec(0,50,50);
		glPopMatrix();

	}
	
	private void Rotate(){
		Vector2D p= new Vector2D(0, 50.0f);
		Vector2D p1= new Vector2D(0, -50.0f);

//		glPushMatrix();
		glColor4f( 1f, 1f, 1.0f,1.0f);
//		glTranslatef(Display.getWidth()/2f, Display.getHeight()/2f,0);

		p.rotate(0.5f*count);
		p1.rotate(-0.2f*count);
//		System.out.println("p: "+p);
		glBegin(GL_LINE_STRIP);
//		glRec(0, 0, 200);
		  glVertex2f(0,0);		
		  glVertex2f(p.getX(),p.getY());
		  glColor4f( 1f, 0.5f, 1.0f, 0.8f);
		  glVertex2f(p1.getX(),p1.getY());
		  glVertex2f(0,0);
		  
		glEnd();
		
		glPointSize(5.5f); //never PointSize inside glBegin-glEnd !
		glBegin(GL_POINTS);
		glVertex2f(p.getX()*2,p.getY()*2);
		glVertex2f(p1.getX()*2,p1.getY()*2);
		glEnd();
		
//		glPopMatrix();


	}
	
	
	private void drawPositions(Vector2D[] v) {
		float scale=200.0f;
		//
//		glPushMatrix();
//		glTranslatef(Display.getWidth()/2f, Display.getHeight()/2f,0);
		glColor4f( 1f, 1f, 0.50f, 1.0f);
		glPointSize(2.5f); //never PointSize inside glBegin-glEnd !

		for (int i = 0; i < v.length; i++) {
			{    
				v[i].scale(scale);
				glBegin(GL_POINTS);
				
				glVertex2f(v[i].getX(),v[i].getY());
				glEnd();	
				
//				System.out.println(i+": "+v[i]);
			}
		
		//	glPopMatrix();

		}

	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param width
	 */
	private void glRec(float x, float y, float width) {
	    
		glBegin(GL_LINE_LOOP);
		{
			glVertex2f( x, y);
			glVertex2f(x+width, y);
			glVertex2f(x+width , y+width);
			glVertex2f(x, y+width);
		}
		glEnd();
	
	}


	/**
	 * 
	 */
	private void cleanUp() {
		Display.destroy();
		System.out.println(" Display is destroyed ");
	}


	/**
	 * 
	 * @return
	 */
	public boolean check(){
		
		int ix=0;
		boolean pressed=false;
		String characters="";
		frames++;

		// CHECKING the keyboard
		//============================= 
		do {
			if (Keyboard.getEventKeyState()) characters+= checkKeyboard();

			if(ix>0){
				System.out.println("ix : " + ix+" String: " + characters+ " waiting "+waiting);
			}
			ix++;

		}while (Keyboard.next()  && !waiting);
		pressed= (!characters.isEmpty());
		
		return pressed;     
	}

	
	/**
	 * 
	 * @return
	 */
	private char checkKeyboard() {
		char ch= Keyboard.getEventCharacter();
	return ch;			
}


	/**
	 * 
	 * @return - The canvas where the display is drawn upon    
	 */
	public Canvas getCanvas() {
		return canvas;
	}


	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
	}

	
	public void showInJFrame(Canvas canvas){

		JPanel panel= new JPanel();
		panel.setSize(canvas.getWidth(), canvas.getHeight());
		panel.add(canvas);
				
		JFrame frame= new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(250, 50);
		frame.setSize(canvas.getWidth(), canvas.getHeight());
        frame.setResizable(true);
        frame.setVisible(true);
        frame.add(panel);
        frame.pack();
	}

	
	public static void main(String[] args) {
		int width= 900;
		int height=600;
		
		Canvas canvas= new Canvas();
		canvas.setSize(width, height);
		
        Render render= new Render(canvas);
		render.showInJFrame(canvas);
		
		render.start();
	}

}