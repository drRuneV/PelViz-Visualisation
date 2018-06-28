package graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import testing.TestValues;

/**
 * 
 * @author Admin
 *
 */
public class ValueJPanel extends JPanel implements KeyListener, ActionListener,
										MouseListener, MouseMotionListener{

	private JLabel infoJLabel;
	private static final long serialVersionUID = 1L;
	private BufferedImage image;
	private int count=0;
	private Point  offset= new Point(4500, 200);  
	private static Timer T;
	private TestValues values;
	//later we want to pass in a grid distribution instead of the test values.!!
	// «•»
	private Point offspeed= new Point(0,0);
	private BufferedImage bi=null;
	private BufferedImage bi2;
	// Mouse
	private Point mouseDownAt = new Point();
	private Point mouseAt= new Point();
	private boolean mDown=false;

	
	
	// ==================================================
	
	/**
	 * Constructor 
	 */
	public ValueJPanel(TestValues values) {
		
		// We now have access to the values
		this.values = values;
		
		int width= values.getWidth();
		int height= values.getHeight();
//		Dimension dimension= new Dimension(width,height);
		Dimension dimension= new Dimension(1600,900);
		setPreferredSize(dimension);
		this.setFocusable(true);
		this.requestFocusInWindow();

		// Create the image we are drawing
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		// Insert values into BufferedImage
		values.insertValues(image);
		values.darkenedValue(image,60);
//		

		//  Create and start the timer
		T = new Timer(20, this);
		T.setInitialDelay(500);
	    T.start(); 
	    // As listeners and other components
	    this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.addKeyListener(this);
		infoJLabel= new JLabel("info");
		this.add(infoJLabel);
	}




	/**
	 * Paint the component, continuously using a timer linked to actionPerformed
	 */
	@ Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		count++;
			
		if(bi!=null)changeOffset();
		
		if (count%100==0) System.out.println("painting component "+count);

		//  Redraw the image every time on the components Graphics
		Graphics2D g2 = (Graphics2D) g;
		g2.setBackground(Color.BLACK);
		g2.clearRect(0, 0, image.getWidth(), image.getHeight());
		
		
		bi= image.getSubimage(offset.x, offset.y, Math.min(1600,image.getWidth()-offset.x), 
				Math.min(900,image.getHeight()-offset.y));
//					image.getWidth()-offset.x, image.getHeight()-offset.y);

		//Create another BufferedImage
		bi2 =new BufferedImage(bi.getWidth(),bi.getHeight() , BufferedImage.TYPE_INT_ARGB);
		Graphics2D gbi = bi2.createGraphics();
		gbi.drawImage(bi, 0, 0,null, null);	
		drawColourGradient(gbi);
		displayValueInfo(mouseAt.x, mouseAt.y);
		
		// Finally draw the image
		g2.drawImage(bi2, 0, 0,null, null);

		g2.dispose();
		gbi.dispose();
	}

	/**
	 * 
	 * @param g
	 */
	private void drawColourGradient(Graphics2D g){
//		values.drawGradientonImage(bi);
		values.gradient.drawGradient();
		g.drawImage(values.gradient.getImage(),5,5,null,null);
	}


	/**
	 * 
	 */
	private void changeOffset() {
		offset.x+= offspeed.x;
		offset.y+= offspeed.y;
		offspeed.x*= 0.96f;
		offspeed.y*= 0.96f;
		offset.x= Math.min(image.getWidth()-bi.getWidth()-1, Math.max(0, offset.x) );
		offset.y= Math.min(image.getHeight()-bi.getHeight()-1,Math.max(0,offset.y));
	}
	
	
	@Override
	/**
	 * This action occurs when the timer triggers
	 */
	public void actionPerformed(ActionEvent arg0) {
		if (count> 5000) {
			System.exit(0);
		}
		repaint();
		
	}

	/**
	 * Displays information about the value where the mouse is.
	 * @param me - The mouse event
	 */
	private void displayValueInfo( int x1,int y1){ //MouseEvent me){
		
		Graphics2D g2 = (Graphics2D) bi2.createGraphics();
		int x = x1+ offset.x;
		int y = y1+offset.y;
		// Get the values from the current position
		float v = values.getValues()[x+ y*values.getWidth()];
		// Build the String we want to display
		String str="v= "+v+" at xy:"+x+":"+y;
		int posX= 100;//+ offset.x;// values.getWidth()/2;
		int posY= 20;
		g2.setBackground(new Color(0.3f,0.3f,0.3f,0.6f));
		g2.setColor(new Color(0.15f,0.15f,0.2f,0.5f));
		g2.fillRoundRect(posX, posY, 170, 20, 18,8);
		g2.setColor(Color.LIGHT_GRAY);
		g2.drawString(str, posX+10, posY+15);
	}
	
	
	
	
	
	@Override
	/**
	 * //Label1
	 */
	public void mouseClicked(MouseEvent me) {
		int x = me.getX();
		int y = me.getY();
		
		displayValueInfo(x,y);
		
		System.out.println("Mouse was clicked at "+x+" :"+y); 
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override 
	public void mousePressed(MouseEvent  me) {
		mouseDownAt.setLocation(me.getX() ,me.getY());
		mDown=true;
		System.out.println("mousePressed! "+ mDown);
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		mouseAt.setLocation(me.getX() ,me.getY());
//		offset.x +=   mouseDownAt.x-mouseAt.x;:
		offspeed.x+= (mouseDownAt.x-mouseAt.x)/5;
		offspeed.y+= (mouseDownAt.y-mouseAt.y)/5;
//		offset.y += mouseDownAt.y-mouseAt.y;
		System.out.println("mouse released "+(mouseAt.x -mouseDownAt.x));
		offset.x = Math.max(0,offset.x);
		offset.x = Math.min(image.getWidth(),offset.x);
		mDown=false;
	}


	@Override
	public void mouseMoved(MouseEvent me) {
		displayValueInfo(me.getX(),me.getY());
		mouseAt.setLocation(me.getX() ,me.getY());
//		if(mDown){
//			offset.x += mouseAt.x -mouseDownAt.x;
//			System.out.println("-Mouse was moved!!");
//		}
	}




	@Override
	public void keyPressed(KeyEvent e) {
		char keyc = e.getKeyChar();
		int key = e.getKeyCode();
		
		if (key == KeyEvent.VK_ESCAPE) {
			System.exit(0);
			System.out.println("pressing the escape");
		}
		else if(key == KeyEvent.VK_LEFT){
			offset.x--;
			offspeed.x-=8;
			offset.x = Math.max(0,offset.x);
		}
		else if(key == KeyEvent.VK_RIGHT){
			offset.x++;
			offspeed.x+=8;
			offset.x = Math.min(image.getWidth(),offset.x);
			System.out.println("«•» we press the right");
		}
		else if (key ==KeyEvent.VK_DOWN){
			offset.y++;
			offspeed.y+=8;
			offset.y = Math.max(0,offset.y);
		}
		else if (key ==KeyEvent.VK_UP){
			offset.y--;
			offspeed.y-=8;
			offset.y = Math.min(image.getHeight(),offset.y);
		}
		else if (key ==KeyEvent.VK_N){
			offset.x= 4500;
			offset.y= 300;
		}
		else if (key ==KeyEvent.VK_A){
			offset.x= 2300;
			offset.y= 1400;
		}
		else if (key ==KeyEvent.VK_E){
			offset.x= 5000;
			offset.y= 800;
		}
		else if (key ==KeyEvent.VK_I){
			offset.x= 8200;
			offset.y= 2200;
		}

		System.out.println("Keystroke:"+key+" "+e.getKeyCode());

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	
}
