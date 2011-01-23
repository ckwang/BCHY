package team017.AI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import team017.construction.UnitType;
import team017.message.ConstructUnitMessage;
import team017.message.GridMapMessage;
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
	
	private boolean scouted = false;
	
	private Set<MapLocation> blockedMineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> emptyMineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> alliedMineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> enemyMineLocations = new HashSet<MapLocation>();
	

	private List<RobotInfo> nearbyEnemy = new ArrayList<RobotInfo>();
	
	private MapLocation scoutingLocation;
	private Direction scoutingDir;
	private boolean leftward;
	private boolean branch;
	
	private int inquiryQuota;
	
	private double prevHp = 0;
	private boolean attacked = false;
	
	private MapLocation neareastRecycler;
	
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
		if (senseBorder())	scoutingLocation = gridMap.getScoutLocation();
		attacked = controllers.myRC.getHitpoints() < prevHp;
		prevHp = controllers.myRC.getHitpoints();

	}

	@Override
	public void proceed() {
		
		msgHandler.queueMessage(new ScoutingInquiryMessage(false));
		while (scoutingDir == null) {
			try {processMessages();} catch (Exception e) {e.printStackTrace();}
			controllers.myRC.setIndicatorString(0, homeLocation + "," + scoutingLocation);
			yield();
		}
			
		
		while (true) {
			
			try {processMessages();} catch (Exception e) {e.printStackTrace();}
			

			controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation()+"," + homeLocation + "," + scoutingLocation);
			if (controllers.distanceToNearestEnemy < 121 || attacked )
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
				controllers.myRC.setIndicatorString(2, "received");
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
					scoutingLocation = gridMap.getScoutLocation();
				}
				
				inquiryQuota = 1;
				
				break;
			}
			
			case MINE_INQUIRY_MESSAGE: {
				MineInquiryMessage handler = new MineInquiryMessage(msg);
				
				if ( scouted ){
					msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));
					yield();
					msgHandler.queueMessage(new MineResponseMessage(handler.getSourceID(), emptyMineLocations, blockedMineLocations));
					inquiryQuota--;
					
					if ( inquiryQuota == 0 ) {
						while ( !gridMap.updateScoutLocation(scoutingDir) ) {
							scoutingDir = leftward ? scoutingDir.rotateLeft() : scoutingDir.rotateRight();
						}
						scoutingLocation = gridMap.getScoutLocation();
						scouted = false;
						inquiryQuota = 1;
					}
				}
				
				controllers.myRC.setIndicatorString(2, "MINE_INQUIRY_MESSAGE " + scoutingLocation);
				yield();
				yield();
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
		if (scouted) {
			watch();
			return;
		} 
		else {
			desDir = currentLoc.directionTo(scoutingLocation);
			// If arrived at scoutLoc
			if ( desDir == Direction.OMNI ){
				watch();
				gridMap.setScouted(controllers.myRC.getLocation());
				scouted = true;
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
				msgHandler.queueMessage(new ConstructUnitMessage(neareastRecycler, UnitType.APOCALYPSE , true));
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
	
}
