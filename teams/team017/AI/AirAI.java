package team017.AI;

import battlecode.common.RobotController;

public class AirAI extends AI {
	
	public AirAI(RobotController rc) {
		super(rc);
	}

	public void proceed() {
		while (true) {
			try {
				myRC.yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}
}
