package team017.AI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import team017.construction.UnitType;
import team017.message.ConstructBaseMessage;
import team017.message.ConstructUnitMessage;
import team017.message.ConstructionCompleteMessage;
import team017.message.GoToMessage;
import team017.message.GreetingMessage;
import team017.message.GridMapMessage;
import team017.message.HasArrivedMessage;
import team017.message.MineInquiryMessage;
import team017.message.MineLocationsMessage;
import team017.message.MineResponseMessage;
import team017.message.ScoutingInquiryMessage;
import team017.message.ScoutingResponseMessage;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;
import battlecode.common.TerrainTile;

public class ScoutAI extends AI {

	private int id;
	
	private Set<MapLocation> blockedMineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> emptyMineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> alliedMineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> enemyMineLocations = new HashSet<MapLocation>();

	private List<RobotInfo> nearbyEnemy = new ArrayList<RobotInfo>();
	
	private int childID = -1;
	
	private double prevHp = 0;
	private boolean attacked = false;
	
	private boolean scouted = false;
	private MapLocation destination = null;
	private MapLocation scoutingLocation = null;
	private MapLocation neareastRecycler;
	
	private Direction scoutingDir;
	private boolean leftward;
	private boolean branch;
	
	private int scoutCount = 1;
	private boolean builtBranch = true;
	
	public ScoutAI(RobotController rc) {
		super(rc);
		id = rc.getRobot().getID();
		neareastRecycler = homeLocation;
	}
	
	@Override
	public void yield() {
		super.yield();
		nearbyEnemy.clear();
		controllers.scoutNearby();
		controllers.myRC.setIndicatorString(1, controllers.distanceToNearestEnemy+"");
		if (senseBorder())	{
			scoutingLocation = gridMap.getScoutLocation();
		}
		attacked = controllers.myRC.getHitpoints() < prevHp;
		prevHp = controllers.myRC.getHitpoints();

	}

	@Override
	public void proceed() {
		
		msgHandler.queueMessage(new ScoutingInquiryMessage(false));
		while (scoutingDir == null) {
			try {processMessages();} catch (Exception e) {e.printStackTrace();}
//			controllers.myRC.setIndicatorString(0, homeLocation + "," + destination);
			yield();
		}
			
		
		while (true) {
			
			try {processMessages();} catch (Exception e) {e.printStackTrace();}
			

			controllers.myRC.setIndicatorString(0, emptyMineLocations + "");
			
			if ( (controllers.distanceToNearestEnemy < 121 || attacked) )
				flee();
			else
				navigate();
			
			yield();
		}
	}
	
//	public boolean evaluateDanger() {
//		Direction dir = controllers.myRC.getDirection().opposite();
//		for (int i = 0; i < 3;) {
//			if (!controllers.motor.isActive() && controllers.motor.canMove(dir)) {
//				try {
//					controllers.motor.moveBackward();
//					++i;
//				} 
//				catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		return true;
//	}
	
