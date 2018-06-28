package basicGUI;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.SystemColor;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import java.awt.CardLayout;
import javax.swing.BoxLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import distribution.BiomassMeasure;
import interaction.LineGraph;
import visualised.VisualDistribution;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JToggleButton;
import javax.swing.ButtonGroup;
import net.miginfocom.swing.MigLayout;
import java.awt.Cursor;
import java.awt.Dimension;

public class BiomassFrame extends JFrame {

	private JPanel contentPane;

	// 
	private LineGraph graph =null;;//ChartPanel chartPanel=null;
	/**
	 * @wbp.nonvisual location=41,194
	 */
	private final ButtonGroup buttonGroup = new ButtonGroup();


//	/**
//	 * Launch the application.
//	 */
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					BiomassFrame frame = new BiomassFrame();
//					frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}

	/**
	 * Create the frame.
	 */
	public BiomassFrame(VisualDistribution distribution) {
		setTitle("Biomass");
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 306, 300);
		contentPane = new JPanel();
		contentPane.setMinimumSize(new Dimension(400, 250));
		contentPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		// Create the graph
		BiomassMeasure bm= distribution.getBiomass() ;
		String title= distribution.getFullName();
		graph= new LineGraph(bm.getByDay(),title, "Day", bm.getUnit());
		graph.defineTheAxis(distribution.getStatistics().getAverage()*5);
		if (distribution.getSelectedMask()!=null) {
			if (distribution.getSelectedMask().getPixelCoverage()>0) {
				graph.addSerie(bm.getByDayMask(), title+ " :Mask");				
			}
		}
		
		// vd.getBiomass().getByDay
		
		
		// ==================================================
		// automatically generated code:
		// 
		// ==================================================
		JPanel panelWest = new JPanel();
		panelWest.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent arg0) {
			}
		});
		panelWest.setBackground(SystemColor.inactiveCaption);
		contentPane.add(panelWest, BorderLayout.WEST);
		
		JButton btApply = new JButton("Save");

		//  Radio buttons
		JRadioButton rdbtnAlongTheSurvey = new JRadioButton("Along the Survey");
		JRadioButton rdbtnTotalByDate = new JRadioButton("Total by date");
		buttonGroup.add(rdbtnTotalByDate);
		buttonGroup.add(rdbtnAlongTheSurvey);
		// Automatically generated grouplayout 				
		GroupLayout gl_panelWest = new GroupLayout(panelWest);
		gl_panelWest.setHorizontalGroup(
			gl_panelWest.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelWest.createSequentialGroup()
					.addGroup(gl_panelWest.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelWest.createSequentialGroup()
							.addGap(5)
							.addComponent(btApply))
						.addGroup(gl_panelWest.createSequentialGroup()
							.addContainerGap()
							.addComponent(rdbtnTotalByDate))
						.addGroup(gl_panelWest.createSequentialGroup()
							.addContainerGap()
							.addComponent(rdbtnAlongTheSurvey)))
					.addContainerGap(10, Short.MAX_VALUE))
		);
		gl_panelWest.setVerticalGroup(
			gl_panelWest.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelWest.createSequentialGroup()
					.addGap(5)
					.addComponent(btApply)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(rdbtnTotalByDate)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(rdbtnAlongTheSurvey)
					.addContainerGap(15, Short.MAX_VALUE))
		);
		panelWest.setLayout(gl_panelWest);
		
		// Centre panel
		JPanel panelCentre = new JPanel();
		panelCentre.setToolTipText("The Biomass by Day");
		if (graph.getChartPanel()!=null) {
		 panelCentre.add( graph.getChartPanel());
		}
		contentPane.add(panelCentre, BorderLayout.EAST);
//		panelCentre.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		// 
//		panelWest.setSize(200, 300);
		createMouseListener();
		pack();
		int w= graph.getChartPanel().getWidth()+ panelWest.getWidth()+40 ;
		setSize(w, 400);
		setMinimumSize(new Dimension(w-20, 300));
//		setVisible(true);
		graph.show();
	}
	
	
	private void createMouseListener(){
		addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
			
			@Override
			public void mousePressed(MouseEvent arg0) {
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) {
			}
			
			@Override
			public void mouseEntered(MouseEvent arg0) {
			}
			
			@Override
			public void mouseClicked(MouseEvent arg0) {
				graph.show();
				System.out.println("clicking");
			}
		});
	}

	/**
	 * @return the graph
	 */
	public LineGraph getGraph() {
		return graph;
	}

	/**
	 * @param graph the graph to set
	 */
	public void setGraph(LineGraph graph) {
		this.graph = graph;
	}

	
	
}
