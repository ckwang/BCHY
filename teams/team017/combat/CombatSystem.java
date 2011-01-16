package team017.combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import team017.util.Controllers;
import team017.util.EnemyInfo;
import team017.util.Util;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.MovementController;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.Team;
import battlecode.common.WeaponController;

public class CombatSystem {

	public class compareEnemyInfoByDistance implements Comparator<EnemyInfo> {
		public int compare(EnemyInfo o1, EnemyInfo o2) {
			if (o1.priority < o2.priority) {
				return 1;
			} else if (o1.priority > o2.priority) {
				return -1;
			} else {
				return 0;
			}
		}		
	}
	
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
//			controllers.motor.moveForward();
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
	
//	/*
//	 * return true is moved
//	 */
//	public boolean approach(Robot r) {
//		if (controllers.motor.isActive())
//			return false;
//		MapLocation cur = controllers.myRC.getLocation();
//		try {
//			MapLocation loc = controllers.sensor.senseLocationOf(r);
//			Direction dir = cur.directionTo(loc);
//			if (dir == Direction.OMNI || dir == Direction.NONE)
//				return false;
//			if (cur.isAdjacentTo(loc)) {
//				controllers.motor.setDirection(dir);
//			}
//			else if (Util.isFacing(controllers.myRC.getDirection(), dir)
//					&& controllers.motor.canMove(dir))
//				controllers.motor.moveForward();
//			else if (Util.isFacing(controllers.myRC.getDirection().opposite(),
//					dir)
//					&& controllers.motor.canMove(controllers.myRC
//							.getDirection().opposite()))
//				controllers.motor.moveBackward();
//			else {
//				controllers.motor.setDirection(dir);
//			}
//			return true;
//		} catch (GameActionException e) {
//			return false;
//		}
//	}
	
//	public MapLocation mass() {
//		Robot r;
//		MapLocation buildingLoc = null;
//		for (WeaponController w : controllers.weapons) {
//			if (w.isActive())
//				continue;
//			while (immobileEnemies.size() > 0) {
//				r = immobileEnemies.get(0);
//				try {
//					RobotInfo info = controllers.sensor.senseRobotInfo(r);
//					if (info.hitpoints <= 0) {
//						immobileEnemies.remove(0);
//						continue;
//					}
//					MapLocation loc = controllers.sensor
//					.senseLocationOf(r);
//					int dist = controllers.myRC.getLocation().distanceSquaredTo(loc);
//					if (dist > maxRange) {
//						immobileEnemies.remove(0);
//						continue;
//					}
//					w.attackSquare(loc, r.getRobotLevel());
//					if (info.hitpoints < aveDamage) {
//						immobileEnemies.remove(0);
//						buildingLoc = loc;
//						break;
//					}
//				} catch (GameActionException e) {
//					immobileEnemies.remove(0);
//					continue;
//				}
//			}
//		}
//		return buildingLoc;
//	}
	
//	public void massacre() {
////		if (immobileEnemies.size() == 0)
////			return;
//		int i = 0, dist;
//		for (WeaponController w : controllers.weapons) {
//			if (w.isActive())
//				continue;
//			try {
//				MapLocation loc = controllers.sensor.senseLocationOf(immobileEnemies.get(i));
//				dist = controllers.myRC.getLocation().distanceSquaredTo(loc);
//				if (dist > maxRange) {
//					++i;
//					if (i >= immobileEnemies.size())
//						return;
//					continue;
//				}
//				w.attackSquare(loc, immobileEnemies.get(i).getRobotLevel());
//			} catch (GameActionException e) {
//				++i;
//				if (i >= immobileEnemies.size())
//					return;
//			}
//		}
//	}
	
//	public boolean flee() throws GameActionException {
//		if (controllers.motor.isActive())
//			return false;
//		MapLocation cur = controllers.myRC.getLocation();
//		MapLocation enemyCenter = Util.aveLocation(elocs);
//		if (enemyCenter == null)
//			return true;
//		Direction edir = cur.directionTo(enemyCenter);
//		Direction mydir = controllers.myRC.getDirection();
//		if (Util.isFacing(mydir, edir)
//				&& controllers.motor.canMove(edir.opposite())) {
//			controllers.motor.moveBackward();
//			return true;
//		}
//		else if (Util.isFacing(mydir.opposite(), edir)) {
//			controllers.motor.moveForward();
//			return true;
//		}
//		else {
//			controllers.motor.setDirection(edir.opposite());
//			return false;
//		}
//	}
	
//	public void attack() {
//		Robot r;
//		for (WeaponController w : controllers.weapons) {
//			if (w.isActive())
//				continue;
//			while (enemies.size() > 0) {
//				r = enemies.get(0);
//				try {
//					RobotInfo info = controllers.sensor.senseRobotInfo(r);
//					if (info.hitpoints <= 0) {
//						enemies.remove(0);
//						continue;
//					}
//					MapLocation loc = controllers.sensor
//					.senseLocationOf(r);
//					int dist = controllers.myRC.getLocation().distanceSquaredTo(loc);
//					if (dist > maxRange) {
//						enemies.remove(0);
//						continue;
//					}
//					w.attackSquare(loc, r.getRobotLevel());
//					if (info.hitpoints < aveDamage) {
//						enemies.remove(0);
//						break;
//					}
//				} catch (GameActionException e) {
//					enemies.remove(0);
//					continue;
//				}
//			}
//		}
//	}

//	public boolean attack() {
//		if (target1 == null) {
////			destroyDeadEnemy();
//			return false;
//		}
//		boolean attacked = false;
//		for (WeaponController w : controllers.weapons) {
//			if (w.isActive())
//				continue;
//			try {
//				RobotInfo info = controllers.sensor.senseRobotInfo(target1);
//				if (info.hitpoints < 0) {
////					if (target2 == null)
////						return false;
////					target1 = target2;
////					target2 = null;
//				}
//				MapLocation weakest = controllers.sensor
//				.senseLocationOf(target1);
//				int dist = controllers.myRC.getLocation().distanceSquaredTo(weakest);
//				if (dist > maxRange) {
//					return false;
//				}
//				w.attackSquare(weakest, target1.getRobotLevel());
//				attacked = true;
//			} catch (GameActionException e) {
//				if (target2 == null)
//					return false;
//				target1 = target2;
//				target2 = null;
//			}
//		}
//		return false;
//	}

