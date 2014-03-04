import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.PriorityQueue;
import java.util.ArrayList;


public class DVDfactory {
	

/////------------------------------------ Testing	---------------------------------------\\\\\\\\\\	
	// deze dingen heb ik aangemaakt om te testen
	public static int dvdsStarted = 0;
	public static int totalRepairTime = 0;
	public static int repairNumber = 0;
	public static int dvdsM2 = 0;
	public static int brokenDVDs = 0;
	public static int hour = 0;
	
	
	
/////------------------------------------ Declarations and init() --------------------------\\\\\\\\\

	public static int currentTime = 0;
	public static int bufferSize = 20;
	public static int crateSize = 20;
	public static int amountM1 = 4;
	public static int amountM2 = 2;
	public static int amountM3 = 2;
	public static int amountM4 = 2;

	
	// This should be a priorityqueue, with DVD.eventTime as keys. We will always need the first event.
	// Sometimes events are set to be infinite, but we still need their data i.e. if the machine is idle.
	// In this case we need to save the DVD data in another array. 
	public static PriorityQueue<Event> eventList = new PriorityQueue<Event>();
	
	// states for all machines 1
	public static boolean[] m1Repairing = new boolean[amountM1];
	public static boolean[] m1Idle = new boolean[amountM1];
	public static int[] m1RestTime = new int[amountM1];
	public static ArrayList<DVD> m1DVDWaiting = new ArrayList<DVD>();
	public static int[] m1StartRepairTime = new int[amountM1];
	
	// states for all buffers  //
	public static LinkedList<Queue<DVD>> bufferList = new LinkedList<Queue<DVD>>();

	// state for all machines 2
	public static boolean[] m2Idle = new boolean[amountM2];
	public static DVD[] m2DVDWaiting = new DVD[amountM2];
	public static boolean[] m2Busy = new boolean[amountM2]; //!!!!! nieuw

	
	// state for conveyor belt 
	public static boolean[] cbIdle = new boolean[amountM2];
	public static int[] cbIdleTime = new int[amountM2];
	public static LinkedList<Queue<Integer>> cbWaitingTime = new LinkedList<Queue<Integer>>();
	public static LinkedList<Queue<DVD>> cbWaitingDVD = new LinkedList<Queue<DVD>>();
	public static boolean[] cbWaitingForSwap = new boolean[amountM2];
	
	// state for all crates in front of machine 3
	public static ArrayList<ArrayList<DVD>> crateFrontList = new ArrayList<ArrayList<DVD>>();
	public static int crateFrontCount = 0;
	
	// state for all crates in machine 3
	public static ArrayList<ArrayList<DVD>> crateInList = new ArrayList<ArrayList<DVD>>();
	public static int crateInCount = 0;
	
	// state for all crates in front of machine 4
	public static ArrayList<ArrayList<DVD>> crateBackList = new ArrayList<ArrayList<DVD>>();
	public static int crateBackCount = 0;
	
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
			Event m1FinishedEvent = new Event((currentTime+eventTimeM1()),1,i,dvd);
			Event m1StartRepairEvent = new Event((currentTime+eventTimeStartRepairM1()),2,i,null);
			
			eventList.add(m1FinishedEvent);
			eventList.add(m1StartRepairEvent);
			m1DVDWaiting.add(i,null);
			dvdsStarted++; // testing
		}
		
		// ProductionStep 2 is running, and all buffers are empty
		for ( int i = 0; i < amountM2; i++){
			cbIdle[i] = false;
			m2Idle[i] = false;
			Queue<DVD> buffer = new LinkedList<DVD>();
			bufferList.add(buffer);
			Queue<Integer> cbWaitTime = new LinkedList<Integer>();
			cbWaitingTime.add(cbWaitTime);
			Queue<DVD> cbWaitDVD = new LinkedList<DVD>();
			cbWaitingDVD.add(cbWaitDVD);
		}
		
		// All crates are empty
		for ( int i = 0; i < amountM3; i++){
			m3_3WaitingForSwap[i] = false;
			ArrayList<DVD> crateFront = new ArrayList<DVD>();
			ArrayList<DVD> crateIn = new ArrayList<DVD>();
			ArrayList<DVD> crateBack = new ArrayList<DVD>();
			for(int j = 0; j < crateSize; j++ ) {
				crateFront.add(j,null);
				crateIn.add(j,null);
				crateBack.add(j,null);
			}
			crateFrontList.add(i,crateFront);
			crateInList.add(i,crateIn);
			crateBackList.add(i,crateBack);
		}
		
		// ProductionStep 4 is running, and cartridgeSize is initialized
		for ( int i = 0; i < amountM4; i++){
			m4Repairing[i] = false;
			m4Idle[i] = false;
			cartridge[i] = getCartridgeSize();
			countDVDsC[i] = 0;
		}
		Event endSimulationEvent = new Event((24*60*60),11,0,null);
		eventList.add(endSimulationEvent);
		
		Event newHourCheck = new Event(currentTime+(60*60)+1,10,0,null);
		eventList.add(newHourCheck);
		
	}
	
	
	
