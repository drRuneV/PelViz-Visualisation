package interaction;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ecosystem.Clock;
import ecosystem.Coordinate;
import visualised.VisualDistribution;

/**
 * 
 * @author a1500
 *
 */
public class PositionChart extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private VisualDistribution distribution;
	private Point pos= new Point();
	
	public Point speed= new Point();

	private JFreeChart chart= null;
//	private XYDataset dataset;
	public XYSeries  serie;
	//add clear collection
	private XYSeriesCollection series;

	private ChartPanel chartPanel;

//	public DefaultCategoryDataset dataset;
	
	

	/**
	 * @param value
	 */
	public PositionChart(VisualDistribution distribution) {
		this.distribution= distribution;

		
		// Main series
		serie= new XYSeries(distribution.giveNameWithYear());	//+Math.random());
		fillDataSerie(distribution,serie);
		// collection of series
		series= new XYSeriesCollection(serie);
		
		// Add guests distributions as separate lines
		if (!distribution.getGuestList().isEmpty()) {
			for (VisualDistribution dist : distribution.getGuestList()) {
				if (!series.getSeries().contains(dist)) {
				series.addSeries(addSerie(dist));
				}
			}
		}
		
		chart = createLineChart();
        // we put the chart into a panel
        chartPanel = new ChartPanel(chart);
        // default size
        scalePanel(2);
        // add it to our application
        this.add(chartPanel);
        //
        double maxY = distribution.getStatistics().getMax() * 0.1f;
        restrictAxis(maxY);
         
        // JFrame
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(500, 400);
		setMinimumSize(new Dimension(450, 350));

//		System.out.println(" Creating a chart");
	}

	/**
	 * Scales the panel
	 * @param scale
	 */
	public void scalePanel(int scale){
		scale= Math.max(2,scale);
		Dimension d=  new Dimension( (int) (distribution.getHeight()*scale*0.5),
									 (int) (distribution.getWidth()*scale*0.5f));
		chartPanel.setPreferredSize(d);
	}

	/**
	 * 
	 * @param screen
	 * @param p
	 *///•••// We should send in the pixelInformation to get information about position «•»••
	public void update(Point screen, Point p,int scale){
		pos=p;
		scale= Math.max(2,scale);
//		pos.x+=speed.x;
//		pos.y+=speed.y;
		//		chart= createLineChart();
		
		// Rename the title
		Font font= new Font("Times", Font.BOLD, 10+scale); 
		chart.getTitle().setFont(font);
		int index= pos.x + pos.y*distribution.getWidth();
		Coordinate co= distribution.getCoordinates()[index];
		String title=  distribution.getFullName()+" at "+co.toString();
		chart.setTitle(title);
		// The main Serie
		serie.clear();
		fillDataSerie(distribution, serie);
		
		//Guest distributions
		if (!distribution.getGuestList().isEmpty()) {
			for (int i = 1; i < series.getSeriesCount(); i++) {
				VisualDistribution dis=  distribution.getGuestList().get(i-1);
				//  Clear current series and fill it with the data from distribution
				XYSeries ser= series.getSeries(i);
				ser.clear();
				fillDataSerie(dis,ser);
//				chart.addSubtitle(i-1, new dis.getName());
			}
		}

		setLocation(screen.x-150,screen.y+50);
//		scalePanel(scale);
		//		setVisible(true);
	}



	/**
	 * Creates a line chart
	 * @return
	 */
	private  JFreeChart createLineChart(){

		String s= (distribution!=null) ? distribution.giveNameWithYear() : "nothing" ;
		String unit= (distribution!=null) ? distribution.getUnit(): "unit" ;
		serie.clear();

		//
		JFreeChart lineChart = ChartFactory.createXYLineChart(s, "Time(days)", unit, series);

		return lineChart;
	}

	
	/**
	 * Restricts the Y axis
	 * @param maxY
	 */
	public void restrictAxis(double maxY){
		ValueAxis rangeAxis =  ((XYPlot) chart.getPlot()).getRangeAxis();
		rangeAxis.setRange(0.0, maxY);
	}

    
    /**
     * 
     * @return
     */
    private void fillDataSerie(VisualDistribution  dist, XYSeries serieThis ) {
    	int index=0;
    	if (dist!=null) {
    		// All time steps
    		for (int i = 0; i < dist.getTime(); i++) {
    			index= pos.x + pos.y*dist.getWidth() +i*dist.getWH(); 
    			double value= (double) dist.getValues()[index];	
    			if (value!=dist.getFillV()) {
    				serieThis.add(i, value);
    			}
    		}
    	}
    	//    	else data.addValue( 1 , "none" , String.format("0") );
    	System.out.println("InsertedSerie: "+serieThis.getItemCount());//getColumnCount());
    }

    

    /**
     * Add another data series to the line plot
     * @param dist The distribution where we get the data from
     * @return A XYSerie
     */
    private XYSeries addSerie(VisualDistribution dist){
    	String name = dist.giveNameWithYear();
    	System.out.println("the new name:"+name);
    	XYSeries  dataSerie= new XYSeries(name);

    	fillDataSerie(dist, dataSerie);
    	
    	return dataSerie;
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
	 * @return the chart
	 */
	public JFreeChart getChart() {
		return chart;
	}

	/**
	 * @param chart the chart to set
	 */
	public void setChart(JFreeChart chart) {
		this.chart = chart;
	}

	
	
	// ==================================================
//	public static void main(String[] args) {
//		System.out.println(" starting main:");
//		PositionChart chart= new PositionChart(null);
//		chart.update(new Point(300, 300), new Point(), 0);
//	}
//

}
