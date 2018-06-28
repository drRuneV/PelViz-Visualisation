package ecosystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.amazonaws.util.IOUtils;

import ecosystem.Coordinate;

public class PolygonCoordinate {

	private Coordinate polygon[] ;

	
	
	/**
	 * Constructor
	 * @param textToScan text string to scan from
	 */
	public PolygonCoordinate(String textToScan){
		createCoordinates(textToScan);
		
	}

	/**
	 * Creates the polygon of coordinates from a text string 
	 * @param textToScan text string to scan from
	 */
	private void createCoordinates(String textToScan) {
		ArrayList<Coordinate> co= scanLinesOfText(textToScan);
		polygon= (Coordinate[]) co.toArray(new Coordinate[co.size()]);
	}
	
	/**
	 * Constructor
	 * @param file text file to scan from
	 */
	public PolygonCoordinate(File file) {
		//
		try(FileInputStream inputStream = new FileInputStream(file)) {     
		    String textToScan = IOUtils.toString(inputStream);
			createCoordinates(textToScan);


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(" Cannot handle the file");
			e.printStackTrace();
		}
	}

	/**
	 * Reads are lines of text and finds a coordinate
	 * @param line the text line to scan
	 * @return a new coordinate
	 */
	private Coordinate parseCoordinate(String line) throws  NumberFormatException{

		Coordinate c= new Coordinate(0, 0);
		Scanner sc= new Scanner(line);
		String field="";
		//		while(sc.hasNext()){
		// First latitude
		field= sc.next().replaceAll(",", ".");
		float  lat= Float.parseFloat(field);
		c.setLat(lat);
		// Then Longitude
		field= sc.next();
		float  lon= Float.parseFloat(field);
		c.setLon(lon);

		return c;
	}
	
	
	
/**
 * Scans a text for coordinates, one coordinate at each line
 *   
 * @param text the text to scan
 */
	private ArrayList<Coordinate> scanLinesOfText(String text){
	Scanner sc = new Scanner(text);
//	Coordinate c=null;
	String line="";
	int n=0;
	ArrayList<Coordinate> co= new ArrayList<>();
	//
	
	while(sc.hasNextLine()){
		line= sc.nextLine();
		Coordinate c= parseCoordinate(line);
		co.add(c);
		n++;
//		System.out.println(c.toString());

	}
	sc.close();

	return co;
}
	
	
	
	

	// ==================================================
	// Getters and setters	//
	// ==================================================
	
	
	
	
/**
 * @return the polygon
 */
public Coordinate[] getPolygon() {
	return polygon;
}

/**
 * @param polygon the polygon to set
 */
public void setPolygon(Coordinate[] polygon) {
	this.polygon = polygon;
}
	
	

}
