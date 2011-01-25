package team017.AI;

import java.util.HashSet;
import java.util.Set;

import team017.combat.CombatSystem;
import team017.message.FollowMeMessage;
import team017.message.GridMapMessage;
import team017.message.MineLocationsMessage;
import team017.message.PatrolDirectionMessage;
import team017.message.ScoutingInquiryMessage;
import team017.message.ScoutingResponseMessage;
import team017.util.Util;
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

	private Set<MapLocation> emptyMineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> alliedMineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> enemyMineLocations = new HashSet<MapLocation>();
	
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
	
	private MapLocation scoutingLocation;
	private Direction scoutingDir;
	private boolean leftward;
	
	private boolean enemyInSight = false;

	public SoldierAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);
		navigator.updateMap();
		scoutingDir = controllers.myRC.getDirection();
	}

	public void proceed() {
		birthRound = Clock.getRoundNum();
		RobotInfo target;
		
		proceed:
		while (true) {
//			controllers.myRC.setIndicatorString(0, scoutingDir + "" + controllers.myRC.getLocation() + scoutingLocation);
//			controllers.myRC.setIndicatorString(1, borders[0] + "," + borders[1] + "," + borders[2] + "," + borders[3]);

			if (controllers.enemyNum() > 0){
				enemyInSight = true;
			}
			else {
				enemyInSight = false;
			}
			
			
			int aband = 0;
			while (controllers.mobileEnemyNum() > 0) {
				
				target = combat.getMobile();
				if (target == null) {
					target = controllers.enemyMobile.get(0);
					Direction edir = rc.getLocation().directionTo(target.location);
					if (Util.isFacing(rc.getDirection(), edir))
						combat.moveForward();
					else
						combat.setDirection(edir);
					break;
//					if (!combat.moveForward()) break;
//					else continue;
				} else if (target.robot.getID() == aband)
					continue;
//				rc.setIndicatorString(0, "attacking mobile");
//				rc.setIndicatorString(1, "target" + target.robot.getID());
				aband = attackMobile(target);
				scoutingDir = controllers.myRC.getLocation().directionTo(target.location);
				while ( !gridMap.updateScoutLocation(scoutingDir) ) {
					scoutingDir = leftward ? scoutingDir.rotateLeft() : scoutingDir.rotateRight();
				}
				scoutingLocation = gridMap.getScoutLocation();
				yield();
			}
			while (controllers.immobileEnemyNum() > 0) {
				target = combat.getImmobile();
				if (target == null)
					break;
				Direction edir = rc.getLocation().directionTo(target.location);
				
				scoutingDir = controllers.myRC.getLocation().directionTo(target.location);
				while ( !gridMap.updateScoutLocation(scoutingDir) ) {
					scoutingDir = leftward ? scoutingDir.rotateLeft() : scoutingDir.rotateRight();
				}
				scoutingLocation = gridMap.getScoutLocation();
				
				while (!combat.setDirection(edir)) {
					combat.shoot(target);
					yield();
//					rc.setIndicatorString(0, "attacing immobile");
//					rc.setIndicatorString(1, "target" + target.robot.getID());
					if (controllers.mobileEnemyNum() > 0)
						continue proceed;
				}
//				for (int i = 0; i < 2 && !combat.primary.withinRange(target.location);) {
////					if (combat.approachTarget(target))
////						++i;
////					if (!combat.moveForward())
////						++i;
//					combat.shoot(target);
//					yield();
//					if (controllers.mobileEnemyNum() > 0)
//						continue proceed;
//				}
				if (!attackImmobile(target))
					unkilled = target;
			}
			
//			if (unkilled != null) {
//				rc.setIndicatorString(0, "attacing mobile");
//				int d = unkilled.location.distanceSquaredTo(rc.getLocation());
//				if (d < 30)	
//					navigateToDestination(unkilled.location, 9);
//			}
//			
//			if (attacked && controllers.enemyNum() == 0) {
//				rc.setIndicatorString(0, "checking surrounding");
//				Direction dir = rc.getDirection().opposite();
//				while (!combat.setDirection(dir))
//					yield();
//			}
			
			if (controllers.debrisNum() > 0 && !attacked) {
				target = null;
				for (RobotInfo d: controllers.debris) {
					if (combat.primary.withinRange(d.location)) {
						target = d;
						break;
					}
				}
				if (target != null) {
//					rc.setIndicatorString(0,"attacking debris "+ target.robot.getID());
					combat.shoot(target);
					yield();
				}

			}
			
			// If attacked and enemy not in sight, turn around
			if (attacked && controllers.enemyNum() == 0){
				scoutingDir = scoutingDir.opposite();
				while ( !gridMap.updateScoutLocation(scoutingDir) ) {
					scoutingDir = leftward ? scoutingDir.rotateLeft() : scoutingDir.rotateRight();
				}
				scoutingLocation = gridMap.getScoutLocation();
			}
			
			if (Clock.getRoundNum() < 1000 || Clock.getRoundNum() - birthRound > 100) {
				try {
					navigate();
				}
				catch (Exception e) {}
			}
			
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
		combat.heal();
		if (senseBorder())	scoutingLocation = gridMap.getScoutLocation();
		navigator.updateMap();
		processMessages();
		swarming = controllers.allyMobile.size() > 2;
		controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation()+"");
		controllers.myRC.setIndicatorString(1, enemyMineLocations.size()+"");
	}
	
	//return has target
	public int attackMobile(RobotInfo target) {
		int i;
		int round = controllers.motor.roundsUntilIdle() + 1;
		for (i = 0; i < 3 && !combat.primary.withinRange(target.location);) {
			try {
				target = sensor.senseRobotInfo(target.robot);
				combat.shoot(target);
			} catch (GameActionException e) {
				++i;
			}
			if (combat.approachTarget(target)) {
				yield();
				++i;
			}
//			rc.setIndicatorString(2, "approach i: " + i);
			yield();
		}
		if (i == 3)
			return target.robot.getID();
		round = combat.primary.roundsUntilIdle() + 1;
		yield();
		for (i = 0; i < 2 && !combat.shoot(target);) {
//			rc.setIndicatorString(2, "shooting i: " + i);
			try {
				target = sensor.senseRobotInfo(target.robot);
				if (combat.trackTarget(target))
					++i;
			} catch (GameActionException e) {
				++i; 
				combat.approachTarget(target);
					 
			}
			yield();
		}
//		rc.setIndicatorString(2, " ");
		if (i == 2)
			return target.robot.getID();
		return 0;
	}
	
	
	public boolean attackImmobile(RobotInfo target) {
		int i;
		for (i = 0; i < 2 && !combat.shoot(target); ++i) {
			int d = target.location.distanceSquaredTo(rc.getLocation());
			if (d > combat.optRange) {
				if (combat.moveForward())
					++i;
			}
			else if (attacked)
				combat.moveBackward();
			yield();
		}
		if (i == 2)
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
				if (scoutingLocation == null)
					scoutingLocation = homeLocation;
				computeEnemyBaseLocation();
				gridMap.merge(homeLocation, handler.getBorders(), handler.getInternalRecords());

				if (!gridMap.currentIsInbound())
					gridMap.updateScoutLocation(controllers.myRC.getLocation());
				break;
			}
			
			case PATROL_DIRECTION_MESSAGE: {
				PatrolDirectionMessage handler = new PatrolDirectionMessage(msg);
				
				
				if (scoutingDir == null) {
					scoutingDir = handler.getPatrolDirection();
					leftward = handler.isLeftward();
					if (homeLocation.distanceSquaredTo(controllers.myRC.getLocation()) > 16) {
						gridMap.setScoutLocation(handler.getSourceLocation());
					}

					scoutingLocation = homeLocation;
				}
				
				break;
			}
			
			case MINE_LOCATIONS_MESSAGE: {
				MineLocationsMessage handler = new MineLocationsMessage(msg);
				
				emptyMineLocations.addAll(handler.getEmptyMineLocations());
				alliedMineLocations.addAll(handler.getAlliedMineLocations());
				enemyMineLocations.addAll(handler.getEnemyMineLocations());

				break;
			}
			
			}
		}
	}

	private void navigate() throws GameActionException {
//		rc.setIndicatorString(0, "navigating");
		if (!enemyInSight){
			if ( navigateToDestination(scoutingLocation, 16) ) {
				if (enemyMineLocations.size() == 0){
					while ( !gridMap.updateScoutLocation(scoutingDir) ) {
						scoutingDir = leftward ? scoutingDir.rotateLeft() : scoutingDir.rotateRight();
					}
					scoutingLocation = gridMap.getScoutLocation();
				} 
//				else {
//					getAttackingLoc();
//				}
				
			}
		}
		else {
			if ( walkingNavigateToDestination(scoutingLocation, 16) ) {
				if (enemyMineLocations.size() == 0){
					while ( !gridMap.updateScoutLocation(scoutingDir) ) {
						scoutingDir = leftward ? scoutingDir.rotateLeft() : scoutingDir.rotateRight();
					}
					scoutingLocation = gridMap.getScoutLocation();
				} 
//				else {
//					getAttackingLoc();
//				}
			}
		}
		
	}
	
	private boolean getAttackingLoc(){
		if (enemyMineLocations.size() == 0)
			return false;
		
		MapLocation currentLoc = controllers.myRC.getLocation();
		
		scoutingLocation = homeLocation;
		
		for (MapLocation mineLoc : enemyMineLocations) {
			
			if (currentLoc.distanceSquaredTo(mineLoc) < currentLoc.distanceSquaredTo(scoutingLocation)){
				scoutingLocation = mineLoc;
			}
				
		}
		
		enemyMineLocations.remove(scoutingLocation);
		return true;

	}
}
