package team017.AI;

import team017.construction.BuilderDirections;
import team017.construction.UnitType;
import team017.message.BorderMessage;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructionCompleteMessage;
import team017.message.MessageHandler;
import team017.util.Util;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class RecyclerAI extends AI {

	BuilderDirections builderDirs;
	
	public RecyclerAI(RobotController rc) {
		super(rc);
		
		builderDirs = new BuilderDirections();
	}

	@Override
	public void yield() throws GameActionException {
		controllers.myRC.yield();
		updateComponents();
		updateFluxRate();
	}

	@Override
	public void proceed() {

		if (Clock.getRoundNum() == 0)
			init();

		while (true) {
			try {

				// receive messages and handle them
				Message[] messages = controllers.myRC.getAllMessages();
				for (Message msg : messages) {

					switch (MessageHandler.getMessageType(msg)) {
					case BORDER: {
						BorderMessage handler = new BorderMessage(msg);

						// update the borders
						int[] newBorders = handler.getBorderDirection();

						for (int i = 0; i < 4; ++i) {
							if (borders[i] == -1)
								borders[i] = newBorders[i];
						}
						break;
					}
					case BUILDING_REQUEST: {
						BuildingRequestMessage handler = new BuildingRequestMessage(msg);
						if (handler.getBuilderLocation().equals(controllers.myRC.getLocation())) {
							buildingSystem.constructComponent(handler.getBuildingLocation(),handler.getUnitType());
							yield();
						}
						break;
					}
					case CONSTRUCTION_COMPLETE: {
						ConstructionCompleteMessage handler = new ConstructionCompleteMessage(msg);
						
						/*
						 * When a new building is constructed, we would like to build an antenna on it.
						 */
						
						MapLocation currentLoc = controllers.myRC.getLocation();

						// see if the target is adjacent
						if (handler.getBuildingLocation().isAdjacentTo(currentLoc)) {
							
							// update the builderDirs
							Direction builderDir = currentLoc.directionTo(handler.getBuildingLocation());
							builderDirs.setDirections(handler.getBuilderType(), builderDir);
							
							// face the correct direction
							if (controllers.myRC.getDirection() != builderDir){
								controllers.motor.setDirection(builderDir);
								yield();
							}
							
							// build an antenna if it doesn't have one
							if (!Util.containsComponent(controllers, handler.getBuildingLocation(), RobotLevel.ON_GROUND, ComponentType.ANTENNA)) 
								controllers.builder.build(ComponentType.ANTENNA, handler.getBuildingLocation(), RobotLevel.ON_GROUND);
						}
						
						break;
					}

					}
				}

//				if (fluxRate > 0 && controllers.myRC.getTeamResources() > 100) {
//					if (Clock.getRoundNum() < 1000) {
//						if (Clock.getRoundNum() % 3 == 0)
//							buildingSystem.constructUnit(UnitType.CONSTRUCTOR);
//						else
//							buildingSystem.constructUnit(UnitType.GRIZZLY);
//
//					} else {
//						if (Clock.getRoundNum() % 5 == 0)
//							buildingSystem.constructUnit(UnitType.CONSTRUCTOR);
//						else
//							buildingSystem.constructUnit(UnitType.GRIZZLY);
//					}
//				}
				yield();
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
			if (info != null
					&& controllers.myRC.getTeamResources() >= 2 * ComponentType.ANTENNA.cost
					&& !Util.containsComponent(info.components,
							ComponentType.ANTENNA)) {
				controllers.builder.build(ComponentType.ANTENNA, info.location,
						RobotLevel.ON_GROUND);
			}
			yield();

			info = senseAdjacentChassis(Chassis.BUILDING);
			if (info != null
					&& controllers.myRC.getTeamResources() >= 2 * ComponentType.ANTENNA.cost) {
				controllers.builder.build(ComponentType.ANTENNA, info.location,
						RobotLevel.ON_GROUND);
			}
		} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
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