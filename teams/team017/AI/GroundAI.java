package team017.AI;

import battlecode.common.BuilderController;
import battlecode.common.Chassis;
import battlecode.common.ComponentController;
import battlecode.common.MovementController;
import battlecode.common.RobotController;

public class GroundAI {
	private RobotController myRC;

	public GroundAI(RobotController rc) {
		myRC = rc;
	}

	public void proceed() {
		ComponentController[] components = myRC.newComponents();
		MovementController motor = (MovementController) components[0];

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
