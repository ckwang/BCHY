package team017.AI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class ScoutAI extends AI {

	private int id;
	
	private boolean scouted = false;
	
	private Set<MapLocation> emptyMineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> alliedMineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> enemyMineLocations = new HashSet<MapLocation>();
	

	private List<RobotInfo> nearbyEnemy = new ArrayList<RobotInfo>();
	
	private MapLocation scoutingLocation;
	private Direction scoutingDir;
	
	private int inquiryQuota;
	
	private double prevHp = 0;
	private boolean attacked = false;
	
	public ScoutAI(RobotController rc) {
		super(rc);
		id = rc.getRobot().getID();
	}
	
	@Override
	public void yield() {
		super.yield();
		nearbyEnemy.clear();
		controllers.scoutNearby();
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

			
//			// ask for a scouting location if there is none
//			if (scoutingLocation == null && controllers.myRC.getLocation().distanceSquaredTo(homeLocation) <= 16) {
//				msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));
//				yield();
//				msgHandler.queueMessage(new MineLocationsMessage(emptyMineLocations, alliedMineLocations, enemyMineLocations));
//				yield();
//				msgHandler.queueMessage(new ScoutingInquiryMessage());
//				yield();
//			}
//			// constantly queue grid map message
//			if (Clock.getRoundNum() % 10 == 0)
//				msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));
//			if (controllers.myRC.getLocation().equals(scoutingLocation)) {
//				while (controllers.motor.isActive())
//					yield();
//				
//				while (true) {
//					try {
//						if (evaluateDanger()) {
//							scoutingLocation = homeLocation;
//							break;
//						}
//						emptyMineLocations.addAll(controllers.emptyMines);
//						emptyMineLocations.removeAll(controllers.allyMines);
//						emptyMineLocations.removeAll(controllers.enemyMines);
//						
//						alliedMineLocations.addAll(controllers.allyMines);
//						enemyMineLocations.addAll(controllers.enemyMines);
//
//						controllers.motor.setDirection(controllers.myRC.getDirection().rotateRight());
//						yield();
//					} catch (GameActionException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//			controllers.myRC.setIndicatorString(0, homeLocation + "," + scoutingLocation);
			controllers.myRC.setIndicatorString(1, gridMap.gridBorders[0] + "," + gridMap.gridBorders[1] + "," + gridMap.gridBorders[2] + "," + gridMap.gridBorders[3]);
			controllers.myRC.setIndicatorString(2, borders[0] + "," + borders[1] + "," + borders[2] + "," + borders[3] );

			
			navigate();
			
			yield();
		}
	}
	
	public boolean evaluateDanger() {
		if (controllers.mobileEnemyNum() == 0)
			return false;
		int d;
		MapLocation loc = controllers.myRC.getLocation();
		for (RobotInfo r: controllers.enemyMobile) {
			d = loc.distanceSquaredTo(r.location);
			if (d < ComponentType.SMG.range) {
				nearbyEnemy.add(r);
			}
		}
		if (nearbyEnemy.size() == 0)
			return false;
		Direction dir = controllers.myRC.getDirection().opposite();
		for (int i = 0; i < 3;) {
			if (!controllers.motor.isActive() && controllers.motor.canMove(dir)) {
				try {
					controllers.motor.moveBackward();
					++i;
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	
	private void watch() {
		while (controllers.motor.isActive())
			yield();
		
		for (int i = 0; i < 8; i++) {
			try {
				emptyMineLocations.addAll(controllers.emptyMines);
				emptyMineLocations.removeAll(controllers.allyMines);
				emptyMineLocations.removeAll(controllers.enemyMines);
				
				alliedMineLocations.addAll(controllers.allyMines);
				enemyMineLocations.addAll(controllers.enemyMines);

				controllers.motor.setDirection(controllers.myRC.getDirection().rotateRight());
				yield();
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
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
					if( gridMap.updateScoutLocation(scoutingDir) ) {
						scoutingLocation = gridMap.getScoutLocation();
					}
				}
				
				inquiryQuota = 2;
				
				break;
			}
			
			case MINE_INQUIRY_MESSAGE: {
				MineInquiryMessage handler = new MineInquiryMessage(msg);
				
				if ( scouted ){
					msgHandler.queueMessage(new MineResponseMessage(handler.getSourceID(), emptyMineLocations));
					inquiryQuota--;
					
					if ( inquiryQuota == 0 && gridMap.updateScoutLocation(scoutingDir) ) {
						scoutingLocation = gridMap.getScoutLocation();
						scouted = false;
						inquiryQuota = 2;
					}
				}
				
				controllers.myRC.setIndicatorString(2, "MINE_INQUIRY_MESSAGE " + scoutingLocation);
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
			// Can go toward destination
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
	
}
