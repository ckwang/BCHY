package team017.construction;

import java.util.ArrayList;
import java.util.List;

import team017.util.Controllers;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentClass;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

public class BuildingLocations {
	
	private Controllers controllers;
	
	public MapLocation recyclerLocation;
	public MapLocation armoryLocation;
	public MapLocation factoryLocation;
	public List<MapLocation> towerLocations = new ArrayList<MapLocation>();
	public List<MapLocation> railgunTowerLocations = new ArrayList<MapLocation>();
	
//	public Direction recyclerDirection;
//	public Direction armoryDirection;
//	public Direction factoryDirection;
//	public Direction towerDirection;
//	public Direction railgunTowerDirection;
	/*
	 * 7 0 1
	 * 6 * 2
	 * 5 4 3
	 */
//	public boolean[] emptyDirections = {true, true, true, true, true, true, true, true};
	
	public boolean[] emptyLocations = {true, true, true, true, true, true, true, true};
	
	public int clusterSize;
	
	private MapLocation currentLoc;
	
	
	private static final Direction[] directionMapping = 
		{Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
		Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	private static final MapLocation[] locationMapping = new MapLocation[8];
	
//	private static int indexMapping (MapLocation loc) {
//		switch (loc) {
//		case currentLoc.add(Direction.NORTH):
//			return 0;
//		}
//	}
	
	
	private static int indexMapping(Direction dir) {
		switch(dir){
		case NORTH:
			return 0;
		case NORTH_EAST:
			return 1;
		case EAST:
			return 2;
		case SOUTH_EAST:
			return 3;
		case SOUTH:
			return 4;
		case SOUTH_WEST:
			return 5;
		case WEST:
			return 6;
		case NORTH_WEST:
			return 7;
		default:
			return -1;
		}
	}
	
	public BuildingLocations(Controllers controllers) {
		this.controllers = controllers;
		currentLoc = controllers.myRC.getLocation();
		for (int i = 0; i < 8; i++)
			locationMapping[i] = currentLoc.add(directionMapping[i]);
		updateEmptyLocations();
//		updateEmptyDirections();
		updateBuildingLocs();
//		updateBuildingDirs();
	}
	
	public MapLocation getLocations (ComponentType type) {
		switch (type) {
		case RECYCLER:
			return recyclerLocation;
		case ARMORY:
			return armoryLocation;
		case FACTORY:
			return factoryLocation;
		default:
			return null;
		}
	}
	
	public MapLocation getLocations (UnitType type) {
		switch (type) {
		case RECYCLER:
			return recyclerLocation;
		case ARMORY:
			return armoryLocation;
		case FACTORY:
			return factoryLocation;
		case TOWER:
			if (towerLocations.size() == 0)
				return null;
			return towerLocations.get(0);
		case RAILGUN_TOWER:
			if (railgunTowerLocations.size() == 0)
				return null;
			return railgunTowerLocations.get(0);
		default:
			return null;
		}
	}
	
//	public Direction getDirections(ComponentType type){
//		if (type == null)
//			return towerDirection;
//		switch(type){
//		case RECYCLER:
//			return recyclerDirection;
//		case ARMORY:
//			return armoryDirection;
//		case FACTORY:
//			return factoryDirection;
//		default:
//			return null;
//		}
//	}
//	
//	public Direction getDirections(UnitType type){
//		switch(type){
//		case RECYCLER:
//			return recyclerDirection;
//		case ARMORY:
//			return armoryDirection;
//		case FACTORY:
//			return factoryDirection;
//		case TOWER:
//			return towerDirection;
//		case RAILGUN_TOWER:
//			return railgunTowerDirection;
//		default:
//			return null;
//		}
//	}
	
	public void setLocations (UnitType type, MapLocation loc) {
		switch (type) {
		case RECYCLER:
			recyclerLocation = loc;
			break;
		case ARMORY:
			armoryLocation = loc;
			break;
		case FACTORY:
			factoryLocation = loc;
			break;
		case TOWER:
			towerLocations.add(loc);
			break;
		case RAILGUN_TOWER:
			railgunTowerLocations.add(loc);
			break;
		}
	}

	public void setLocations (ComponentType type, MapLocation loc) {
		switch (type) {
		case RECYCLER:
			recyclerLocation = loc;
			break;
		case ARMORY:
			armoryLocation = loc;
			break;
		case FACTORY:
			factoryLocation = loc;
			break;
		}
	}

//	public void setDirections(ComponentType type, Direction dir) {
//		switch(type){
//		case RECYCLER:
//			recyclerDirection = dir;
//			break;
//		case ARMORY:
//			armoryDirection = dir;
//			break;
//		case FACTORY:
//			factoryDirection = dir;
//			break;
//		}
//	}
//	
//	public void setDirections(UnitType type, Direction dir) {
//		switch(type){
//		case RECYCLER:
//			recyclerDirection = dir;
//			break;
//		case ARMORY:
//			armoryDirection = dir;
//			break;
//		case FACTORY:
//			factoryDirection = dir;
//			break;
//		case TOWER:
//			towerDirection = dir;
//			break;
//		}
//	}
	
	/*
	 * Might want to build in somewhere near enemy base; Haven't been added yet
	 */
	
	public MapLocation consecutiveEmpties (int length) {
		updateEmptyLocations();
		
		int n = 0;
		
		for (int i = 0; i < 7 + length; ++i) {
			n = emptyLocations[i%8] ? n + 1 : 0;
			if (n == length) return locationMapping [ (i + 8 - (length - 1)) % 8];
		}
		return null;
	}
	
//	public Direction consecutiveEmpties(int length) {
//		updateEmptyDirections();
//		
//		int n = 0;
//		
//		for (int i = 0; i < 7 + length; ++i) {
//			n = emptyDirections[i%8] ? n + 1 : 0;
//			if (n == length)	return directionMapping[(i+8-(length-1))%8];
//		}
//		
//		return Direction.NONE;
//	}
	
	public boolean isComplete(ComponentType thisBuilder, ComponentType[] builders) {
		for (ComponentType b : builders) {
			if (getLocations(b) == null && b != thisBuilder)
				return false;
		}
		return true;
	}
	
//	public boolean isComplete(ComponentType thisBuilder, ComponentType[] builders) {
//		for (ComponentType b : builders) {
//			if (getDirections(b) == null && b != thisBuilder)	return false;
//		}
//		
//		return true;
//	}
	
	public void updateBuildingLocs() {
		clusterSize = 0;
		Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
		Mine[] mines = controllers.sensor.senseNearbyGameObjects(Mine.class);
		for (Mine m: mines) {
			clusterSize++;
		}
		
		for (Robot r : robots) {
			if (r.getTeam() == controllers.myRC.getTeam()) {
				try {
					RobotInfo info = controllers.sensor.senseRobotInfo(r);
					MapLocation currentLoc = controllers.myRC.getLocation();
					
					if (info.location.isAdjacentTo(currentLoc) && info.on && info.chassis == Chassis.BUILDING) {
						for (ComponentType com : info.components) {
							if (com.componentClass == ComponentClass.BUILDER) {
								setLocations(com, info.location);
							} else if (com == ComponentType.BLASTER) {
								setLocations(UnitType.TOWER, info.location);
							} else if (com == ComponentType.RAILGUN) {
								setLocations(UnitType.RAILGUN_TOWER, info.location);
							}
						}
					}
				} catch (GameActionException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
//	public void updateBuildingDirs() {
//		clusterSize = 0;
//		Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
//		Mine[] mines = controllers.sensor.senseNearbyGameObjects(Mine.class);
//		
//		for (Mine m: mines) {
//			clusterSize++;
//		}
//		
//		for (Robot r : robots) {
//			if (r.getTeam() == controllers.myRC.getTeam()) {
//				try {
//					RobotInfo info = controllers.sensor.senseRobotInfo(r);
//					MapLocation currentLoc = controllers.myRC.getLocation();
//					
//					if (info.location.isAdjacentTo(currentLoc) && info.on && info.chassis == Chassis.BUILDING) {
//						for (ComponentType com : info.components) {
//
//							if (com.componentClass == ComponentClass.BUILDER) {
//								setDirections(com, currentLoc.directionTo(info.location));
////								if (com == ComponentType.RECYCLER)
////									clusterSize++;
//							} else if (com == ComponentType.BLASTER) {
//								setDirections(UnitType.TOWER, currentLoc.directionTo(info.location));
//							}
//						}
//					}
//				} catch (GameActionException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}
	
	public void updateEmptyLocations() {
		for (int i = 0; i < 8; ++i) {
			MapLocation loc = locationMapping[i];
			if (controllers.myRC.senseTerrainTile(loc) != TerrainTile.LAND) {
				emptyLocations[i] = false;
			} else {
				try {
					Object objectOnGround = controllers.sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND); 
					if (objectOnGround != null) {
						if (controllers.sensor.senseRobotInfo((Robot) objectOnGround).chassis == Chassis.BUILDING)
							emptyLocations[i] = false;
					} else if (controllers.sensor.senseObjectAtLocation(loc, RobotLevel.MINE) != null) {
						emptyLocations[i] = false;
					} else {
						emptyLocations[i] = true;
					}
					
				} catch (GameActionException e) {
					e.printStackTrace();
					emptyLocations[i] = false;
				}
			}
		}
	}
	
//	public void updateEmptyDirections() {
//		
//		MapLocation currentLoc = controllers.myRC.getLocation();
//		
//		Direction dir = Direction.NORTH;
//		for (int i = 0; i < 8; ++i) {
//			MapLocation loc = currentLoc.add(dir);
//			if (controllers.myRC.senseTerrainTile(loc) != TerrainTile.LAND) {
//				emptyDirections[i] = false;
//			} else {
//				try {
//					Object objectOnGround = controllers.sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND); 
//					if (objectOnGround != null) {
//						if (controllers.sensor.senseRobotInfo((Robot) objectOnGround).chassis == Chassis.BUILDING)
//							emptyDirections[i] = false;
//					} else if (controllers.sensor.senseObjectAtLocation(loc, RobotLevel.MINE) != null) {
//						emptyDirections[i] = false;
//					} else {
//						emptyDirections[i] = true;
//					}
//					
//				} catch (GameActionException e) {
//					e.printStackTrace();
//					emptyDirections[i] = false;
//				}
//			}
//			
//			dir = dir.rotateRight();
//		}
//		
//		
////		String indicator = "";
////		for (int i = 0; i < 8; ++i) {
////			indicator += emptyDirections[i] + ",";
////		}
////		controllers.myRC.setIndicatorString(1, indicator);
//	}
	
	
//	public boolean checkDirEmpty(Direction dir){
//		return emptyDirections[indexMapping(dir)];
//	}
	
	public MapLocation constructableLocation (ComponentType thisBuilder, ComponentType[] requiredBuilders) {
		updateEmptyLocations();

		MapLocation[] builderLocs = new MapLocation[requiredBuilders.length - 1];
		int i = 0;
		for (ComponentType c : requiredBuilders) {
			if (thisBuilder != c)
				builderLocs[i++] = getLocations(c);
		}
		
		outer:
		for (i = 0; i < 8; ++i) {
			if (emptyLocations[i] == true) {
				MapLocation buildingLoc = currentLoc.add(directionMapping[i]);
				for (MapLocation builderLoc : builderLocs) {
					if (!buildingLoc.isAdjacentTo(builderLoc))
						continue outer;
				}
				return buildingLoc;
			}
			
		}
		return null;
	}
	
	public MapLocation rotateRight(MapLocation loc) {
		if (!loc.isAdjacentTo(currentLoc))
			return null;
		Direction dir = currentLoc.directionTo(loc);
		dir = dir.rotateRight();
		return currentLoc.add(dir);
	}

	public MapLocation rotateRight(MapLocation loc, int times) {
		if (!loc.isAdjacentTo(currentLoc))
			return null;
		Direction dir = currentLoc.directionTo(loc);
		for (int i = 0; i < times; i++)
			dir = dir.rotateRight();
		return currentLoc.add(dir);
	}
	
	public MapLocation rotateLeft(MapLocation loc) {
		if (!loc.isAdjacentTo(currentLoc))
			return null;
		Direction dir = currentLoc.directionTo(loc);
		dir = dir.rotateLeft();
		return currentLoc.add(dir);
	}

	public MapLocation rotateLeft(MapLocation loc, int times) {
		if (!loc.isAdjacentTo(currentLoc))
			return null;
		Direction dir = currentLoc.directionTo(loc);
		for (int i = 0; i < times; ++i)
			dir = dir.rotateLeft();
		return currentLoc.add(dir);
	}

	
}
	
//	public MapLocation constructableLocation(ComponentType thisBuilder, ComponentType[] requiredBuilders) {
//		updateEmptyDirections();
//		MapLocation currentLoc = controllers.myRC.getLocation();
//		
//		MapLocation[] builderLocs = new MapLocation[requiredBuilders.length - 1];
//		int i = 0;
//		for (ComponentType c : requiredBuilders) {
//			if (thisBuilder != c)
//				builderLocs[i++] = currentLoc.add(getDirections(c));
//		}
//		
//		outer:
//		for (i = 0; i < 8; ++i) {
//			if (emptyDirections[i] == true) {
//				MapLocation buildingLoc = currentLoc.add(directionMapping[i]);
//				for (MapLocation builderLoc : builderLocs) {
//					if (!buildingLoc.isAdjacentTo(builderLoc))
//						continue outer;
//				}
//				return buildingLoc;
//			}
//			
//		}
//		return null;
//	}
	

