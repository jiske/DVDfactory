
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import org.apache.commons.math3.distribution.*;


public class DVDfactory {
	

/////-------------------------------------------- Testing	-------------------------------\\\\\\\\\\	
	// deze dingen heb ik aangemaakt om te testen
	public static int dvdsStarted = 0;
	public static double totalRepairTime = 0;
	public static int repairNumber = 0;
	public static int dvdsM2 = 0;
	public static int brokenDVDs = 0;
	public static int hour = 0;
	public static int m2Int = 0;
	public static int cbInt = 0;
	public static int m3_12Int = 0;
	public static int m3_3Int = 0;
	public static int m4Int = 0;
	public static int cSwapInt = 0;
	
	
/////------------------------------------ Declarations and init() --------------------------\\\\\\\\\

	public static double currentTime = 0;
	public static int bufferSize = 20;
	public static int crateSize = 20;
	public static int amountM1 = 4;
	public static int amountM2 = 2;
	public static int amountM3 = 2;
	public static int amountM4 = 2;
	
	 
	public static PriorityQueue<Event> eventList = new PriorityQueue<Event>();
	public static Queue<DVD> producedDVDQueue = new LinkedList<DVD>();
	
	// states for all machines 1
	public static boolean[] m1Repairing = new boolean[amountM1];
	public static boolean[] m1Idle = new boolean[amountM1];
	public static double[] m1RestTime = new double[amountM1];
	public static ArrayList<DVD> m1DVDWaiting = new ArrayList<DVD>();
	public static double[] m1StartRepairTime = new double[amountM1];
	
	// states for all buffers 
	public static ArrayList<Queue<DVD>> bufferList = new ArrayList<Queue<DVD>>();

	// state for all machines 2
	public static boolean[] m2Idle = new boolean[amountM2];
	public static ArrayList<DVD> m2WaitingDVD = new ArrayList<DVD>();
	public static boolean[] m2Busy = new boolean[amountM2]; //!!!!! nieuw
	
	// state for conveyor belt 
	public static boolean[] cbIdle = new boolean[amountM2];
	public static double[] cbIdleTime = new double[amountM2];
	public static ArrayList<Queue<Double>> cbWaitingTime = new ArrayList<Queue<Double>>();
	public static ArrayList<Queue<DVD>> cbWaitingDVD = new ArrayList<Queue<DVD>>();
	public static boolean[] cbWaitingForSwap = new boolean[amountM2];
	
	// state for all crates in front of machine 3
	public static ArrayList<ArrayList<DVD>> crateFrontList = new ArrayList<ArrayList<DVD>>();
	
	// state for all crates in machine 3
	public static ArrayList<ArrayList<DVD>> crateInList = new ArrayList<ArrayList<DVD>>();
	
	// state for all crates in front of machine 4
	public static ArrayList<ArrayList<DVD>> crateBackList = new ArrayList<ArrayList<DVD>>();
	
	// state for all machine 3	
	public static boolean[] m3_3WaitingForSwap = new boolean[amountM3];
	
	// state for all machine 4
	public static boolean[] m4Idle = new boolean[amountM4];
	public static boolean[] m4Repairing = new boolean[amountM4];
	public static int[] cartridge = new int[amountM4]; 
	public static int[] countDVDs = new int[amountM4];
	
	
	
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
			
			DVD dvd = new DVD(0,0);
			Event m1FinishedEvent = new Event((currentTime+eventTimeM1()),1,i,dvd);
			Event m1StartRepairEvent = new Event((currentTime+eventTimeStartRepairM1()),2,i,null);
			
