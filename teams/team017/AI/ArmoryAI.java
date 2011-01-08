package team017.AI;

import team017.message.BuildingRequestMessage;
import team017.message.ConstructionCompleteMessage;
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
						BuildingRequestMessage handler = new BuildingRequestMessage(
								msg);
						if (handler.getBuilderLocation().equals(
								controllers.myRC.getLocation())) {
							buildingSystem.constructComponent(
									handler.getBuildingLocation(),
									handler.getUnitType());
							yield();
						}
						break;}
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

				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}

	}

}
