package team017.AI;

import team017.construction.UnitType;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructionCompleteMessage;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;

public class ArmoryAI extends BuildingAI{
	MapLocation currentLoc = controllers.myRC.getLocation();
	int buildIdleRound = 0;
	
	public ArmoryAI(RobotController rc) {
		super(rc);
	}
	
	@Override
	public void yield() {
		super.yield();
		if (buildIdleRound != 0)
			buildIdleRound--;
	}

	@Override
	public void proceed() {

		while (true) {
			try {
				processMessages();
				
				double fluxRate = getEffectiveFluxRate();
//				if (buildIdleRound == 0 && controllers.myRC.getTeamResources() > 150 && fluxRate > 0.4) {
//					if (builderDirs.recyclerDirection != null) {
//						Direction recyclerDir = builderDirs.recyclerDirection; 
//						SensorController sensor = controllers.sensor;
//						MapLocation[] buildLocs = {currentLoc.add(recyclerDir.rotateLeft()), currentLoc.add(recyclerDir.rotateRight()), currentLoc.add(recyclerDir.rotateLeft().rotateLeft()), currentLoc.add(recyclerDir.rotateRight().rotateRight())};
//						if (recyclerDir.isDiagonal()) {
//							for (int i = 0; i < 2; ++i) {
//								if (sensor.senseObjectAtLocation(buildLocs[i], RobotLevel.IN_AIR) == null) {
//									while (!buildingSystem.constructUnit(buildLocs[i], UnitType.FLYING_CONSTRUCTOR, builderDirs)) {
//										if (sensor.senseObjectAtLocation(buildLocs[i], RobotLevel.IN_AIR) != null)
//											break;
//										yield();
//									}
//									buildIdleRound = 300;
//									break;
//								}
//							}
//						} else {
//							for (int i = 0; i < 4; ++i) {
//								if (sensor.senseObjectAtLocation(buildLocs[i], RobotLevel.IN_AIR) == null) {
//									while (!buildingSystem.constructUnit(buildLocs[i], UnitType.FLYING_CONSTRUCTOR, builderDirs)) {
//										if (sensor.senseObjectAtLocation(buildLocs[i], RobotLevel.IN_AIR) != null)
//											break;
//										yield();
//									}
//									buildIdleRound = 300;
//									break;
//								}
//							}
//						}
//					}
//					while (!buildingSystem.constructUnit(buildLoc, type, builderDirs) {
//						yield();
//					}
//				}
//				 if (fluxRate > 0.6 && Clock.getRoundNum() < 1000 && controllers.myRC.getTeamResources() > 120) {
//						Direction recyclerDir = builderDirs.recyclerDirection; 
//						SensorController sensor = controllers.sensor;
//						MapLocation[] buildLocs = {currentLoc.add(recyclerDir.rotateLeft()), currentLoc.add(recyclerDir.rotateRight()), currentLoc.add(recyclerDir.rotateLeft().rotateLeft()), currentLoc.add(recyclerDir.rotateRight().rotateRight())};
//						if (recyclerDir.isDiagonal()) {
//							for (int i = 0; i < 2; ++i) {
//								if (sensor.senseObjectAtLocation(buildLocs[i], RobotLevel.ON_GROUND) == null) {
//									while (!buildingSystem.constructUnit(buildLocs[i], UnitType.NETWORK_COMMANDER, builderDirs)) {
//										if (sensor.senseObjectAtLocation(buildLocs[i], RobotLevel.ON_GROUND) != null)
//											break;
//										yield();
//									}
//									break;
//								}
//							}
//						} else {
//							for (int i = 0; i < 4; ++i) {
//								if (sensor.senseObjectAtLocation(buildLocs[i], RobotLevel.ON_GROUND) == null) {
//									while (!buildingSystem.constructUnit(buildLocs[i], UnitType.NETWORK_COMMANDER, builderDirs)) {
//										if (sensor.senseObjectAtLocation(buildLocs[i], RobotLevel.ON_GROUND) != null)
//											break;
//										yield();
//									}
//									break;
//								}
//							}
//						}
//					}
				
				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void processMessages() throws GameActionException {
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {
			case BUILDING_REQUEST:{
				BuildingRequestMessage handler = new BuildingRequestMessage(msg);
				if (handler.getBuilderLocation().equals(controllers.myRC.getLocation())) {
					while(!buildingSystem.constructComponent(handler.getBuildingLocation(),handler.getUnitType())){
						if(controllers.sensor.senseObjectAtLocation(handler.getBuilderLocation(),handler.getUnitType().chassis.level).getTeam() != controllers.myRC.getTeam())
							break;
						yield();
					}	
				}
				break;
			}
			case CONSTRUCTION_COMPLETE: {
				ConstructionCompleteMessage handler = new ConstructionCompleteMessage(msg);
				MapLocation currentLoc = controllers.myRC.getLocation();
				if (handler.getBuildingLocation().isAdjacentTo(currentLoc)) {
					builderDirs.setDirections(handler.getBuildingType(), currentLoc.directionTo(handler.getBuildingLocation()));
				}
				break;
			}
			}
		}
	}
}
