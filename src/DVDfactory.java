import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.util.PriorityQueue;


public class DVDfactory {
	
	
	/////------------------------------------ Declarations and Init() --------------------------\\\\\\\\\

	public static int currentTime;
	public static int bufferSize = 20;
	public static int crateSize = 20;
	public static int amountM1 = 4;
	public static int amountM2 = 2;
	public static int amountM3 = 2;
	public static int amountM4 = 2;

	
	// This should be a priorityqueue, with DVD.eventTime as keys. We will always need the first event.
	// Sometimes events are set to be infinite, but we still need their data i.e. if the machine is idle.
	// In this case we need to save the DVD data in another array. 
	public static PriorityQueue<Event> eventlist = new PriorityQueue<Event>();
	public static ArrayList<DVD> eventList = new ArrayList<DVD>(); // eventtime starttime
	
	// states for all machines 1
	public static boolean[] m1Repairing = new boolean[amountM1];
	public static boolean[] m1Idle = new boolean[amountM1];
	public static int[] m1RestTime = new int[amountM1];
	
	// states for all buffers 
	public static int[][] buffer = new int[amountM2][bufferSize]; 
	public static int[] dvdsInBuffer = new int[amountM2];

	// state for all machines 2
	public static boolean[] m2Idle = new boolean[amountM2];
	
	// state for conveyor belt 
	public static boolean[] cbIdle = new boolean[amountM2];
	Queue<Integer> cbRestTime1 = new LinkedList<Integer>();
	Queue<Integer> cbDVD1 = new LinkedList<Integer>();
	Queue<Integer> cbRestTime2 = new LinkedList<Integer>();
	Queue<Integer> cbDVD2 = new LinkedList<Integer>();
	
	// state for all crates in front of machine 3
	public static int[][] crateFront = new int[amountM3][crateSize];
	
	// state for all crates in machine 3
	public static int[][] crateIn = new int[amountM3][crateSize];
	
	// state for all crates in front of machine 4
	public static int[][] crateBack = new int[amountM4][crateSize];
	
	
	// state for all machine 3	
	public static boolean[] m3_3WaitingForSwap = new boolean[amountM3];
	
	// state for all machine 4
	public static boolean[] m4Idle = new boolean[amountM4];
	public static boolean[] m4Repairing = new boolean[amountM4];
	public static int[] cartridge = new int[amountM4]; 
	public static int[] countDVDsC = new int[amountM4];
	
	
	
	//Initial states are declared here
	public static void init(){
		
		
		// Production Step 1 is running. And needs initial events for the process to begin
		// We need to create m1Amount*2 initial events, 
		// One event for each machine in production Step 1, 
		// and a scheduled breaking time for each machine in production step 1.
		for ( int i = 0; i < amountM1; i++){
			m1Repairing[i] = false;
			m1Idle[i] = false;
			m1RestTime[i] = 0;
			
			DVD dvd = new DVD(0,1,i);
			Event m1FinishedEvent = new Event(eventTimeM1(),1,dvd);
			Event m1StartRepairEvent = new Event(eventTimeStartRepairM1(),2,null);
		}
		
		// ProductionStep 2 is running, and all buffers are empty
		for ( int i = 0; i < amountM2; i++){
			cbIdle[i] = false;
			m2Idle[i] = false;
			dvdsInBuffer[i] = 0;
			for(int j = 0; j < bufferSize; j++) {
				buffer[i][j] = 0;
			}
		}
		
		// All crates are empty
		for ( int i = 0; i < amountM3; i++){
			m3_3WaitingForSwap[i] = false;
			
			for(int j = 0; j < crateSize; j++ ) {
				crateFront[i][j] = 0;
				crateIn[i][j] = 0;
				crateBack[i][j] = 0;
			}
		}
		
		// ProductionStep 4 is running, and cartridgeSize is initialized
		for ( int i = 0; i < amountM4; i++){
			m4Repairing[i] = false;
			m4Idle[i] = false;
			cartridge[i] = getCartridgeSize();
			countDVDsC[i] = 0;
		}
		
		
	}
	
	
	
	/////------------------------------------ Event Handlers--------------------------------------\\\\\\\\\


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
	
	/////------------------------------------ Event time calculations --------------------------\\\\\\\\\
	
	private static int eventTimeStartRepairM1() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private static int eventTimeM1(){
		// calculates next m1ScheduledFinished
		return 60;
	}
	
	private static int getCartridgeSize(){
		double rand1, rand2;
		
		// I think this one has a period of 2^48, gotta check though.
		Random random = new Random();
		rand1 = random.nextDouble();
		rand2 = random.nextDouble();

		if (rand1 <= .6){
			return 200;
		} else if ( rand1 <= .8 ) {
			if (rand2 <= .5) return 201; else return 199;
		} else {
			if (rand2 <= .5) return 202; else return 198;
		}
	}
	
	/////-------------------------------------------- Main method ------------------------------------------\\\\\\\\\

	
	
	public static void main(String[] args){
		init();
		System.out.println("Compiles, at least.");
	}

}
