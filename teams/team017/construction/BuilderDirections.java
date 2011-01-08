package team017.construction;

import team017.util.Controllers;
import battlecode.common.Chassis;
import battlecode.common.ComponentClass;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;

public class BuilderDirections {
	
	private Controllers controllers;
	
	public Direction recyclerDirection;
	public Direction armoryDirection;
	public Direction factoryDirection;
	/*
	 * 7 0 1
	 * 6 * 2
	 * 5 4 3
	 */
	public boolean[] emptyDirections = {true, true, true, true, true, true, true, true};
	public int clusterSize;
	
	private static final Direction[] directionMapping = 
		{Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
		Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
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
	
	public BuilderDirections(Controllers controllers) {
		this.controllers = controllers;
		
		updateEmptyDirections();
		updateBuilderDirs();
	}
	
	public Direction getDirections(ComponentType type){
		switch(type){
		case RECYCLER:
			return recyclerDirection;
		case ARMORY:
			return armoryDirection;
		case FACTORY:
			return factoryDirection;
		default:
			return null;
		}
	}
	
	public void setDirections(ComponentType type, Direction dir) {
		switch(type){
		case RECYCLER:
			recyclerDirection = dir;
		case ARMORY:
			armoryDirection = dir;
		case FACTORY:
			factoryDirection = dir;
		}
	}
	
	public Direction consecutiveEmpties(int length) {
		updateEmptyDirections();
		
		int n = 0;
		
		for (int i = 0; i < 7 + length; ++i) {
			n = emptyDirections[i%8] ? n + 1 : 0;
			if (n == length)	return directionMapping[(i+8-(length-1))%8];
		}
		
		return Direction.NONE;
	}
	
	public boolean isComplete(ComponentType thisBuilder, ComponentType[] builders) {
		for (ComponentType b : builders) {
			if (getDirections(b) == null && b != thisBuilder)	return false;
		}
		
		return true;
	}
	
	
	public void updateBuilderDirs() {
		clusterSize = 0;
		Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
		
		for (Robot r : robots) {
			if (r.getTeam() == controllers.myRC.getTeam()) {
				try {
					RobotInfo info = controllers.sensor.senseRobotInfo(r);
					MapLocation currentLoc = controllers.myRC.getLocation();
					
					if (info.location.isAdjacentTo(currentLoc)) {
						for (ComponentType com : info.components) {
							if (com.componentClass == ComponentClass.BUILDER) {
								setDirections(com, currentLoc.directionTo(info.location));
							}
						}
					}
				} catch (GameActionException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void updateEmptyDirections() {
		
		MapLocation currentLoc = controllers.myRC.getLocation();
		
		Direction dir = Direction.NORTH;
		for (int i = 0; i < 8; ++i) {
			MapLocation loc = currentLoc.add(dir);
			if (controllers.myRC.senseTerrainTile(loc) != TerrainTile.LAND) {
				emptyDirections[i] = false;
			} else {
				try {
					GameObject object = controllers.sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND);
					if (object != null) {
						if (controllers.sensor.senseRobotInfo((Robot) object).chassis == Chassis.BUILDING)
							emptyDirections[i] = false;
					}
					
				} catch (GameActionException e) {
					e.printStackTrace();
					emptyDirections[i] = false;
				}
			}
			
			dir = dir.rotateRight();
		}
		
		
//		String indicator = "";
//		for (int i = 0; i < 8; ++i) {
//			indicator += emptyDirections[i] + ",";
//		}
//		controllers.myRC.setIndicatorString(1, indicator);
	}
	
	public boolean checkDirEmpty(Direction dir){
		return emptyDirections[indexMapping(dir)];
	}
	
}
