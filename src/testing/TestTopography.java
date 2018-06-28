package testing;

import java.awt.Color;
import java.awt.image.BufferedImage;

import distribution.Topography;
import graphics.ColorGradient;
import graphics.ColorInt;


public class TestTopography  extends TestValues{

	public TestTopography(int width, int height) {
		super(width, height);
		// We want to use another ColourGradient than the default!
		ColorGradient g= new ColorGradient(2300.0f, -4200.0f);
		setGradient(g);
//		defaultTopoGradient();
		setGradient(new ColorGradient(3500.0f, 5.0f));
		defaultTopo2();
//		defaultTopoGradientSimple();
	}
	
	/**
	 * we want to avoid going through this, the super only sets the size of the value array
	 * Everything we really do here is setting a specific gradient
	 * @param topography
	 */
	public TestTopography(Topography topography){
		super(topography.getWidth(),  topography.getHeight());
		
		// We want to use another ColourGradient than the default!
		setGradient(new ColorGradient(2300.0f, -4200.0f));
		defaultTopoGradient();

		

	}

	private  void defaultTopo2(){
		int m= gradient.findIndex(50);
		System.out.println("index is:"+m);
		gradient.insert(new Color(0, 0, 110, 255), 255);  
 		gradient.insert(new Color(0, 20, 150, 255), 200);  //dark blue
		gradient.insert( ColorGradient.GREY_BLUE,m+5);//gray blue
		gradient.insert(Color.orange, m);
		gradient.insert(Color.gray, m+1);
		gradient.insert(Color.white, 0);
		gradient.interpolate(false);
			
	}
	
	private  void defaultTopoGradient(){
		int m= gradient.findIndex(0);
		gradient.insert(new Color(0, 0, 50, 255), 0);  //very dark blue
		gradient.insert(new Color(0, 0, 120, 255), 40);  //dark blue
		gradient.insert(new Color(0, 90, 150, 255), 80);  //gray blue
//		gradient.insert(Color.blue, m-10);
		gradient.insert(Color.white, m-1);
		gradient.insert(Color.gray, m);
		gradient.insert(new Color( 50, 70 ,10, 255), m+5);  //
		gradient.insert(new Color( 70, 90 ,10, 255), m+8);  //moss Green
//		gradient.insert(Color.yellow,180);			
		gradient.insert(Color.orange, 190);
		gradient.insert(new Color(120, 60, 0, 255), 250);  //Brown mountain
		gradient.insert(new Color(250, 250, 255, 255), 255);  
		gradient.interpolate(false);
	}
	
	private void defaultTopoGradientSimple(){
		int m= gradient.findIndex(0);
		gradient.insert(new Color(0, 0, 50, 255), 0);  //very dark blue
		gradient.insert(new Color(0, 90, 100, 255), m-1);  //gray blue
		gradient.insert(Color.gray, m);
		gradient.insert(new Color(120, 60, 0, 255), 255);  //Brown mountain
		gradient.interpolate(true);
	}
	

	/**
	 * 
	 * @param image
	 * @param l - The reference length
	 */
	@Override
	public void darkenedValue(BufferedImage image,float l){
		int x;		//Label2
		int y;
		float factor=1;
		float d=1;	  // The diagonal
		float h=1;	  // The difference in values between 2 neighbours

		// Please change the values here
		for (int i = 0; i < values.length-1; i++) {
			y= i/width;
			x= i- y*width;
			// Must create a completely new colour each time
			ColorInt color= new ColorInt( gradient.retrieveColor(values[i]));
			// 
			h= values[i+1]-values[i];
			//If values increase make it darker
			d= (float) Math.sqrt(h*h+ l*l);
			factor= Math.max(0.4f, l/d);
//			if (h<0)factor= Math.min(1.5f, 1/factor);
			//if (factor<0.3f)System.out.println("Darkening colour by: "+factor);

			color.darker(factor);	
//			color.alpha*=factor;
			if (x< width && y<height) {
				image.setRGB(x, y ,  color.toInt());
			}
		}

	}

}
