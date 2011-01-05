package team017.AI;

import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class BuildingAI extends AI {

	public BuildingAI(RobotController rc) {
		super(rc);
	}

	public void proceed() {
		
		if (Clock.getRoundNum() == 0)
			init();
		
		while (true) {

			try {
				myRC.yield();
				updateComponents();

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
	
	private void init() {
		try {
			// install an antenna to the adjacent recycler
			RobotInfo info = senseAdjacentChassis(Chassis.LIGHT);
			if ( info != null && myRC.getTeamResources() >= 2 * ComponentType.ANTENNA.cost 
					&& !containsComponent(info.components, ComponentType.ANTENNA) ) {
				builder.build(ComponentType.ANTENNA, info.location, RobotLevel.ON_GROUND);
			}
			myRC.yield();
			
			info = senseAdjacentChassis(Chassis.BUILDING);
			if ( info != null && myRC.getTeamResources() >= 2 * ComponentType.ANTENNA.cost ) {
				builder.build(ComponentType.ANTENNA, info.location, RobotLevel.ON_GROUND);
			}
		} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}
	}
	
	/***
	 * Sense nearby robots and return the location of one robot with specific chassis.
	 * Return none if there is no such robot.
	 * @throws GameActionException
	 */
	private RobotInfo senseAdjacentChassis(Chassis chassis) throws GameActionException {
		Robot[] robots = sensor.senseNearbyGameObjects(Robot.class);
		for ( Robot r : robots ) {
			if (r.getTeam() == myRC.getTeam()) {
				RobotInfo info = sensor.senseRobotInfo(r);
				if (info.chassis == chassis)
					return info;
			}
		}
		
		return null;
	}
	
	private boolean containsComponent(ComponentType[] list, ComponentType com) {
		for ( ComponentType c : list ) {
			if (c == com)	return true;
		}
		return false;
	}

}
