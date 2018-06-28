package testing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ImageFrame  extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8862488924885257817L;
	private Component panel;

	
	/**
	 * Constructor 
	 * @param title
	 * @param p
	 */
	public ImageFrame(String title, JPanel p, Rectangle r) {// throws HeadlessException {
		super(title);
		this.panel= p;

		//		super(new BorderLayout());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		System.out.println("location "+r.x+" "+r.y);
		setLocation(5+r.x, r.y);
		setResizable(true);
		setVisible(true);
		panel.setFocusable(true);
		//
		if (panel.getKeyListeners()!=null   &&  panel.getMouseMotionListeners()!=null) {
			addKeyListener(panel.getKeyListeners()[0]);
			addMouseMotionListener(panel.getMouseMotionListeners()[0]);
		}
		add(panel);
		pack();
	}

	
	

}
