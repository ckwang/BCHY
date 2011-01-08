package team017.AI;

import java.util.HashSet;
import java.util.Set;

import team017.construction.UnitType;
import team017.message.BorderMessage;
import team017.message.BuildingLocationInquiryMessage;
import team017.message.BuildingLocationResponseMessage;
import team017.message.ConstructionCompleteMessage;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.Mine;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

public class ConstructorAI extends AI {

	private Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	private Set<MapLocation> recyclerLocations = new HashSet<MapLocation>();

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

				// Check messages
				if (controllers.myRC.getTeamResources() > 200)
					checkEmptyRecyclers();

				while (msgHandler.hasMessage()) {
					Message msg = msgHandler.nextMessage();
					switch (msgHandler.getMessageType(msg)) {
					case BUILDING_LOCATION_RESPONSE_MESSAGE: {
						BuildingLocationResponseMessage handler = new BuildingLocationResponseMessage(msg);
						if (handler.getBuildableDirection() != Direction.NONE) {
							MapLocation buildLoc = handler.getSourceLocation().add(handler.getBuildableDirection());

							if (handler.getAvailableSpace() == 3) {
								if (buildBuildingAtLoc(buildLoc,UnitType.ARMORY)) {
									MapLocation nextBuildLoc = handler.getSourceLocation().add(handler.getBuildableDirection().rotateRight());
									buildBuildingAtLoc(nextBuildLoc,UnitType.FACTORY);
								}
							} else {
								buildBuildingAtLoc(buildLoc, UnitType.ARMORY);
							}
						}
						break;
					}
					}
				}
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

				if (!controllers.motor.isActive()
						&& controllers.motor.canMove(nextDir)) {
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

	private void updateLocationSets() {
		Mine[] minelist = controllers.sensor.senseNearbyGameObjects(Mine.class);
		for (Mine mine : minelist) {
			try {
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
			} catch (GameActionException e) {
				continue;
			}
		}
	}

	private void checkEmptyRecyclers() {
		for (MapLocation recyclerLoc : recyclerLocations) {
			if (controllers.myRC.getLocation().isAdjacentTo(recyclerLoc)) {
				msgHandler.queueMessage(new BuildingLocationInquiryMessage(
						recyclerLoc));
				break;
			}
		}
	}

	private void buildRecyclers() throws GameActionException{
//		controllers.myRC.setIndicatorString(2, mineLocations.toString());
		
		for (MapLocation mineLoc : mineLocations){
			if(controllers.myRC.getLocation().isAdjacentTo(mineLoc)){
				if(controllers.sensor.canSenseSquare(mineLoc)){
					 if(controllers.sensor.senseObjectAtLocation(mineLoc, RobotLevel.ON_GROUND) != null)
						 continue;

				}
				if (controllers.myRC.getDirection() != controllers.myRC
						.getLocation().directionTo(mineLoc)) {
					while (controllers.motor.isActive())
						controllers.myRC.yield();
					controllers.motor.setDirection(controllers.myRC
							.getLocation().directionTo(mineLoc));
					controllers.myRC.yield();
				}
				if (controllers.sensor.senseObjectAtLocation(mineLoc,
						RobotLevel.ON_GROUND) == null) {
					while (!buildingSystem.constructUnit(
							controllers.myRC.getLocation().add(
									controllers.myRC.getDirection()),
							UnitType.RECYCLER)) {
						if (buildingSystem.canConstruct(RobotLevel.ON_GROUND) == false)
							break;
						controllers.myRC.yield();
					}

					msgHandler.queueMessage(new ConstructionCompleteMessage(
							mineLoc, ComponentType.RECYCLER));
					msgHandler.queueMessage(new BorderMessage(borders));
					controllers.myRC.yield();
				}
			}
		}
	}

	private boolean buildBuildingAtLoc(MapLocation buildLoc, UnitType type)
			throws GameActionException {
		while (!controllers.myRC.getLocation().add(
				controllers.myRC.getDirection()).equals(buildLoc)) {
			if (controllers.sensor.canSenseSquare(buildLoc)
					&& controllers.sensor.senseObjectAtLocation(buildLoc,
							type.chassis.level) != null)
				return false;
			if (!controllers.motor.isActive()) {
				if (!controllers.myRC.getLocation().isAdjacentTo(buildLoc)) {
					navigator.setDestination(buildLoc);
					Direction nextDir = navigator.getNextDir(0);
					if (controllers.myRC.getDirection() != nextDir) {

						controllers.motor.setDirection(nextDir);
					} else {
						controllers.motor.moveForward();
					}
				} else if (!controllers.myRC.getLocation().add(
						controllers.myRC.getDirection()).equals(buildLoc)) {
					controllers.motor.setDirection(controllers.myRC
							.getLocation().directionTo(buildLoc));
				}
			}
			yield();
		}

		while (!buildingSystem.constructUnit(buildLoc, type)) {
			if (controllers.sensor.senseObjectAtLocation(buildLoc,
					type.chassis.level) != null)
				return false;
			yield();
		}
		return true;
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
			else {
				roachNavigate();
			}
		}
	}
}