	public void senseNearby() {
//		reset();
		RobotController rc = controllers.myRC;
		int roundNum = Clock.getRoundNum();
//		int before = Clock.getBytecodeNum();
		Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
		for (Robot r : robots) {
			try {
				RobotInfo info = controllers.sensor.senseRobotInfo(r);
//				if (r.getTeam() == controllers.myRC.getTeam()) {
//					ComponentType[] components = info.components;
//					if (Util.hasWeapon(components)) {
//						allies.add(r);
//						MapLocation loc = controllers.sensor.senseLocationOf(r);
//						alocs.add(loc);
//					}
//				} else 
				if (r.getTeam() == controllers.myRC.getTeam().opponent()) {
					EnemyInfo thisEnemy = new EnemyInfo(roundNum, info);
					controllers.enemyInfosSet.remove(thisEnemy);
					controllers.enemyInfosSet.add(thisEnemy);
				} else if (r.getTeam() == Team.NEUTRAL) {
					controllers.debrisLoc.add(info.location);
				}
			} catch (GameActionException e) {
				continue;
			}
		}
//		int after = Clock.getBytecodeNum();
//		Util.sortHp(hps, enemies);
	}
	
//	public void senseNearby() {
//		reset();
//		Robot[] robots = controllers.sensor.senseNearbyGameObjects(Robot.class);
//		double leasthp1 = Chassis.HEAVY.maxHp;
//		double leasthp2 = Chassis.HEAVY.maxHp;
//		List<Double> hps = new ArrayList<Double>();
//		for (Robot r : robots) {
//			try {
//				RobotInfo info = controllers.sensor.senseRobotInfo(r);
//				if (r.getTeam() == controllers.myRC.getTeam()) {
//					ComponentType[] components = info.components;
//					if (Util.hasWeapon(components)) {
//						allies.add(r);
//						MapLocation loc = controllers.sensor.senseLocationOf(r);
//						alocs.add(loc);
//					}
//				} else if ((r.getTeam() != Team.NEUTRAL)) {
//					if (!info.on || info.chassis == Chassis.BUILDING) {
//						immobileEnemies.add(r);
//						continue;
//					}
//					MapLocation loc = controllers.sensor.senseLocationOf(r);
//					int dist = controllers.myRC.getLocation().distanceSquaredTo(loc);
//					hps.add(info.hitpoints);
//					
//					totalEnemiesHp += info.hitpoints;
//					if (info.hitpoints < leasthp1) {
//						leasthp2 = leasthp1;
//						target2 = target1;
//						target1 = r;
//						leasthp1 = info.hitpoints;
//					} else if (info.hitpoints < leasthp2) {
//						target2 = r;
//						leasthp2 = info.hitpoints;
//					} else {
//						enemies.add(r);
//						elocs.add(loc);
//					}
//					if (target1 != null) {
//						enemies.add(target1);
//						if (target2 != null)
//							enemies.add(target2);
//					}
//				}
//			} catch (GameActionException e) {
//				continue;
//			}
//		}
//	}

//	public void reset() {
//		debrisLoc.clear();
//		enemyInfosSet.clear();
//		allies.clear();
//		enemies.clear();
//		immobileEnemies.clear();
//		elocs.clear();
//		alocs.clear();
//		totalEnemiesHp = 0;
//		target1 = null;
//		target2 = null;
//	}

