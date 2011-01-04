package team017.AI;

import battlecode.common.RobotController;

public class AirAI {
	private RobotController myRC;

	public AirAI(RobotController rc) {
		myRC = rc;
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
