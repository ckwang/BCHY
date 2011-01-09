package team017.AI;

import team017.construction.UnitType;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructionCompleteMessage;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class FactoryAI extends BuildingAI {

	public FactoryAI(RobotController rc) {
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
						
						if (handler.getBuildingLocation().isAdjacentTo(currentLoc)) {
							builderDirs.setDirections(handler.getBuilderType(), currentLoc.directionTo(handler.getBuildingLocation()));
						}
						break;
					}
				}
			}
			
			controllers.myRC.setIndicatorString(2, getEffectiveFluxRate() + "");
			if(controllers.myRC.getTeamResources() > UnitType.APOCALYPSE.totalCost * 1.1 && getEffectiveFluxRate() > UnitType.APOCALYPSE.chassis.upkeep * 1.5){
				if(builderDirs.recyclerDirection != null){
					MapLocation buildLoc = builderDirs.constructableLocation(ComponentType.FACTORY, UnitType.APOCALYPSE.requiredBuilders);
					buildingSystem.constructUnit(buildLoc, UnitType.APOCALYPSE, builderDirs);
				}
			}

//				if (fluxRate > 0 && controllers.myRC.getTeamResources() > 120)
//					buildingSystem.constructUnit(UnitType.TANK_KILLER);
				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}

	}

}
