package team017.AI;

import team017.construction.UnitType;
import team017.message.BorderMessage;
import team017.message.MessageHandler;
import team017.util.Util;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.Message;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class RecyclerAI extends AI {

	public RecyclerAI(RobotController rc) {
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

		if (Clock.getRoundNum() == 0)
			init();

		while (true) {
			try {

				// receive messages and handle them
				Message[] messages = controllers.myRC.getAllMessages();
				for (Message msg : messages) {

					switch (MessageHandler.getMessageType(msg)) {
					case BORDER:
						BorderMessage handler = new BorderMessage(msg);

						// update the borders
						int[] newBorders = handler.getBorderDirection();

						for (int i = 0; i < 4; ++i) {
							if (borders[i] == -1)
								borders[i] = newBorders[i];
						}
					}
				}

				if (fluxRate > 0 && controllers.myRC.getTeamResources() > 100) {
					if (Clock.getRoundNum() < 1000) {
						if (Clock.getRoundNum() % 3 == 0)
							buildingSystem.constructUnit(UnitType.CONSTRUCTOR);
						else
							buildingSystem.constructUnit(UnitType.GRIZZLY);

					} else {
						if (Clock.getRoundNum() % 5 == 0)
							buildingSystem.constructUnit(UnitType.CONSTRUCTOR);
						else
							buildingSystem.constructUnit(UnitType.GRIZZLY);
					}
				}
				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();

			}

		}

	}

	private void init() {
		try {
			// install an antenna to the adjacent recycler
			RobotInfo info = senseAdjacentChassis(Chassis.LIGHT);
			if (info != null
					&& controllers.myRC.getTeamResources() >= 2 * ComponentType.ANTENNA.cost
					&& !Util.containsComponent(info.components,
							ComponentType.ANTENNA)) {
				controllers.builder.build(ComponentType.ANTENNA, info.location,
						RobotLevel.ON_GROUND);
			}
			yield();

			info = senseAdjacentChassis(Chassis.BUILDING);
			if (info != null
					&& controllers.myRC.getTeamResources() >= 2 * ComponentType.ANTENNA.cost) {
				controllers.builder.build(ComponentType.ANTENNA, info.location,
						RobotLevel.ON_GROUND);
			}
		} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}
	}

	/***
	 * Sense nearby robots and return the location of one robot with specific
	 * chassis. Return none if there is no such robot.
	 * 
	 * @throws GameActionException
	 */
	private RobotInfo senseAdjacentChassis(Chassis chassis)
			throws GameActionException {
		Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
		for (Robot r : robots) {
			if (r.getTeam() == controllers.myRC.getTeam()) {
				RobotInfo info = controllers.sensor.senseRobotInfo(r);
				if (info.chassis == chassis)
					return info;
			}
		}
		return null;
	}

}
