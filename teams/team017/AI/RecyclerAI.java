package team017.AI;

import team017.construction.UnitType;
import team017.message.BorderMessage;
import team017.message.BuildingLocationInquiryMessage;
import team017.message.BuildingLocationResponseMessage;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructionCompleteMessage;
import team017.message.EnemyLocationMessage;
import team017.util.Util;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Mine;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class RecyclerAI extends BuildingAI {

	private MapLocation enemyBase;
	private Mine myMine;
	
	public RecyclerAI(RobotController rc) {
		super(rc);		
		enemyBase = controllers.myRC.getLocation();
		
		try {
			myMine = (Mine) controllers.sensor.senseObjectAtLocation(controllers.myRC.getLocation(), RobotLevel.MINE);
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void proceed() {
		
		if (Clock.getRoundNum() == 0)
			init();
		else{
			
			// turn off if there is already a recycler nearby
			if (builderDirs.recyclerDirection != null) {
				try {
					GameObject object = controllers.sensor.senseObjectAtLocation(
							controllers.myRC.getLocation().add(builderDirs.recyclerDirection), RobotLevel.ON_GROUND);
					
					if (object.getID() < controllers.myRC.getRobot().getID())
						controllers.myRC.turnOff();
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			while(controllers.myRC.getTeamResources() < 6)
				controllers.myRC.yield();
			try {
				// build an antenna on itself
				controllers.builder.build(ComponentType.ANTENNA, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
			} catch (GameActionException e1) {
				System.out.println("caught exception:");
				e1.printStackTrace();
			}
		}
		
		while (true) {
			try {
				// receive messages and handle them
				while (msgHandler.hasMessage()) {
					Message msg = msgHandler.nextMessage();
					switch (msgHandler.getMessageType(msg)) {
					case BORDER: {
						BorderMessage handler = new BorderMessage(msg);

						// update the borders
						int[] newBorders = handler.getBorderDirection();

						for (int i = 0; i < 4; ++i) {
							if (newBorders[i] != -1){
								if (borders[i] != newBorders[i]){
									borders[i] = newBorders[i];
									enemyBase = enemyBase.add(enemyDir[i],60);
								}
							}
						}
						
						break;
					}
					case BUILDING_REQUEST: {
						BuildingRequestMessage handler = new BuildingRequestMessage(msg);
						if (handler.getBuilderLocation().equals(controllers.myRC.getLocation())) {
							while(!buildingSystem.constructComponent(handler.getBuildingLocation(),handler.getUnitType())){
								if(controllers.sensor.senseObjectAtLocation(handler.getBuilderLocation(),handler.getUnitType().chassis.level).getTeam() != controllers.myRC.getTeam())
									break;
								yield();
							}	
						}
						break;
						
//						BuildingRequestMessage handler = new BuildingRequestMessage(msg);
//						if (handler.getBuilderLocation().equals(controllers.myRC.getLocation())) {
//							buildingSystem.constructComponent(handler.getBuildingLocation(),handler.getUnitType());
//							yield();
//						}
//						break;
					}
					
					case BUILDING_LOCATION_INQUIRY_MESSAGE: {
						BuildingLocationInquiryMessage handler = new BuildingLocationInquiryMessage(msg);
						if(handler.getBuilderLocation().equals(controllers.myRC.getLocation())){
							if(builderDirs.consecutiveEmpties(3) != Direction.NONE){
								msgHandler.queueMessage(new BuildingLocationResponseMessage(builderDirs.consecutiveEmpties(3), 3));
							}
								
							else if(builderDirs.consecutiveEmpties(2) != Direction.NONE){
								msgHandler.queueMessage(new BuildingLocationResponseMessage(builderDirs.consecutiveEmpties(2), 2));
							}
								
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
							
							if(handler.getBuilderType() != ComponentType.RECYCLER){
								// face the correct direction
								if (controllers.myRC.getDirection() != builderDir){
									controllers.motor.setDirection(builderDir);
									yield();
								}
								
								// build an antenna if it doesn't have one
								if (!Util.containsComponent(controllers, handler.getBuildingLocation(), RobotLevel.ON_GROUND, ComponentType.ANTENNA)) {
									controllers.builder.build(ComponentType.ANTENNA, handler.getBuildingLocation(), RobotLevel.ON_GROUND);
								}
							}
						}
						break;
					}

					}
				}


//				if (fluxRate > 0 && controllers.myRC.getTeamResources() > 100) {
//					if (Clock.getRoundNum() < 1000) {
//						if (Clock.getRoundNum() % 3 == 0){
//							buildingSystem.constructUnit(UnitType.CONSTRUCTOR);
//						}
//						else{
//							buildingSystem.constructUnit(UnitType.GRIZZLY);
//							if (enemyBase != null){
//								msgHandler.queueMessage(new EnemyLocationMessage(enemyBase));
//								yield();
//							}
//						}
//
//					} else {
//						if (Clock.getRoundNum() % 5 == 0)
//							buildingSystem.constructUnit(UnitType.CONSTRUCTOR);
//						else{
//							buildingSystem.constructUnit(UnitType.GRIZZLY);
//							if (enemyBase != null){
//								msgHandler.queueMessage(new EnemyLocationMessage(enemyBase));
//								yield();
//							}
//						}
//					}
//				}
				
				// turn off when the mine is depleted
				if (controllers.sensor.senseMineInfo(myMine).roundsLeft == 0)
					controllers.myRC.turnOff();

				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}

		}

	}

	private void init() {
		try {
			// install an antenna to the adjacent constructor
			RobotInfo info = senseAdjacentChassis(Chassis.LIGHT);
			if (info != null
					&& controllers.myRC.getTeamResources() >= 2 * ComponentType.ANTENNA.cost
					&& !Util.containsComponent(info.components,ComponentType.ANTENNA)) {
				controllers.builder.build(ComponentType.ANTENNA, info.location,RobotLevel.ON_GROUND);
			}
			yield();

			// build an antenna on itself
			controllers.builder.build(ComponentType.ANTENNA, controllers.myRC.getLocation(), RobotLevel.ON_GROUND);
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
