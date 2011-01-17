package team017.util;

import java.util.List;

import team017.construction.UnitType;
import battlecode.common.ComponentClass;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class Util {

	public static Direction[] dirs = { Direction.NORTH, Direction.NORTH_EAST,
			Direction.EAST, Direction.SOUTH_EAST, Direction.SOUTH,
			Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST };

	public static boolean isFacing(Direction d1, Direction d2) {
		return d1 == d2 || d1 == d2.rotateLeft() || d1 == d2.rotateRight();
	}

	public static boolean isNotTurningBackward(Direction d1, Direction d2) {
		return !d1.opposite().equals(d2)
				&& !d1.opposite().equals(d2.rotateLeft())
				&& !d1.opposite().equals(d2.rotateRight());
	}

	public static MapLocation aveLocation(List<RobotInfo> list) {
		if (list.size() == 0)
			return null;
		int x = 0, y = 0;
		for (RobotInfo info: list) {
			x += info.location.x;
			y += info.location.y;
		}
		return new MapLocation(x / list.size(), y / list.size());
	}

	public static boolean containsComponent(ComponentType[] list,
			ComponentType com) {
		for (ComponentType c : list) {
			if (c == com)
				return true;
		}
		return false;
	}

	public static boolean hasWeapon(ComponentType[] components) {
		for (ComponentType c : components) {
			if (c.componentClass == ComponentClass.WEAPON)
				return true;
		}
		return false;
	}

	public static boolean containsComponent(ComponentController[] list,
			ComponentType com) {
		for (ComponentController c : list) {
			if (c.type() == com)
				return true;
		}
		return false;
	}

	public static boolean containsComponent(Controllers controllers,
			MapLocation loc, RobotLevel level, ComponentType com) {
		try {
			Robot robot = (Robot) controllers.sensor.senseObjectAtLocation(loc,
					level);
			return containsComponent(
					controllers.sensor.senseRobotInfo(robot).components, com);
		} catch (GameActionException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean containsComponent(Controllers controllers,
			MapLocation loc, RobotLevel level, UnitType type) {
		Robot robot;
		try {
			robot = (Robot) controllers.sensor
					.senseObjectAtLocation(loc, level);
			ComponentType[] parts = controllers.sensor.senseRobotInfo(robot).components;

			for (ComponentType com : type.allComs) {
				if (!containsComponent(parts, com))
					return false;
			}
			return true;
		} catch (GameActionException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void sortHp(List<Double> hps, List<Robot> robots) {
		int j;
		double tmp;
		Robot r;
		if (hps.size() != robots.size() || hps.size() == 0)
			return;
		if (hps.size() == 0)
			return;
		for (int i = 1; i < robots.size(); ++i) {
			tmp = hps.get(i);
			r = robots.get(i);
			j = i;
			for (; j > 0 && tmp < hps.get(j-1); j--) {
				hps.set(j, hps.get(j-1));
				robots.set(j, robots.get(j-1));
			}
			hps.set(j, tmp);
			robots.set(j, r);
		}
	}
	
	public static void sortHp(List<RobotInfo> robots) {
		int j;
		RobotInfo tmp;
		if (robots.size() < 1)
			return;
		for (int i = 1; i < robots.size(); ++i) {
			tmp = robots.get(i);
			for (j = i; j > 0 && tmp.hitpoints < robots.get(j-1).hitpoints; --j) {
				robots.set(j, robots.get(j-1));
			}
			robots.set(j, tmp);
		}
	}
	
	public static void sortLocation(List<Integer> dists, List<Robot> robots) {
		int j, tmp;
		Robot r;
		if (dists.size() != robots.size() || dists.size() == 0)
			return;
		for (int i = 1; i < robots.size(); ++i) {
			tmp = dists.get(i);
			r = robots.get(i);
			j = i;
			for (; j > 0 && tmp < dists.get(j-1); j--) {
				dists.set(j, dists.get(j-1));
				robots.set(j, robots.get(j-1));
			}
			dists.set(j, tmp);
			robots.set(j, r);
		}
	}

}
