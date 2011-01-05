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
			MapLocation recyclerLocation = senseAdjacentRecycler();
			if ( recyclerLocation != null && myRC.getTeamResources() >= 2 * ComponentType.ANTENNA.cost ) {
				builder.build(ComponentType.ANTENNA, recyclerLocation, RobotLevel.ON_GROUND);
			}
		} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}
	}
	
	/***
	 * Sense nearby robots and return the location of one Recycler if there exists one
	 * @throws GameActionException
	 */
	private MapLocation senseAdjacentRecycler() throws GameActionException {
		Robot[] robots = sensor.senseNearbyGameObjects(Robot.class);
		for ( Robot r : robots ) {
			if (r.getTeam() == myRC.getTeam()) {
				RobotInfo info = sensor.senseRobotInfo(r);
				if (info.chassis == Chassis.BUILDING)
					return info.location;
			}
		}
		
		return null;
	}

}
