package testing;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.security.auth.Refreshable;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class ImagePanel extends JPanel implements ActionListener , MouseMotionListener{
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Timer T;
	private	BufferedImage image;
	private int width;
	private int height;
	private Graphics2D aGraphics;
	protected static JFrame frame;
	public static int count;
	
	/*
	 * Constructor
	 */
	public ImagePanel(int w, int h){
		this.height=h;
		this.width=w;
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}	
	

	/**  The main method */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int width = 600;
		int height = 600;
        count=0;

        EventQueue.invokeLater(new Runnable() {

		public void run() {
		
        ImagePanel 	graphicPanel= new    ImagePanel(width, height);
        
        // Timer to keep Drawing continuously
        T = new Timer(1, graphicPanel);
        T.start();
        
        Dimension dimension= new Dimension(width, height);
		graphicPanel.setPreferredSize(dimension);
//		graphicPanel.setFocusable(true);
		graphicPanel.addMouseMotionListener(graphicPanel);
		

		// Put the panel on a frame
		frame = new JFrame("GraphicImage pixel test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(width, height);
        frame.setVisible(true);
        frame.setResizable(true);
        frame.add(graphicPanel);
        frame.pack();
        
//        testDrawing(graphicPanel);


        frame.createBufferStrategy(2);
        
//        System.out.println("buffer "+buffer);
        // 
		frame.addKeyListener(        
				new KeyListener() {

					@Override
					public void keyTyped(KeyEvent e) {
						// TODO Auto-generated method stub
						char key = e.getKeyChar();
						if (key=='b'){
							System.exit(0);
						}
						if (key=='m'){
							if (!T.isRunning()) {
							T.start();
							}
							else T.stop();
						}
						System.out.println("key: "+key);  
					}

					@Override
					public void keyPressed(KeyEvent e) {
						// TODO Auto-generated method stub
						char key = e.getKeyChar();
					    if (key == KeyEvent.VK_ESCAPE) {
					        System.exit(0);
					    }
					}

					@Override
					public void keyReleased(KeyEvent arg0) {
					}
				});
		
           }
       });


	}


	/**
	 * @param graphicPanel
	 */
	private static void testDrawing(ImagePanel graphicPanel) {
		int W= graphicPanel.getImage().getWidth();
        int intervals= 220;
        int boxW= W/intervals;
        int color=Color.blue.getRGB();
//        System.out.println(boxW+" "  + boxW*intervals);
        Color c= new Color(color);

        for (int i = 0; i < intervals; i++) {	// Label1
        	if (0+(boxW)*(i+1)<W-1) {
//				System.out.println(" "+ i+ " "+ boxW*(i+1));  
        		color = (int) Math.sin(System.currentTimeMillis()*1.0)*color;

        		try {
					graphicPanel.drawRect(c, 0+(boxW)*i,100, 0+(boxW), 200); //misunderstanding for X  width
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
        }
	}

	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@ Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        count++;

        
        System.out.println("painting component "+count);
       
//        aGraphics
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(image, null, null);
//        BufferStrategy buffer= frame.getBufferStrategy();
//        buffer.show();
//        g2.dispose();
        
    }
//    this works in another application using repaint from action performed 
//    Canvas is a buffered image were all drawing is being done through the graphics, 
//	  Like this: 	Graphics aGraphics = (Graphics2D) canvas.getGraphics();
//	/** Paints the component */ 
//	public void paintComponent(Graphics g) {
//		super.paintComponent(g);
//		Graphics2D g2 = (Graphics2D) g;
//		g2.drawImage(canvas.getImage(), null, null);
//	}
//   " we may want to look into how the buffer strategy is being used " 
//   g.dispose();
//    // flip buffer
//    b.show();
//
//    // cap frame rate to 60 fps
//    do {
//        Thread.sleep(1);
//    } while (System.nanoTime() < lastFrame + 16000000);
//    lastFrame = System.nanoTime();

    

	
	/**  Gets the buffered image  */	
	public BufferedImage getImage() {
		return image;
	}


	public void setImage(BufferedImage image) {
		this.image = image;
	}



	////////////////////////////////////////
	/**  
     * Draws a rectangle by filling single pixels
     *  */
    public void drawRect(Color c, int x1, int y1, int width, int height) {
		 int color = c.getRGB();
		 color = (int) (color*Math.random());
		 // Implement rectangle drawing
		 for (int x = x1; x < x1 + width; x++) {
			 if (x>=image.getWidth()) {
//				 System.out.print(x+" "+"_!!");
			 }
//			 color = (int) (color*Math.random());	
			 for (int y = y1; y < y1 + height; y++) {
				 image.setRGB(x, y, color);
			 }
		 }
//		 System.out.println("drawing rectangle");
    }

	/*
     * Fills the entire canvas  a given colour
     */
    public void fillCanvas(Color c) {
        int color = c.getRGB();
        color= (int) Math.sin(System.currentTimeMillis()*1.0)*color;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                image.setRGB(x, y, color);
            }
        }
	}


	public int getWidth() {
		return width;
	}


	public int getHeight() {
		return height;
	}


	/**  Gets the graphics from the buffered image */
	public Graphics2D getGraphics() {
		return (Graphics2D) image.getGraphics();
	}

	
	public void draw(){

	double y=  100.0f*Math.sin(count * 0.01f);
	double x=  100.0f*Math.cos(count * 0.01f);
	aGraphics = (Graphics2D) image.getGraphics();
	aGraphics.drawOval((int) (x+100),(int) (y+ 100), 50, 50);
	}
	

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		System.out.println("action");
		testDrawing(this);
		
		
		draw();
//		aGraphics.drawImage(image, null, null);


		repaint();
//		this.validate();
//		frame.revalidate();
		paintImmediately(getVisibleRect());
		frame.setSize(frame.getWidth()-1, frame.getHeight()-1);
		frame.setSize(frame.getWidth()+1, frame.getHeight()+1);
		
//      frame.setVisible(true);
//      frame.validate();

	}


	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub		
	}


	@Override
	public void mouseMoved(MouseEvent arg0) {
		repaint();
		System.out.println(" mouse was moved");
	}

}
