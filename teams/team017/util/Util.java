package team017.util;

import java.util.List;

import battlecode.common.ComponentClass;
import battlecode.common.ComponentController;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Util {

	public static Direction[] dirs = { Direction.NORTH, Direction.NORTH_EAST,
			Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
			Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };
	
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

	public static ComponentController getComponentController(
			ComponentClass component, RobotController rc) {
		ComponentController[] components = rc.components();
		for (ComponentController cc : components) {
			if (cc.componentClass() == ComponentClass.SENSOR)
				return cc;
		}
		return null;
	}

}
