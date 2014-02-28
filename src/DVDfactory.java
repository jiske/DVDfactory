import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Queue;

public class DVDfactory {
	
	public static int currentTime;
	public static int bufferSize = 20;
	public static int crateSize = 20;
	public static int amountM1 = 4;
	public static int amountM2 = 2;
	public static int amountM3 = 2;
	public static int amountM4 = 2;
	
	
	public static ArrayList<Event> eventList = new ArrayList(); // eventtime starttime
	
	// states for all machines 1
	public static boolean[] m1Repairing = new boolean[amountM1];
	public static boolean[] m1Idle = new boolean[amountM1];
	public static int[] m1RestTime = new int[amountM1];
	
	// states for all buffers 
	public static int[][] buffer = new int[amountM2][bufferSize]; 
	public static int[] dvdsInBuffer = new int[amountM2];

	// state for all machines 2
	public static boolean[] m2Idle = new boolean[amountM2];
	
	// state for all crates in front of machine 3
	public static int[][] crateFront = new int[amountM3][crateSize];
	
	// state for all crates in machine 3
	public static int[][] crateIn = new int[amountM3][crateSize];
	
	// state for all crates in front of machine 4
	public static int[][] crateBack = new int[amountM4][crateSize];
	
	// state for conveyor belt 
	public static boolean[] cbIdle = new boolean[amountM2];
	Queue<Integer> cbRestTime1 = new LinkedList<Integer>();
	Queue<Integer> cbDVD1 = new LinkedList<Integer>();
	Queue<Integer> cbRestTime2 = new LinkedList<Integer>();
	Queue<Integer> cbDVD2 = new LinkedList<Integer>();
	
	// state for all machine 3	
	public static boolean[] m3_3WaitingForSwap = new boolean[amountM3];
	
	// state for all machine 4
	public static boolean[] m4Idle = new boolean[amountM4];
	public static boolean[] m4Repairing = new boolean[amountM4];
	public static int[] cartridge = new int[amountM4]; 
	public static int[] countDVDsC = new int[amountM4];
	
	public static void init(){
		for ( int i = 0; i < amountM1; i++){
			m1Repairing[i] = false;
			m1Idle[i] = false;
			m1RestTime[i] = 0;
		}
	}
	
	
	private static void m1ScheduledFinished(int machine, int eventTime){
		int indexBuffer = machine/(amountM1/amountM2); // calculates which buffer belongs to which machine
		
		currentTime = eventTime;
		if(!m1Repairing[machine]) {
			if(dvdsInBuffer[indexBuffer]<20){
				//eventList[0][0] = eventTimeM1();
				//eventList[0][1] = currentTime;
			}
		}
		
	}
	
	private static int eventTimeM1(){
		// calculates next m1ScheduledFinished
		return 60;
	}
	
	
	public static void main(String[] args){
		
		
	}

}
