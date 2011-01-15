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

public class AirAI extends AI {
	
	private Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> recyclerLocations = new HashSet<MapLocation>();
	private Set<MapLocation> builtLocations = new HashSet<MapLocation>();
		
	public AirAI(RobotController rc) {
		super(rc);
	}

	public void yield() {
		super.yield();
		updateLocationSets();
		sense_border();
	}

	public void proceed() {

		while (true) {

			try {
				processMessages();
				
				buildRecyclers();
				
				navigate();
				
//				if (buildRecyclers()) {
//					recyclerLocations.add(controllers.myRC.getLocation().add(controllers.myRC.getDirection()));
//				}

				if (controllers.myRC.getTeamResources() > 100 && Clock.getRoundNum() % 2 == 1)
					checkEmptyRecyclers();

				

				if (Clock.getRoundNum() % 6 == 0) {
					msgHandler.queueMessage(new FollowMeMessage(controllers.myRC.getDirection()));
					msgHandler.queueMessage(new BorderMessage(borders, homeLocation));
				}
				
//				if (enemyBaseLoc[0] != null)
//					controllers.myRC.setIndicatorString(2, enemyBaseLoc[0].toString());
//				controllers.myRC.setIndicatorString(1, scoutDir.toString());
				yield();

				// Conditions of building factories/armories

			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}

	private void updateLocationSets() {
		Mine[] minelist = controllers.sensor.senseNearbyGameObjects(Mine.class);
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
			
			// move forward or backward if already on the site
			if (controllers.myRC.getLocation().equals(target)) {
				while (controllers.motor.isActive())
					yield();
				if (controllers.motor.canMove(controllers.myRC.getDirection().opposite())) {
					controllers.motor.moveBackward();
				} else if (controllers.motor.canMove(controllers.myRC.getDirection())) {
					controllers.motor.moveForward();
				} else {
					return false;
				}
				yield();
			}
			
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
		while (!controllers.myRC.getLocation().isAdjacentTo(buildLoc)) {
			if (!controllers.motor.isActive()) {
				Direction nextDir = controllers.myRC.getLocation().directionTo(buildLoc);
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
		return true;
		
//		while (!controllers.myRC.getLocation().add(controllers.myRC.getDirection()).equals(buildLoc)) {
//			MapLocation currentLoc = controllers.myRC.getLocation();
//			if(currentLoc.equals(buildLoc) && !controllers.motor.isActive()){
////				while(controllers.motor.isActive()){
////					yield();
////				}
//				if (controllers.motor.canMove(controllers.myRC.getDirection().opposite()));
//					controllers.motor.moveBackward();
//				yield();
//				continue;
//			}
//			if (controllers.sensor.canSenseSquare(buildLoc) && controllers.sensor.senseObjectAtLocation(buildLoc,type.chassis.level) != null)
//				return false;
//			if (!controllers.motor.isActive()) {
//				if (!controllers.myRC.getLocation().isAdjacentTo(buildLoc)) {
//					navigator.setDestination(buildLoc);
//					Direction nextDir = navigator.getNextDir(0);
//					if(nextDir == Direction.OMNI && !controllers.motor.isActive()){
////						while(controllers.motor.isActive()){
////							yield();
////						}
//						if (controllers.motor.canMove(controllers.myRC.getDirection().opposite()))
//							controllers.motor.moveBackward();
//						break;				
//					}
//					if (controllers.myRC.getDirection() != nextDir) {
//						controllers.motor.setDirection(nextDir);
//					} else {
//						if (controllers.motor.canMove(controllers.myRC.getDirection()))
//							controllers.motor.moveForward();
//					}
//				} else if (!controllers.myRC.getLocation().add(controllers.myRC.getDirection()).equals(buildLoc)) {
//					controllers.motor.setDirection(controllers.myRC.getLocation().directionTo(buildLoc));
//				}
//			}
//			yield();
//		}
	}

	private void navigate() throws GameActionException {
		MapLocation currentLoc = controllers.myRC.getLocation();
		Direction nextDir = Direction.OMNI;
		
		if (!mineLocations.isEmpty()) {
//			controllers.myRC.setIndicatorString(1,"Mine");
			MapLocation nearest = currentLoc.add(Direction.NORTH, 100);
			for (MapLocation loc : mineLocations) {
				if (currentLoc.distanceSquaredTo(loc) < currentLoc.distanceSquaredTo(nearest))
					nearest = loc;
				}
				
			nextDir = currentLoc.directionTo(nearest);
		}
		else {
			
			nextDir = currentLoc.directionTo(gridMap.getScoutLocation());
			
			if (nextDir == Direction.OMNI){
				gridMap.setCurrentAsScouted();
				gridMap.updateScoutLocation();
				nextDir = currentLoc.directionTo(gridMap.getScoutLocation());
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
//		else if (scoutDir != Direction.NONE && scoutDir != Direction.OMNI){
////			controllers.myRC.setIndicatorString(0,"Scouting");
//			if ( roachRounds > 0 ){
//				controllers.myRC.setIndicatorString(0,"roachNavigate");
//				roachNavigate();
//				roachRounds--;
//			} else if (sense_border() == scoutDir) {
//				controllers.myRC.setIndicatorString(0,"enemyBaseLoc");
//				navigator.setDestination(enemyBaseLoc[0]);
//				scoutDir = Direction.OMNI;
//			}
//			else {
//				controllers.myRC.setIndicatorString(0,"Scouting");
//				navigator.setDestination(controllers.myRC.getLocation().add(scoutDir, 10));
//				roachRounds = 100;
//			}
//
//		}
		else {
//			controllers.myRC.setIndicatorString(1,"roachNavigate");
			// do nothing;
			if (!controllers.motor.isActive() )
				roachNavigate();
		}

			// else if (!recyclerLocations.isEmpty()) {
			// MapLocation currentLoc = controllers.myRC.getLocation();
			// MapLocation nearest = currentLoc.add(Direction.NORTH, 100);
			// for (MapLocation loc : recyclerLocations) {
			// if (currentLoc.distanceSquaredTo(loc) < currentLoc
			// .distanceSquaredTo(nearest))
			// nearest = loc;
			// }
			//
			// controllers.myRC.setIndicatorString(0, currentLoc + ","
			// + nearest);
			//
			// navigator.setDestination(nearest);
			// Direction nextDir = navigator.getNextDir(0);
			//
			// if (nextDir != Direction.OMNI) {
			// if (controllers.myRC.getDirection() == nextDir) {
			// if (controllers.motor.canMove(nextDir)) {
			// controllers.motor.moveForward();
			// }
			// } else {
			// controllers.motor.setDirection(nextDir);
			// }
			// }
			//
			// }
			
		
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
