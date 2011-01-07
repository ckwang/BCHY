package team017.AI;

import team017.construction.BuilderDirections;
import team017.construction.UnitType;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructionCompleteMessage;
import team017.message.MessageHandler;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class ArmoryAI extends AI{
	BuilderDirections builderDirs;

	public ArmoryAI(RobotController rc) {
		super(rc);
		builderDirs = new BuilderDirections(controllers);

	}

	@Override
	public void yield() throws GameActionException {
		controllers.myRC.yield();
		controllers.updateComponents();
		updateFluxRate();
	}

	@Override
	public void proceed() {

		while (true) {
			try {
				Message[] messages = controllers.myRC.getAllMessages();
				for (Message msg : messages) {

					switch (MessageHandler.getMessageType(msg)) {
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
