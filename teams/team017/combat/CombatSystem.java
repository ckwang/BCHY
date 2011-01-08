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

	// private RobotController controllers.myRC.
	// private MovementController controllers.motor.;
	// private Sensocontrollers.myRC.ntroller controllers.sensor;
	// private List<WeaponController> weapons;
	private Controllers controllers;

	public boolean aggressive = true;

	private List<Robot> enemies = new ArrayList<Robot>(); // exclude off robot
	private List<Robot> allies = new ArrayList<Robot>(); // exclude self
	private List<MapLocation> elocs = new ArrayList<MapLocation>();
	private List<MapLocation> alocs = new ArrayList<MapLocation>();

	private Robot target1 = null;
	private Robot target2 = null;
	private int lastUpdate = -10;

	public CombatSystem(Controllers c) {
		controllers = c;
	}

	public boolean approachTarget() {
		if (target1 != null)
			return approach(target1);
		else
			return false;
	};

	/*
	 * return true is moved
	 */
	public boolean approach(Robot r) {
		if (controllers.motor == null || controllers.motor.isActive())
			return false;
		MapLocation cur = controllers.myRC.getLocation();
		try {
			MapLocation loc = controllers.sensor.senseLocationOf(r);
			if (cur.isAdjacentTo(loc))
				return false;
			Direction dir = cur.directionTo(loc);
			if (dir == Direction.OMNI || dir == Direction.NONE)
				return false;
			if (Util.isFacing(controllers.myRC.getDirection(), dir)
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

	public boolean attack() {
		// if (Clock.getRoundNum() >= lastUpdate +
		// ComponentType.SMALL_MOTOR.delay) {
		// if (GameConstants.BYTECODE_LIMIT_BASE - Clock.getBytecodeNum() <
		// 1400)
		// controllers.myRC.yield();
		// senseNearby();
		// }
		// if (GameConstants.BYTECODE_LIMIT_BASE - Clock.getBytecodeNum() <
		// 1000)
		// controllers.myRC.yield();

		// if (enemies.size() == 0) {
		// if (target1 != null)
		// System.out.println("target not equal null");
		// return false;
		// }
		controllers.myRC.yield();
		boolean attacked = false;
		if (target1 == null) {
			if (enemies.size() != 0)
				System.out.println("size of enemies not 0");
			return false;
		}

		for (WeaponController w : controllers.weapons) {
			if (w.isActive())
				continue;
			try {
				MapLocation weakest = controllers.sensor
						.senseLocationOf(target1);
				if (weakest == null)
					return attacked;
				w.attackSquare(weakest, target1.getRobotLevel());
				attacked = true;
			} catch (GameActionException e) {
				if (target2 == null)
					return attacked;
				target1 = target2;
				target2 = null;
			}
		}
		return attacked;
	}

	private void senseNearby() {
		enemies.clear();
		allies.clear();
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
				} else if (r.getTeam() != Team.NEUTRAL && !info.on) {
					enemies.add(r);
					MapLocation loc = controllers.sensor.senseLocationOf(r);
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
		if (outdated()) {
			senseNearby();
		}
		return enemies.size();
	}

	public int allyNum() {
		if (outdated())
			senseNearby();
		return allies.size() + 1; // add self
	}

	public boolean outdated() {
		return Clock.getRoundNum() > lastUpdate + 1;
	}

	public void addWeapon(WeaponController w) {
		controllers.weapons.add(w);
	}
}
