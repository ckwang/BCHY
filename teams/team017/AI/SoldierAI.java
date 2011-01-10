package team017.AI;

import java.util.List;

import team017.combat.CombatSystem;
import team017.message.BorderMessage;
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
//			rc.setIndicatorString(0, "Soldier");
			processMessage();
//			rc.setIndicatorString(1, String.valueOf(combat.enemyNum()));
//			rc.setIndicatorString(2, isEngaged? "true": "false");
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
			if (loc == null) {
				leaderLoc = null;
				return;
			}
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
			case BORDER:
				BorderMessage handler = new BorderMessage(msg);
				// update the borders
				int[] newBorders = handler.getBorderDirection();

				for (int i = 0; i < 4; ++i) {
					if (newBorders[i] != -1){
						if (borders[i] != newBorders[i]){
							borders[i] = newBorders[i];
						}
					}
				}
				
				homeLocation = handler.getHomeLocation();
				computeEnemyBaseLocation();
				controllers.myRC.setIndicatorString(1, homeLocation + "," + borders[0] + "," +borders[1] + "," +borders[2] + "," +borders[3] + "," +enemyBaseLoc);
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

	private void navigate() throws GameActionException {
		if (leaderLoc == null && enemyBaseLoc != null) {
			navigator.setDestination(enemyBaseLoc);
		}
		
		Direction nextDir = navigator.getNextDir(0);
		if (nextDir != Direction.OMNI) {
			if (!motor.isActive() && motor.canMove(nextDir)) {
				if (rc.getDirection() == nextDir) {
					motor.moveForward();
				} else {
					motor.setDirection(nextDir);
				}
			}
		} else if (enemyBaseLoc != null) {
			navigator.setDestination(enemyBaseLoc);
			leaderLoc = null;
		} else {
			controllers.myRC.setIndicatorString(2,"roachNavigate");
			leaderLoc = null;
			roachNavigate();
		}
	}

}
