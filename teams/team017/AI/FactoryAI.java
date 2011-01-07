package team017.AI;

import team017.construction.UnitType;
import team017.message.BorderMessage;
import team017.message.BuildingRequestMessage;
import team017.message.MessageHandler;
import team017.message.MessageType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class FactoryAI extends AI {

	public FactoryAI(RobotController rc) {
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
