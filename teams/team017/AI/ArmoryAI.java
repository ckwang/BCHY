package team017.AI;

import team017.construction.UnitType;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructionCompleteMessage;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class ArmoryAI extends BuildingAI{

	public ArmoryAI(RobotController rc) {
		super(rc);
	}

	@Override
	public void proceed() {

		while (true) {
			try {
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
							builderDirs.setDirections(handler.getBuilderType(), currentLoc.directionTo(handler.getBuildingLocation()));
						}
						break;
					}
					}
				}
				
				if (controllers.myRC.getTeamResources() > 100) {
					builderDirs.updateEmptyDirections();
					Direction recyclerDir = builderDirs.recyclerDirection;
					MapLocation myLoc = controllers.myRC.getLocation();
					if (recyclerDir != null) {
						if (recyclerDir.isDiagonal()) {
							// try from left twice to right twice
							Direction buildDir = recyclerDir.rotateLeft().rotateLeft();
							for(int i = 0; i < 5; ++i){
								if(builderDirs.checkDirEmpty(buildDir)){
									buildingSystem.constructUnit(myLoc.add(buildDir), UnitType.BATTLE_FORTRESS, builderDirs);
									break;
								}
								buildDir = buildDir.rotateRight();
							}
						} else {
							Direction buildDir = recyclerDir.rotateLeft();
							if (builderDirs.checkDirEmpty(buildDir)) {
								buildingSystem.constructUnit(myLoc.add(buildDir), UnitType.BATTLE_FORTRESS, builderDirs);
							} else {
								buildDir = buildDir.rotateRight().rotateRight();
								buildingSystem.constructUnit(myLoc.add(buildDir), UnitType.BATTLE_FORTRESS, builderDirs);
							}	
						}
						
					}
				}

				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}

	}

}
