package team017.combat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import team017.util.Controllers;
import team017.util.Util;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.WeaponController;

public class CombatSystem {

	private Controllers controllers;

	public List<Robot> allies = new LinkedList<Robot>(); // exclude self
	public List<Robot> enemies = new LinkedList<Robot>(); // exclude off robot
	public List<Robot> immobileEnemies = new LinkedList<Robot>();
	public List<MapLocation> elocs = new ArrayList<MapLocation>();
	public List<MapLocation> alocs = new ArrayList<MapLocation>();
	
	public Robot target1 = null;
	public Robot target2 = null;
	public int totalEnemiesHp = 0;
	
	public MapLocation selfLoc;
	public int lastUpdate = -10;
	public Direction nextDir = Direction.NONE;
	
	
	public float totalDamage = 0;
	public float aveDamage = 0;
	public int maxRange = 0;
	
	public CombatSystem(Controllers c) {
		controllers = c;
		for (WeaponController w: c.weapons) {
			totalDamage += w.type().attackPower;
			int r = w.type().range * w.type().range;
			if (maxRange < r)
				maxRange = r;
		}
		aveDamage = totalDamage/(float)c.weapons.size();
	}

	public boolean approachTarget() {
		if (target1 != null)
			return approach(target1);
		else
			return false;
	};
	
	public boolean chaseTarget() {
		if (target1 != null)
			return chase(target1);
		else
			return false;
	}

	
	public boolean chase(Robot r) {
		if (controllers.motor.isActive())
			return false;
		MapLocation cur = controllers.myRC.getLocation();
		try {
			MapLocation loc = controllers.sensor.senseLocationOf(r);
			RobotInfo info = controllers.sensor.senseRobotInfo(r);
			Direction dir1 = cur.directionTo(loc);
			if (dir1 == Direction.OMNI || dir1 == Direction.NONE)
				return false;
			MapLocation nextloc = loc.add(info.direction);
			Direction dir2 = cur.directionTo(nextloc);
			if (cur.isAdjacentTo(loc)) {
					Direction newdir;
					if (dir1.rotateLeft() == dir2.rotateRight())
						newdir = dir1.rotateLeft();
					else if (dir2.rotateLeft() == dir1.rotateRight())
						newdir = dir2.rotateLeft();
					else
						newdir = dir1;
					controllers.motor.setDirection(newdir);
					nextDir = dir2;
					return true;
			}
			if (dir2 == Direction.OMNI || dir2 == Direction.NONE)
				return false;
			if (controllers.myRC.getDirection() != dir2) {
				controllers.motor.setDirection(dir2);
				return true;
			}
			controllers.motor.moveForward();
			return true;
		} catch (GameActionException e) {
			if (nextDir != Direction.OMNI && nextDir != Direction.NONE) {
				try {
					controllers.motor.setDirection(nextDir);
					return true;
				} catch (GameActionException e1) {}
			}
			return false;
		}
	}
	
	/*
	 * return true is moved
	 */
	public boolean approach(Robot r) {
		if (controllers.motor.isActive())
			return false;
		MapLocation cur = controllers.myRC.getLocation();
		try {
			MapLocation loc = controllers.sensor.senseLocationOf(r);
			Direction dir = cur.directionTo(loc);
			if (dir == Direction.OMNI || dir == Direction.NONE)
				return false;
			if (cur.isAdjacentTo(loc)) {
				controllers.motor.setDirection(dir);
			}
			else if (Util.isFacing(controllers.myRC.getDirection(), dir)
					&& controllers.motor.canMove(dir))
				controllers.motor.moveForward();
			else if (Util.isFacing(controllers.myRC.getDirection().opposite(),
					dir)
					&& controllers.motor.canMove(controllers.myRC
							.getDirection().opposite()))
				controllers.motor.moveBackward();
			else {
				controllers.motor.setDirection(dir);
			}
			return true;
		} catch (GameActionException e) {
			return false;
		}
	}
	
	public void massacre() {
//		if (immobileEnemies.size() == 0)
//			return;
		int i = 0;
		for (WeaponController w : controllers.weapons) {
			if (w.isActive())
				continue;
			try {
				MapLocation loc = controllers.sensor.senseLocationOf(immobileEnemies.get(i));
				w.attackSquare(loc, immobileEnemies.get(i).getRobotLevel());
			} catch (GameActionException e) {
				++i;
				if (i >= immobileEnemies.size())
					return;
			}
		}
	}
	
