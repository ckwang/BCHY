package team017.AI;

import java.util.List;

import team017.combat.CombatSystem;
import team017.message.EnemyLocationMessage;
import team017.message.FollowMeMessage;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.MovementController;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.SensorController;
import battlecode.common.TerrainTile;
import battlecode.common.WeaponController;

public class SoldierAI extends AI {

	private CombatSystem combat;
	private MapLocation enemyBase;
	private RobotController rc = controllers.myRC;
	private MovementController motor = controllers.motor;
	private SensorController sensor = controllers.sensor;
	private List<WeaponController> weapons = controllers.weapons;

	private int leaderID;
	private MapLocation leaderLoc;
	private Robot leader = null;
	private boolean isEngaged = false;
//	private boolean massacre = true;

	public SoldierAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);
	}

	public void proceed() {

		while (true) {
			rc.setIndicatorString(0, "Soldier");
			processMessage();
			rc.setIndicatorString(1, String.valueOf(combat.enemyNum()));
			rc.setIndicatorString(2, isEngaged? "true": "false");
			if (isEngaged) {
				if (combat.hasEnemy()) {
					if (combat.enemyNum() > 0)
						launchAttack();
					else if (combat.immobileEnemyNum() > 0)
						combat.massacre();
					
				} else
					isEngaged = false;
			} else {
				try {
					navigate();
				} catch (GameActionException e) {}
				if (combat.hasEnemy())
					isEngaged = true;
			}

			sense_border();

			yield();
		}
	}

	public boolean launchAttack() {
		if (combat.hasEnemy() && controllers.weaponNum() > 0) {
			combat.chaseTarget();
			return combat.attack();
		}
		return false;
	}

	public void followLeader() {
		MapLocation loc = null;
		try {
			loc = sensor.senseLocationOf(leader);
		} catch (GameActionException e1) {
			loc = leaderLoc;
			if (loc == null)
				return;
		}
		navigator.setDestination(loc);
		try {
			Direction dir = navigator.getNextDir(2);
			if (dir == Direction.OMNI || dir == Direction.NONE)
				dir = rc.getLocation().directionTo(loc);
			if (!rc.getDirection().equals(dir)) {
				motor.setDirection(dir);
			}
			if (!motor.isActive())
				motor.moveForward();
		} catch (GameActionException e1) {
		}
		// try {
		// RobotInfo info = sensor.senseRobotInfo(leader);
		// if (rc.getLocation().isAdjacentTo(loc)) {
		// motor.setDirection(info.direction);
		// yield();
		// }
		// } catch (GameActionException e) {}
	}

	public boolean trackLeader() {
		Robot[] robots = sensor.senseNearbyGameObjects(Robot.class);
		for (Robot r : robots) {
			if (r.getID() == leaderID) {
				leader = r;
				return true;
			}
		}
		return false;
	}

	private void processMessage() {
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {
			case ENEMY_LOCATION:
				EnemyLocationMessage handler = new EnemyLocationMessage(msg);
				enemyBase = handler.getEnemyLocation();
				break;

			case FOLLOW_ME_MESSAGE:
				if (leader == null) {
					// System.out.println("follow me message");
					FollowMeMessage fhandler = new FollowMeMessage(msg);
					leaderID = fhandler.getSourceID();
					leaderLoc = fhandler.getSourceLocation();
					navigator.setDestination(leaderLoc);
				}
				break;

			}
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
				if (rc.senseTerrainTile(currentLoc.add(addDirs[j], i)) != TerrainTile.OFF_MAP)
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
		rc.setIndicatorString(2, "I am stucked");
		Direction nextDir = navigator.getNextDir(0);
		if (nextDir != Direction.OMNI) {
			if (!motor.isActive() && motor.canMove(nextDir)) {
				if (rc.getDirection() == nextDir) {
					motor.moveForward();
				} else {
					motor.setDirection(nextDir);
				}
			}
		} else if (enemyBase != null) {
			navigator.setDestination(enemyBase);
			leaderLoc = null;
		} else {
			leaderLoc = null;
			roachNavigate();
		}
	}

}
