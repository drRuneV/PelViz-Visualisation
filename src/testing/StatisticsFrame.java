package testing;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class StatisticsFrame extends JFrame{

	public StatisticsFrame(){
		JPanel panel = new JPanel(new MigLayout());

	    JLabel lMax= new JLabel("Max:");
	    JLabel lMin= new JLabel("Min:");
	    JLabel lMedian= new JLabel("Median:");
	    JLabel lAverage = new JLabel("Average :");
	    JLabel lCount= new JLabel("Count :");
	    Canvas canvas= new Canvas();  
	    canvas.setBackground(Color.blue);
	    canvas.setForeground(Color.black);
	    canvas.setSize(20, 50);
	    		
	    panel.add(lMin);
	    panel.add(lMax);
	    panel.add(lMedian);
	    panel.add(lAverage ,       "gap unrelated, wrap");
	    panel.add(lCount,   "wrap");
	    panel.add(canvas, "span, grow");
	    
	    canvas.repaint();
	    
	    this.add(panel);
	}
	
	
	public static void main(String[] args) {
		StatisticsFrame frame = new StatisticsFrame();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true) ;
		frame.setLocation(300, 300);
		frame.setSize(400, 400);
		
	}
	
}
