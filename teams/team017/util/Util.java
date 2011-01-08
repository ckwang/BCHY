package team017.util;

import java.util.List;

import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotLevel;

public class Util {

	public static Direction[] dirs = { Direction.NORTH, Direction.NORTH_EAST,
			Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
			Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };

	public static boolean isFacing( Direction d1, Direction d2 ){
		return d1 == d2 || d1 == d2.rotateLeft() || d1 == d2.rotateRight();  
	}
	
	public static boolean isNotTurningBackward( Direction d1, Direction d2 ){
		return !d1.opposite().equals(d2)  
				&& !d1.opposite().equals(d2.rotateLeft()) 
				&& !d1.opposite().equals(d2.rotateRight());
	}
	
	public static MapLocation aveLocation(List<MapLocation> list) {
		int x = 0, y = 0;
		for (MapLocation loc: list) {
			x += loc.x;
			y += loc.y;
		}
		return new MapLocation(x/list.size(), y/list.size());
	}

	public static boolean containsComponent(ComponentType[] list, ComponentType com) {
		for (ComponentType c : list) {
			if (c == com)
				return true;
		}
		return false;
	}
	
	public static boolean containsComponent(ComponentController[] list, ComponentType com) {
		for (ComponentController c : list) {
			if (c.type() == com)
				return true;
		}
		return false;
	}
	
	public static boolean containsComponent(Controllers controllers, MapLocation loc, RobotLevel level, ComponentType com) {
		try {
			Robot robot = (Robot) controllers.sensor.senseObjectAtLocation(loc, level);
			return containsComponent(controllers.sensor.senseRobotInfo(robot).components, com);
		} catch (GameActionException e) {
			e.printStackTrace();
			return false;
		}
	}

}
