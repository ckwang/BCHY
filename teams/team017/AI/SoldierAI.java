package team017.AI;

import java.util.List;

import team017.combat.CombatSystem;
import team017.message.BorderMessage;
import team017.message.FollowMeMessage;
import battlecode.common.Clock;
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
	private double prevHp = 50;
	private boolean attacked = false;
	
	private boolean reachedFirstBase = false;

	private Direction followDir;

	public SoldierAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);
	}

	public void proceed() {
		
		outer:
		while (true) {
			
			rc.setIndicatorString(0, "Soldier");
			
			try {processMessages();}
			catch (GameActionException e1) {}

			combat.senseNearby();
			rc.setIndicatorString(1, combat.enemyNum()+"");
			while (combat.enemyNum() > 0) {
				combat.chaseTarget();
				combat.attack();
				yield();
				combat.senseNearby();
				rc.setIndicatorString(1, combat.enemyNum()+"");
			}
			
			while (combat.immobileEnemyNum() > 0) {
				rc.setIndicatorString(2, combat.immobileEnemyNum() +"");
				MapLocation buildingLoc = combat.mass();
				if (buildingLoc != null)
					navigator.setDestination(buildingLoc);
				yield();
				combat.senseNearby();
				if (combat.enemyNum() > 0)
					continue outer;
			}
			
			if (attacked) {
				if (!motor.isActive()) {
					try {
						if (Clock.getRoundNum() % 2 == 1)
							motor.setDirection(controllers.myRC.getDirection().rotateLeft().rotateLeft());
						else
							motor.setDirection(controllers.myRC.getDirection().rotateRight().rotateRight());
					} catch (GameActionException e) {
					}
				}
				yield();
			}
			// if (combat.enemyNum() > combat.allyNum() + 3) {
			// try {
			// combat.flee();
			// yield();
			// combat.flee();
			// } catch (GameActionException e1) {}
			// }
			// else {
			// while (combat.enemyNum() > 0) {
			// combat.chaseTarget();
			// combat.attack();
			// yield();
			// combat.senseNearby();
			// }
			// }
//			int k = 0;
//			while (combat.immobileEnemyNum() > 0) {
//				combat.massacre();
//				++k;
//				if (k % 3 == 0)
//					combat.senseNearby();
//				yield();
//				if (combat.enemyNum() > 0)
//					break;
//			}

			try {navigate();}
//			controllers.myRC.setIndicatorString(2, "navigate");}
			catch (GameActionException e) {}
			
			sense_border();
			yield();

		}
	}

	public void flee(int steps) {
		int a = 0;
		while (a < steps) {
			try {
				if (combat.flee())
					a++;
				yield();
			} catch (GameActionException e1) {
			}
		}
	}

	public boolean launchAttack() {
		if (combat.hasEnemy() && controllers.weaponNum() > 0) {
			combat.chaseTarget();
			combat.attack();
			return true;
		}
		return false;
	}

	// public void followLeader() {
	// MapLocation loc = null;
	// try {
	// loc = sensor.senseLocationOf(leader);
	// } catch (GameActionException e1) {
	// loc = leaderLoc;
	// }
	// if (loc == null)
	// return;
	// navigator.setDestination(loc);
	// try {
	// Direction dir = navigator.getNextDir(2);
	// if (!rc.getDirection().equals(dir)) {
	// motor.setDirection(dir);
	// yield();
	// }
	// if (!motor.isActive() && motor.canMove(rc.getDirection()))
	// motor.moveForward();
	// } catch (GameActionException e1) {}
	// }

	// public boolean trackLeader() {
	// Robot[] robots = sensor.senseNearbyGameObjects(Robot.class);
	// for (Robot r : robots) {
	// if (r.getID() == leaderID) {
	// leader = r;
	// return true;
	// }
	// }
	// return false;
	// }

	public void yield() {
		super.yield();
		if (controllers.myRC.getHitpoints() < prevHp) {
			prevHp = controllers.myRC.getHitpoints();
			attacked = true;
		} else {
			attacked = false;
		}
	}
	
	@Override
	protected void processMessages() throws GameActionException{
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {
			case BORDER:
				BorderMessage handler = new BorderMessage(msg);
				// update the borders
				int[] newBorders = handler.getBorderDirection();

				for (int i = 0; i < 4; ++i) {
					if (newBorders[i] != -1) {
						if (borders[i] != newBorders[i]) {
							borders[i] = newBorders[i];
						}
					}
				}
				homeLocation = handler.getHomeLocation();
				computeEnemyBaseLocation();
				controllers.myRC.setIndicatorString(1, homeLocation + ","
						+ borders[0] + "," + borders[1] + "," + borders[2]
						+ "," + borders[3] + "," + enemyBaseLoc);
				break;
			case FOLLOW_ME_MESSAGE:
				// System.out.println("follow me message");
				FollowMeMessage fhandler = new FollowMeMessage(msg);
				MapLocation loc = fhandler.getSourceLocation();
				followDir = fhandler.getFollowDirection();
				if (leaderLoc != null) {
					int curdist = rc.getLocation().distanceSquaredTo(leaderLoc);
					int newdist = rc.getLocation().distanceSquaredTo(loc);
					if (newdist < curdist)
						leaderLoc = loc.add(followDir, 3);
				} else
					leaderLoc = loc.add(followDir, 3);
				navigator.setDestination(leaderLoc);
				break;

			}
		}
	}
	
	public void roachNavigate() throws GameActionException {
		int round = Clock.getRoundNum();
		switch (round % 3) {
		case 0:
			motor.setDirection(controllers.myRC.getDirection().rotateLeft().rotateLeft());
			break;
		case 1:
			super.roachNavigate();
			break;
		case 2:
			super.roachNavigate();
			break;
		case 3:
			motor.setDirection(controllers.myRC.getDirection().rotateRight().rotateRight());
			break;
		}
		return;
	}

	private void navigate() throws GameActionException {
		if (leaderLoc == null && enemyBaseLoc[0] != null) {
			if (reachedFirstBase)
				navigator.setDestination(enemyBaseLoc[controllers.myRC
						.getRobot().getID() % 2 + 1]);
			else
				navigator.setDestination(enemyBaseLoc[0]);
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
			if (enemyBaseLoc[0] != null
					&& controllers.myRC.getLocation().distanceSquaredTo(
							enemyBaseLoc[0]) < 25)
				reachedFirstBase = true;
		} else if (enemyBaseLoc[0] != null) {
			if (reachedFirstBase)
				navigator.setDestination(enemyBaseLoc[controllers.myRC
						.getRobot().getID() % 2 + 1]);
			else
				navigator.setDestination(enemyBaseLoc[0]);

			leaderLoc = null;
		} else {
			leaderLoc = null;
			if (!motor.isActive())
				roachNavigate();
		}
	}

}
