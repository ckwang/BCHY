package team017.AI;

import java.util.HashSet;
import java.util.Set;

import team017.construction.UnitType;
import team017.message.BorderMessage;
import team017.message.BuildingLocationInquiryMessage;
import team017.message.ConstructionCompleteMessage;
import team017.message.FollowMeMessage;
import team017.message.ScoutingMessage;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Mine;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;

public class ConstructorAI extends AI {

	private Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> recyclerLocations = new HashSet<MapLocation>();
	private Set<MapLocation> builtLocations = new HashSet<MapLocation>();
	
	private MapLocation scoutLoc;
	
	public ConstructorAI(RobotController rc) {
		super(rc);
	}

	public void yield() {
		super.yield();
		updateLocationSets();
		sense_border();
	}

	public void proceed() {

		// Initial movement
		if (Clock.getRoundNum() == 0) {
			try {
				init();
				init_revolve();
				computeEnemyBaseLocation();
				init_return();
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}

		while (true) {

			try {
				
				processMessages();
				
				navigate();
				
				buildRecyclers();
				
//				if (buildRecyclers()) {
//					recyclerLocations.add(controllers.myRC.getLocation().add(controllers.myRC.getDirection()));
//				}

				if (controllers.myRC.getTeamResources() > 100)
					checkEmptyRecyclers();

				
				if (Clock.getRoundNum() % 2 == 0)
					msgHandler.queueMessage(new FollowMeMessage());
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
			for (int i = 0; i < 4; ++i) {
				// Rotate twice Right for a 90 degrees turn
				controllers.motor.setDirection(controllers.myRC.getDirection()
						.rotateRight());
				yield();
				controllers.updateComponents();
				// controllers.myRC.setIndicatorString(0, borders[0] + "," +
				// borders[1] + "," + borders[2] + "," + borders[3]);
				// controllers.myRC.setIndicatorString(1,controllers.myRC.getLocation()
				// + "");
			}
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
		msgHandler.queueMessage(new BorderMessage(borders, homeLocation));
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
			if (controllers.myRC.getLocation().isAdjacentTo(recyclerLoc)
				&& !builtLocations.contains(recyclerLoc)) {
				msgHandler.queueMessage(new BuildingLocationInquiryMessage(recyclerLoc));
			}
		}
	}

	private boolean buildRecyclers() throws GameActionException{
//		controllers.myRC.setIndicatorString(2, mineLocations.toString());
		
		// find a eligible mine
		MapLocation target = null;
		for (MapLocation mineLoc : mineLocations) {
			// it needs to be adjacent
			if (!controllers.myRC.getLocation().isAdjacentTo(mineLoc)) 
				continue;
			
			// it needs to be empty
			if (controllers.sensor.canSenseSquare(mineLoc)){
				 if (controllers.sensor.senseObjectAtLocation(mineLoc, RobotLevel.ON_GROUND) != null)
					 continue;
			}
			
			// find one!
			target = mineLoc;
			break;
		}
		
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
			
			// the building location should be clear
			if (controllers.sensor.senseObjectAtLocation(target, RobotLevel.ON_GROUND) == null) {
				while (!buildingSystem.constructUnit(target, UnitType.RECYCLER)) {
					if (buildingSystem.canConstruct(RobotLevel.ON_GROUND) == false)
						return false;
					yield();
				}
				msgHandler.queueMessage(new ConstructionCompleteMessage(target, ComponentType.RECYCLER));
				msgHandler.queueMessage(new BorderMessage(borders, homeLocation));
				yield();
				return true;
			}
		}
		
		return false;
		
//		for (MapLocation mineLoc : mineLocations){
//			if(controllers.myRC.getLocation().isAdjacentTo(mineLoc)){
//				if(controllers.sensor.canSenseSquare(mineLoc)){
//					 if(controllers.sensor.senseObjectAtLocation(mineLoc, RobotLevel.ON_GROUND) != null)
//						 continue;
//				}
//				if (controllers.myRC.getDirection() != controllers.myRC.getLocation().directionTo(mineLoc)) {
//					while (controllers.motor.isActive())
//						controllers.myRC.yield();
//					controllers.motor.setDirection(controllers.myRC.getLocation().directionTo(mineLoc));
//					controllers.myRC.yield();
//				}
//				if (controllers.sensor.senseObjectAtLocation(mineLoc,RobotLevel.ON_GROUND) == null) {
//					while (!buildingSystem.constructUnit(controllers.myRC.getLocation().add(controllers.myRC.getDirection()),UnitType.RECYCLER)) {
//						if (buildingSystem.canConstruct(RobotLevel.ON_GROUND) == false)
//							return false;
//						controllers.myRC.yield();
//					}
//					msgHandler.queueMessage(new ConstructionCompleteMessage(mineLoc, ComponentType.RECYCLER));
//					msgHandler.queueMessage(new BorderMessage(borders));
//					if (enemyBaseLoc != null)
//						msgHandler.queueMessage(new EnemyLocationMessage(enemyBaseLoc));
//					controllers.myRC.yield();
//					return true;
//				}
//			}
//		}
//		return false;
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

		if (!mineLocations.isEmpty()) {
			controllers.myRC.setIndicatorString(1,"Mine");
			MapLocation currentLoc = controllers.myRC.getLocation();
			MapLocation nearest = currentLoc.add(Direction.NORTH, 100);
			for (MapLocation loc : mineLocations) {
				if (currentLoc.distanceSquaredTo(loc) < currentLoc.distanceSquaredTo(nearest))
					nearest = loc;
				}
				
			navigator.setDestination(nearest);
		}
		
		Direction nextDir = navigator.getNextDir(2);
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
		else if (scoutLoc != null){
			controllers.myRC.setIndicatorString(1,"scouting");
			navigator.setDestination(scoutLoc);
		}
		else {
			controllers.myRC.setIndicatorString(1,"roachNavigate");
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
			case SCOUTING_MESSAGE: {						
				ScoutingMessage handler = new ScoutingMessage(msg);
				// update the borders
				scoutLoc = handler.getScoutLocation();

				break;
			}
			}
		}
	}
}
