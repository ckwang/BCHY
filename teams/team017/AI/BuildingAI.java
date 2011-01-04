package team017.AI;

import battlecode.common.Chassis;
import battlecode.common.RobotController;

public class BuildingAI extends AI {

	public BuildingAI(RobotController rc) {
		super(rc);
	}

	public void proceed() {

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
