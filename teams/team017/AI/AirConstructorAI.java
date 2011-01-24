package team017.AI;

import java.util.HashSet;
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
import team017.message.MineResponseMessage;
import team017.message.ScoutingInquiryMessage;
import team017.message.ScoutingResponseMessage;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;


public class AirConstructorAI extends AI {


	private int id;
	
	private boolean needStay = false;
	private boolean arrivedScoutingLoc = true;
	
	private int scoutingLocationCount = 0;
	private boolean builtBranch = true;
	
//	private MapLocation scoutingLocation;
	private Direction scoutingDir;
	private boolean branch;
	private boolean leftward;
	
	private int scoutingResponseDistance = 100;
	
	private Set<MapLocation> blockedMineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> recyclerLocations = new HashSet<MapLocation>();
	private Set<MapLocation> builtLocations = new HashSet<MapLocation>();
	private MapLocation currentLoc = controllers.myRC.getLocation();
	
	private int roundSinceLastInquired = 0;
	private int builtIdleRound = 0;
	
	private int parentID = -1;
	
	MapLocation nearestMine = null;
	MapLocation destination = null;
	boolean isMine = false;
	
	
	public AirConstructorAI(RobotController rc) {
		super(rc);
		id = rc.getRobot().getID();
		destination = null;
	}
	
	@Override
	public void yield() {
		super.yield();
		currentLoc = controllers.myRC.getLocation();
		if (builtIdleRound > 0)
			builtIdleRound--;
	}

	@Override
	public void proceed() {

		// ask for mine locations
		while (controllers.comm.isActive())
			yield();
		msgHandler.queueMessage(new ScoutingInquiryMessage(true));
		yield();
//		msgHandler.queueMessage(new MineInquiryMessage());
		
		while (true) {
			
			try {processMessages();} catch (Exception e) {e.printStackTrace();}
			
			try {
//				if (buildRecyclers()) {

//					msgHandler.queueMessage(new HasArrivedMessage());
//					boolean hasAdjacentMine = false;
//					for (MapLocation mineLoc: mineLocations) {
//						if (mineLoc.isAdjacentTo(nearestMine) && !mineLoc.equals(nearestMine)) {
//							if (!recyclerLocations.contains(mineLoc)) {
//								hasAdjacentMine = true;
//								nearestMine = mineLoc;
//								break;
//							}	
//						}
//					}
//					if (!hasAdjacentMine) {
//						builtIdleRound = 50;
//						nearestMine = null;
//					}
//				}
			} catch (Exception e) {e.printStackTrace();}
			

//			if (roundSinceLastBuilt > 30)
			
			navigate();

//			String s = "";
//			for (MapLocation loc : mineLocations) {
//				s += loc.toString();
//			}
//			
//			controllers.myRC.setIndicatorString(0, s);
			
//			s = "";
//			for (MapLocation loc : recyclerLocations) {
//				s += loc.toString();
//			}
//			controllers.myRC.setIndicatorString(1, s);
			
			if ( parentID == -1 && controllers.myRC.getLocation().distanceSquaredTo(destination) <= 2 && Clock.getRoundNum() - roundSinceLastInquired > 10) {
				msgHandler.queueMessage(new GreetingMessage(true));
				roundSinceLastInquired = Clock.getRoundNum();
			}
			
//			if ( !arrivedScoutingLoc && destination != null && controllers.myRC.getLocation().distanceSquaredTo(destination) < controllers.comm.type().range &&
//					Clock.getRoundNum() - roundSinceLastInquired > 10) {
//				msgHandler.queueMessage(new GreetingMessage(true));
//				roundSinceLastInquired = Clock.getRoundNum();
//			}
		
			
			controllers.myRC.setIndicatorString(1, builtBranch + "");
				
			yield();
		}
	}
	
