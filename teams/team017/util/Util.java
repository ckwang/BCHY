package team017.util;

import java.util.ArrayList;
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

	public final static int RECYCLER_CODE = 1 << 0;
	public final static int FACTORY_CODE = 1 << 1;
	public final static int ARMORY_CODE = 1 << 2;
	public final static int CONSTRUCTOR_CODE = 1 << 3;
	public final static int[] builderCodes = {Util.RECYCLER_CODE, Util.ARMORY_CODE, Util.FACTORY_CODE};

	public static List<Integer> tmplist = new ArrayList<Integer>(); 

	
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
			robot = (Robot) controllers.sensor.senseObjectAtLocation(loc, level);
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
	
	public static void sortHp(List<RobotInfo> robots) {
		if (robots.size() < 2)
			return;
		int j;
		RobotInfo tmp;
		for (int i = 1; i < robots.size(); ++i) {
			tmp = robots.get(i);
			for (j = i; j > 0 && tmp.hitpoints < robots.get(j-1).hitpoints; --j) {
				robots.set(j, robots.get(j-1));
			}
			robots.set(j, tmp);
		}
	}
	
	public static void sortHpPercentage(List<RobotInfo> robots) {
		if (robots.size() < 2)
			return;
		int j;
		RobotInfo tmp;
		for (int i = 1; i < robots.size(); ++i) {
			tmp = robots.get(i);
			for (j = i; j > 0 && (tmp.hitpoints / tmp.maxHp) < (robots.get(j-1).hitpoints / robots.get(j-1).maxHp); --j) {
				robots.set(j, robots.get(j-1));
			}
			robots.set(j, tmp);
		}
	}
	
	public static void sortLocation(List<RobotInfo> robots, MapLocation myloc) {
		if (robots.size() < 2)
			return;
		int j, dis;
		RobotInfo r;
		tmplist.clear();
		tmplist.add(myloc.distanceSquaredTo(robots.get(0).location));
		for (int i = 1; i < robots.size(); ++i) {
			r = robots.get(i);
			dis = myloc.distanceSquaredTo(r.location);
			tmplist.add(dis);
			for (j = i; j > 0 && dis < tmplist.get(j-1); --j) {
				tmplist.set(j, tmplist.get(j-1));
				robots.set(j, robots.get(j-1));
			}
			tmplist.set(j, dis);
			robots.set(j, r);
		}
	}
	
	public static void sortLocation(List<Integer> dists, List<Robot> robots) {
		if (dists.size() != robots.size() || dists.size() < 2)
			return;
		int j, tmp;
		Robot r;
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

	public static int getBuilderCode (ComponentType type) {
		switch (type) {
		case RECYCLER:
			return RECYCLER_CODE;
		case ARMORY:
			return ARMORY_CODE;
		case FACTORY:
			return FACTORY_CODE;
		default:
			return 0;
		}
	}
}
