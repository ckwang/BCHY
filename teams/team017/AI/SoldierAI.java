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
import battlecode.common.RobotController;
import battlecode.common.SensorController;
import battlecode.common.WeaponController;

public class SoldierAI extends AI {

	private CombatSystem combat;
	private RobotController rc = controllers.myRC;
	private MovementController motor = controllers.motor;
	private SensorController sensor = controllers.sensor;
	private List<WeaponController> weapons = controllers.weapons;

	private MapLocation leaderLoc;
//	private boolean isEngaged = false;

	public SoldierAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);
	}

	public void proceed() {

		while (true) {
			rc.setIndicatorString(0, "Soldier");
			processMessage();
			
			combat.senseNearby();
			if (combat.enemyNum() > combat.allyNum() + 3) {
				try {
					combat.flee();
					yield();
					combat.flee();
				} catch (GameActionException e1) {}
			}
			else {
				while (combat.enemyNum() > 0) {
					combat.chaseTarget();
					combat.attack();
					yield();
					combat.senseNearby();
				}
			}
			while (combat.immobileEnemyNum() > 0) {
				combat.massacre();
				combat.senseNearby();
				yield();
			}
			
			try {
				navigate();
			} catch (GameActionException e) {}

			
			
			
//			if (isEngaged) {
//				if (combat.hasEnemy()) {
//					if (combat.enemyNum() > 0)
//						launchAttack();
//					else if (combat.immobileEnemyNum() > 0)
//						combat.massacre();
//
//				} else
//					isEngaged = false;
//			} else {
//				try {
//					navigate();
//				} catch (GameActionException e) {
//				}
//				if (combat.hasEnemy())
//					isEngaged = true;
//			}

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

//	public void followLeader() {
//		MapLocation loc = null;
//		try {
//			loc = sensor.senseLocationOf(leader);
//		} catch (GameActionException e1) {
//			loc = leaderLoc;
//		}
//		if (loc == null)
//			return;
//		navigator.setDestination(loc);
//		try {
//			Direction dir = navigator.getNextDir(2);
//			if (!rc.getDirection().equals(dir)) {
//				motor.setDirection(dir);
//				yield();
//			}
//			if (!motor.isActive() && motor.canMove(rc.getDirection()))
//				motor.moveForward();
//		} catch (GameActionException e1) {}
//	}

//	public boolean trackLeader() {
//		Robot[] robots = sensor.senseNearbyGameObjects(Robot.class);
//		for (Robot r : robots) {
//			if (r.getID() == leaderID) {
//				leader = r;
//				return true;
//			}
//		}
//		return false;
//	}

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
				// System.out.println("follow me message");
				FollowMeMessage fhandler = new FollowMeMessage(msg);
				MapLocation loc = fhandler.getSourceLocation();
				if (leaderLoc != null) {
					int curdist = rc.getLocation().distanceSquaredTo(leaderLoc);
					int newdist = rc.getLocation().distanceSquaredTo(loc);
					if (newdist < curdist)
						leaderLoc = loc;
				} else
					leaderLoc = loc;
				navigator.setDestination(leaderLoc);
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
			controllers.myRC.setIndicatorString(2, "roachNavigate");
			leaderLoc = null;
			roachNavigate();
		}
	}
	
}
