package team017.AI;

import team017.construction.UnitType;
import team017.message.BuildingRequestMessage;
import team017.message.MessageHandler;
import battlecode.common.GameActionException;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class ArmoryAI extends AI{
	public ArmoryAI(RobotController rc) {
		super(rc);
	}

	@Override
	public void yield() throws GameActionException {
		controllers.myRC.yield();
		updateComponents();
		updateFluxRate();
	}

	@Override
	public void proceed() {

		while (true) {
			try {
				Message[] messages = controllers.myRC.getAllMessages();
				for (Message msg : messages) {

					switch (MessageHandler.getMessageType(msg)) {
					case BUILDING_REQUEST:
						BuildingRequestMessage handler = new BuildingRequestMessage(
								msg);
						if (handler.getBuilderLocation().equals(
								controllers.myRC.getLocation())) {
							buildingSystem.constructComponent(
									handler.getBuildingLocation(),
									handler.getUnitType());
							yield();
						}
						break;
					}
				}

				if (fluxRate > 0 && controllers.myRC.getTeamResources() > 120)
					buildingSystem.constructUnit(UnitType.TANK_KILLER);
				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}

	}

}
