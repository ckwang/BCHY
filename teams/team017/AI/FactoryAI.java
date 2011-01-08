package team017.AI;

import team017.construction.BuilderDirections;
import team017.construction.UnitType;
import team017.message.BorderMessage;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructionCompleteMessage;
import team017.message.GenericMessage;
import team017.message.MessageType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class FactoryAI extends AI {
	
	BuilderDirections builderDirs;

	public FactoryAI(RobotController rc) {
		super(rc);
		
		builderDirs = new BuilderDirections(controllers);
	}

	@Override
	public void yield() throws GameActionException {
		controllers.myRC.yield();
		controllers.updateComponents();
		msgHandler.process();
		updateFluxRate();
	}

	@Override
	public void proceed() {

		while (true) {
			try {
				while (msgHandler.hasMessage()) {
					Message msg = msgHandler.nextMessage();
					switch (msgHandler.getMessageType(msg)) {
					case BUILDING_REQUEST:{
						BuildingRequestMessage handler = new BuildingRequestMessage(
								msg);
						if (handler.getBuilderLocation().equals(
								controllers.myRC.getLocation())) {
							
							Direction buildDir = controllers.myRC.getLocation().directionTo(handler.getBuildingLocation());
							if (controllers.myRC.getDirection() != buildDir) {
								controllers.motor.setDirection(buildDir);
								yield();
							}
							
							while(!buildingSystem.constructComponent(
									handler.getBuildingLocation(),
									handler.getUnitType()))
								continue;
							

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
