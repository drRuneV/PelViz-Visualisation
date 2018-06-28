package interaction;

import java.awt.Dimension;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

public class TryChart extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private float value=0;
	
	

	/**
	 * @param value
	 */
	public TryChart(float value) {
		this.value = value;

		System.out.println(" Creating a chart");
		DefaultCategoryDataset dataset = createDataset();
		XYAreaRenderer area= new XYAreaRenderer();
		
        // based on the dataset we create the chart
//        JFreeChart chart = createChart(dataset, "Title");
		JFreeChart chart = createLineChart();
        // we put the chart into a panel
        ChartPanel chartPanel = new ChartPanel(chart);
        // default size
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        // add it to our application
//         setContentPane(chartPanel);
         this.add(chartPanel);
         
        //
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(450, 370);
		setMinimumSize(new Dimension(450, 350));
		setLocation(600, 300);
		setVisible(true);

	}
	
	
	/**
     * Creates a chart
     */
    private JFreeChart createChart(PieDataset dataset, String title) {

        JFreeChart chart = ChartFactory.createPieChart3D(
            title,                  // chart title
            dataset,                // data
            true,                   // include legend
            true,
            false
        );

        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setStartAngle(290);
//        plot.setDirection(Rotation);
        plot.setForegroundAlpha(0.5f);
        return chart;

}
            
    private  JFreeChart createLineChart(){
    	
        JFreeChart lineChart = ChartFactory.createLineChart(
                "Title",
                "Years","biomass",
                createDataset(),
                PlotOrientation.VERTICAL,
                true,true,false);

		return lineChart;
    }
    
    
    /**
     * 
     * @return
     */
      private DefaultCategoryDataset createDataset( ) {
         DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
         dataset.addValue( 15 , "schools" , "1970" );
         dataset.addValue( 30 , "schools" , "1980" );
         dataset.addValue( 60 , "schools" ,  "1990" );
         dataset.addValue( 120 , "schools" , "2000" );
         dataset.addValue( 240 , "schools" , "2010" );
         dataset.addValue( 300 , "schools" , "2014" );
         return dataset;
      }
      
    

    /**
     * Creates a sample dataset
     */
    private  PieDataset createDataset2() {
        DefaultPieDataset result = new DefaultPieDataset();
        result.setValue("Linux", 29);
        result.setValue("Mac", 20);
        result.setValue("Windows", 51);
        return result;

    }

    
	public static void main(String[] args) {
		System.out.println(" starting main:");
		TryChart chart= new TryChart(1.0f);
		
	}

}