	private void watch() {
		while (controllers.motor.isActive())
			yield();
		
		for (int i = 0; i < 8; i++) {
			try {
				if (branch) {
					for (MapLocation m : controllers.emptyMines) {
						if (isBlocked(m))
							blockedMineLocations.add(m);
					}
				}
				
				emptyMineLocations.addAll(controllers.emptyMines);
				emptyMineLocations.removeAll(controllers.allyMines);
				emptyMineLocations.removeAll(controllers.enemyMines);
				
				alliedMineLocations.addAll(controllers.allyMines);
				enemyMineLocations.addAll(controllers.enemyMines);

				controllers.motor.setDirection(controllers.myRC.getDirection().rotateRight());
				yield();
				if (controllers.distanceToNearestEnemy < 81 || attacked )
					break;
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
		findNearestRecycler();
	}
	
	private boolean isBlocked(MapLocation loc) {
		final int THRESHOLD = 3;
		
		Direction dir = Direction.NORTH;
		SensorController sensor = controllers.sensor;
		int n = 0;
		
		for (int i = 0; i < 7 + THRESHOLD; i++) {
			try {
				MapLocation m = loc.add(dir);
				if (sensor.canSenseSquare(m)) {
					if (controllers.myRC.senseTerrainTile(m) == TerrainTile.LAND &&
							sensor.senseObjectAtLocation(m, RobotLevel.ON_GROUND) == null &&
							sensor.senseObjectAtLocation(m, RobotLevel.MINE) == null) {
						n++;
					} else {
						n = 0;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (n == THRESHOLD)	return false;
			dir = dir.rotateRight();
		}
		
		return true;
	}

	@Override
	protected void processMessages() throws GameActionException {
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
			
			case SCOUTING_RESPONSE_MESSAGE: {
				ScoutingResponseMessage handler = new ScoutingResponseMessage(msg);
//				controllers.myRC.setIndicatorString(2, "received");
				if (handler.getTelescoperID() == id && handler.getSourceLocation().isAdjacentTo(controllers.myRC.getLocation())) {
					scoutingDir = handler.getScoutingDirection();
					leftward = handler.isLeftward();
					branch = handler.isBranch();
					
					if (homeLocation.distanceSquaredTo(controllers.myRC.getLocation()) > 16) {
						gridMap.setScoutLocation(handler.getSourceLocation());
					}
					while ( !gridMap.updateScoutLocation(scoutingDir) ) {
						scoutingDir = leftward ? scoutingDir.rotateLeft() : scoutingDir.rotateRight();
					}
					scoutingLocation = destination = gridMap.getScoutLocation();
				}
				
				break;
			}
			
//			case MINE_INQUIRY_MESSAGE: {
//				MineInquiryMessage handler = new MineInquiryMessage(msg);
//				
//				if ( scouted ){
//					msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));
//					yield();
//					msgHandler.queueMessage(new MineResponseMessage(handler.getSourceID(), emptyMineLocations, blockedMineLocations));
//					inquiryQuota--;
//					
//					if ( inquiryQuota == 0 ) {
//						while ( !gridMap.updateScoutLocation(scoutingDir) ) {
//							scoutingDir = leftward ? scoutingDir.rotateLeft() : scoutingDir.rotateRight();
//						}
//						destination = gridMap.getScoutLocation();
//						scouted = false;
//						inquiryQuota = 1;
//					}
//				}
//				
//				yield();
//				yield();
//				break;
//			}
			
			case GREETING_MESSAGE: {
				if (childID == -1 && scouted) {
					GreetingMessage handler = new GreetingMessage(msg);
					
					if (!handler.getSourceLocation().isAdjacentTo(controllers.myRC.getLocation()))
						break;
					
					if (handler.isConstructor()) {
						childID = handler.getSourceID();
					}
					
					msgHandler.queueMessage(new GreetingMessage(false));
					
					destination = findNearestMine();
					
					if (destination == null) {
						while ( !gridMap.updateScoutLocation(scoutingDir) ) {
							scoutingDir = leftward ? scoutingDir.rotateLeft() : scoutingDir.rotateRight();
						}
						scoutingLocation = destination = gridMap.getScoutLocation();
						scouted = false;
						scoutCount++;
						if (scoutCount % 2 == 0)	builtBranch = false;
						msgHandler.queueMessage(new GoToMessage(destination, false));
					} else {
						msgHandler.queueMessage(new GoToMessage(destination, true));
					}
					
//					controllers.myRC.setIndicatorString(0, "GREETING_MESSAGE " + childID + destination);
//					controllers.myRC.setIndicatorString(2, emptyMineLocations + "");
				}
				break;
			}
			
			case HAS_ARRIVED_MESSAGE: {
				HasArrivedMessage handler = new HasArrivedMessage(msg);
				
				if (handler.getSourceID() == childID) {
					
					if (handler.isMine()) {
						alliedMineLocations.add(destination);
						emptyMineLocations.remove(destination);
						controllers.myRC.setIndicatorString(2, emptyMineLocations + "");
						
						final UnitType[] constructingQueue = {UnitType.TELESCOPER, UnitType.FLYING_CONSTRUCTOR, UnitType.TELESCOPER, UnitType.FLYING_CONSTRUCTOR};
						if (branch && !builtBranch && !blockedMineLocations.contains(destination)) {
							msgHandler.queueMessage(new ConstructUnitMessage(destination, constructingQueue, false));
							builtBranch = true;
						}
					}
					
					destination = findNearestMine();
					
					if (destination == null) {
						while ( !gridMap.updateScoutLocation(scoutingDir) ) {
							scoutingDir = leftward ? scoutingDir.rotateLeft() : scoutingDir.rotateRight();
						}
						scoutingLocation = destination = gridMap.getScoutLocation();
						scouted = false;
						scoutCount++;
						if (scoutCount % 2 == 0)	builtBranch = false;
						msgHandler.queueMessage(new GoToMessage(destination, false));
					} else {
						msgHandler.queueMessage(new GoToMessage(destination, true));
					}
				}
				break;
			}
				
			}
		}
		
	}
	
	private void navigate() {
		MapLocation currentLoc = controllers.myRC.getLocation();
		Direction currentDir = controllers.myRC.getDirection();
		
		if (controllers.motor.isActive())
			return;
		
		Direction desDir;
		if (destination == null || (scouted == true && childID == -1)) {
			watch();
			return;
		} else {
			desDir = currentLoc.directionTo(destination);
			// If arrived at scoutLoc
			if ( desDir == Direction.OMNI ){
				if (!scouted) {
					scouted = true;
					watch();
					
					if (childID == -1)	return;
					
				}
				gridMap.setScouted(controllers.myRC.getLocation());
				return;
			}
		}
		
		
		
		try{
			if ( controllers.motor.canMove(desDir) ){
				if (currentDir == desDir)
					controllers.motor.moveForward();
				else
					controllers.motor.setDirection(desDir);
			} 
			// if can go to the 
			else if ( controllers.motor.canMove(desDir.rotateLeft()) ){
				if (currentDir == desDir.rotateLeft())
					controllers.motor.moveForward();
				else
					controllers.motor.setDirection(desDir.rotateLeft());
			}
			else if ( controllers.motor.canMove(desDir.rotateRight()) ){
				if (currentDir == desDir.rotateRight())
					controllers.motor.moveForward();
				else
					controllers.motor.setDirection(desDir.rotateRight());
			}
		} catch (GameActionException e){
			
		}
		
	}
	
	private void flee(){
		MapLocation currentLoc = controllers.myRC.getLocation();
		Direction currentDir = controllers.myRC.getDirection();
		
		// If the enemy is too faraway, scout nearby first
		if (!attacked && controllers.distanceToNearestEnemy > 81)
			watch();
		
		while ( !currentLoc.equals(neareastRecycler) ){

			
			Direction desDir = currentLoc.directionTo(neareastRecycler);
			
			desDir = desDir.opposite();
			
			try{
				if ( controllers.motor.canMove(desDir ) ){
					if (currentDir == desDir)
						controllers.motor.moveBackward();
					else
						controllers.motor.setDirection(desDir);
				} 
				// if can go to the 
				else if ( controllers.motor.canMove(desDir.rotateLeft()) ){
					if (currentDir == desDir.rotateLeft())
						controllers.motor.moveBackward();
					else
						controllers.motor.setDirection(desDir.rotateLeft());
				}
				else if ( controllers.motor.canMove(desDir.rotateRight()) ){
					if (currentDir == desDir.rotateRight())
						controllers.motor.moveBackward();
					else
						controllers.motor.setDirection(desDir.rotateRight());
				}
			} catch (GameActionException e){
				
			}
			
			yield();
			currentLoc = controllers.myRC.getLocation();
			currentDir = controllers.myRC.getDirection();
			
			if ( currentLoc.distanceSquaredTo(neareastRecycler) < 36 ){
				msgHandler.queueMessage(new ConstructBaseMessage(neareastRecycler, UnitType.RAILGUN_TOWER));
				msgHandler.queueMessage(new ConstructUnitMessage(neareastRecycler, UnitType.APOCALYPSE , true));
				return;
			}
			
		}
		
		watch();
		
	}
	
	private void findNearestRecycler() {
		MapLocation currentLoc = controllers.myRC.getLocation();
		neareastRecycler = homeLocation;
		
		for (MapLocation mineLoc : alliedMineLocations) {
			
			if (currentLoc.distanceSquaredTo(mineLoc) < currentLoc.distanceSquaredTo(neareastRecycler))
				neareastRecycler = mineLoc;
		}

	}
	
	private boolean isMyBusiness(MapLocation loc) {
		return scoutingLocation.distanceSquaredTo(loc) <= 100;
	}
	
	private MapLocation findNearestMine() {
		MapLocation result = new MapLocation(0, 0);

		MapLocation currentLoc = controllers.myRC.getLocation();
		
		// find a eligible mine
		for (MapLocation mineLoc : emptyMineLocations) {
			if (alliedMineLocations.contains(mineLoc) || enemyMineLocations.contains(mineLoc) || !isMyBusiness(mineLoc))
				continue;
			
			if (currentLoc.distanceSquaredTo(mineLoc) < currentLoc.distanceSquaredTo(result))
				result = mineLoc;
		}
		
		if (result.x == 0)
			result = null;
		
		return result;
	}
	
}
