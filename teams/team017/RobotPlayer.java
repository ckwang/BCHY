package team017;

import team017.AI.*;
import battlecode.common.*;

public class RobotPlayer implements Runnable {

	private final RobotController myRC;

    public RobotPlayer(RobotController rc) {
        myRC = rc;
    }

	public void run() {
		switch (myRC.getChassis()){
			case BUILDING:
				new BuildingAI(myRC).proceed();
				break;
			
			case FLYING:
				new AirAI(myRC).proceed();
				break;
			
			default:
				new GroundAI(myRC).proceed();
				break;
		}
	}
}
