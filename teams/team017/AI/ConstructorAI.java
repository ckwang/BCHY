package team017.AI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import team017.construction.UnitType;
import team017.message.BorderMessage;
import team017.message.BuildingLocationInquiryMessage;
import team017.message.BuildingLocationResponseMessage;
import team017.message.ConstructionCompleteMessage;
import team017.message.FollowMeMessage;
import team017.message.ScoutingMessage;
import team017.navigation.GridMap;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Mine;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

public class ConstructorAI extends AI {

	private Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> recyclerLocations = new HashSet<MapLocation>();
	private Set<MapLocation> builtLocations = new HashSet<MapLocation>();
	
	private int roachRounds = 0;
	Mine[] minelist;
	
	public ConstructorAI(RobotController rc) {
		super(rc);
		navigator.updateMap();
	}

	public void yield() {
		super.yield();
		updateLocationSets();
		sense_border();
		navigator.updateMap();
	}

	public void proceed() {

		// Initial movement
		if (Clock.getRoundNum() == 0) {
				init();
				computeEnemyBaseLocation();
				msgHandler.queueMessage(new BorderMessage(borders, homeLocation));
		}

		while (true) {
			
			try {
				
				controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation().toString() );
				controllers.myRC.setIndicatorString(1, "");
				controllers.myRC.setIndicatorString(2, "");
				
				processMessages();
				
				buildRecyclers();
				
				navigate();
				
//				if (buildRecyclers()) {
//					recyclerLocations.add(controllers.myRC.getLocation().add(controllers.myRC.getDirection()));
//				}


				if (controllers.myRC.getTeamResources() > 100 && Clock.getRoundNum() > 400 && Clock.getRoundNum() % 2 == 1)
					checkEmptyRecyclers();

				

//				if (Clock.getRoundNum() % 6 == 0) {
//					msgHandler.queueMessage(new FollowMeMessage(controllers.myRC.getDirection()));
//					msgHandler.queueMessage(new BorderMessage(borders, homeLocation));
//				}
				
				
				yield();

				// Conditions of building factories/armories

			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}

	private void init() {
		try {
//			int [] recyclerIDs = new int[2];
//
//			Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
//			if (robots.length == 2) {
//				int j = 0;
//				for (Robot r : robots) {
//					if (r.getTeam() == controllers.myRC.getTeam()) {
//						recyclerIDs[j++] = r.getID();
//					}
//				}
//			}
			
			// look at the other three angles
			for (int i = 0; i < 4; ++i) {
				// Rotate twice Right for a 90 degrees turn
				controllers.motor.setDirection(controllers.myRC.getDirection().rotateRight().rotateRight());
				yield();
				controllers.updateComponents();
				
//				// sense the initial 2 recyclers
//				robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
//				if (robots.length >= 2) {
//					int j = 0;
//					for (Robot r : robots) {
//						if (r.getTeam() == controllers.myRC.getTeam()) {
//							recyclerIDs[j++] = r.getID();
//						}
//					}
//				}
			}
			
//			controllers.myRC.setIndicatorString(0, recyclerIDs[0] + "," + recyclerIDs[1]);
			
			// go build recyclers on the other two initial mines
			if (!mineLocations.isEmpty()) {
				buildBuildingAtLoc((MapLocation) mineLocations.toArray()[0], UnitType.RECYCLER);
			}
			yield();
			if (!mineLocations.isEmpty())
				buildBuildingAtLoc((MapLocation) mineLocations.toArray()[0], UnitType.RECYCLER);
			yield();
//			controllers.myRC.setIndicatorString(2, "here");
			
//			// wake up one recycler
//			while(controllers.comm.isActive())
//				controllers.myRC.yield();
//			controllers.comm.broadcastTurnOn(recyclerIDs);
			
		} catch (GameActionException e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}
	}

	private void init_revolve() {

		MapLocation[] locationList = {
				homeLocation.add(Direction.NORTH_EAST, 2),
				homeLocation.add(Direction.SOUTH_EAST, 2),
				homeLocation.add(Direction.SOUTH_WEST, 2),
				homeLocation.add(Direction.NORTH_WEST, 2) };
		int index = 0;

		while (true) {
			try {

				navigator.setDestination(locationList[index]);
				// controllers.myRC.setIndicatorString(2,
				// controllers.myRC.getLocation().toString()+locationList[index].toString());

				Direction nextDir = navigator.getNextDir(0);
				if (nextDir == Direction.OMNI) {
					index++;
					if (index == 4)
						return;
					continue;
				}

				if (!controllers.motor.isActive()
						&& controllers.motor.canMove(nextDir)) {
					if (controllers.myRC.getDirection() == nextDir) {
						controllers.motor.moveForward();
					} else {
						controllers.motor.setDirection(nextDir);
					}
				}

				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}

	private void init_return() throws GameActionException {
		navigator.setDestination(homeLocation);

		while (true) {
			try {
				Direction nextDir = navigator.getNextDir(0);

				if (nextDir == Direction.OMNI)
					break;

				if (!controllers.motor.isActive() && controllers.motor.canMove(nextDir)) {
					if (controllers.myRC.getDirection() == nextDir)
						controllers.motor.moveForward();
					else
						controllers.motor.setDirection(nextDir);
				}
				yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}

	private void updateLocationSets() {
		minelist = controllers.sensor.senseNearbyGameObjects(Mine.class);
		for (Mine mine : minelist) {
			try {
				GameObject object = controllers.sensor.senseObjectAtLocation(mine.getLocation(), RobotLevel.ON_GROUND);
				if (object != null) {
					if (controllers.sensor.senseRobotInfo ((Robot) object).chassis == Chassis.BUILDING) {
						if (mineLocations.contains(mine.getLocation()))
							mineLocations.remove(mine.getLocation());
						if (object.getTeam() == controllers.myRC.getTeam()){
							recyclerLocations.add(mine.getLocation());
							if (!controllers.sensor.senseRobotInfo((Robot) object).on){
								recyclerLocations.remove(mine.getLocation());
							}
						}	
					}	
				} else {
					mineLocations.add(mine.getLocation());
				}
			} catch (GameActionException e) {
				continue;
			}
		}
//		mineLocations.toString();
//		controllers.myRC.setIndicatorString(1, controllers.myRC.getLocation() + ";" + mineLocations.toString());
	}

	private void checkEmptyRecyclers() throws GameActionException {
		for (MapLocation recyclerLoc : recyclerLocations) {
			if (controllers.myRC.getLocation().distanceSquaredTo(recyclerLoc) <= 9
				&& !builtLocations.contains(recyclerLoc)) {
				msgHandler.queueMessage(new BuildingLocationInquiryMessage(recyclerLoc));
				break;
			}
		}
	}

	private boolean buildRecyclers() throws GameActionException{
//		controllers.myRC.setIndicatorString(2, mineLocations.toString());
		
		// find a eligible mine
		MapLocation target = null;
		List<MapLocation> toBeRemoved = new ArrayList<MapLocation>();
		for (MapLocation mineLoc : mineLocations) {
			// it needs to be empty
			if (controllers.sensor.canSenseSquare(mineLoc)){
				GameObject object = controllers.sensor.senseObjectAtLocation(mineLoc, RobotLevel.ON_GROUND); 
				if (object != null) {
					toBeRemoved.add(mineLoc);
					continue;
				}
			}
			
			// it needs to be adjacent
			if (controllers.myRC.getLocation().distanceSquaredTo(mineLoc) > 2) 
				continue;
			
			// find one!
			target = mineLoc;
			break;
		}
		
		// remove mines with buildings on them
		mineLocations.removeAll(toBeRemoved);
		
		// if there is a eligible site
		if (target != null) {
			
			// face the building site
			Direction buildDir = controllers.myRC.getLocation().directionTo(target);
			if (controllers.myRC.getDirection() != buildDir) {
				while (controllers.motor.isActive())
					yield();
				
				controllers.motor.setDirection(buildDir);
				yield();
			}
			
//			if (!controllers.myRC.getLocation().isAdjacentTo(target))
//				System.out.println("no!!!");
			
			// the building location should be clear
			if (controllers.sensor.senseObjectAtLocation(target, RobotLevel.ON_GROUND) == null) {
				while (!buildingSystem.constructUnit(target, UnitType.RECYCLER)) {
					if (buildingSystem.canConstruct(RobotLevel.ON_GROUND) == false)
						return false;
					yield();
				}
				msgHandler.queueMessage(new ConstructionCompleteMessage(target, UnitType.RECYCLER));
				msgHandler.queueMessage(new BorderMessage(borders, homeLocation));
				yield();
				return true;
			}
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
			if (!controllers.motor.isActive()) {
				Direction nextDir = navigator.getNextDir(2);
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
			if (controllers.sensor.senseObjectAtLocation(buildLoc, type.chassis.level) != null)
				return false;
			yield();
		}
		msgHandler.queueMessage(new BorderMessage(borders, homeLocation));
		return true;
	}

	private void navigate() throws GameActionException {
		Direction nextDir = Direction.OMNI;
		if (!mineLocations.isEmpty()) {
//			controllers.myRC.setIndicatorString(1,"Mine");
			MapLocation currentLoc = controllers.myRC.getLocation();
			MapLocation nearest = currentLoc.add(Direction.NORTH, 100);
			for (MapLocation loc : mineLocations) {
				if (currentLoc.distanceSquaredTo(loc) < currentLoc.distanceSquaredTo(nearest))
					nearest = loc;
				}
				
			navigator.setDestination(nearest);
			nextDir = navigator.getNextDir(2);
			controllers.myRC.setIndicatorString(0, controllers.myRC.getLocation() + ", mine:" + nearest);
		}
		else {
			
//			TerrainTile checkTile = 
//				controllers.myRC.senseTerrainTile(
//						controllers.myRC.getLocation().add(controllers.myRC.getDirection(), 3));
			
//			if (checkTile == TerrainTile.OFF_MAP)
//				gridMap.updateScoutLocation(Clock.getRoundNum());
			
			// if the scout location is too old
			if (Clock.getRoundNum() - gridMap.getAssignedRoundNum() < 200) {
				gridMap.setCurrentAsScouted();
				gridMap.updateScoutLocation(Clock.getRoundNum(), Clock.getRoundNum());
			}
			
			navigator.setDestination(gridMap.getScoutLocation());
			nextDir = navigator.getNextDir(4);
			
			if (nextDir == Direction.OMNI){
				controllers.myRC.setIndicatorString(0, Clock.getRoundNum() + ": update!");
				gridMap.setCurrentAsScouted();
				gridMap.updateScoutLocation(Clock.getRoundNum(), Clock.getRoundNum());
				navigator.setDestination(gridMap.getScoutLocation());
				nextDir = navigator.getNextDir(4);
			}
		}
		
		
		if (nextDir != Direction.OMNI) {
			if (!controllers.motor.isActive() ) {
				if (controllers.myRC.getDirection() == nextDir) {
					if (controllers.motor.canMove(nextDir)) {
						controllers.motor.moveForward();
					}
				} else {
					controllers.motor.setDirection(nextDir);
				}
			}
		}
		else {
//			controllers.myRC.setIndicatorString(1,"roachNavigate");
			// do nothing;
			if (!controllers.motor.isActive() )
				roachNavigate();
		}
	}
	
	private MapLocation getNextScoutLoc() {
		TerrainTile tile, checkTile;
		Direction faceDir = controllers.myRC.getDirection();
		final int EXPLORATION_SIZE = 10;
		
		// add some randomness to the initial direction
		int n = Clock.getRoundNum() % 8;
		for (int i = 0; i < n; i++) {
			faceDir = faceDir.rotateRight();
		}
		
		MapLocation currentLoc = controllers.myRC.getLocation();
		int multiple = 1;
		while( multiple < 5 ){
		
			for (int i = 0; i < 8; i++){
				MapLocation projectedLoc = currentLoc.add(faceDir, EXPLORATION_SIZE*multiple);
				if ( (borders[1] == -1 || projectedLoc.x < borders[1]) &&
					 (borders[3] == -1 || projectedLoc.x > borders[3]) &&
					 (borders[2] == -1 || projectedLoc.y < borders[2]) &&
					 (borders[0] == -1 || projectedLoc.y > borders[0])) {
					
				
					tile = controllers.myRC.senseTerrainTile(projectedLoc);
					checkTile = controllers.myRC.senseTerrainTile(currentLoc.add(faceDir, 3));
					if (tile == null && checkTile != TerrainTile.OFF_MAP)
						return projectedLoc;
				}
				faceDir = faceDir.rotateRight();
			}
			
			multiple++;
		}
		
		return currentLoc.add(faceDir.opposite(), EXPLORATION_SIZE*multiple);
	}

	@Override
	protected void processMessages() throws GameActionException {
		// Check messages
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {
//			case BUILDING_LOCATION_RESPONSE_MESSAGE: {
//				BuildingLocationResponseMessage handler = new BuildingLocationResponseMessage(msg);
//				if (!builtLocations.contains(handler.getSourceLocation())){
//					if(handler.getAvailableSpace() == -1){
//						builtLocations.add(handler.getSourceLocation());
//					} else if (handler.getBuildableDirection() != Direction.NONE) {
//						MapLocation buildLoc = handler.getSourceLocation().add(handler.getBuildableDirection());
//						if (handler.getAvailableSpace() == 3) {
//							if (buildBuildingAtLoc(buildLoc,UnitType.FACTORY)) {
//								builtLocations.add(handler.getSourceLocation());
//								msgHandler.queueMessage(new ConstructionCompleteMessage(buildLoc, ComponentType.FACTORY));
//								MapLocation nextBuildLoc = handler.getSourceLocation().add(handler.getBuildableDirection().rotateRight());
//								if(buildBuildingAtLoc(nextBuildLoc,UnitType.ARMORY)){
//									msgHandler.queueMessage(new ConstructionCompleteMessage(nextBuildLoc, ComponentType.ARMORY));
//								}
//							}
//						} else if (handler.getAvailableSpace() == 2) {
//							if(buildBuildingAtLoc(buildLoc, UnitType.FACTORY)){
//								msgHandler.queueMessage(new ConstructionCompleteMessage(buildLoc, ComponentType.FACTORY));
//								builtLocations.add(handler.getSourceLocation());
//							}
//						}
//					}							
//				}
//				break;
//			}
			
			case BUILDING_LOCATION_RESPONSE_MESSAGE: {
				BuildingLocationResponseMessage handler = new BuildingLocationResponseMessage(msg);
				
				// see if the message is intended for it
				if (handler.getConstructorID() != controllers.myRC.getRobot().getID())
					break;
				
				// if it is not built
				if (builtLocations.contains(handler.getSourceLocation()))
					break;
				
				UnitType type = handler.getUnitType();
				if (type == null){	// there is nothing to build
					builtLocations.add(handler.getSourceLocation());
				} else if (handler.getBuildableDirection() != Direction.NONE) {
					MapLocation buildLoc = handler.getSourceLocation().add(handler.getBuildableDirection());
					if (buildBuildingAtLoc(buildLoc, type)){
						msgHandler.clearOutQueue();
						msgHandler.queueMessage(new ConstructionCompleteMessage(buildLoc, type));
//							builtLocations.add(handler.getSourceLocation());
						yield();
					}
				}							
				
				break;
			}

			case BORDER: {
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
				break;
			}
//			case SCOUTING_MESSAGE: {						
//				ScoutingMessage handler = new ScoutingMessage(msg);
//				// update the borders
//				if (scoutDir == Direction.NONE)
//					scoutDir = handler.getScoutDirection();
//				break;
//			}
			}
		}
	}
}
