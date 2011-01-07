package team017.AI;

import team017.combat.CombatSystem;
import team017.message.BorderMessage;
import team017.message.BuildingRequestMessage;
import team017.message.EnemyLocationMessage;
import team017.message.MessageHandler;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class SoldierAI extends AI {

	private CombatSystem combat;
	private MapLocation enemyLoc;
	
	public SoldierAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers.myRC, controllers.motor, controllers.sensor, controllers.weapons);
	}

	public void yield() throws GameActionException {
		
	}
	
	public void proceed() {

		while (true) {
			// MessageHandler encoder = new BorderMessage(controllers.myRC, comm,
			// Direction.NORTH);
			// encoder.send();
			//
			// Message incomingMsg;
			// MessageHandler decoder = new BorderMessage(incomingMsg);
			// decoder.getMessageType();
			// ((BorderMessage) decoder).getBorderDirection();

			try {
				controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation().toString());
				updateComponents();
				// receive messages and handle them
				Message[] messages = controllers.myRC.getAllMessages();
				for (Message msg : messages) {

					switch (MessageHandler.getMessageType(msg)) {
					case ENEMY_LOCATION: {
						EnemyLocationMessage handler = new EnemyLocationMessage(msg);
						enemyLoc = handler.getEnemyLocation();
						navigator.setDestination(enemyLoc);
						
						controllers.myRC.setIndicatorString(1, enemyLoc.toString());
						break;
					}

					}
				}

				/*** beginning of main loop ***/
				if (controllers.motor != null) {
					navigate();
				}

				combat.init_attack();

				sense_border();

				controllers.myRC.yield();

				/*** end of main loop ***/
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}

	private void sense_border() {
		try {

			Direction[] addDirs = new Direction[3];

			if (controllers.myRC.getDirection().isDiagonal()) {
				addDirs[0] = controllers.myRC.getDirection().rotateLeft();
				addDirs[1] = controllers.myRC.getDirection().rotateRight();
			} else {
				addDirs[0] = controllers.myRC.getDirection();
			}

			int j = -1;
			while (addDirs[++j] != null) {
				MapLocation currentLoc = controllers.myRC.getLocation();

				int i;
				for (i = 3; i > 0; i--) {
					if (controllers.myRC.senseTerrainTile(currentLoc.add(addDirs[j], i)) != TerrainTile.OFF_MAP)
						break;
				}

				// i == 3 means no OFF_MAP sensed
				if (i != 3) {
					switch (addDirs[j]) {
					case NORTH:
						borders[0] = currentLoc.y - (i + 1);
						break;
					case EAST:
						borders[1] = currentLoc.x + (i + 1);
						break;
					case SOUTH:
						borders[2] = currentLoc.y + (i + 1);
						break;
					case WEST:
						borders[3] = currentLoc.x - (i + 1);
						break;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}

	}

	private void navigate() throws GameActionException {

		Direction nextDir = navigator.getNextDir(0);
		if (nextDir != Direction.OMNI) {
		
			if (!controllers.motor.isActive() && controllers.motor.canMove(nextDir) ) {
				if ( controllers.myRC.getDirection() == nextDir ) {
					controllers.motor.moveForward();
				} else {
					controllers.motor.setDirection(nextDir);
				}
			}
		
		}
		else if (!controllers.motor.isActive()) {
			roachNavigate();
		}

		yield();
	}

	private void roachNavigate() throws GameActionException {
		// navigate();
		if (controllers.motor.canMove(controllers.myRC.getDirection())) {
			// System.out.println("about to move");
			controllers.motor.moveForward();
		} else {
			Direction tempDir = controllers.myRC.getDirection();
			int rotationTimes = Clock.getRoundNum() % 7;
			for (int i = 0; i <= rotationTimes; ++i){
				tempDir = tempDir.rotateRight();
			}
				controllers.motor.setDirection(tempDir);
		}
	}

}
