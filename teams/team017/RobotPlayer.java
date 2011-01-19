package team017;

import team017.AI.*;
import team017.util.Util;
import battlecode.common.*;

public class RobotPlayer implements Runnable {

	private final RobotController myRC;

    public RobotPlayer(RobotController rc) {
        myRC = rc;
    }

	public void run() {
		
		if (Clock.getRoundNum() != 0)
			myRC.turnOff();
		
		switch (myRC.getChassis()){
			case BUILDING:
				if (Util.containsComponent(myRC.components(), ComponentType.RECYCLER)) {
					new RecyclerAI(myRC).proceed();
				} else if (Util.containsComponent(myRC.components(), ComponentType.ARMORY)) {
					new ArmoryAI(myRC).proceed();
				} else if (Util.containsComponent(myRC.components(), ComponentType.FACTORY)){
					new FactoryAI(myRC).proceed();
				} else {
					new TowerAI(myRC).proceed();
				}
				
				break;
			
			case FLYING:
				if (Util.containsComponent(myRC.components(), ComponentType.TELESCOPE)) {
					new ScoutAI(myRC).proceed();
				} else {
					new AirAI(myRC).proceed();
				}
				break;
			
			case LIGHT:
			case MEDIUM:
			case HEAVY:
				if (Util.containsComponent(myRC.components(), ComponentType.CONSTRUCTOR)) {
					new ConstructorAI(myRC).proceed();
				} else if (Util.containsComponent(myRC.components(), ComponentType.DISH)) {
					new CommanderAI(myRC).proceed();
				} else {
					new SoldierAI(myRC).proceed();
				}
				break;
		}
	}
	
}