/////------------------------------------ Event Handlers--------------------------------------\\\\\\\\\


	private static void m1ScheduledFinished(Event e){
		// calculates which buffer belongs to which machine
		int indexBuffer = 3;
		if(e.machineNum == 0 || e.machineNum == 1) {
			indexBuffer = 0;
		} else {
			indexBuffer = 1;
		}
	
		currentTime = e.eventTime;
		if(!m1Repairing[e.machineNum]) {
			if(bufferList.get(indexBuffer).size() <20) {
				dvdsStarted++; 
				
				DVD new_dvd = new DVD(currentTime,e.dvd.productionStep, e.dvd.machineNum);
				Event m1Finished = new Event(eventTimeM1(),1,e.machineNum,new_dvd);
				eventList.add(m1Finished);
				if (bufferList.get(indexBuffer).isEmpty() && !m2Idle[indexBuffer] && !m2Busy[indexBuffer]) {
					
					Event m2Finished = new Event(eventTimeM2(),4,indexBuffer,e.dvd);	
					eventList.add(m2Finished);
					m2Busy[indexBuffer] = true; // !!!!!!!!!!!!!!! volgens mij is dit nodig om te checken of er niet nog een DVD in die machine zit 
				} else {
					bufferList.get(indexBuffer).add(e.dvd);
				}	
			} else {
				m1Idle[e.machineNum] = true;
				m1DVDWaiting.set(e.machineNum, e.dvd);
			}
		} else {
			m1RestTime[e.machineNum] = currentTime - m1StartRepairTime[e.machineNum];
			m1DVDWaiting.set(e.machineNum, e.dvd);
			
		}
		// Machines go from 4 to 2, so we need to change machineNum accordingly
		e.dvd.machineNum = indexBuffer;
	}
	
	private static void m1StartRepairing(Event e){
		currentTime = e.eventTime;
		m1Repairing[e.machineNum] = true;
		m1StartRepairTime[e.machineNum] = currentTime;
		Event m1FinishedRepairing = new Event(eventTimeM1FinishedRepair(),3,e.machineNum,null);
		eventList.add(m1FinishedRepairing);
		m1Idle[e.machineNum] = false;
	}

	private static void m1FinishedRepairing(Event e){
		currentTime = e.eventTime;
		if(!m1Idle[e.machineNum]) {
			int time = (currentTime+m1RestTime[e.machineNum]);
			Event m1Finished = new Event(time,1,e.machineNum,m1DVDWaiting.get(e.machineNum));
			// m1DVDWaiting[e.machineNum] = null; // remove DVD from waiting list. !!!!!!!!!!!!!!!Opletten!!!!!!!!!!!!!!!!!!!!
			m1RestTime[e.machineNum] = 0;
			eventList.add(m1Finished);
		}
		m1Repairing[e.machineNum] = false;
		totalRepairTime = m1StartRepairTime[e.machineNum] + totalRepairTime;  // testing
		repairNumber++; // testing
		Event m1StartRepairEvent = new Event(eventTimeStartRepairM1(),2,e.machineNum,null);
		eventList.add(m1StartRepairEvent);
	}
	
	////!!!!!! deze code is om te testen tot aan machine 1 + buffer !!!!!!!!//////////
	
	////--------------------------------------------------------------------////////
	/* 
	private static void m2ScheduledFinished(Event e) { // !!!!!!! jiske: deze heb ik dus geschreven
		m2Busy[e.machineNum] = false;
		currentTime = e.eventTime;
		
		// Again, we still need to check this PRNG.
		double dvdBrokenRand;
		Random rand = new Random();
		dvdBrokenRand = rand.nextDouble();
		
		
		if (false){//dvdBrokenRand > .03 && !cbIdle[e.machineNum]) { // DVDs to conveyor belt
			Event cbScheduledFinished = new Event((currentTime+(5*60)),5,e.machineNum,e.dvd);
			eventList.add(cbScheduledFinished);
		} else if (true){//dvdBrokenRand <= .03) { // DVD breaks
			// delete DVD
			// e.dvd = null; // jiske: volgens mij niet nodig 
			brokenDVDs++;
		} else {
			m2Idle[e.machineNum] = true;
		}
		if(!m2Idle[e.machineNum]){
			int option1 = 5;
			int option2 = 5;
			if(e.machineNum == 0){
				option1 = 0;
				option2 = 1;
			} else {
				option1 = 2;
				option2 = 3;
			}
			if (m1Idle[option1]){
				Event m1Finished = new Event(currentTime,1,option1,m1DVDWaiting[option1]);
				eventList.add(m1Finished);
				m1Idle[option1] = false;
			}
			if (m1Idle[option2]){
				Event m1Finished = new Event(currentTime,1,option2,m1DVDWaiting[option2]);
				eventList.add(m1Finished);
				m1Idle[option2] = false;
			}
			if(!bufferList.get(e.machineNum).isEmpty()){
				DVD new_dvd = bufferList.get(e.machineNum).remove(); 
				Event m2Finished = new Event(eventTimeM2(),4,e.machineNum,new_dvd);
				eventList.add(m2Finished);
				m2Busy[e.machineNum] = true;
			}
		}
		
	}
	
*/
		////--------------------------------------------------------------------////////
		////!!!!!! deze code is om te testen tot aan machine 1 + buffer !!!!!!!!//////////
	
	
	private static void m2ScheduledFinished(Event e) { // !!!!!!! Jiske: deze heb ik dus geschreven
		m2Busy[e.machineNum] = false;
		currentTime = e.eventTime;
		
		// Again, we still need to check this PRNG.
		double dvdBrokenRand;
		Random rand = new Random();
		dvdBrokenRand = rand.nextDouble();
		
		
		if (dvdBrokenRand > .02 && !cbIdle[e.machineNum]) { // DVDs to conveyor belt
			Event cbScheduledFinished = new Event((currentTime+(5*60)),5,e.machineNum,e.dvd);
			eventList.add(cbScheduledFinished);
		} else if (dvdBrokenRand <= .02) { // DVD breaks
			// delete DVD
			// e.dvd = null; // Jiske: volgens mij niet nodig 
			brokenDVDs++;
		} else {
			m2Idle[e.machineNum] = true;
			m2DVDWaiting[e.machineNum] = e.dvd;
		}
		if(!m2Idle[e.machineNum]){
			int option1 = 5;
			int option2 = 5;
			if(e.machineNum == 0){
				option1 = 0;
				option2 = 1;
			} else {
				option1 = 2;
				option2 = 3;
			}
			if (m1Idle[option1]){
				Event m1Finished = new Event(currentTime,1,option1,m1DVDWaiting.get(option1));
				eventList.add(m1Finished);
				m1Idle[option1] = false;
			}
			if (m1Idle[option2]){
				Event m1Finished = new Event(currentTime,1,option2,m1DVDWaiting.get(option2));
				eventList.add(m1Finished);
				m1Idle[option2] = false;
			}
			if(!bufferList.get(e.machineNum).isEmpty()){
				DVD new_dvd = bufferList.get(e.machineNum).remove(); 
				Event m2Finished = new Event(eventTimeM2(),4,e.machineNum,new_dvd);
				eventList.add(m2Finished);
				m2Busy[e.machineNum] = true;
			}
		}
		
	}
	
	private static void cbScheduledFinished(Event e) {
		ArrayList<DVD> tempCrateFront = new ArrayList<DVD>();
		tempCrateFront = crateFrontList.get(e.machineNum);
		currentTime = e.eventTime;
		if(cbIdle[e.machineNum]){
			// If not 20 put DVD in crate
			if(crateFrontCount < 20){ 
				tempCrateFront.set(crateFrontCount,e.dvd);
				crateFrontCount++;
				// If it becomes 20 by doing so create swap crates event. 
				if(crateFrontCount == 20){
					Event swapCrates = new Event(currentTime,6,e.machineNum,null);
					eventList.add(swapCrates);
					cbWaitingForSwap[e.machineNum] = true; // Jiske: Denk dat dit toch nodig is. 
				}
			// If 
			} else {
				cbIdle[e.machineNum] = true;
				cbIdleTime[e.machineNum] = currentTime;
				cbWaitingTime.get(e.machineNum).add(0);
				cbWaitingDVD.get(e.machineNum).add(e.dvd);
			}
		}
		int waitingTime = currentTime - cbIdleTime[e.machineNum];
		cbWaitingTime.get(e.machineNum).add(waitingTime);
		cbWaitingDVD.get(e.machineNum).add(e.dvd);
	}
	
	private static void m4ScheduledFinished(Event e) {
		// TODO Auto-generated method stub
		
	}



	private static void m3_3ScheduledFinished(Event e) {
		// TODO Auto-generated method stub
		
	}



	private static void m3_12ScheduledFinished(Event e) {
		// TODO Auto-generated method stub
		
	}



	private static void cratesScheduledSwap(Event e) {
		// TODO Auto-generated method stub
		
	}
	
