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
import battlecode.common.MovementController;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.SensorController;
import battlecode.common.Team;
import battlecode.common.WeaponController;

public class CombatSystem {

	// private RobotController rc.
	// private MovementController motor.;
	// private Sensorc.ntroller sensor;
	// private List<WeaponController> weapons;
	private Controllers controllers;
	private RobotController rc = controllers.myRC;
	private MovementController motor = controllers.motor;
	private SensorController sensor = controllers.sensor;

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
		if (motor.isActive())
			return false;
		MapLocation cur = rc.getLocation();
		try {
			MapLocation loc = sensor.senseLocationOf(r);
			RobotInfo info = sensor.senseRobotInfo(r);
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
					motor.setDirection(newdir);
					nextDir = dir2;
					return true;
			}
			if (dir2 == Direction.OMNI || dir2 == Direction.NONE)
				return false;
			if (rc.getDirection() != dir2) {
				motor.setDirection(dir2);
				return true;
			}
			motor.moveForward();
			return true;
		} catch (GameActionException e) {
			if (nextDir != Direction.OMNI && nextDir != Direction.NONE) {
				try {
					motor.setDirection(nextDir);
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
		if (motor.isActive())
			return false;
		MapLocation cur = rc.getLocation();
		try {
			MapLocation loc = sensor.senseLocationOf(r);
			Direction dir = cur.directionTo(loc);
			if (dir == Direction.OMNI || dir == Direction.NONE)
				return false;
			if (cur.isAdjacentTo(loc)) {
				motor.setDirection(dir);
			}
			else if (Util.isFacing(rc.getDirection(), dir)
					&& motor.canMove(dir))
				motor.moveForward();
			else if (Util.isFacing(rc.getDirection().opposite(),
					dir)
					&& motor.canMove(rc
							.getDirection().opposite()))
				motor.moveBackward();
			else {
				motor.setDirection(dir);
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
				MapLocation loc = sensor.senseLocationOf(deadEnemies.get(i));
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
			RobotInfo info = sensor.senseRobotInfo(target1);
			if (info.hitpoints < 0)
				System.out.println("hp less than 0");
		} catch (GameActionException e1) {}
		
		for (WeaponController w : controllers.weapons) {
			if (w.isActive())
				continue;
			try {
				MapLocation weakest = sensor
						.senseLocationOf(target1);
				int dist = rc.getLocation().distanceSquaredTo(weakest);
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
		
		Robot[] robots = sensor.senseNearbyGameObjects(Robot.class);
		double leasthp1 = Chassis.HEAVY.maxHp;
		double leasthp2 = Chassis.HEAVY.maxHp;
		for (Robot r : robots) {
			try {
				RobotInfo info = sensor.senseRobotInfo(r);
				if (r.getTeam() == rc.getTeam()) {
					allies.add(r);
					MapLocation loc = sensor.senseLocationOf(r);
					alocs.add(loc);
				} else if ((r.getTeam() != Team.NEUTRAL)) {
					if (!info.on || info.chassis == Chassis.BUILDING) {
//						System.out.println(r.getTeam());
						deadEnemies.add(r);
						continue;
					}
					enemies.add(r);
					MapLocation loc = sensor.senseLocationOf(r);
					int dist = rc.getLocation().distanceSquaredTo(loc);
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
