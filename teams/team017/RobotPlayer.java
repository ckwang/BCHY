package team017;

import team017.AI.*;
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
				new BuildingAI(myRC).proceed();
				break;
			
			case FLYING:
				new AirAI(myRC).proceed();
				break;
			
			case LIGHT:
			case MEDIUM:
			case HEAVY:
				if (isConstructor()) {
					new ConstructorAI(myRC).proceed();
				} else {
					new SoldierAI(myRC).proceed();
				}
				break;
		}
	}
	
	private boolean isConstructor() {
		for ( ComponentController com : myRC.components() ) {
			if (com.type() == ComponentType.CONSTRUCTOR ) {
				return true;
			}
		}
		return false;
	}
}