/////------------------------------------ Checking --------------------------------------\\\\\\\\\
	
	private static void hourCheck(Event e){
		currentTime = e.eventTime;
		hour++;
		System.out.println("Hour: " + hour);
		System.out.println("Total number of DVDs started machine 1: " + dvdsStarted);
		for(int i = 0; i<amountM2; i++) {
			System.out.println("DVDs in buffer " + (i) + ": " + bufferList.get(i).size());
		}
		System.out.println("Total number of DVDs started machine 2: " + brokenDVDs);	
		System.out.print("Idle machines are: ");
		for(int i = 0; i<amountM1; i++) { // volgens mij klopt het niet hoe het gaat met repairen en idle zijn. Ik denk dat we dat niet goed weer veranderen ofzo. 
			if (m1Idle[i]){
				System.out.print("M1." + (i) + ", ");
			}
		}
		System.out.println();
		System.out.print("Repairing machines are: ");
		for(int i = 0; i<amountM1; i++) {
			if (m1Repairing[i]){
				System.out.print("M1." + (i) + ", ");
			} 
		}
		System.out.println();
		System.out.print("Busy machines are: ");
		for(int i = 0; i<amountM2; i++) {
			if (m2Busy[i]){
				System.out.print("M2." + (i) + ", ");
			} 
		}
		System.out.println();
		System.out.println("---");
		Event newHourCheck = new Event(currentTime+(60*60),10,0,null);
		eventList.add(newHourCheck);
	}