	public MapLocation attack() {
		try {
			RobotController rc = controllers.myRC;
			MapLocation currentLoc = rc.getLocation();
			compareEnemyInfoByDistance comparator = new compareEnemyInfoByDistance();
			boolean attacked = false;
			if (controllers.enemyInfosSet.size() == 0)
				return null;

			EnemyInfo[] enemyInfos = new EnemyInfo[controllers.enemyInfosSet.size()];
			int i = 0;
			for (EnemyInfo info : controllers.enemyInfosSet) {
				info.calculateCost(currentLoc);
				enemyInfos[i] = info;
				++i;
			}
			Arrays.sort(enemyInfos, comparator);
			int listPointer = 0;
			EnemyInfo enemy = enemyInfos[listPointer];
			
			for (WeaponController w : controllers.weapons) {
				if (!w.isActive() && w.withinRange(enemy.location)) {
					w.attackSquare(enemy.location, enemy.level);
					enemy.hp -= w.type().attackPower;
					if (enemy.hp < 0) {
						listPointer++;
						if (listPointer == enemyInfos.length) {
							break;
						} else {
							enemy = enemyInfos[listPointer];
						}
					}
					attacked = true;
				}
			}
			return attacked ? rc.getLocation() : enemy.location;
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		return null;
	}
	

	
	public void attackDebris() throws GameActionException {
		int listPointer = 0;
		MapLocation loc = controllers.debrisLoc.get(listPointer);
		
		for (WeaponController w : controllers.weapons) {
			if (!w.isActive()){
				if (w.withinRange(loc)) {
					w.attackSquare(loc, RobotLevel.ON_GROUND);
				} else {
					listPointer++;
					if (listPointer == controllers.debrisLoc.size()) {
						break;
					} else {
						loc = controllers.debrisLoc.get(listPointer);
					}
				}
			}
		}
	}
	
//	public void towerAttack() {
//		try {
//			compareEnemyInfoByDistance comparator = new compareEnemyInfoByDistance();
//			if (enemyInfosSet.size() == 0)
//				return;
//			
//			EnemyInfo[] enemyInfos = new EnemyInfo[enemyInfosSet.size()];
//			enemyInfosSet.toArray(enemyInfos);
//			Arrays.sort(enemyInfos, comparator);
//			
//			double [] attackedHp = new double [enemyInfos.length];
//			int listPointer = 0;
//			for (WeaponController w : controllers.weapons) {
//				if (!w.isActive()) {
//					if (listPointer == enemyInfos.length)
//						--listPointer;
//					EnemyInfo enemy = enemyInfos[listPointer];
//					if (w.withinRange(enemy.location)) {
//						w.attackSquare(enemy.location, enemy.level);
//						attackedHp [listPointer] += w.type().attackPower;
//						if (attackedHp [listPointer] > enemy.hp) {
//							++listPointer;
//						}
//					} else if(!controllers.motor.isActive()) {
//						controllers.motor.setDirection(controllers.myRC.getLocation().directionTo(enemy.location));
//					}
//				}
//			}
//		} catch (GameActionException e) {
//			e.printStackTrace();
//		}
//	}
}
