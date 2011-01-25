package team017.AI;

import java.util.ArrayList;
import java.util.List;

import team017.combat.CombatSystem;
import team017.message.FollowMeMessage;
import team017.message.GridMapMessage;
import team017.message.PatrolDirectionMessage;
import team017.util.Util;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.JumpController;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Mine;
import battlecode.common.MovementController;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.SensorController;

public class BattleConstructorAI extends GroundAI {

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
	private int lastUpdate = -1;
	
	private boolean enemyInSight = false;
	
	private RobotInfo target = null;
	private List<RobotInfo> mytypes = new ArrayList<RobotInfo>();
	private List<RobotInfo> others = new ArrayList<RobotInfo>();
	private List<RobotInfo> constructors = new ArrayList<RobotInfo>();
	private List<MapLocation> mines = new ArrayList<MapLocation>();
	private List<MapLocation> debris = new ArrayList<MapLocation>();

	public BattleConstructorAI(RobotController rc) {
		super(rc);
		combat = new CombatSystem(controllers);
		navigator.updateMap();
	}

	public void proceed() {
		birthRound = Clock.getRoundNum();
		while (true) {

			attack();
			
			// If attacked and enemy not in sight, turn around
			if (attacked && enemyNum() == 0){
				scoutingDir = scoutingDir.opposite();
				while ( !gridMap.updateScoutLocation(scoutingDir) ) {
					scoutingDir = leftward ? scoutingDir.rotateLeft() : scoutingDir.rotateRight();
				}
				scoutingLocation = gridMap.getScoutLocation();
			}
			
			try {navigate();}
			catch (Exception e) {}
			
			yield();

		}
	}
	
	public void attack() {
		if (combat.primary.isActive())
			return;
		if (target != null) {
			try {
				target = sensor.senseRobotInfo(target.robot);
				if (target.hitpoints <= 0)
					target = null;
			} catch (Exception e) {
				target = null;
			}
		}
		
		if (target != null) {
			if (combat.primary.withinRange(target.location))
				try {
					combat.primary.attackSquare(target.location, target.robot.getRobotLevel());
					return;
				} catch (GameActionException e) {
					e.printStackTrace();
					target = null;
				}
			else if (!motor.isActive()) {
				Direction edir = rc.getLocation().directionTo(target.location);
				try {
					motor.setDirection(edir);
				} catch (GameActionException e) {
					e.printStackTrace();
				}
				if (combat.primary.withinRange(target.location)) {
					try {
						combat.primary.attackSquare(target.location, target.robot.getRobotLevel());
						return;
					} catch (GameActionException e) {
						e.printStackTrace();
						target = null;
					}
				} 
			} 
		}
		
		if (motor.isActive()) {
			Util.sortHp(mytypes);
			for (RobotInfo r: mytypes) {
				if (combat.primary.withinRange(r.location)) {
					try {
						combat.primary.attackSquare(r.location, r.robot.getRobotLevel());
						target = r;
					} catch (GameActionException e) {
						e.printStackTrace();
					}
					return;
				}
			}
			Util.sortHp(others);
			for (RobotInfo r: others) {
				if (combat.primary.withinRange(r.location)) {
					try {
						combat.primary.attackSquare(r.location, r.robot.getRobotLevel());
						target = r;
					} catch (GameActionException e) {
						e.printStackTrace();
					}
					return;
				}
			}
		} else {
			Util.sortHp(mytypes);
			int d = combat.maxRange;
			MapLocation myloc = rc.getLocation();
			for (RobotInfo r: mytypes) {
				d = myloc.distanceSquaredTo(r.location);
				if (d < combat.maxRange) {
					Direction edir = rc.getLocation().directionTo(target.location);
					try {
						motor.setDirection(edir);
					} catch (GameActionException e) {
						e.printStackTrace();
					}
					if (combat.primary.withinRange(target.location)) {
						try {
							combat.primary.attackSquare(target.location, target.robot.getRobotLevel());
							target = null;
							return;
						} catch (GameActionException e) {
							e.printStackTrace();
							target = null;
						}
					} 
				}
			}
			Util.sortHp(others);
			for (RobotInfo r: mytypes) {
				d = myloc.distanceSquaredTo(r.location);
				if (d < combat.maxRange) {
					Direction edir = rc.getLocation().directionTo(target.location);
					try {
						motor.setDirection(edir);
					} catch (GameActionException e) {
						e.printStackTrace();
					}
					if (combat.primary.withinRange(target.location)) {
						try {
							combat.primary.attackSquare(target.location, target.robot.getRobotLevel());
							target = null;
							return;
						} catch (GameActionException e) {
							e.printStackTrace();
							target = null;
						}
					} 
				}
			}
		}
	}
	
	
	public void senseNearby() {
		int roundNum = Clock.getRoundNum();
		if (roundNum == lastUpdate)
			return;
		mytypes.clear();
		mines.clear();
		debris.clear();
		constructors.clear();
		others.clear();
		RobotInfo info;
		Boolean mytype;
		GameObject[] objects = sensor.senseNearbyGameObjects(GameObject.class);
		for (GameObject o: objects) {
			if (o instanceof Mine) {
				try {
					mines.add(((Mine) o).getLocation());
				} catch (Exception e) {
					e.printStackTrace();
				}
				continue;
			}
			try {
				info = sensor.senseRobotInfo((Robot)o);
				if (o.getTeam() == rc.getTeam()) {
					ComponentType[] coms = info.components;
					for (ComponentType c: coms) {
						if (c == ComponentType.CONSTRUCTOR || c == ComponentType.TELESCOPE)
							constructors.add(info);
						break;
					}
				} 
				else if (o.getTeam() == rc.getTeam().opponent()) {
					mytype = info.chassis == Chassis.BUILDING || info.chassis == Chassis.HEAVY;
					if (mytype)
						mytypes.add(info);
					else
						mytypes.add(info);
				} 
				else if (info.chassis == Chassis.DEBRIS) {
					if (combat.primary.withinRange(info.location))
						debris.add(info.location);
				}
			} catch (Exception e) {
				continue;
			}
		}
		lastUpdate = roundNum;
	}