	public void flee() throws GameActionException {
		if (controllers.motor.isActive())
			return;
		MapLocation cur = controllers.myRC.getLocation();
		MapLocation enemyCenter = Util.aveLocation(elocs);
		if (enemyCenter == null)
			System.out.println("no enemy to flee from");
		Direction edir = cur.directionTo(enemyCenter);
		Direction mydir = controllers.myRC.getDirection();
		if (Util.isFacing(mydir, edir)
				&& controllers.motor.canMove(edir.opposite()))
			controllers.motor.moveBackward();
		else if (Util.isFacing(mydir.opposite(), edir))
			controllers.motor.moveForward();
		else
			controllers.motor.setDirection(edir.opposite());
	}

	public boolean attack() {
		if (target1 == null) {
//			destroyDeadEnemy();
			System.out.println("Null target");
			return false;
		}
		boolean attacked = false;
		for (WeaponController w : controllers.weapons) {
			if (w.isActive())
				continue;
			try {
				RobotInfo info = controllers.sensor.senseRobotInfo(target1);
				if (info.hitpoints < 0) {
					if (target2 == null)
						return false;
					target1 = target2;
					target2 = null;
				}
				MapLocation weakest = controllers.sensor
				.senseLocationOf(target1);
				int dist = controllers.myRC.getLocation().distanceSquaredTo(weakest);
				if (dist > maxRange) {
					return attacked;
				}
				w.attackSquare(weakest, target1.getRobotLevel());
				attacked = true;
			} catch (GameActionException e) {
				if (target2 == null)
					return false;
				target1 = target2;
				target2 = null;
			}
		}
		return attacked;
	}

	public void senseNearby() {
		reset();
		Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
		double leasthp1 = Chassis.HEAVY.maxHp;
		double leasthp2 = Chassis.HEAVY.maxHp;
		for (Robot r : robots) {
			try {
				RobotInfo info = controllers.sensor.senseRobotInfo(r);
				if (r.getTeam() == controllers.myRC.getTeam()) {
					ComponentType[] components = info.components;
					if (Util.hasWeapon(components)) {
						allies.add(r);
						MapLocation loc = controllers.sensor.senseLocationOf(r);
						alocs.add(loc);
					}
				} else if ((r.getTeam() != Team.NEUTRAL)) {
					if (!info.on || info.chassis == Chassis.BUILDING) {
						immobileEnemies.add(r);
						continue;
					}
					MapLocation loc = controllers.sensor.senseLocationOf(r);
					int dist = controllers.myRC.getLocation().distanceSquaredTo(loc);
					if (dist > maxRange)
						continue;
					totalEnemiesHp += info.hitpoints;
					if (info.hitpoints < leasthp1) {
						leasthp2 = leasthp1;
						target2 = target1;
						target1 = r;
						leasthp1 = info.hitpoints;
					} else if (info.hitpoints < leasthp2) {
						target2 = r;
						leasthp2 = info.hitpoints;
					} else {
						enemies.add(r);
						elocs.add(loc);
					}
					if (target1 != null) {
						enemies.add(target1);
						if (target2 != null)
							enemies.add(target2);
					}
				}
			} catch (GameActionException e) {
				continue;
			}
		}
		lastUpdate = Clock.getRoundNum();
	}

	public void reset() {
		allies.clear();
		enemies.clear();
		immobileEnemies.clear();
		elocs.clear();
		alocs.clear();
		totalEnemiesHp = 0;
		target1 = null;
		target2 = null;
	}
	
	public int allyNum() {
		return allies.size();
	}
	
	public int enemyNum() {
		if (outdated())
			senseNearby();
		return enemies.size();
	}
	
	public int immobileEnemyNum() {
		if (outdated())
			senseNearby();
		return immobileEnemies.size();
	}
	
	public boolean hasEnemy() {
		if (outdated())
			senseNearby();
		return (enemies.size() + immobileEnemies.size()) > 0;
	}

	public boolean outdated() {
		return Clock.getRoundNum() > lastUpdate + 1;
	}

}
