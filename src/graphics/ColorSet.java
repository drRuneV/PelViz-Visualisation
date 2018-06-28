package graphics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

public class ColorSet {
	
	String name;
	ArrayList<Color> colors;
	
	// About initialising array lists
	//https://stackoverflow.com/questions/1005073/initialization-of-an-arraylist-in-one-line
	//ArrayList<String> places = new ArrayList<>(Arrays.asList("Buenos Aires", "Lata"));
	//•
	/**
	 * @param name
	 */
	public ColorSet(String name) {
		this.name = name;
		define(name);
	}
	
	/**
	 * Creates a colour array
	 * @return
	 */
	public Color[] toColorArray(){
		Color[] c= new  Color[colors.size()];//null;// colors.toArray(); //toArray();
		for (int i = 0; i < c.length; i++) {
			c[i]= colors.get(i);
		}
		return c;
	}
	
	

	/**
	 * Defined one of the following color sets
	 * @param colorName
	 */
	public void define(String colorName){
		colorName.toLowerCase();
		
		switch(colorName){
		case "blue":
			colors= createBlue();
			break;

		case "orange":
			colors= createOrange();
			break;

		case "purple":
			colors= createPurple();
			break;
			
		case "green":
			colors= createGreen();
			break;

		case "pink":
			colors= createPink();
			break;

		default:
			colors= createStandard(); 
			break;

		}


	}



	/**
	 * 
	 */
	public void defineBlue(){
		colors= createBlue();
	}

	/**
	 * 
	 */
	public static ArrayList<Color> createBlue(){
		ArrayList<Color> caList= new ArrayList<Color>(Arrays.asList(
				new Color(247,251,255),
//				new Color(222,235,247),
				new Color(198,219,239),
				new Color(158,202,225),
				new Color(107,174,214),
				new Color(66,146,198),
				new Color(33,113,181),
				new Color(8,81,156),
				new Color(8,48,107)
				));
		return caList;
	}
	

	
	public static ArrayList<Color> createStandard(){
		ArrayList<Color> caList= new ArrayList<Color>(Arrays.asList(
				Color.white,
				ColorGradient.GREY_BLUE,
				Color.blue,
				Color.green,
				Color.yellow,
				Color.orange,
				Color.red, 
				Color.red.darker()
				));
		return caList;
	}
	
	
	
	
	public static ArrayList<Color> createPurple(){
		ArrayList<Color> caList= new ArrayList<Color>(Arrays.asList(
				new Color(252,251,253),
//				new Color(239,237,245),
				new Color(218,218,235),
				new Color(188,189,220),
				new Color(158,154,200),
				new Color(128,125,186),
				new Color(106,81,163),
				new Color(84,39,143),
				new Color(63,0,125)
				));
		return caList;
	}

	/**
	 * @param colors the colors to set
	 */
	public void setColors(ArrayList<Color> colors) {
		this.colors = colors;
	}

	/**
	 * @return the colors
	 */
	public ArrayList<Color> getColors() {
		return colors;
	}

	public static ArrayList<Color> createGreen(){
		ArrayList<Color> caList= new ArrayList<Color>(Arrays.asList(
				new Color(255,255,229),
//				new Color(247,252,185),
				new Color(217,240,163),
				new Color(173,221,142),
				new Color(120,198,121),
				new Color(65,171,93),
				new Color(35,132,67),
				new Color(0,104,55),
				new Color(0,69,41)
				));
		return caList;
	}


	public static ArrayList<Color> createOrange(){
		ArrayList<Color> caList= new ArrayList<Color>(Arrays.asList(
				new Color(255,245,235),
//				new Color(254,230,206),
				new Color(253,208,162),
				new Color(253,174,107),
				new Color(253,141,60),
				new Color(241,105,19),
				new Color(217,72,1),
				new Color(166,54,3),
				new Color(127,39,4)
				));
		return caList;
	}

	
	public static ArrayList<Color> createPink(){
		ArrayList<Color> caList= new ArrayList<Color>(Arrays.asList(
				new Color(255,247,243),
//				new Color(253,224,221),
				new Color(252,197,192),
				new Color(250,159,181),
				new Color(247,104,161),
				new Color(221,52,151),
				new Color(174,1,126),
				new Color(122,1,119)
				));
		return caList;
	}
	

}
