package team017.construction;

import team017.util.Controllers;
import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
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
	
	private static final Direction[] directionMapping = 
		{Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
		Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
	
	public BuilderDirections(Controllers controllers) {
		this.controllers = controllers;
		
		updateEmptyDirections();
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
	
	public boolean isComplete() {
		switch(controllers.builder.type()) {
		case RECYCLER:
			return (armoryDirection != null && factoryDirection != null);
		case ARMORY:
			return (recyclerDirection != null && factoryDirection != null);
		case FACTORY:
			return (recyclerDirection != null && armoryDirection != null);
		default:
			return false;
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
					if (object != null && object.getTeam() == controllers.myRC.getTeam()) {
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
//		controllers.myRC.setIndicatorString(2, indicator);
	}
	
}
