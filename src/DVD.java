
public class DVD {

	public int startTime = 0;
	public int eventTime = 0;
	public int productionStep = 0;
	public int machineNum = 0;
	
    //constructor
	
	// Dit moeten toch events worden, anders heb je niet genoeg informatie.
	// Er moet namelijk bijgehouden worden, bij welke event je bent, niet welke productiestap.
	// Eigenlijk allebei.. 
    public DVD(int a, int b, int c) {
    	startTime = a;
    	productionStep = b;
    	machineNum = c;
    	
    }
}