/////------------------------------------ Event time calculations --------------------------\\\\\\\\\
	

	private static int eventTimeM1(){
		// calculates next m1ScheduledFinished
		return currentTime + 60;
	}
	
	private static int eventTimeStartRepairM1() {
		// TODO Auto-generated method stub
		return currentTime + 8*60*60;
	}
	
	private static int eventTimeM1FinishedRepair() {
		// TODO Auto-generated method stub
		return currentTime + 2*60*60;
	}
	
	private static int eventTimeM2() {
		// TODO Auto-generated method stub
		return currentTime + 24;
	}
	
	private static int eventTimeM4() {
		return currentTime + 24;
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
		
		// An eventstep 10 is the "End Simulation" event. If this is the next event, the simulation should stop.
		while(eventList.peek().eventStep != 11 ) {
			// TODO: Make a switch here that calls the method that's needed according to the e.eventStep int.
			Event e = eventList.remove();
			switch(e.eventStep) {
			case 1: m1ScheduledFinished(e); 
					break;
			case 2: m1StartRepairing(e);
					break;
			case 3: m1FinishedRepairing(e);
					break;
			case 4: m2ScheduledFinished(e);
					break;
			case 5: cbScheduledFinished(e);
					break;
			case 6: cratesScheduledSwap(e);
					break;
			case 7: m3_12ScheduledFinished(e);
					break;
			case 8: m3_3ScheduledFinished(e);
					break;
			case 9: m4ScheduledFinished(e); 
					break;
			case 10: hourCheck(e);
					//System.out.println("This is happening");
					break;
			default: System.out.println("What's happening?!?!");
			}
		}
	}



}
