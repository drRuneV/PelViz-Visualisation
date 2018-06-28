package testing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JPanel;


// Offers a ColourGradient with 256 colours represented as integers
// Colour stored as integer 
// The alpha values could be separated…
// 
// The colours can be defined by inserting a few colours at certain indexes, given by the map.
// The other colours in the gradient are then interpolated.


public class ColorGradientInt {

	private String name;
	// The array of colours in the gradient
	private int[] colors;
	// 
	private int[] alpha;
	// The maximum value which the last element in the array corresponds to
	private float max;
	// The minimum value which the first element in the array corresponds to
	private float min;
	// The value corresponding to one interval in the colour array 
	private float step;

	//	private String type="linear"; 

	// Defined colours fixed at certain indexes; 
	private Map<Integer, Integer> colorIndex;

	/**
	 * 
	 * @param max The maximum value
	 * @param min
	 */
	public ColorGradientInt(float max,float min) {
		colors= new int[256];
		alpha= new int[256];
		colorIndex= new TreeMap<Integer, Integer>();
		this.max= max;
		this.min= min;
		step = (max-min)/255.0f;
		clear(Color.black);
	}


	/**
	 * 
	 * @param c
	 */
	public void clear(Color c){
		for (int i = 0; i < colors.length; i++) {
			colors[i]= c.getRGB();
			alpha[i]=  255;
		}
	}
	
	public void interpolate(boolean useAlpha){

		int color2=0;
		int color1 = 0;
		
		int[] index = new int[colorIndex.size()];
		int i=0;

//		Iterator<Entry<Integer, Integer>> iterator= colorIndex.entrySet().iterator();

		// Find all the indexes we want to interpolate between
		for (Integer key : colorIndex.keySet()) {
			index[i]= key;
			i++;
		}
		// The dColor is the difference of 2 neighbouring colours in the index colourmap 
		int dColor;
		// The interval is the difference in the array between 2 colours
		int interval;	
		// 
		for (i = 0; i < index.length-1; i++) {
			color1 =colorIndex.get(index[i]);
			color2 =colorIndex.get(index[i+1]);

			interval = index[i+1]-index[i];
			dColor = (color2 - color1)/interval;
			System.out.println("dColor "+ dColor +" interval:"+interval +"  ix "+index[i]+" to "+index[i+1]); 
			for (int j = 0; j < interval; j++) {
			 colors[index[i]+j]= color1+j*dColor;
			}
		}
		
//		for (Iterator<TreeMap<Integer, Integer>> iterator = colorIndex.entrySet().iterator(); iterator.hasNext();) {
			
	}
	
	/**
	 * 
	 * @param c - The colour to be inserted
	 * @param index - The index where the colour is to be inserted
	 */
	public void insert(Color c,int index){

		if (index<256 && index>=0) {
			colors[index]= c.getRGB();
			colorIndex.put(index, c.getRGB());			
		}		
	}

	/**
	 * Retrieves the colour from the array of colours given a value
	 * @param value - the value
	 * @return	- a integer representing a colour
	 */
	public int retrieveColor(float value){

		int index = (int) ((value-min)/step);	
		return colors[index];
	}


	//	int r = (argb>>16)&0xFF;
	//	int g = (argb>>8)&0xFF;
	//	int b = (argb>>0)&0xFF;

	
	public void drawGradient(BufferedImage b){
		int color=0;
		for (int j = 0; j < colors.length; j++) {
			color = colors[j];
			for (int i = 0; i < b.getWidth(); i++) {
				b.setRGB(i, j, color);	
			}
			
		}
	}
	
	public void drawRandom(BufferedImage b){
		int color= Color.BLUE.getRGB(); //256*256; 
		for (int i = 0; i < b.getWidth(); i++) {
			int  rgb = (int) (color*Math.random());
		for (int j = 0; j < b.getHeight(); j++) {
			b.setRGB(i, j, rgb);
			System.out.println("rgb "+rgb); 
		}
		}
	}
	
	public void test(){

		int width= 150;
		int height=300;
		BufferedImage image = new BufferedImage(width-10, height-10, BufferedImage.TYPE_INT_ARGB);
		drawGradient(image);
		
		JPanel p= new testPanel(image,width,height, false);
		
		JFrame frame= new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(150, 300);
        frame.setResizable(true);
        frame.setVisible(true);
        frame.add(p);
        frame.pack();	
	
	}
	

	public static void main(String[] args) {

		ColorGradientInt gradient= new ColorGradientInt(100, 0);
		gradient.clear(Color.white);
		int a= 255;
		
		gradient.insert(new Color(0,1,0,a), 0);
//		gradient.insert(new Color(0,0,1,a), 1);
//		gradient.insert(new Color(0,1,1,a), 2);
//		gradient.insert(new Color(1,1,1,a), 3);
		
		for (int i = 1; i < 250; i++) {
//			gradient.insert(new Color(0,i,0, a +0*(3+1*i)), 3+i);
		}
//		gradient.insert(Color.BLUE, 250);
//		gradient.insert(Color.green, 200);
//		gradient.insert(new Color(0,0,0,255), 252);
//		gradient.insert(new Color(255,0,20,0), 253);
//		gradient.insert(new Color(255,0,10), 254);
		gradient.insert(new Color(255,0,0,a), 255);
		//
		gradient.interpolate(false);
		
		for (int i = 0; i < 256;i++) {
			if (gradient.getColorIndex().containsKey(i)) {
					
			System.out.print("MapColor:"+i+" ; "+gradient.getColorIndex().get(i) ); 
			System.out.println(" from array: "+gradient.getColors()[i]);
			}
		}

		float value=10;
		int colorint= gradient.retrieveColor(value);
		
		// rgb = 65536 * r + 256 * g + b;
		// red = 256*256*r…  green= 256*g / Blue=b	/ alpha= 256*256*256*a…
//		int red= 25;
//		int green= 35;
//		int blue= 45;
//		int alpha= 4;

		Integer c2= gradient.colorIndex.get(250);

		//		Color c = new Color(256*256*red+256*green+blue+ 256*256*256*a, true);
		Color c = new Color(colorint);//(256*256*red+256*green+blue+ 256*256*256*a, true);
		System.out.println("value "+value+ " corresponding to:");
		System.out.println("r "+c.getRed());
		System.out.println("g "+c.getGreen());
		System.out.println("b "+c.getBlue());
		System.out.println("a "+c.getAlpha());
		
		for (int i = 0; i < gradient.getColors().length; i++) {
			System.out.println(gradient.getColors()[i]);
		}
		
		gradient.test();

	}




	public Map<Integer, Integer> getColorIndex() {
		return colorIndex;
	}


	public void setColorIndex(Map<Integer, Integer> colorIndex) {
		this.colorIndex = colorIndex;
	}


	/**
	 * @return the colors
	 */
	public int[] getColors() {
		return colors;
	}




	/**
	 * @param colors the colors to set
	 */
	public void setColors(int[] colors) {
		this.colors = colors;
	}
// blue: 0.. 256  green: 256…65536  red: 65536…16777216 Alpha: 16777216…4294967296 
//	25.63= 16777216
}