			eventList.add(m1FinishedEvent);
			eventList.add(m1StartRepairEvent);
			m1DVDWaiting.add(i,null);
			dvdsStarted++;
		}
		
		// ProductionStep 2 is running, and all buffers are empty
		for ( int i = 0; i < amountM2; i++){
			cbIdle[i] = false;
			m2Idle[i] = false;
			m2Busy[i] = false;
			cbWaitingForSwap[i] = false;
			Queue<DVD> buffer = new LinkedList<DVD>();
			bufferList.add(buffer);
			Queue<Double> cbWaitTime = new LinkedList<Double>();
			cbWaitingTime.add(cbWaitTime);
			Queue<DVD> cbWaitDVD = new LinkedList<DVD>();
			cbWaitingDVD.add(cbWaitDVD);
			m2WaitingDVD.add(i,null);
		}
		
		// All crates are empty
		for ( int i = 0; i < amountM3; i++){
			m3_3WaitingForSwap[i] = true;
			ArrayList<DVD> crateFront = new ArrayList<DVD>();
			ArrayList<DVD> crateIn = new ArrayList<DVD>();
			ArrayList<DVD> crateBack = new ArrayList<DVD>();
			
			crateFrontList.add(crateFront);
			crateInList.add(crateIn);
			crateBackList.add(crateBack);
		}
		
		// ProductionStep 4 is running, and cartridgeSize is initialized
		for ( int i = 0; i < amountM4; i++){
			m4Repairing[i] = false;
			m4Idle[i] = true;
			cartridge[i] = getCartridgeSize();
			countDVDs[i] = 0;
		}
		Event endSimulationEvent = new Event((7*24*60*60),11,0,null);
		eventList.add(endSimulationEvent);
		
		Event newHourCheck = new Event(currentTime+(24*60*60)+1,10,0,null);
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
				DVD new_dvd = new DVD(currentTime, 0);
				Event m1Finished = new Event(eventTimeM1(),1,e.machineNum,new_dvd);
				eventList.add(m1Finished);
				dvdsStarted++;
				if (bufferList.get(indexBuffer).isEmpty() && !m2Idle[indexBuffer] && !m2Busy[indexBuffer]) {
					
					Event m2Finished = new Event(eventTimeM2(),4,indexBuffer,e.dvd);	
					eventList.add(m2Finished);
					m2Busy[indexBuffer] = true;  
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
		e.machineNum = indexBuffer;
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
			double eventM1time = (currentTime+m1RestTime[e.machineNum]);
			Event m1Finished = new Event(eventM1time,1,e.machineNum,m1DVDWaiting.get(e.machineNum));
			m1RestTime[e.machineNum] = 0;
			eventList.add(m1Finished);
		}
		m1Repairing[e.machineNum] = false;
		totalRepairTime = m1StartRepairTime[e.machineNum] + totalRepairTime;  // testing
		repairNumber++; // testing
		Event m1StartRepairEvent = new Event(eventTimeStartRepairM1(),2,e.machineNum,null);
		eventList.add(m1StartRepairEvent);
	}
	
	private static void m2ScheduledFinished(Event e) { 
		currentTime = e.eventTime;

		// Again, we still need to check this PRNG.
		double dvdBrokenRand;
		Random rand = new Random();
		dvdBrokenRand = rand.nextDouble();

		if (!cbIdle[e.machineNum]){
			m2Busy[e.machineNum] = false;
			if (dvdBrokenRand > .02) { // DVDs to conveyor belt
				Event cbScheduledFinished = new Event((currentTime+(5*60)),5,e.machineNum,e.dvd);
				eventList.add(cbScheduledFinished);
			} else { // DVD breaks
				// delete DVD
				// e.dvd = null; // Jiske: volgens mij niet nodig 
				brokenDVDs++;
			}
			// In case any M1 of this M2 is Idle	
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
			// If buffer not empty schedule new m2Finished
			if(!bufferList.get(e.machineNum).isEmpty()){
				DVD new_dvd = bufferList.get(e.machineNum).remove(); 
				Event m2Finished = new Event(eventTimeM2(),4,e.machineNum,new_dvd);
				eventList.add(m2Finished);
				m2Busy[e.machineNum] = true;
			}
		// If conveyor belt is idle set true
		} else {
			m2Idle[e.machineNum] = true;
			m2WaitingDVD.set(e.machineNum, e.dvd);
		}
	}
	
	private static void cbScheduledFinished(Event e) {
		currentTime = e.eventTime;
		if(!cbIdle[e.machineNum]){
			// If not 20 put DVD in crate
			if(crateFrontList.get(e.machineNum).size() < 20){ 
				crateFrontList.get(e.machineNum).add(e.dvd);
				// If it becomes 20 by doing so create swap crates event. 
				if(crateFrontList.get(e.machineNum).size() == 20){
					Event swapCrates = new Event(currentTime,6,e.machineNum,null);
					eventList.add(swapCrates);
					cbWaitingForSwap[e.machineNum] = true; // Jiske: Denk dat dit toch nodig is. 
				}
			// If 20 
			} else {
				cbIdle[e.machineNum] = true;
				cbIdleTime[e.machineNum] = currentTime;
				cbWaitingTime.get(e.machineNum).add(0.0);
				cbWaitingDVD.get(e.machineNum).add(e.dvd);
			}
		// if cb idle
		} else {
			double waitingTime = currentTime - cbIdleTime[e.machineNum];
			cbWaitingTime.get(e.machineNum).add(waitingTime);
			cbWaitingDVD.get(e.machineNum).add(e.dvd);
		}
	}
	
	private static void cratesScheduledSwap(Event e) {
		currentTime = e.eventTime;
		
		ArrayList<DVD> tempCrateFront = new ArrayList<DVD>();
		ArrayList<DVD> tempCrateIn = new ArrayList<DVD>();
		
		for(int i = 0; i < cbWaitingForSwap.length; i++ ){
			for(int j = 0; j < m3_3WaitingForSwap.length; j++ ) {
				for(int k = 0; k < m4Idle.length; k++) {
					if(cbWaitingForSwap[i] && m3_3WaitingForSwap[j] && m4Idle[k]){
						tempCrateFront = (ArrayList<DVD>) crateFrontList.get(i).clone();
						tempCrateIn = (ArrayList<DVD>) crateInList.get(j).clone();
						
						crateBackList.set(k,tempCrateIn);
						crateInList.set(j,tempCrateFront);
						crateFrontList.get(i).clear();
						
						Event m3_12ScheduledFinished = new Event(eventTimeM3_12(),7,j,null);
						eventList.add(m3_12ScheduledFinished);
						
						
						cbWaitingForSwap[i] = false;
						m3_3WaitingForSwap[j] = false;
						m4Idle[k] = false;
						cbIdle[i] = false;
						
						while(!cbWaitingDVD.get(i).isEmpty()){
							DVD this_DVD = cbWaitingDVD.get(i).remove();
							Event CBfinished = new Event((currentTime + cbWaitingTime.get(i).remove()),5,i,this_DVD);
							eventList.add(CBfinished);
						}
						
						if(m2Busy[i]) {
							m2Idle[i] = false;
							Event m2ScheduledFinished = new Event(currentTime,4,i,m2WaitingDVD.get(i));
							eventList.add(m2ScheduledFinished);
							
						} else if(!bufferList.get(i).isEmpty()){
							m2Idle[i] = false;
							Event m2ScheduledFinished = new Event(eventTimeM2(),4,i,bufferList.get(i).remove());
							eventList.add(m2ScheduledFinished);
						}
						
						if(!m4Repairing[k]) {
							Event m4ScheduledFinished = new Event(eventTimeM4(),9,k,null);
							eventList.add(m4ScheduledFinished);
						}
					}	
				}
			}
		}
	}
	
    private static void m3_12ScheduledFinished(Event e) {
    	currentTime = e.eventTime;
           
        //Delay in seconds
        int delay = 0;
       
        // Make a random object
        Random rand = new Random();
        double nozzleBlockChance;
       
        // Make temporary Arraylist for easy access.
        ArrayList<DVD> tempCrateIn = new ArrayList<DVD>();
        tempCrateIn = crateInList.get(e.machineNum);
       
        // Loop over the crate that is currently in the machine
        // check for each dvd if the nozzle gets blocked.
            for(int i = 0; i < tempCrateIn.size(); i++){
                    nozzleBlockChance = rand.nextDouble();
                    if (nozzleBlockChance < .03) {
                            delay += 300;
                    }
            }
           
            Event m3_3Finished = new Event((eventTimeM3_3()+delay),8,e.machineNum,null);
            eventList.add(m3_3Finished);
       
        }

	private static void m3_3ScheduledFinished(Event e) {
        currentTime = e.eventTime;
        m3_3WaitingForSwap[e.machineNum] = true;
        Event cratesScheduledSwap = new Event(currentTime,6,e.machineNum,null);
        eventList.add(cratesScheduledSwap);
    }
	
	private static void m4ScheduledFinished(Event e) {
		currentTime = e.eventTime;
		if(!crateBackList.get(e.machineNum).isEmpty()){
			if (countDVDs[e.machineNum] < cartridge[e.machineNum] ) {
				m4Repairing[e.machineNum] = false;
				countDVDs[e.machineNum]++;
				
				DVD this_dvd = crateBackList.get(e.machineNum).remove(0);
				producedDVDQueue.add(this_dvd);
				
				if(crateBackList.get(e.machineNum).isEmpty()){
					m4Idle[e.machineNum] = true;
					Event crateScheduledSwap = new Event(currentTime,6,e.machineNum,null);
					eventList.add(crateScheduledSwap);
				} else {
					Event m4ScheduledFinished = new Event(eventTimeM4(),9,e.machineNum,null);
					eventList.add(m4ScheduledFinished);
				}
				
			} else {
				
				cartridge[e.machineNum] = getCartridgeSize();
				countDVDs[e.machineNum] = 0;
				Event m4ScheduledFinished = new Event(eventTimeM4()+eventTimeM4Refill(),9,e.machineNum,null);
				eventList.add(m4ScheduledFinished);
			}
		} else {
			m4Idle[e.machineNum] = true;
		}
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
		System.out.println("Total number of DVDs broken in machine 2: " + brokenDVDs);	
		//System.out.println("Total number of DVDs finished in machine 2: " + m2DVDsFinished);
		//System.out.println("Total number of DVDs finished cb: " + cbFinish);
		System.out.println("DVDs on conveyor belt 0: " + cbWaitingDVD.get(0).size());
		System.out.println("DVDs on conveyor belt 1: " + cbWaitingDVD.get(1).size());

		for(int i = 0; i<amountM2; i++) {
			System.out.println("DVDs in crateFront " + (i) + ": " + crateFrontList.get(i).size());
		}
		for(int i = 0; i<amountM2; i++) {
			System.out.println("DVDs in crateIn " + (i) + ": " + crateInList.get(i).size());
		}
		for(int i = 0; i<amountM2; i++) {
			System.out.println("DVDs in crateBack " + (i) + ": " + crateBackList.get(i).size());
		}
		System.out.println("Calls to method m2Finished: " + m2Int);
		System.out.println("Calls to method cbFinished: " + cbInt);
		System.out.println("Calls to method cbSwap: " + cSwapInt);
		System.out.println("Calls to method m3_12: " + m3_12Int);
		System.out.println("Calls to method m3_3: " + m3_3Int);
		System.out.println("Calls to method m4: " + m4Int);
		System.out.println("Total DVD's produced: " + producedDVDQueue.size());
		System.out.print("Idle machines are: ");
		for(int i = 0; i<amountM1; i++) { 
			if (m1Idle[i]){
				System.out.print("M1." + (i) + ", ");
			}
		}	
		for(int i = 0; i<amountM2; i++){
			if (m2Idle[i]){
				System.out.print("M2." + (i) + ", ");
			}
		}
		for(int i = 0; i<amountM2; i++){
			if (cbIdle[i]){
				System.out.print("CB." + (i) + ", ");
			}
		}
		for(int i = 0; i<amountM3; i++){
			if (m3_3WaitingForSwap[i]){
				System.out.print("M3." + (i) + ", ");
			}
		}
		for(int i = 0; i<amountM4; i++){
			if (m4Idle[i]){
				System.out.print("M4." + (i) + ", ");
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
		Event newHourCheck = new Event(currentTime+(24*60*60),10,0,null);
		eventList.add(newHourCheck);
	}


/////------------------------------------ Event time calculations --------------------------\\\\\\\\\
	

	private static double eventTimeM1(){
		double scale=3.51;
		double shape=1.23;
		LogNormalDistribution log = new LogNormalDistribution(scale, shape);
		
		return currentTime + log.sample();
	}
	
	private static double eventTimeStartRepairM1() {
		ExponentialDistribution exp = new ExponentialDistribution(8*60*60);
		return currentTime + exp.sample();
	}
	
	private static double eventTimeM1FinishedRepair() {
		ExponentialDistribution exp = new ExponentialDistribution(2*60*60);
		return currentTime + exp.sample();
	}
	
	private static double eventTimeM2() {
		double scale=2.91;
		double shape=0.822;
		LogNormalDistribution log = new LogNormalDistribution(scale, shape);
		return currentTime + log.sample();
	}
	
	private static double eventTimeM3_12() {
		ExponentialDistribution exp1 = new ExponentialDistribution(10);
		ExponentialDistribution exp2 = new ExponentialDistribution(6);
		double m3_12Time = 0;
		for(int i = 0; i < crateSize; i++) {
			m3_12Time += exp1.sample() + exp2.sample();
		}
		
		
		return currentTime + m3_12Time;
	}
	
	// This is always exactly 3 minutes
	private static double eventTimeM3_3() {
		return currentTime + 180;
	}
	
	private static double eventTimeM4() {
		Random rng = new Random();
		double m4Time = rng.nextInt(10) + rng.nextDouble() + 20;
		return currentTime + m4Time;
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
	
	
	private static double eventTimeM4Refill() {
		NormalDistribution normalDis = new NormalDistribution(900, 60);
		return  normalDis.sample();
	}
	
	/////-------------------------------------------- Main method ------------------------------------------\\\\\\\\\

	
	
	public static void main(String[] args){
		init();
		

		
		// An eventstep 11 is the "End Simulation" event. If this is the next event, the simulation should stop.
		while(eventList.peek().eventStep != 11 ) {
			Event e = eventList.remove();
			switch(e.eventStep) {
			case 1: m1ScheduledFinished(e); 
					break;
			case 2: m1StartRepairing(e);
					break;
			case 3: m1FinishedRepairing(e);
					break;
			case 4: m2ScheduledFinished(e);
					m2Int++;
					break;
			case 5: cbScheduledFinished(e);
					cbInt++;
					break;
			case 6: cratesScheduledSwap(e);
					cSwapInt++;
					break;
			case 7: m3_12ScheduledFinished(e);
					m3_12Int++;
					break;
			case 8: m3_3ScheduledFinished(e);
					m3_3Int++;
					break;
			case 9: m4ScheduledFinished(e);
					m4Int++;
					break;
			case 10: hourCheck(e);
					break;
			default: System.out.println("What's happening?!?!");
			}
		}
		System.out.println("Average DVD's produced per hour: " + producedDVDQueue.size()/(24*7));
	}



}
