package team017.AI;

import team017.combat.CombatSystem;
import team017.message.FollowMeMessage;
import team017.message.GridMapMessage;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.JumpController;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.MovementController;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.SensorController;

public class SoldierAI extends GroundAI {

	private CombatSystem combat;
	private RobotController rc = controllers.myRC;
	private MovementController motor = controllers.motor;
	private SensorController sensor = controllers.sensor;
	private JumpController jumper = controllers.jumper;

	private MapLocation leaderLoc = null;

	private double prevHp = 0;
	private boolean attacked = false;
	private boolean swarming = false;
	
	private RobotInfo unkilled = null;
	private int leaderID = -1;
	private int birthRound;

	private boolean reachedFirstBase = false;
	private boolean hasLeader = false;

	private Direction followDir;
	private Direction previousDir = null;

	public SoldierAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);
		navigator.updateMap();
	}

	public void proceed() {
		birthRound = Clock.getRoundNum();
		RobotInfo target;
		proceed: while (true) {
			
			while (controllers.mobileEnemyNum() > 0) {
				target = combat.getMobile();
				if (target == null) {
					if (!combat.moveForward()) break;
					else continue;
				}
				attackMobile(target);
			}
			while (controllers.immobileEnemyNum() > 0) {
				target = combat.getImmobile();
				if (target == null)
					break;
				Direction edir = rc.getLocation().directionTo(target.location);
				while (!combat.setDirection(edir)) {
					combat.shoot(target);
					yield();
					if (controllers.mobileEnemyNum() > 0)
						continue proceed;
				}
				for (int i = 0; i < 4 && !combat.primary.withinRange(target.location);) {
					if (!combat.moveForward())
						++i;
					combat.shoot(target);
					yield();
					if (controllers.mobileEnemyNum() > 0)
						continue proceed;
				}
				if (!attackImmobile(target))
					unkilled = target;
			}
			
			if (unkilled != null) {
				int d = unkilled.location.distanceSquaredTo(rc.getLocation());
				if (d < 36)	
					navigator.setDestination(unkilled.location);
			}
			
			if (controllers.debrisNum() > 0 && !attacked) {
				target = null;
				for (RobotInfo d: controllers.debris) {
					if (combat.primary.withinRange(d.location)) {
						target = d;
					}
				}
				if (target != null) {
					combat.shoot(target);
					yield();
				}

			}

			if (Clock.getRoundNum() < 1000 || Clock.getRoundNum() - birthRound > 100) {
				try {
					navigate();
					yield();
				}
				catch (Exception e) {}
			}
			
 			broadcast();
			yield();

		}
	}

	public void yield() {
		
		super.yield();
//		int before = Clock.getBytecodesLeft();
		attacked = rc.getHitpoints() < prevHp;
//		if ((rc.getHitpoints() - prevHp) < -0.4 * rc.getMaxHp())
//			combat.moveBackward();
		prevHp = rc.getHitpoints();
		controllers.senseRobot();
		int before = Clock.getBytecodesLeft();
		combat.heal();
		senseBorder();
		int after = Clock.getBytecodesLeft();
		navigator.updateMap();
		if ((before - after) > 0)
			System.out.println("" + (before - after));
		processMessages();
		swarming = controllers.allyMobile.size() > 2;
	}
	
	//return has target
	public boolean attackMobile(RobotInfo target) {
		boolean shot = false;
		int i;

		for (i = 0; i < 4 && !combat.primary.withinRange(target.location);) {
			if (combat.approachTarget(target)) {
				yield();
			}
			try {
				target = sensor.senseRobotInfo(target.robot);
				shot = combat.shoot(target);
			} catch (GameActionException e) {
				++i;
				combat.moveForward();
			}
		}
		if (i == 4 && !shot)
			return true;
		for (i = 0; i < 5 && !combat.shoot(target);) {
			try {
				target = sensor.senseRobotInfo(target.robot);
				combat.trackTarget(target);
			} catch (GameActionException e) {
				combat.approachTarget(target);
				++i;
			}
			yield();
		}
		if (i == 5)
			return false;
		return true;
	}
	
	
	public boolean attackImmobile(RobotInfo target) {
		int i;
		for (i = 0; i < 6 && !combat.shoot(target); ++i) {
			int d = target.location.distanceSquaredTo(rc.getLocation());
			if (d > combat.optRange)
				combat.moveForward();
			else if (attacked)
				combat.moveBackward();
			yield();
		}
		if (i == 6)
			return false;
		return true;
	}
	
	
	public void flee() {
		Direction toTurn;
//		MapLocation myloc = rc.getLocation();
//		if (controllers.mobileEnemyNum() > 0) {
//			MapLocation enemyCenter = Util.aveLocation(controllers.enemyMobile);
//			Direction edir = myloc.directionTo(enemyCenter);
//			toTurn = edir.opposite();
//		}
//		else if (controllers.immobileEnemyNum() > 0) {
//			MapLocation enemyCenter = Util.aveLocation(controllers.enemyImmobile);
//			Direction edir = myloc.directionTo(enemyCenter);
//			toTurn = edir.opposite();
//		}
//		else
			toTurn = rc.getDirection();
		if (!motor.isActive()) {
			try {
				motor.setDirection(toTurn);
			} catch (Exception e) {
				e.printStackTrace();
			}
			yield();
		}
		if (!motor.isActive() && 
			motor.canMove(toTurn) && 
			rc.getDirection() == toTurn) {
			try {
				motor.moveForward();
			} catch (GameActionException e) {
			}
		}
	}

	public void broadcast() {
		if (controllers.comm == null)
			return;
		if (controllers.mobileEnemyNum() > 0) {
			msgHandler.clearOutQueue();
			// msgHandler.queueMessage(new
			// EnemyInformationMessage(controllers.enemyMobile));
			msgHandler.process();
		} else if (!hasLeader) {
			if (previousDir != rc.getDirection()
					|| Clock.getRoundNum() % 3 == 0) {
				msgHandler.queueMessage(new FollowMeMessage(rc
						.getDirection(), controllers.comm.type().range));
			}
		}
	}

	@Override
	protected void processMessages() {
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {

			case GRID_MAP_MESSAGE: {
				GridMapMessage handler = new GridMapMessage(msg);
				// update the borders
				int[] newBorders = handler.getBorders();
				for (int i = 0; i < 4; ++i) {
					if (newBorders[i] != -1) {
						if (borders[i] != newBorders[i]) {
							borders[i] = newBorders[i];
						}
					}
				}
				homeLocation = handler.getHomeLocation();
				computeEnemyBaseLocation();
				gridMap.merge(homeLocation, handler.getBorders(), handler.getInternalRecords());

				break;
			}
//			case FOLLOW_ME_MESSAGE:
//				FollowMeMessage fhandler = new FollowMeMessage(msg);
//				if (leaderID == -1) {
//					leaderID = fhandler.getSourceID();
//				}
//				if (leaderID == fhandler.getSourceID()) {
//					/*
//					 * If 2 commanders meet, follow the one with a longer range
//					 * of broadcast If the range is the same, follow the one
//					 * with a smaller ID
//					 */
//					if (controllers.comm != null) {
//						if (controllers.comm.type().range < fhandler
//								.getCommRange())
//							break;
//						else if (controllers.comm.type().range == fhandler
//								.getCommRange()
//								&& fhandler.getSourceID() < rc.getRobot()
//										.getID())
//							break;
//					}
//					leaderMessageRoundCounter = 0;
//					hasLeader = true;
//					MapLocation loc = fhandler.getSourceLocation();
//					followDir = fhandler.getFollowDirection();
//					if (leaderLoc != null) {
//						int curdist = rc.getLocation().distanceSquaredTo(
//								leaderLoc);
//						int newdist = rc.getLocation().distanceSquaredTo(loc);
//						if (newdist < curdist)
//							leaderLoc = loc;
//					} else
//						leaderLoc = loc;
//				}
//				break;
			}
		}
	}

	private void navigate() throws GameActionException {
		if (enemyBaseLoc[0] != null) {
			if ( navigateToDestination(enemyBaseLoc[0], 9) )
				enemyBaseLoc[0] = null;
		} else if (enemyBaseLoc[1] != null) {
			if ( navigateToDestination(enemyBaseLoc[1], 9) )
				enemyBaseLoc[1] = null;
		} else if (enemyBaseLoc[2] != null) {
			if ( navigateToDestination(enemyBaseLoc[2], 9) )
				enemyBaseLoc[2] = null;
		} else {
			navigateToDestination(homeLocation.add(Direction.NORTH, 60), 9);
			controllers.myRC.setIndicatorString(0, "currentLoc: " + controllers.myRC.getLocation());
		}
	}
}
