package team017.construction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import team017.util.Controllers;
import team017.util.Util;
import battlecode.common.Chassis;
import battlecode.common.ComponentClass;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
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
	public int adjacentBuilders = 0;
	
	/*
	 * 7 0 1
	 * 6 * 2
	 * 5 4 3
	 */
	
	public boolean[] emptyLocations = {true, true, true, true, true, true, true, true};
	public int emptySize;
	
	public int clusterSize;
	
	private MapLocation currentLoc;
	
	
	private static final Direction[] directionMapping = 
		{Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
		Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	private static final MapLocation[] locationMapping = new MapLocation[8];
	
	
	public static int indexMapping(Direction dir) {
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
		updateBuildingLocs();
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

	public MapLocation getLocations (int code) {
		switch (code) {
		case Util.RECYCLER_CODE:
			return recyclerLocation;
		case Util.ARMORY_CODE:
			return armoryLocation;
		case Util.FACTORY_CODE:
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
	
	public void setLocations (UnitType type, MapLocation loc) {
		switch (type) {
		case RECYCLER:
			if (loc != null)  {
				adjacentBuilders |= Util.RECYCLER_CODE;
				if (recyclerLocation == null)
					recyclerLocation = loc;
			} else {
				adjacentBuilders &= ~Util.RECYCLER_CODE;
				recyclerLocation = null;
			}
			
			break;
		case ARMORY:
			if (loc != null)  {
				adjacentBuilders |= Util.ARMORY_CODE;
				if (armoryLocation == null)
					armoryLocation = loc;
			} else {
				adjacentBuilders &= ~Util.ARMORY_CODE;
				armoryLocation = null;
			}
			
			break;
		case FACTORY:
			if (loc != null)  {
				adjacentBuilders |= Util.FACTORY_CODE;
				if (factoryLocation == null)
					factoryLocation = loc;
			} else {
				adjacentBuilders &= ~Util.FACTORY_CODE;
				factoryLocation = null;
			}
			
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
			
			if (loc != null)  {
				adjacentBuilders |= Util.RECYCLER_CODE;
				if (recyclerLocation == null)
					recyclerLocation = loc;
			} else {
				adjacentBuilders &= ~Util.RECYCLER_CODE;
				recyclerLocation = null;
			}
			
			break;
		case ARMORY:
			if (loc != null)  {
				adjacentBuilders |= Util.ARMORY_CODE;
				if (armoryLocation == null)
					armoryLocation = loc;
			} else {
				adjacentBuilders &= ~Util.ARMORY_CODE;
				armoryLocation = null;
			}
			
			break;
		case FACTORY:
			if (loc != null)  {
				adjacentBuilders |= Util.FACTORY_CODE;
				if (factoryLocation == null)
					factoryLocation = loc;
			} else {
				adjacentBuilders &= ~Util.FACTORY_CODE;
				factoryLocation = null;
			}
			
			break;
		}
	}

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
	
	public boolean isComplete(int thisBuilderCode, int requiredBuilders) {

		for (int c: Util.builderCodes) {
			if (thisBuilderCode != c && ((requiredBuilders & c) > 0) && getLocations(c) == null)
				return false;
		}
		return true;
	}
	
	public void updateBuildingLocs() {
		Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
		Mine[] mines = controllers.sensor.senseNearbyGameObjects(Mine.class);
		clusterSize = mines.length;
		
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
//					e.printStackTrace();
				}
			}
		}
	}
	
	public void updateEmptyLocations() {
		emptySize = 0;
		for (int i = 0; i < 8; ++i) {
			MapLocation loc = locationMapping[i];
			if (controllers.myRC.senseTerrainTile(loc) != TerrainTile.LAND) {
				emptyLocations[i] = false;
			} else {
				try {

					if (controllers.myRC.senseTerrainTile(loc) != TerrainTile.LAND)
						emptyLocations[i] = false;
					else {
						if (controllers.sensor.canSenseSquare(loc)) {
							if (controllers.sensor.senseObjectAtLocation(loc, RobotLevel.MINE) != null)
								emptyLocations[i] = false;
							else {
								Object objectOnGround = controllers.sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND); 
								if (objectOnGround != null) {
									Chassis chassis = controllers.sensor.senseRobotInfo((Robot) objectOnGround).chassis;
									if ( chassis == Chassis.BUILDING || chassis == Chassis.DEBRIS ) {
										emptyLocations[i] = false;
									} else {
										emptyLocations[i] = true;
										emptySize++;
									}
								}
							}
						} else {
							if (controllers.builder.canBuild(Chassis.LIGHT, loc)) {
								emptyLocations[i] = true;
							} else {
								emptyLocations[i] = false;
							}
						}
					}
					
					
					
//					Object objectOnGround = controllers.sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND); 
//					if (objectOnGround != null) {
//						Chassis chassis = controllers.sensor.senseRobotInfo((Robot) objectOnGround).chassis;
//						if ( chassis == Chassis.BUILDING || chassis == Chassis.DEBRIS ) {
//							emptyLocations[i] = false;
//						} else {
//							emptyLocations[i] = true;
//							emptySize++;
//						}
//					} else if (controllers.sensor.senseObjectAtLocation(loc, RobotLevel.MINE) != null) {
//						emptyLocations[i] = false;
//
//					} else {
//						emptyLocations[i] = true;
//						emptySize++;
//					}
//					
				} catch (GameActionException e) {
					e.printStackTrace();
					emptyLocations[i] = false;
				}
			}
		}
		for (int i = 0; i < 8; i++) {
			if (emptyLocations[i])
				emptySize++;
		}
			
	}
	

	public MapLocation constructableLocation (int thisBuilderCode, int requiredBuilders) {
		updateEmptyLocations();

		int i = 0;
		List<MapLocation> builderLocs = new LinkedList();
		
		for (int c: Util.builderCodes) {
			if (thisBuilderCode != c && ((requiredBuilders & c) > 0)) {
				if (getLocations(c) == null)
					return null;
				builderLocs.add(getLocations(c));
			}
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
	
	public int getConsecutiveEmptySize() {
		updateEmptyLocations();
		for (int i = 8; i >0; i--) {
			if (consecutiveEmpties(i) != null)
				return i;
		}
		return 0;
	}
	
}
