package team017.AI;

import team017.construction.BuildingDirections;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public abstract class BuildingAI extends AI {

	protected BuildingDirections builderDirs;
	
	private double[] fluxRecord = new double[10];
	private int[] roundRecord = new int[10];
	private double[] fluxRate = new double[9];

	public BuildingAI(RobotController rc) {
		super(rc);

		builderDirs = new BuildingDirections(controllers);
		builderDirs.updateBuildingDirs();
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
			roundRecord[i] = roundRecord[i - 1];
		}
		fluxRecord[0] = controllers.myRC.getTeamResources();
		roundRecord[0] = Clock.getRoundNum();
		for (int i = 0; i < 9; ++i) {
			fluxRate[i] = (fluxRecord[i] - fluxRecord[i+1]) / (roundRecord[i] - roundRecord[i+1]);
		}
	}
	
	protected double getEffectiveFluxRate() {
		double sum = 0;
		int n = 0;
		for (int i = 0; i < 9; ++i) {
			if (fluxRate[i] >= -10) {
				sum += fluxRate[i];
				n++;
			}
		}
		
		return fluxRate[0] + (sum / n);
	}
	
	protected double getFluxAcceleration() {
		return (fluxRate[0] = fluxRate[1]) / (roundRecord[0] - roundRecord[1]);
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