	public void yield() {
		
		super.yield();
//		int before = Clock.getBytecodesLeft();
		attacked = rc.getHitpoints() < prevHp;
		prevHp = rc.getHitpoints();
		controllers.senseRobot();
		if (senseBorder())	scoutingLocation = gridMap.getScoutLocation();
		navigator.updateMap();
		processMessages();
		controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation()+"");
	}
	
	public int enemyNum() {
		if (Clock.getRoundNum() > lastUpdate)
			senseNearby();
		return mytypes.size() + others.size();
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
			
			}
		}
	}

	private void navigate() throws GameActionException {
//		rc.setIndicatorString(0, "navigating");
//		if (jumper == null) {
//			if (enemyBaseLoc[0] != null) {
//				if ( navigateToDestination(enemyBaseLoc[0], 9) )
//					enemyBaseLoc[0] = null;
//			} else if (enemyBaseLoc[1] != null) {
//				if ( navigateToDestination(enemyBaseLoc[1], 9) )
//					enemyBaseLoc[1] = null;
//			} else if (enemyBaseLoc[2] != null) {
//				if ( navigateToDestination(enemyBaseLoc[2], 9) )
//					enemyBaseLoc[2] = null;
//			} else {
//				roachNavigate();
//			}
//		} else {
		if (!enemyInSight){
			if ( navigateToDestination(scoutingLocation, 4) ) {
				while ( !gridMap.updateScoutLocation(scoutingDir) ) {
					scoutingDir = leftward ? scoutingDir.rotateLeft() : scoutingDir.rotateRight();
				}
				scoutingLocation = gridMap.getScoutLocation();
			}
		}
		else {
			if ( walkingNavigateToDestination(scoutingLocation, 4) ) {
				while ( !gridMap.updateScoutLocation(scoutingDir) ) {
					scoutingDir = leftward ? scoutingDir.rotateLeft() : scoutingDir.rotateRight();
				}
				scoutingLocation = gridMap.getScoutLocation();
			}
		}
			
//		}
	}
}
