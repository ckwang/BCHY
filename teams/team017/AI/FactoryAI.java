package team017.AI;

import team017.construction.UnitType;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructUnitMessage;
import team017.message.ConstructionCompleteMessage;
import team017.util.Util;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;

public class FactoryAI extends BuildingAI {

	public FactoryAI(RobotController rc) {
		super(rc);
	}

	@Override
	public void proceed() {
		while (controllers.motor.isActive())
			yield();
		while (true) {
			try {

				processMessages();
//			controllers.myRC.setIndicatorString(2, getEffectiveFluxRate() + "");
//			if(controllers.myRC.getTeamResources() > UnitType.APOCALYPSE.totalCost * 1.1 && getEffectiveFluxRate() > UnitType.APOCALYPSE.chassis.upkeep * 1.5){
//				if(buildingDirs.recyclerDirection != null){
//					MapLocation buildLoc = buildingDirs.constructableLocation(ComponentType.FACTORY, UnitType.APOCALYPSE.requiredBuilders);
//					buildingSystem.constructUnit(buildLoc, UnitType.APOCALYPSE, buildingDirs);
//				}
//			}
//			if(buildingLocs.recyclerLocation != null){

//				if(Clock.getRoundNum() < 1000 && controllers.myRC.getTeamResources() > UnitType.MEDIUM_CONSTRUCTOR.totalCost * 1.1 && getEffectiveFluxRate() > UnitType.MEDIUM_COMMANDER.chassis.upkeep * 1.5){
//					MapLocation buildLoc = buildingDirs.constructableLocation(ComponentType.FACTORY, UnitType.MEDIUM_COMMANDER.requiredBuilders);
//					buildingSystem.constructUnit(buildLoc, UnitType.MEDIUM_COMMANDER, buildingDirs);
//				} else 
//				
				
				
				
//				if (Clock.getRoundNum() > 1000 && controllers.myRC.getTeamResources() > 180 && getEffectiveFluxRate() > 1.2) {
//				MapLocation buildLoc = buildingLocs.constructableLocation(ComponentType.FACTORY, UnitType.APOCALYPSE.requiredBuilders);
//				buildingSystem.constructUnit(buildLoc, UnitType.APOCALYPSE, buildingLocs);
//			
//				}
//			}
//				if (fluxRate > 0 && controllers.myRC.getTeamResources() > 120)
//					buildingSystem.constructUnit(UnitType.TANK_KILLER);
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
			outer:
			switch (msgHandler.getMessageType(msg)) {
			case BUILDING_REQUEST:{
				BuildingRequestMessage handler = new BuildingRequestMessage(msg);
				if (handler.getBuilderLocation().equals(controllers.myRC.getLocation())) {
					Direction buildDir = controllers.myRC.getLocation().directionTo(handler.getBuildingLocation());
					if (controllers.myRC.getDirection() != buildDir) {
						controllers.motor.setDirection(buildDir);
						yield();
					}
					
					while(!buildingSystem.constructComponent(handler.getBuildingLocation(),handler.getUnitType())){
						if(controllers.sensor.senseObjectAtLocation(handler.getBuilderLocation(),handler.getUnitType().chassis.level).getTeam() != controllers.myRC.getTeam())
							break;
						yield();
					}
					yield();
				}
				break;
			}
			
			case CONSTRUCTION_COMPLETE: {
				ConstructionCompleteMessage handler = new ConstructionCompleteMessage(msg);
				MapLocation currentLoc = controllers.myRC.getLocation();
				MapLocation buildingLocation = handler.getBuildingLocation();
//				Direction builderDir = currentLoc.directionTo(buildingLocation);
				
				if (handler.getBuildingLocation().isAdjacentTo(currentLoc)) {

					buildingLocs.setLocations(handler.getBuildingType(), handler.getBuildingLocation());
					if (handler.getBuildingType() == UnitType.RAILGUN_TOWER) {
						buildingLocs.setLocations(handler.getBuildingType(), buildingLocation);
						while(!buildingSystem.constructComponent(buildingLocation, UnitType.RAILGUN_TOWER)) {
							GameObject obj = controllers.sensor.senseObjectAtLocation(buildingLocation,RobotLevel.ON_GROUND);
							if (obj == null || obj.getTeam() != controllers.myRC.getTeam()) {
								buildingLocs.setLocations(handler.getBuildingType(), null);
								break outer;
							}
							yield();
						}
						break;
					}
				}
				break;
			}
			
			case CONSTRUCT_UNIT_MESSAGE: {
				ConstructUnitMessage handler = new ConstructUnitMessage(msg);
				if (controllers.myRC.getLocation() == handler.getBuilderLocation()) {
					UnitType type = handler.getType();
					MapLocation buildLoc = buildingLocs.constructableLocation(Util.FACTORY_CODE, type.requiredBuilders);
					if (buildLoc != null)
						buildingSystem.constructUnit(buildLoc,type, buildingLocs);
				}
				break;
			}
		}
	}
	}
}
