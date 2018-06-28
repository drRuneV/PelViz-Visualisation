package testing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Timer;

import graphics.GraphicsUtil;


/**
 * This test class is used to draw a BufferedImage on a panel within a separate frame. 
 * The test panel receives a BufferedImage and adjusts its size accordingly.
 * Any class that needs to test to draw something on a image can use this test panel,
 * provided it creates its own frame where the panel is put.
 * @author Rune Vabø
 *
 */
public class testPanel extends JPanel implements KeyListener, ActionListener, 
												MouseListener,MouseMotionListener {

		
	/**
	 * 
	 */
	private static final long serialVersionUID = 2603345111598813176L;
	private BufferedImage image;
	private int count=0;
	private static Timer T;

	/**
	 * @param doscale 
	 * 
	 */
	public testPanel(BufferedImage source,int width,int height, boolean doscale) {
		
		// Try to rescale
		int scale= (doscale)? 4:1;
//		System.out.println("Image width="+ width+" ×"+scale);
				
		Dimension dimension= new Dimension(width*scale, height*scale);
		setPreferredSize(dimension);
		//
		T = new Timer(900, this);
	    T.start();
	    // This image points to the source and does not need to be created separately
		this.image =  (doscale)? GraphicsUtil.resizeImage(source, width*scale, height*scale) :source;
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
	}


	@ Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		count++;

//		System.out.println("painting component "+count);

		//        aGraphics
		Graphics2D g2 = (Graphics2D) g;
		Graphics2D g3 = (Graphics2D) g; //image.getGraphics();
		
//		g2.clearRect(0, 0, image.getWidth(), image.getHeight());
		g2.drawImage(image, 0,0,null, null);
		g3.drawRect((int) (5+ 50*Math.sin(count)), 15, 50, 20);

		g2.dispose();
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		Graphics2D g2 = (Graphics2D) image.createGraphics();
		int x = me.getX();
		int y = me.getY();
		String str="y:"+y;
		g2.drawString(str, x, y);
		System.out.println("Mouse was clicked at "+x+" :"+y); 
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (count> 25) {
			System.exit(0);
		}
		repaint();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		char key = e.getKeyChar();
	    if (key == KeyEvent.VK_ESCAPE) {
	        System.exit(0);
	        System.out.println("pressing the escape");
	    }
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}


	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
