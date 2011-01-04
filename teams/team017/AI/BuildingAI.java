package team017.AI;

import battlecode.common.BuilderController;
import battlecode.common.Chassis;
import battlecode.common.ComponentController;
import battlecode.common.MovementController;
import battlecode.common.RobotController;

public class BuildingAI {

	private RobotController myRC;

	public BuildingAI(RobotController rc) {
		myRC = rc;
	}

	public void proceed() {
		ComponentController[] components = myRC.newComponents();
		MovementController motor = (MovementController) components[0];
		BuilderController builder = (BuilderController) components[2];

		while (true) {
			try {

				myRC.yield();

				if (!motor.canMove(myRC.getDirection()))
					motor.setDirection(myRC.getDirection().rotateRight());
				else if (myRC.getTeamResources() >= 2 * Chassis.LIGHT.cost)
					builder.build(Chassis.LIGHT,
							myRC.getLocation().add(myRC.getDirection()));

			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}

	}

}
