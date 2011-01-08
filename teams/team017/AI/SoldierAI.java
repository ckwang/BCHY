package team017.AI;

import team017.combat.CombatSystem;
import team017.message.EnemyLocationMessage;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.MovementController;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class SoldierAI extends AI {

	private CombatSystem combat;
	private MapLocation enemyLoc;
	private RobotController rc = controllers.myRC;
	private MovementController motor = controllers.motor;
	private MapLocation leaderLoc = null;

	public SoldierAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);
	}

	public void proceed() {

		while (true) {
			controllers.myRC.setIndicatorString(0, controllers.myRC
					.getLocation().toString());
			// receive messages and handle them
			while (msgHandler.hasMessage()) {
				Message msg = msgHandler.nextMessage();
				switch (msgHandler.getMessageType(msg)) {
				case ENEMY_LOCATION: {
					EnemyLocationMessage handler = new EnemyLocationMessage(msg);
					enemyLoc = handler.getEnemyLocation();
					navigator.setDestination(enemyLoc);
					controllers.myRC.setIndicatorString(1, enemyLoc.toString());
					break;
				}

				}
			}

			rc.setIndicatorString(1, String.valueOf(combat.enemyNum()));

			if (combat.enemyNum() > 0 && controllers.weaponNum() > 0) {
				// if (combat.approachTarget())
				// rc.yield();
				combat.approachTarget();
				combat.attack();
			}

			try {
				navigate();
			} catch (GameActionException e) {
			}
			sense_border();
		}
	}

	private void sense_border() {
		Direction[] addDirs = new Direction[3];
		if (rc.getDirection().isDiagonal()) {
			addDirs[0] = rc.getDirection().rotateLeft();
			addDirs[1] = rc.getDirection().rotateRight();
		} else {
			addDirs[0] = rc.getDirection();
		}
		int j = -1;
		while (addDirs[++j] != null) {
			MapLocation currentLoc = rc.getLocation();
			int i;
			for (i = 3; i > 0; i--) {
				TerrainTile tile = rc.senseTerrainTile(currentLoc.add(
						addDirs[j], i));
				if (tile == null || tile != TerrainTile.OFF_MAP)
					// if (rc.senseTerrainTile(currentLoc.add(
					// addDirs[j], i)) != TerrainTile.OFF_MAP)
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
	}

	private void navigate() throws GameActionException {
		Direction nextDir = navigator.getNextDir(0);
		if (nextDir != Direction.OMNI) {
			if (!motor.isActive() && motor.canMove(nextDir)) {
				if (rc.getDirection() == nextDir) {
					motor.moveForward();
				} else {
					motor.setDirection(nextDir);
				}
			}
		} else if (!motor.isActive()) {
			roachNavigate();
		}
	}

	private void roachNavigate() throws GameActionException {
		// navigate();
		if (motor.canMove(rc.getDirection())) {
			motor.moveForward();
		} else {
			Direction tempDir = rc.getDirection();
			int rotationTimes = Clock.getRoundNum() % 7;
			for (int i = 0; i <= rotationTimes; ++i) {
				tempDir = tempDir.rotateRight();
			}
			motor.setDirection(tempDir);
		}
	}

}