	@Override
	protected void processMessages() throws GameActionException {
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {
			
			case MINE_RESPONSE_MESSAGE: {
				MineResponseMessage handler = new MineResponseMessage(msg);
				
				if (handler.getConstructorID() == id) {
					for (MapLocation loc : handler.getMineLocations()) {
						mineLocations.add(loc);
						if (!isMyBusiness(loc)) {
							recyclerLocations.add(loc);
						}
					}
					
					if (handler.getBlockedLocations() != null)
						blockedMineLocations.addAll(handler.getBlockedLocations());
				}
				if ( handler.getSourceLocation().equals(destination) ){
					arrivedScoutingLoc = true;
				}
				break;
			}
			
//			case BUILDING_LOCATION_RESPONSE_MESSAGE: {
//
//				BuildingLocationResponseMessage handler = new BuildingLocationResponseMessage(msg);
//				
////				controllers.myRC.setIndicatorString(0, "Type" +handler.getUnitType() + " " +  Clock.getRoundNum());
////				controllers.myRC.setIndicatorString(1, "current location:" + controllers.myRC.getLocation());
////				controllers.myRC.setIndicatorString(2, "build loc:" + handler.getBuildableLocation());
//				
////				// see if the message is intended for it
////				if (handler.getConstructorID() != controllers.myRC.getRobot().getID())
////					break;
//
//				// if it is not built
//				if (builtLocations.contains(handler.getSourceLocation()))
//					break;
//
//				UnitType type = handler.getUnitType();
//				if (type == null) { // there is nothing to build
//					builtLocations.add(handler.getSourceLocation());
//					builtIdleRound = 0;
//				} else if (handler.getBuildableLocation() != null) {
//					MapLocation buildLoc = handler.getBuildableLocation();
//					if (buildBuildingAtLoc(buildLoc, type)) {
//						if (type == UnitType.FACTORY)
//							msgHandler.queueMessage(new MineInquiryMessage());
//						builtIdleRound = 50;
////						msgHandler.queueMessage(new BuildingLocationInquiryMessage(handler.getSourceLocation()));
//						yield();
//					}
//				}
//
//				break;
//			}
			
			case SCOUTING_RESPONSE_MESSAGE: {
				ScoutingResponseMessage handler = new ScoutingResponseMessage(msg);
				controllers.myRC.setIndicatorString(2, "received");
				if (handler.getTelescoperID() == id && handler.getSourceLocation().distanceSquaredTo(currentLoc) < scoutingResponseDistance ) {
					scoutingResponseDistance = handler.getSourceLocation().distanceSquaredTo(currentLoc);
					scoutingDir = handler.getScoutingDirection();
					branch = handler.isBranch();
					leftward = handler.isLeftward();
					if (homeLocation.distanceSquaredTo(controllers.myRC.getLocation()) > 16) {
						gridMap.setScoutLocation(handler.getSourceLocation());
					}
					
					while ( !gridMap.updateScoutLocation(scoutingDir) ) {
						scoutingDir = leftward ? scoutingDir.rotateLeft() : scoutingDir.rotateRight();
					}
					destination = gridMap.getScoutLocation();
				}
				
				break;
			}
			
			
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
				if (destination == null)
					destination = homeLocation;
				computeEnemyBaseLocation();
				gridMap.merge(homeLocation, handler.getBorders(), handler.getInternalRecords());

				break;
			}
			
			case GREETING_MESSAGE: {
				if (parentID == -1) {
					GreetingMessage handler = new GreetingMessage(msg);
					
					if (!handler.isConstructor()) {
						parentID = handler.getSourceID();
					}
				}
				break;
			}
				
			case GO_TO_MESSAGE: {
				GoToMessage handler = new GoToMessage(msg);
				
				if (handler.getSourceID() == parentID) {
					destination = handler.getGoToLocation();
					isMine = handler.isMine();
				}
				
				break;
			}
			
			
			}
		}
		
	}
	
	private boolean isMyBusiness(MapLocation loc) {
		return destination.distanceSquaredTo(loc) <= 144;
		
//		boolean ahead = ((loc.x - scoutingLocation.x) * scoutingDir.dx + (loc.y - scoutingLocation.y) * scoutingDir.dy) > 0;
//		
//		return order == 0 ? ahead : !ahead;
	}
	
	private void findNearestMine() {
		if (nearestMine == null)
			nearestMine = new MapLocation(0, 0);

		// find a eligible mine
		for (MapLocation mineLoc : mineLocations) {
			if (recyclerLocations.contains(mineLoc))
				continue;
			
			if (currentLoc.distanceSquaredTo(mineLoc) < currentLoc.distanceSquaredTo(nearestMine))
				nearestMine = mineLoc;
		}

	}
	
	private boolean buildRecyclers() throws GameActionException {
		
//		findNearestMine();
//		
//		controllers.myRC.setIndicatorString(0, "Building Recycler");
//		if (nearestMine.x == 0) {
//			nearestMine = null;
//			return false;
//		}
		
		final UnitType[] constructingQueue = {UnitType.TELESCOPER, UnitType.FLYING_CONSTRUCTOR, UnitType.TELESCOPER, UnitType.FLYING_CONSTRUCTOR};

		// if there is a eligible site
		if (currentLoc.distanceSquaredTo(destination) <= 2) {
			if (controllers.builder.canBuild(Chassis.BUILDING, destination)) {
				if (buildBuildingAtLoc(destination, UnitType.RECYCLER)) {
					if (branch && !builtBranch && !blockedMineLocations.contains(destination)) {
						msgHandler.queueMessage(new ConstructUnitMessage(destination, constructingQueue, false));
						builtBranch = true;
					}
					
					recyclerLocations.add(destination);
//					msgHandler.queueMessage(new ConstructBaseMessage(nearestMine, UnitType.ARMORY));
					return true;
				}
			} else {
				recyclerLocations.add(destination);
			}
			destination = null;
		}

		return false;
	}
	
	private boolean buildBuildingAtLoc(MapLocation buildLoc, UnitType type) throws GameActionException {
		// if already standing on the building site
		if (controllers.myRC.getLocation().equals(buildLoc)) {
			while (controllers.motor.isActive())
				yield();

			// move forward or backward if possible
			if (controllers.motor.canMove(controllers.myRC.getDirection())) {
				controllers.motor.moveForward();
			} else if (controllers.motor.canMove(controllers.myRC.getDirection().opposite())) {
				controllers.motor.moveBackward();
			} else {
				return false;
			}
			yield();
		}

		// move to the adjacent of the building site
		navigator.setDestination(buildLoc);
		while (!controllers.myRC.getLocation().isAdjacentTo(buildLoc)) {
//			controllers.myRC.setIndicatorString(2, controllers.myRC.getLocation() + "," + buildLoc);
			if (!controllers.motor.isActive()) {
				Direction nextDir = navigator.getNextDir(0);

//				controllers.myRC.setIndicatorString(2, nextDir.toString());
				
				if (nextDir == Direction.OMNI)
					break;
				if (controllers.myRC.getDirection() == nextDir) {
					if (controllers.motor.canMove(nextDir))
						controllers.motor.moveForward();
				} else {
					controllers.motor.setDirection(nextDir);
				}
			}

			yield();
		}

		// face the building site
		Direction buildDir = controllers.myRC.getLocation().directionTo(buildLoc);
		if (controllers.myRC.getDirection() != buildDir) {
			while (controllers.motor.isActive())
				yield();

			controllers.motor.setDirection(buildDir);
			yield();
		}

		// if everything looks okay, construct
		while (!buildingSystem.constructUnit(buildLoc, type)) {
			if (!controllers.builder.canBuild(Chassis.BUILDING, buildLoc))
				return false;
			yield();
		}
		msgHandler.clearOutQueue();
		msgHandler.queueMessage(new ConstructionCompleteMessage(buildLoc, type));
		msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));
		return true;
	}
	
	private void navigate() {
		
		MapLocation currentLoc = controllers.myRC.getLocation();
		Direction currentDir = controllers.myRC.getDirection();
		
		if (controllers.motor.isActive())
			return;
		
		Direction desDir;
		
		if (destination != null) {
			desDir = currentLoc.directionTo(destination);
			if (currentLoc.distanceSquaredTo(destination) <= 2) {
				try {
					if (isMine && buildRecyclers()) {
						msgHandler.queueMessage(new HasArrivedMessage(isMine));
					}
				} catch (GameActionException e) {
					e.printStackTrace();
				}
				
				return;
			}
		} 
//		else if ( !needStay ) {
//			desDir = currentLoc.directionTo(destination);
//			if (currentLoc.distanceSquaredTo(destination) <= 2 || desDir == Direction.OMNI)
//				return;
//		}
		else {
			return;
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
