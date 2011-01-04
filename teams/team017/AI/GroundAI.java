package team017.AI;

import battlecode.common.ComponentController;
import battlecode.common.MovementController;
import battlecode.common.RobotController;

public class GroundAI {
	private RobotController myRC;
	private MovementController motor;

	public GroundAI(RobotController rc) {
		myRC = rc;
		ComponentController[] components = myRC.newComponents();
		motor = (MovementController) components[0];
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
