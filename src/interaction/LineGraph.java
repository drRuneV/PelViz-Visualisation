package interaction;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import visualised.VisualDistribution;

public class LineGraph {


	private ChartPanel chartPanel;
	private JFreeChart chart;
	private XYSeriesCollection series;
	public  XYSeries  serie;
	private JFrame frame= null;
	//
	static final Color[] lineColour= {Color.blue.darker(), Color.red.darker(), 
			new Color(0,90,20),  //dark green
			new Color(90,0,90), //dark purple
			Color.darkGray}; 
	
	/**
	 * Constructor
	 * @param values
	 * @param title
	 * @param xtitle
	 * @param unit
	 * @param useLegend
	 */
	public LineGraph(float[] values ,String title,String xtitle,String unit,boolean useLegend) {
		this(values,title,xtitle,unit);
		if (useLegend) {
			chart.removeLegend();
		}
		
	}

	/**
	 * Constructor.
	 * Constructs a line graph with the values and necessary titles unit string.
	 * @param values the list of floatingpoint values
	 * @param title the title of the graph
	 * @param xtitle the X axis label
	 * @param unit the unit of the y-axis, i.e. the values.
	 */
	public LineGraph(float[] values ,String title,String xtitle,String unit) {
		
		// Main series
		serie= new XYSeries(title);
		fillDataSerie(values,serie);
		// collection of series
		series= new XYSeriesCollection(serie);
		
		// Create the actual line chart  
		 chart = ChartFactory.createXYLineChart(title, xtitle, unit, series);
        // we put the chart into a panel
        chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(Color.white);
        
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(new Color(200, 210, 210, 255));
        chart.getTitle().setFont(new Font("Tahoma", Font.BOLD, 16));
        // default size
        Dimension d= new  Dimension(400, 340);
        chartPanel.setPreferredSize(d);
        
        defineColour(0);
                
	}

	
	/**
	 * Enables definition of the axis. Especially restrict the maximum.
	 * @param maxY
	 */
	public void defineTheAxis(double maxY){
		NumberAxis domain = (NumberAxis) ((XYPlot) chart.getPlot()).getDomainAxis(0);
		ValueAxis rangeAxis =  ((XYPlot) chart.getPlot()).getRangeAxis();
		rangeAxis.setRange(0.0, maxY);
//        domain.setRange(0.00, 100.00);
//        domain.setTickUnit(new NumberTickUnit(0.1));
		domain.setVerticalTickLabels(true);
        
	}

	/**
	 * Convert integer table to float
	 * @param values
	 * @return
	 */
	public static float[] convertFromFloat(int[] values) {
		
		float[] val= new float[values.length];
		int j=0;
		
		for (int  i : values) {
			val[j++]=i;
		}
		return val;
	}
	
	/**
	 * Fills the data series with the given values
	 * @param values the values were going to fill in
	 * @param ser the charter serie
	 */
    private void fillDataSerie(float[] values, XYSeries ser ) {
    	ser.clear();
    	
    	for (int i = 0; i < values.length; i++) {
			ser.add(i, values[i]);
		}
    	
    	
//    	System.out.println("InsertedSerie: "+ser.getItemCount());//getColumnCount());
    }
    
    private void defineColour(int n){
    	//
    	n= Math.min(n, lineColour.length-1);
    	XYPlot plot = chart.getXYPlot();
//        test(plot);
    	XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        Shape circle = new Ellipse2D.Double(-2+n, -2+n, 4-n, 4-n);
        
        renderer.setSeriesShape(n,circle);
        renderer.setSeriesPaint(n, lineColour[n]);
        // Filled symbols if not so many points
        if (serie.getItemCount()< 200) {
        renderer.setUseFillPaint(true);
        renderer.setSeriesShapesFilled(n, false);
        renderer.setSeriesShapesVisible(n, true);
        renderer.setUseOutlinePaint(true);
        renderer.setSeriesOutlinePaint(n, Color.gray);
        }
        
        plot.getDomainAxis().setLabelFont(new Font("Times", Font.PLAIN, 12));
        ValueAxis range = plot.getRangeAxis();
//        range.setLowerBound(0.0);
//        range.setUpperBound(10e5);
        //  
        plot.setRenderer(renderer);
    }
    

	/**
     * Adds another series to the collection of series
     * @param values the values for the serie
     * @param title the title for this serie
     */
    public void addSerie(float[] values,String title){

    	XYSeries  dataSerie= new XYSeries(title);

    	fillDataSerie(values, dataSerie);

    	if (series.getSeries().contains(dataSerie)) {
    		System.out.println("Already containing the serie");
    	}
    	else {
    		series.addSeries(dataSerie);
    		int n=series.getSeriesCount();
    		defineColour(n-1);
    	}
    }
    
    
    /**
     * Updates the main series with a new set of values
     * @param values
     * @param title
     */
    public void update(float[] values,String title,int sn){

    	if (frame==null) {
    		show();
    	}

    	if (sn< series.getSeriesCount()) {

    		XYSeries theSerie = series.getSeries(sn);
    		theSerie.clear();
    		if (sn==0) {
    			chart.setTitle(title);
    		}
    		
    		fillDataSerie(values, theSerie);

    	}
    }
    	
    
    /**
     * 
     * @param d
     * @return
     */
    public BufferedImage createImage(Dimension  d){
		// Create a image of the chart to display
    	BufferedImage imageChart= chart.createBufferedImage(d.width,d.height);
    	return imageChart;
    }


    /**
     * Shows the line graph in a separate JFrame
     * 
     */ 	// We could add parameters for the look out of the frame maybe
    public void show(){
    	 frame =new JFrame()  ;
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setAlwaysOnTop(true);
		frame.add(chartPanel);
		frame.pack();
		
		frame.setSize(500, 400);
		frame.setMinimumSize(new Dimension(450, 350));
		frame.setVisible(true);
    }
    
    public void show(Point p){
    	show();
    	frame.setLocation(p);
    }


    
 // Getters and setters:
//  ==================================================
    
    
    
    
    
	/**
	 * @param plot
	 */
	private void test(XYPlot plot) {
		//
		Paint[] paintArray;
		// create default colors but modify some colors that are hard to see
		paintArray = ChartColor.createDefaultPaintArray();
		paintArray[1] = new Color(0,0,0); 
		paintArray[2] = new Color(0x00, 0xBB, 0x00);
		paintArray[3] = new Color(0xEE, 0xAA, 0x00);
	
		plot.setDrawingSupplier(new DefaultDrawingSupplier(
				paintArray,
				DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
				DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));
		
		//
	}

	/**
	 * @return the chartPanel
	 */
	public ChartPanel getChartPanel() {
		return chartPanel;
	}

	/**
	 * @param chartPanel the chartPanel to set
	 */
	public void setChartPanel(ChartPanel chartPanel) {
		this.chartPanel = chartPanel;
	}

	/**
	 * @return the frame
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * @return the chart
	 */
	public JFreeChart getChart() {
		return chart;
	}

	/**
	 * @return the series
	 */
	public XYSeriesCollection getSeries() {
		return series;
	}

	/**
	 * @return the serie
	 */
	public XYSeries getSerie() {
		return serie;
	}
	
	

	   

    
}
