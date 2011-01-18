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
import battlecode.common.RobotLevel;
import battlecode.common.WeaponController;

public class CombatSystem {

	private Controllers controllers;
	public WeaponController w = null;

	int lastUpdate = -1;
	public MapLocation myloc;
	public Direction mydir;
	
	public Direction nextDir = Direction.NONE;
	RobotInfo lastAttacked = null;
	boolean killed;
	
	public CombatSystem(Controllers c) {
		controllers = c;
		myloc = controllers.myRC.getLocation();
		mydir = controllers.myRC.getDirection();
		if (c.weapons.size() > 0)
			w = controllers.weapons.get(0);
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
			// controllers.motor.moveForward();
			return true;
		} catch (GameActionException e) {
			if (nextDir != Direction.OMNI && nextDir != Direction.NONE) {
				try {
					controllers.motor.setDirection(nextDir);
					return true;
				} catch (GameActionException e1) {
				}
			}
			return false;
		}
	}

	// /*
	// * return true is moved
	// */
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
			} else if (Util.isFacing(controllers.myRC.getDirection(), dir)
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

	// public MapLocation mass() {
	// Robot r;
	// MapLocation buildingLoc = null;
	// for (WeaponController w : controllers.weapons) {
	// if (w.isActive())
	// continue;
	// while (immobileEnemies.size() > 0) {
	// r = immobileEnemies.get(0);
	// try {
	// RobotInfo info = controllers.sensor.senseRobotInfo(r);
	// if (info.hitpoints <= 0) {
	// immobileEnemies.remove(0);
	// continue;
	// }
	// MapLocation loc = controllers.sensor
	// .senseLocationOf(r);
	// int dist = controllers.myRC.getLocation().distanceSquaredTo(loc);
	// if (dist > maxRange) {
	// immobileEnemies.remove(0);
	// continue;
	// }
	// w.attackSquare(loc, r.getRobotLevel());
	// if (info.hitpoints < aveDamage) {
	// immobileEnemies.remove(0);
	// buildingLoc = loc;
	// break;
	// }
	// } catch (GameActionException e) {
	// immobileEnemies.remove(0);
	// continue;
	// }
	// }
	// }
	// return buildingLoc;
	// }

	// public void massacre() {
	// // if (immobileEnemies.size() == 0)
	// // return;
	// int i = 0, dist;
	// for (WeaponController w : controllers.weapons) {
	// if (w.isActive())
	// continue;
	// try {
	// MapLocation loc =
	// controllers.sensor.senseLocationOf(immobileEnemies.get(i));
	// dist = controllers.myRC.getLocation().distanceSquaredTo(loc);
	// if (dist > maxRange) {
	// ++i;
	// if (i >= immobileEnemies.size())
	// return;
	// continue;
	// }
	// w.attackSquare(loc, immobileEnemies.get(i).getRobotLevel());
	// } catch (GameActionException e) {
	// ++i;
	// if (i >= immobileEnemies.size())
	// return;
	// }
	// }
	// }

	/*
	 * return true of moved, false if just set direction or motor active assume
	 * enemy in front (sensor range 180 degree)
	 */
	public boolean flee(Direction edir) throws GameActionException {
		if (controllers.motor.isActive())
			return false;
		Direction mydir = controllers.myRC.getDirection();
		if (Util.isFacing(mydir, edir)
				&& controllers.motor.canMove(mydir.opposite())) {
			controllers.motor.moveBackward();
			return true;
		}
		// else if (Util.isFacing(mydir.opposite(), edir) &&
		// controllers.motor.canMove(mydir)) {
		// controllers.motor.moveForward();
		// return true;
		// }
		else {
			Direction toTurn = null;
			Direction left = edir.rotateLeft();
			Direction right = edir.rotateRight();
			if (controllers.motor.canMove(left.opposite()))
				toTurn = left;
			else if (controllers.motor.canMove(right.opposite()))
				toTurn = right;
			else if (controllers.motor.canMove(right.rotateRight().opposite()))
				toTurn = right.rotateRight();
			else if (controllers.motor.canMove(left.rotateLeft().opposite()))
				toTurn = left.rotateLeft();
			if (toTurn != null)
				controllers.motor.setDirection(toTurn);
			return false;
		}
	}

	// public void attack() {
	// Robot r;
	// for (WeaponController w : controllers.weapons) {
	// if (w.isActive())
	// continue;
	// while (enemies.size() > 0) {
	// r = enemies.get(0);
	// try {
	// RobotInfo info = controllers.sensor.senseRobotInfo(r);
	// if (info.hitpoints <= 0) {
	// enemies.remove(0);
	// continue;
	// }
	// MapLocation loc = controllers.sensor
	// .senseLocationOf(r);
	// int dist = controllers.myRC.getLocation().distanceSquaredTo(loc);
	// if (dist > maxRange) {
	// enemies.remove(0);
	// continue;
	// }
	// w.attackSquare(loc, r.getRobotLevel());
	// if (info.hitpoints < aveDamage) {
	// enemies.remove(0);
	// break;
	// }
	// } catch (GameActionException e) {
	// enemies.remove(0);
	// continue;
	// }
	// }
	// }
	// }

	// public boolean attack() {
	// if (target1 == null) {
	// // destroyDeadEnemy();
	// return false;
	// }
	// boolean attacked = false;
	// for (WeaponController w : controllers.weapons) {
	// if (w.isActive())
	// continue;
	// try {
	// RobotInfo info = controllers.sensor.senseRobotInfo(target1);
	// if (info.hitpoints < 0) {
	// // if (target2 == null)
	// // return false;
	// // target1 = target2;
	// // target2 = null;
	// }
	// MapLocation weakest = controllers.sensor
	// .senseLocationOf(target1);
	// int dist = controllers.myRC.getLocation().distanceSquaredTo(weakest);
	// if (dist > maxRange) {
	// return false;
	// }
	// w.attackSquare(weakest, target1.getRobotLevel());
	// attacked = true;
	// } catch (GameActionException e) {
	// if (target2 == null)
	// return false;
	// target1 = target2;
	// target2 = null;
	// }
	// }
	// return false;
	// }

	// public void senseNearby() {
	// reset();
	// Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
	// double leasthp1 = Chassis.HEAVY.maxHp;
	// double leasthp2 = Chassis.HEAVY.maxHp;
	// List<Double> hps = new ArrayList<Double>();
	// for (Robot r : robots) {
	// try {
	// RobotInfo info = controllers.sensor.senseRobotInfo(r);
	// if (r.getTeam() == controllers.myRC.getTeam()) {
	// ComponentType[] components = info.components;
	// if (Util.hasWeapon(components)) {
	// allies.add(r);
	// MapLocation loc = controllers.sensor.senseLocationOf(r);
	// alocs.add(loc);
	// }
	// } else if ((r.getTeam() != Team.NEUTRAL)) {
	// if (!info.on || info.chassis == Chassis.BUILDING) {
	// immobileEnemies.add(r);
	// continue;
	// }
	// MapLocation loc = controllers.sensor.senseLocationOf(r);
	// int dist = controllers.myRC.getLocation().distanceSquaredTo(loc);
	// hps.add(info.hitpoints);
	//					
	// totalEnemiesHp += info.hitpoints;
	// if (info.hitpoints < leasthp1) {
	// leasthp2 = leasthp1;
	// target2 = target1;
	// target1 = r;
	// leasthp1 = info.hitpoints;
	// } else if (info.hitpoints < leasthp2) {
	// target2 = r;
	// leasthp2 = info.hitpoints;
	// } else {
	// enemies.add(r);
	// elocs.add(loc);
	// }
	// if (target1 != null) {
	// enemies.add(target1);
	// if (target2 != null)
	// enemies.add(target2);
	// }
	// }
	// } catch (GameActionException e) {
	// continue;
	// }
	// }
	// }

	// public void reset() {
	// debrisLoc.clear();
	// enemyInfosSet.clear();
	// allies.clear();
	// enemies.clear();
	// immobileEnemies.clear();
	// elocs.clear();
	// alocs.clear();
	// totalEnemiesHp = 0;
	// target1 = null;
	// target2 = null;
	// }

	public void heal() {
		try {
			WeaponController medic = controllers.medic;
			if (medic == null || medic.isActive())
				return;
			List<RobotInfo> allies = new ArrayList<RobotInfo>();
			allies.add(controllers.sensor.senseRobotInfo(controllers.myRC
					.getRobot()));
			allies.addAll(controllers.allyMobile);
			allies.addAll(controllers.allyImmobile);
			Util.sortHp(allies);
			for (RobotInfo r : allies) {
				if (medic.withinRange(r.location)) {
					medic.attackSquare(r.location, r.robot.getRobotLevel());
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean shoot(RobotInfo r) {
		double hp = r.hitpoints;
		for (WeaponController w : controllers.weapons) {
			if (!w.isActive() && w.withinRange(r.location)) {
				try {
					w.attackSquare(r.location, r.robot.getRobotLevel());
					if (hp <= w.type().attackPower)
						return true;
					hp -= w.type().attackPower;
				} catch (Exception e) {
					e.printStackTrace();
					
				}
			}
		}
		return false;
	}
	
	//return to move forward
	public boolean attackTarget(RobotInfo r) {
		if (shoot(r))
			return true;
		updatePosition();
		Direction edir = myloc.directionTo(r.location);
		if (edir == Direction.OMNI || edir == Direction.NONE) {
			System.out.println("attack self");
		}
		int dis = myloc.distanceSquaredTo(r.location);
		if (dis >= w.type().range*0.9 && Util.isFacing(mydir, edir)) {
			if (controllers.myRC.getHitpoints() < 3.5)
				moveForward();
		}
		//enemy facing, retreat back
		if (dis < w.type().range*0.9 && Util.isFacing(mydir, edir.opposite())) {
			if (controllers.myRC.getHitpoints() < 3.5)
				moveBackward();
		}
//		if (Util.isFacing(r.direction, edir.opposite())) {
//			moveBackward();
//			return false;
//		}
		//behind enemy, chase forward
		if (Util.isFacing(r.direction, edir)) {
			if (dis > w.type().range*0.6 && mydir == edir) {
				moveForward();
			}
			return false;
		}
		else {
			try {
				if (!controllers.motor.isActive())
					controllers.motor.setDirection(edir);
			} catch (GameActionException e) {
				e.printStackTrace();
			}
			return false;
		}
	}
	
	public boolean canAttack(RobotInfo r) {
//		WeaponController w;
//		if (controllers.weapons.size() > 0)
//			w = controllers.weapons.get(0);
//		else
//			return null;
		updatePosition();
		Direction edir = myloc.directionTo(r.location);
		if (w.withinRange(r.location))
			return true;
		if (mydir == edir) {
			moveForward();
		}
		else if (!controllers.motor.isActive()) {
			try {controllers.motor.setDirection(edir);} 
			catch (GameActionException e) {}
		}
		return false;
	}
	
	public RobotInfo getImmobile() {
		RobotInfo target = null;
//		WeaponController w;
//		if (controllers.weapons.size() > 0)
//			w = controllers.weapons.get(0);
//		else
//			return null;
		for (RobotInfo r: controllers.enemyImmobile) {
			if (w.withinRange(r.location)) {
				target = r;
				break;
			}
		}
		if (target == null) {
			for (RobotInfo r: controllers.enemyImmobile) {
				int d = myloc.distanceSquaredTo(r.location);
				if (d < w.type().range) {
					target = r;
					break;
				}
			}
		}
		return target;
	}
	
	public RobotInfo getTarget() {
		RobotInfo target = null;
//		WeaponController w;
//		if (controllers.weapons.size() > 0)
//			w = controllers.weapons.get(0);
//		else
//			return null;
		for (RobotInfo r: controllers.enemyMobile) {
			if (w.withinRange(r.location)) {
				target = r;
				break;
			}
		}
		if (target == null) {
			for (RobotInfo r: controllers.enemyMobile) {
				int d = myloc.distanceSquaredTo(r.location);
				if (d < w.type().range) {
					target = r;
					break;
				}
			}
		}
		return target;
	}

//	public MapLocation attack() {
//		try {
//			RobotController rc = controllers.myRC;
//			MapLocation currentLoc = rc.getLocation();
//			boolean attacked = false;
//
//			List<RobotInfo> enemies = new LinkedList<RobotInfo>();
//			Util.sortHp(controllers.enemyMobile);
//			enemies.addAll(controllers.enemyMobile);
//			enemies.addAll(controllers.enemyImmobile);
//
//			if (enemies.size() == 0)
//				return null;
//
//			int listPointer = 0;
//			RobotInfo enemy = enemies.get(0);
//			double hp = enemy.hitpoints;
//			for (WeaponController w : controllers.weapons) {
//				if (!w.isActive() && w.withinRange(enemy.location)) {
//					w.attackSquare(enemy.location, enemy.robot.getRobotLevel());
//					if (hp <= w.type().attackPower) {
//						listPointer++;
//						if (listPointer == enemies.size()) {
//							break;
//						} else {
//							enemy = enemies.get(listPointer);
//							hp = enemy.hitpoints;
//						}
//					}
//					hp -= w.type().attackPower;
//					attacked = true;
//				}
//			}
//			if (attacked) {
//				if (enemy.direction == null) {
//					return enemy.location;
//				} else if (Util.isFacing(enemy.direction, rc.getDirection()
//						.opposite())) {
//					return currentLoc;
//				}
//			} else {
//				return enemy.location;
//			}
//			return attacked ? rc.getLocation() : enemy.location;
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

	public void attackDebris() throws GameActionException {
		int listPointer = 0;
		MapLocation loc = controllers.debris.get(listPointer).location;

		for (WeaponController w : controllers.weapons) {
			if (!w.isActive()) {
				if (w.withinRange(loc)) {
					w.attackSquare(loc, RobotLevel.ON_GROUND);
				} else {
					listPointer++;
					if (listPointer == controllers.debris.size()) {
						break;
					} else {
						loc = controllers.debris.get(listPointer).location;
					}
				}
			}
		}
	}
	
	public boolean moveForward() {
		updatePosition();
		if (!controllers.motor.isActive() && controllers.motor.canMove(mydir)) {
			try {
				controllers.motor.moveForward();
				return true;
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean moveBackward() {
		updatePosition();
		if (!controllers.motor.isActive() && controllers.motor.canMove(mydir.opposite())) {
			try {
				controllers.motor.moveBackward();
				return true;
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public void updatePosition() {
		if (lastUpdate < Clock.getRoundNum()) {
			mydir = controllers.myRC.getDirection();
			myloc = controllers.myRC.getLocation();
			lastUpdate = Clock.getRoundNum();
		}
	}

}
