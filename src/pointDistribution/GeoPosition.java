package pointDistribution;

import java.util.ArrayList;

import ecosystem.Clock;
import ecosystem.Coordinate;

public class GeoPosition {

	public Coordinate coordinate;
	public Clock	  clock;
	

	/**
	 * Constructor for a new position
	 * @param coordinate The geographical coordinate
	 * @param clock The date/time for the corresponding position
	 */
	public GeoPosition(Coordinate coordinate, Clock clock) {
		this.coordinate = coordinate;
		this.clock = clock;
	}
	
	public GeoPosition(Coordinate coordinate,int hour) {
		this.coordinate = coordinate;
		clock= new Clock(hour,true);
	}

	/**
	 * Creates a text string of the coordinate and date/clock
	 * @return
	 */
	public String displayInfo(){
		String st= coordinate.toString()+" : "+clock.createsDateString();
		return st;
	}
	
}
