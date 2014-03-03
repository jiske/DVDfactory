import java.lang.Comparable;


public class Event implements Comparable<Event> {
	int eventTime = 0;
	int eventStep = 0;
	int machineNum = 0;
	DVD dvd = null;
	
	
	//constructor
	public Event(int a, int b, int c, DVD d) {
		 eventTime = a;
		 eventStep = b;
		 machineNum = c;
		 dvd = d;
	}
	
	public int compareTo(Event e){
		return e.compare(this, e);
	}
	
	
	public int compare(Event e1, Event e2) {
		if( e1.eventTime < e2.eventTime ) return -1;
		if( e1.eventTime == e2.eventTime ) return 0;
		else return 1;
	}
	
	

	

}
