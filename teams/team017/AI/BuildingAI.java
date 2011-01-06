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
				ComponentType[] grizzlyTank = {ComponentType.BLASTER,ComponentType.BLASTER,ComponentType.PROCESSOR,ComponentType.SIGHT};
				constructUnit(Chassis.LIGHT, grizzlyTank);
				myRC.yield();
				updateComponents();

//				if (!motor.canMove(myRC.getDirection()))
//					motor.setDirection(myRC.getDirection().rotateRight());
//				else if (myRC.getTeamResources() >= 2 * Chassis.LIGHT.cost)
//					builder.build(Chassis.LIGHT,
//							myRC.getLocation().add(myRC.getDirection()));

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
	
	private void constructUnit(Chassis chassis, ComponentType [] components) throws GameActionException{
		double totalCost = calculateUnitCost(chassis, components);
		MapLocation buildLoc;
		Direction buildDir = myRC.getDirection();
		updateFluxRate();
		
		for(int i = 1; i < 8; ++i){
			if(sensor.senseObjectAtLocation(myRC.getLocation().add(buildDir), chassis.level) == null){
				motor.setDirection(buildDir);
				myRC.yield();
				break;
			}
			buildDir = buildDir.rotateLeft();
		}
		buildLoc = myRC.getLocation().add(buildDir);
		
		if(myRC.getTeamResources() > totalCost + 10 && fluxRate > chassis.upkeep){
			
			builder.build(chassis, buildLoc);
			myRC.yield();
			for (ComponentType com : components){
				builder.build(com, buildLoc, chassis.level);
				myRC.yield();
			}
			myRC.turnOn(buildLoc, chassis.level);
		}
	}
	private double calculateUnitCost(Chassis chassis, ComponentType [] components){
		double totalCost = chassis.cost;
		for (ComponentType com : components){
			totalCost+= com.cost; 
		}
		return totalCost;
}
}