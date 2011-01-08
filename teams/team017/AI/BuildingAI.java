package team017.AI;

import team017.construction.BuilderDirections;
import battlecode.common.Chassis;
import battlecode.common.ComponentClass;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public abstract class BuildingAI extends AI {

	protected BuilderDirections builderDirs;

	public BuildingAI(RobotController rc) {
		super(rc);

		builderDirs = new BuilderDirections(controllers);
	}

	abstract public void proceed();

	public void yield() {
		super.yield();
		controllers.updateComponents();
		updateFluxRate();
	}

	protected void updateFluxRate() {
		for (int i = 9; i > 0; --i) {
			fluxRecord[i] = fluxRecord[i - 1];
		}
		fluxRecord[0] = controllers.myRC.getTeamResources();
		fluxRate = fluxRecord[0] - fluxRecord[1];
	}

	/***
	 * Sense nearby robots and return the location of one robot with specific
	 * chassis. Return none if there is no such robot.
	 * 
	 * @throws GameActionException
	 */
	private RobotInfo senseAdjacentChassis(Chassis chassis)
			throws GameActionException {
		Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
		for (Robot r : robots) {
			if (r.getTeam() == controllers.myRC.getTeam()) {
				RobotInfo info = controllers.sensor.senseRobotInfo(r);
				if (info.chassis == chassis)
					return info;
			}
		}
		return null;
	}
}
