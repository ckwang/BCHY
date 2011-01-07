package team017.util;

import java.util.List;

import battlecode.common.ComponentClass;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Util {

	public static Direction[] dirs = { Direction.NORTH, Direction.NORTH_EAST,
			Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
			Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };
	
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

}
