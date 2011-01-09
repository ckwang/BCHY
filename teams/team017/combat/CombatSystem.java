package team017.combat;

import java.util.ArrayList;
import java.util.List;

import team017.util.Controllers;
import team017.util.Util;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.WeaponController;

public class CombatSystem {

	private Controllers controllers;

	public List<Robot> allies = new ArrayList<Robot>(); // exclude self
	public List<Robot> enemies = new ArrayList<Robot>(); // exclude off robot
	public List<Robot> deadEnemies = new ArrayList<Robot>();
	public List<MapLocation> elocs = new ArrayList<MapLocation>();
	public List<MapLocation> alocs = new ArrayList<MapLocation>();

	private Robot target1 = null;
	private Robot target2 = null;
	private int lastUpdate = -10;
	private Direction nextDir = Direction.NONE;
	private int attackRange = 16*16;

	public CombatSystem(Controllers c) {
		controllers = c;
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
	
	public void destroyDeadEnemy() {
		if (deadEnemies.size() == 0)
			return;
		int i = 0;
		for (WeaponController w : controllers.weapons) {
			if (w.isActive())
				continue;
			try {
				MapLocation loc = controllers.sensor.senseLocationOf(deadEnemies.get(i));
				w.attackSquare(loc, deadEnemies.get(i).getRobotLevel());
			} catch (GameActionException e) {
				++i;
				if (i >= deadEnemies.size())
					return;
			}
		}
	}

	public void attack() {
		if (target1 == null) {
			destroyDeadEnemy();
			return;
		}
		try {
			RobotInfo info = controllers.sensor.senseRobotInfo(target1);
			if (info.hitpoints < 0)
				System.out.println("hp less than 0");
		} catch (GameActionException e1) {}
		
		for (WeaponController w : controllers.weapons) {
			if (w.isActive())
				continue;
			try {
				MapLocation weakest = controllers.sensor
						.senseLocationOf(target1);
				int dist = controllers.myRC.getLocation().distanceSquaredTo(weakest);
				if (dist > attackRange)
					return;
				w.attackSquare(weakest, target1.getRobotLevel());
			} catch (GameActionException e) {
				if (target2 == null)
					return;
				target1 = target2;
				target2 = null;
			}
		}
	}

	private void senseNearby() {
		allies.clear();
		enemies.clear();
		deadEnemies.clear();
		
		Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
		double leasthp1 = Chassis.HEAVY.maxHp;
		double leasthp2 = Chassis.HEAVY.maxHp;
		for (Robot r : robots) {
			try {
				RobotInfo info = controllers.sensor.senseRobotInfo(r);
				if (r.getTeam() == controllers.myRC.getTeam()) {
					allies.add(r);
					MapLocation loc = controllers.sensor.senseLocationOf(r);
					alocs.add(loc);
				} else if ((r.getTeam() != Team.NEUTRAL)) {
					if (!info.on || info.chassis == Chassis.BUILDING) {
//						System.out.println(r.getTeam());
						deadEnemies.add(r);
						continue;
					}
					enemies.add(r);
					MapLocation loc = controllers.sensor.senseLocationOf(r);
					int dist = controllers.myRC.getLocation().distanceSquaredTo(loc);
					if (dist > attackRange)
						continue;
					elocs.add(loc);
					if (info.hitpoints < leasthp1) {
						leasthp2 = leasthp1;
						target2 = target1;
						target1 = r;
						leasthp1 = info.hitpoints;
					} else if (info.hitpoints < leasthp2) {
						target2 = r;
						leasthp2 = info.hitpoints;
					}
				}
			} catch (GameActionException e) {
				continue;
			}
		}
		lastUpdate = Clock.getRoundNum();
	}

	public int enemyNum() {
		if (outdated())
			senseNearby();
		return enemies.size() + deadEnemies.size();
	}
	
	public boolean hasEnemy() {
		if (outdated())
			senseNearby();
		return enemies.size() > 0 || deadEnemies.size() > 0;
	}

	public boolean outdated() {
		return Clock.getRoundNum() > lastUpdate + 1;
	}

}
