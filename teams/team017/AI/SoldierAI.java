package team017.AI;

import team017.combat.CombatSystem;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class SoldierAI extends AI {

	private CombatSystem combat;
	
	public SoldierAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(myRC, motor, sensor, weapons);
	}

	public void yield() throws GameActionException {
		
	}
	
	public void proceed() {

		while (true) {
			// MessageHandler encoder = new BorderMessage(myRC, comm,
			// Direction.NORTH);
			// encoder.send();
			//
			// Message incomingMsg;
			// MessageHandler decoder = new BorderMessage(incomingMsg);
			// decoder.getMessageType();
			// ((BorderMessage) decoder).getBorderDirection();

			try {
				updateComponents();

				/*** beginning of main loop ***/
				if (motor != null) {
					navigate();
				}

				combat.init_attack();

				sense_border();

				myRC.yield();

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

			if (myRC.getDirection().isDiagonal()) {
				addDirs[0] = myRC.getDirection().rotateLeft();
				addDirs[1] = myRC.getDirection().rotateRight();
			} else {
				addDirs[0] = myRC.getDirection();
			}

			int j = -1;
			while (addDirs[++j] != null) {
				MapLocation currentLoc = myRC.getLocation();

				int i;
				for (i = 3; i > 0; i--) {
					if (myRC.senseTerrainTile(currentLoc.add(addDirs[j], i)) != TerrainTile.OFF_MAP)
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

		if (!motor.isActive()) {
			roachNavigate();
		}

	}

	private void roachNavigate() throws GameActionException {
		// navigate();
		if (motor.canMove(myRC.getDirection())) {
			// System.out.println("about to move");
			motor.moveForward();
		} else {
			if ((Clock.getRoundNum() / 10) % 2 == 0)
				motor.setDirection(myRC.getDirection().rotateRight());
			else
				motor.setDirection(myRC.getDirection().rotateLeft());
		}
	}

}
