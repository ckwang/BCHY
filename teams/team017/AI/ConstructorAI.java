package team017.AI;

import java.util.HashSet;
import java.util.Set;

import team017.message.BorderMessage;
import team017.message.BuildingLocationInquiryMessage;
import team017.message.BuildingRequestMessage;
import team017.message.ConstructionCompleteMessage;
import team017.util.Util;
import team017.construction.UnitType;

import battlecode.common.*;

public class ConstructorAI extends AI {

	private Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> recyclerLocations = new HashSet<MapLocation>();

	public ConstructorAI(RobotController rc) {
		super(rc);
	}

	public void yield() throws GameActionException {
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
				init_return();
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}

		while (true) {

			try {
				if (controllers.motor != null) {
					navigate();
				}
				buildRecyclers();
				
				//Check messages
				
				while (msgHandler.hasMessage()) {
					Message msg = msgHandler.nextMessage();
					switch (msgHandler.getMessageType(msg)) {
					case BUILDING_LOCATION_RESPONSE_MESSAGE: {
						break;
					}
					}
				}
				yield();
				
				// Conditions of building factories/armories
				if(controllers.myRC.getTeamResources() > 150)
					checkEmptyRecyclers();

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
						.rotateRight().rotateRight());
				yield();
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

	private void sense_border() {
		try {

			Direction[] addDirs = new Direction[3];

			if (controllers.myRC.getDirection().isDiagonal()) {
				addDirs[0] = controllers.myRC.getDirection().rotateLeft();
				addDirs[1] = controllers.myRC.getDirection().rotateRight();
			} else {
				addDirs[0] = controllers.myRC.getDirection();
			}

			int j = -1;
			while (addDirs[++j] != null) {
				MapLocation currentLoc = controllers.myRC.getLocation();

				int i;
				for (i = 3; i > 0; i--) {
					if (controllers.myRC.senseTerrainTile(currentLoc.add(
							addDirs[j], i)) != TerrainTile.OFF_MAP)
						break;
				}

				// i == 3 means no OFF_MAP sensed
				if (i != 3) {
					switch (addDirs[j]) {
					case NORTH:
						borders[0] = currentLoc.y - (i + 1);
						break;
					case EAST:
						borders[1] = currentLoc.x + (i + 1);
						break;
					case SOUTH:
						borders[2] = currentLoc.y + (i + 1);
						break;
					case WEST:
						borders[3] = currentLoc.x - (i + 1);
						break;
					}
				}
			}
		} catch (Exception e) {
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

		msgHandler.queueMessage(new BorderMessage(borders));
		
	}

	private void updateLocationSets() throws GameActionException {
		Mine[] minelist = controllers.sensor.senseNearbyGameObjects(Mine.class);
		for (Mine mine : minelist) {
			GameObject object = controllers.sensor.senseObjectAtLocation(
					mine.getLocation(), RobotLevel.ON_GROUND);

			if (object != null) {
				if (mineLocations.contains(mine.getLocation()))
					mineLocations.remove(mine.getLocation());
				if (object.getTeam() == controllers.myRC.getTeam())
					recyclerLocations.add(mine.getLocation());
			} else {
				mineLocations.add(mine.getLocation());
			}

		}
	}
	
	private void checkEmptyRecyclers(){
		for(MapLocation recyclerLoc : recyclerLocations){
			if(controllers.myRC.getLocation().isAdjacentTo(recyclerLoc)){
				msgHandler.queueMessage(new BuildingLocationInquiryMessage(recyclerLoc));
				break;
			}
		}
	}
	
	private void buildRecyclers() throws GameActionException{
//		controllers.myRC.setIndicatorString(0, mineLocations.toString());
		
		for (MapLocation mineLoc : mineLocations){
			if(controllers.myRC.getLocation().isAdjacentTo(mineLoc)){
				if(controllers.sensor.canSenseSquare(mineLoc)){
					 if(controllers.sensor.senseObjectAtLocation(mineLoc, RobotLevel.ON_GROUND) != null)
						 continue;
				}
				if (controllers.myRC.getDirection() != controllers.myRC.getLocation().directionTo(mineLoc)) {
					while (controllers.motor.isActive())
						controllers.myRC.yield();
					controllers.motor.setDirection(controllers.myRC.getLocation().directionTo(mineLoc));
					controllers.myRC.yield();
				}
				if(controllers.sensor.senseObjectAtLocation(mineLoc, RobotLevel.ON_GROUND) == null){
					while(!buildingSystem.constructUnit(controllers.myRC.getLocation().add(controllers.myRC.getDirection()),UnitType.RECYCLER)){
						if(buildingSystem.canConstruct(RobotLevel.ON_GROUND) == false)
							break;
						controllers.myRC.yield();
					}

					msgHandler.queueMessage(new ConstructionCompleteMessage(mineLoc, ComponentType.RECYCLER));
					msgHandler.queueMessage(new BorderMessage(borders));
					controllers.myRC.yield();
				}
			}
		}
	}

	private void navigate() throws GameActionException {

		if (!controllers.motor.isActive()) {
			if (!mineLocations.isEmpty()) {
				MapLocation currentLoc = controllers.myRC.getLocation();
				MapLocation nearest = currentLoc.add(Direction.NORTH, 100);
				for (MapLocation loc : mineLocations) {
					if (currentLoc.distanceSquaredTo(loc) < currentLoc
							.distanceSquaredTo(nearest))
						nearest = loc;
				}
				navigator.setDestination(nearest);
				Direction nextDir = navigator.getNextDir(0);

				if (nextDir != Direction.OMNI) {
					if (controllers.myRC.getDirection() == nextDir) {
						if (controllers.motor.canMove(nextDir)) {
							controllers.motor.moveForward();
						}
					} else {
						controllers.motor.setDirection(nextDir);
					}
				}

			} 
			
//			else if (!recyclerLocations.isEmpty()) {
//				MapLocation currentLoc = controllers.myRC.getLocation();
//				MapLocation nearest = currentLoc.add(Direction.NORTH, 100);
//				for (MapLocation loc : recyclerLocations) {
//					if (currentLoc.distanceSquaredTo(loc) < currentLoc
//							.distanceSquaredTo(nearest))
//						nearest = loc;
//				}
//
//				controllers.myRC.setIndicatorString(0, currentLoc + ","
//						+ nearest);
//
//				navigator.setDestination(nearest);
//				Direction nextDir = navigator.getNextDir(0);
//
//				if (nextDir != Direction.OMNI) {
//					if (controllers.myRC.getDirection() == nextDir) {
//						if (controllers.motor.canMove(nextDir)) {
//							controllers.motor.moveForward();
//						}
//					} else {
//						controllers.motor.setDirection(nextDir);
//					}
//				}
//
//			} 
			else {
				roachNavigate();
			}
		}

	}

	private void roachNavigate() throws GameActionException {
		// navigate();
		if (controllers.motor.canMove(controllers.myRC.getDirection())) {
			// System.out.println("about to move");
			controllers.motor.moveForward();
		} else {
			Direction tempDir = controllers.myRC.getDirection();
			int rotationTimes = (Clock.getRoundNum() / 10) % 7;
			for (int i = 0; i <= rotationTimes; ++i) {
				tempDir = tempDir.rotateRight();
			}
			controllers.motor.setDirection(tempDir);
		}
	}
}
