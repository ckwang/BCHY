package team017.AI;

import team017.construction.BuilderDirections;
import team017.message.BorderMessage;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructionCompleteMessage;
import team017.message.MessageHandler;
import team017.util.Util;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentClass;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public abstract class BuildingAI extends AI {
	protected BuilderDirections builderDirs;
	
	public BuildingAI(RobotController rc) {
		super(rc);
		
		builderDirs = new BuilderDirections();
	}

	@Override
	public void yield() throws GameActionException {
		controllers.myRC.yield();
		updateComponents();
		updateFluxRate();
	}

	abstract public void proceed();
	
	protected void updateBuilderDirs() {
		Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
		for (Robot r : robots) {
			if (r.getTeam() == controllers.myRC.getTeam()) {
				try {
					RobotInfo info = controllers.sensor.senseRobotInfo(r);
					MapLocation currentLoc = controllers.myRC.getLocation();
					
					if (info.location.isAdjacentTo(currentLoc)) {
						for (ComponentType com : info.components) {
							if (com.componentClass == ComponentClass.BUILDER) {
								builderDirs.setDirections(com, currentLoc.directionTo(info.location));
							}
						}
					}
				} catch (GameActionException e) {
					e.printStackTrace();
				}
			}
		}
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
