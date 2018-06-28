package ecosystem;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import graphics.ColorInt;
import graphicsGL.Vector2D;

public class Coordinate {

	float lat;
	float lon;
	
	
	
	/**
	 * @param lat
	 * @param lon
	 */
	public Coordinate(float lat, float lon) {
		this.lat = lat;
		this.lon = lon;
	}



	public Coordinate(Coordinate c) {
		this.lat = c.lat;
		this.lon = c.lon;
	}

	public  void addLonLat(float dlon,float dlat){
		this.lon+= dlon;
		this.lat+= dlat;
	}

	
	/**
	 * 
	 * Interpolates between this coordinate and another coordinates.
	 * @param c the coordinate to interpolate between
	 * @return a new coordinate in between this one and the given one
	 */
	public Coordinate interpolate(Coordinate c){
		Coordinate co= new Coordinate(this);
		co.lat= (co.lat+ c.lat)/2;
		co.lon= (co.lon+ c.lon)/2;
		return co;
	}
	
	/**
	 * Interpolates between 2 coordinates and makes a new array of new list of coordinates
	 * @param c1 the 1st coordinate
	 * @param c2 the 2nd coordinate
	 * @param n number of new coordinates requested
	 * @return an array of new coordinates
	 */
	static public Coordinate[] interpolateBetween(Coordinate c1,Coordinate c2,int n){
		Coordinate[] coordinate= new Coordinate[n];
		
		float dLat= (c2.lat-c1.lat)/n;
		float dLon= (c2.lon-c1.lon)/n;
//		System.out.println("dLat= "+dLat);
				
		for (int i = 0; i < n; i++) {
//			System.out.println("lat:"+c1.lat+dLat);
			coordinate[i]= new Coordinate(c1.lat+dLat*i, c1.lon+dLon*i);
		}
		
		return coordinate;
	}
	
	
	/**
	 * Finds the closest index in Y-X NORWECOM.E2E model space 
	 * from a given geographical coordinates in latitude and longitude
	 * @param coordinates The coordinate we want to find the position for
	 * @param dl The accepted deviation from the coordinate
	 * @return The index which has the coordinate closest to the desired coordinate
	 */
	public int findClosestIndex(Coordinate[] coordinates, float dl){
//		ArrayList<Integer> indexes= new ArrayList<>();
//		ArrayList<Float> deviation= new ArrayList<>();
		TreeMap<Float, Integer> map = new TreeMap<Float,Integer >();
		//•
		int index=-1;
		float dlo=0;
		float dla=0;
		float dev=0;
		Coordinate c;
		for (int i = 0; i < coordinates.length; i++) {
			c= coordinates[i];
			dlo= Math.abs( lon-c.lon);
			dla= Math.abs( lat-c.lat);
			//
			if (dlo<dl && dla<dl) {
				dev= (dlo+dla)/2;
//				indexes.add(i);
//				deviation.add(dev);
				map.put( dev,i);
			}
			
			
		}
		
		if (!map.isEmpty()) {
//		indexes.sort(null);
//		index= indexes.get(0);
			float v= map.firstKey();
			index= map.get(v);
		}
		
		return index;
	}


	/**
	 * 
	 * @param longitude
	 * @return
	 */
	public Vector2D projection(float longitude){
		
		Vector2D p= projectionCircle(longitude);		
		return p;		
	}

	
	/**
	 * 
	 * @param ecoLon - the longitude  origin of the ecosystem
	 * @return - 
	 */
	public Vector2D projectionCircle(float ecoLon){
		//Vector is pointing downwards, then rotated
		Vector2D v= new Vector2D(0,-1); //«•»
		float dlon  = lon - ecoLon;
		v.rotate(dlon);
		// The latitude length from the North Pole (when circle radius=1)
		double length= (90.0d - lat*1.0d)*Ecosystem.TORAD;
		//Starting from the North Pole as origin (0,0)
		//stretch the point the given length along the vector
		v.scale((float) length);
//		System.out.println("length: "+length);
		
		return v;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
//		return "Coordinate [lat=" + lat + ", lon=" + lon + "]";
		return String.format("%.2f", lat) +"°N "+ String.format("%.2f",lon)+" °E" ;
	}



	public float getLat() {
		return lat;
	}


	public void setLat(float lat) {
		this.lat = lat;
	}


	public float getLon() {
		return lon;
	}


	public void setLon(float lon) {
		this.lon = lon;
	}


	// remove this 
	static public float lengthOf1DegreeLatitude(){
		float  l;
		l= (float) Math.toRadians(1.0d)*Ecosystem.EARTHRADIUS;
		return l;
		
	}
	
	
	public static void testing2(int n){
		Coordinate Origin= new Coordinate(70, 0);
		Coordinate c=      new Coordinate(75, 10);
		Coordinate[] co= interpolateBetween(Origin, c, 20);
		for (int i = 0; i < co.length; i++) {
			System.out.println(i+" "+ co[i].toString());
		}
	}

	
	public static Vector2D[] testing(int n){
		Coordinate Origin= new Coordinate(70, 0);
//		System.out.println(Math.toRadians(1.0d));
		Coordinate c[] ;
		c = new Coordinate[n];
		Vector2D[] vectors =new Vector2D[n];
		
		
		
//		System.out.println("Origin:"+Origin.projection(Origin.lon));
		for (int i = 0; i < n; i++) {
			c[i] = new Coordinate(60.0f, -45.0f+i*1.0f);
			vectors[i]= c[i].projection(Origin.lon);
//			System.out.println(vectors[i]);
		}
		
		return vectors;
	}
		

	public static void main(String[] args) {
		testing2(10);
	}
}
