package testing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import graphics.ColorGradient;
import graphics.TopoPanel;


public class TestValues {

	public float[] values; 
	int width= 200;
	int height=300;
	public ColorGradient gradient;  

	
	/**
	 * @param data
	 * @param width
	 * @param height
	 */
	public TestValues( int width, int height) {
		this.width = width;
		this.height = height;
		values= new float[width*height];
		gradient= new ColorGradient(10, 0);
		
	}

	
	public int getWidth() {
		return width;
	}


	public void setWidth(int width) {
		this.width = width;
	}


	public int getHeight() {
		return height;
	}


	public void setHeight(int height) {
		this.height = height;
	}


	public static void main(String[] args) {
		
		TestValues data= new TestValues(1000, 900);
		data.gradient.insert(Color.white, 0);
		data.gradient.insert(Color.orange, 20);
		data.gradient.insert(Color.green, 50);
		data.gradient.insert(ColorGradient.GREY_BLUE, 90);		
		data.gradient.insert(Color.blue, 150);
		data.gradient.insert(Color.red, 255);
		data.gradient.interpolate(false);
		data.test(true);
		
	}
	
	
	public float[] getValues() {
		return values;
	}


	public void setValues(float[] values) {
		this.values = values;
	}


	public ColorGradient getGradient() {
		return gradient;
	}


	public void setGradient(ColorGradient gradient) {
		this.gradient = gradient;
	}


	/**
	 * Create some test values ,by a math formula. 
	 */
	private void createValues(){
		int x;
		int y;
		
		for (int i = 0; i < values.length; i++) {
			y= i/width;
			x= i- y*width; 
//			values[i]= (float) (Math.random()*gradient.getMax());
//			values[i]= (float) (Math.cos(x*y/(100.0f*width))*Math.cos(x*x/(10.0f*height)) *gradient.getMax()); 
			values[i]= (float) (Math.sin(x*y/(10.0f*width))*Math.cos(x*x/(10.0f*height)) *gradient.getMax());
//			values[i]= (float) (Math.cos(x*y/(100.0f*width))*Math.cos(x*x/(1.0f*height)) *gradient.getMax());
//			values[i]= (float) 
//			Math.abs(Math.sin(x*y/(500.0f*width))*Math.cos(x*y/(100.0f*height)) *gradient.getMax());
		}
		
	}

	/**
	 * 
	 * @param docreate
	 */ //rememberx 
	public void test(boolean docreate){
		
		long lastFrame = System.nanoTime();
		System.out.println("values "+values.length);
		
		if(docreate)  createValues();
		
		// Set the pixels in the image according to the values
//		insertValues(image);
		// Create a graphics from the image in order to draw the gradient image onto the image
//		drawGradientonImage(image);
		
		
		//  Test Drawing on a panel to be shown
		JPanel p= new TopoPanel(this);//testPanel(image,width,height);
		// Show the panel on a JFrame
		JFrame frame= new ImageFrame("Value",p, new  Rectangle());
		//
		long timeTook= (long) ((System.nanoTime()- lastFrame)*0.0000001);
		System.out.println("Creating this took:  "+ timeTook+" ms");
	}
	
	/**
	 * 
	 */
	public void test2(){
		long lastFrame = System.nanoTime();
		
		System.out.println("values "+values.length);
		
//		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		// try the opposite
		BufferedImage image = new BufferedImage(height,width,  BufferedImage.TYPE_INT_ARGB);//
		// Set the pixels in the image according to the values
		insertValuesYX(image);
		// Create a graphics from the image in order to draw the gradient image onto the image
//		drawGradientonImage(image);
		
		
		//  Test Drawing on a panel to be shown
		JPanel p= new testPanel(image,width,height, true);
		// Show the panel on a JFrame
		JFrame frame= new ImageFrame("Value",p, new Rectangle());
		//
		long timeTook= (long) ((System.nanoTime()- lastFrame)*0.0000001);
		System.out.println("Creating this took:  "+ timeTook+" ms");
		
	}


	/**
	 * @param image
	 */
	public void drawGradientonImage(BufferedImage image) {
		Graphics2D g= image.createGraphics();
		gradient.drawGradient();
		g.drawImage( gradient.getImage(),  5, 20, null);
	}

	
	
	/**
	 * @param image
	 */
	public void insertValues(BufferedImage image) {
		int x;		//Label1
		int y;
		
		// Please set the values here
		for (int i = 0; i < values.length; i++) {
			y= i/width;
			x= i- y*width;
			int color=  gradient.retrieveColorInt(values[i]);
			if (x< width && y<height) {
//				image.setRGB(x, image.getHeight()-y-1 ,  color); //image.getHeight()-y-1,  color);
				image.setRGB(x, y ,  color); //image.getHeight()-y-1,  color);
//				image.setRGB(y, x ,  color); //image.getHeight()-y-1,  color);
			}
			
			else System.out.println("x"+x+" y"+y);
		}
	}

	
	//
	public void insertValuesYX(BufferedImage image) {
		
		// Please set the values here
		for (int y = 0; y < height ;y++) {
		for (int x = 0; x < width; x++) {
			int color=  gradient.retrieveColorInt(values[x+y*width]);
			if (x< width && y<height) {
//				image.setRGB(x, image.getHeight()-y-1 ,  color); //image.getHeight()-y-1,  color);
				image.setRGB(y,width-1-x ,  color); //image.getHeight()-y-1,  color);
			}
			else System.out.println("x"+y+" y"+y);
		}
		}
	}




	public void darkenedValue(BufferedImage image, float l) {
		// TODO Auto-generated method stub
	}

}
