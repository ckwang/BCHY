package team017.AI;

import team017.construction.UnitType;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructUnitMessage;
import team017.message.ConstructionCompleteMessage;
import team017.util.Util;
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
		while (controllers.motor.isActive() || controllers.comm == null)
			yield();
		msgHandler.queueMessage(new ConstructionCompleteMessage(controllers.myRC.getLocation(), UnitType.ARMORY));

		
		while (true) {
			try {
//				controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation().toString());
//				controllers.myRC.setIndicatorString(1, "F:" + buildingLocs.factoryLocation + ",R:" + buildingLocs.recyclerLocation);
				processMessages();
				
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
//				controllers.myRC.setIndicatorString(2, "Complete:" + Clock.getRoundNum());
				ConstructionCompleteMessage handler = new ConstructionCompleteMessage(msg);
				MapLocation currentLoc = controllers.myRC.getLocation();
				if (handler.getBuildingLocation().distanceSquaredTo(currentLoc) < 5) {
					buildingLocs.setLocations(handler.getBuildingType(), handler.getBuildingLocation());
				}
				break;
			}
			
			case CONSTRUCT_UNIT_MESSAGE: {
				controllers.myRC.setIndicatorString(0, "ConstructMessageGot" + Clock.getRoundNum());
				ConstructUnitMessage handler = new ConstructUnitMessage(msg);
				if (controllers.myRC.getLocation() == handler.getBuilderLocation()) {
					UnitType type = handler.getType();
					MapLocation buildLoc = buildingLocs.constructableLocation(Util.ARMORY_CODE, type.requiredBuilders);
					if (buildLoc != null)
						buildingSystem.constructUnit(buildLoc,type, buildingLocs);
				}
				break;
			}
			}
		}
	}
}
