package team017.AI;

import battlecode.common.RobotController;

public class GroundAI extends AI {

	public GroundAI(RobotController rc) {
		super(rc);
	}

	public void proceed() {
		while (true) {

			try {

				/*** beginning of main loop ***/
				while (motor.isActive()) {
					myRC.yield();
				}

				if (motor.canMove(myRC.getDirection())) {
					// System.out.println("about to move");
					motor.moveForward();
				} else {
					motor.setDirection(myRC.getDirection().rotateRight());
				}

				/*** end of main loop ***/
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}

	}

}
