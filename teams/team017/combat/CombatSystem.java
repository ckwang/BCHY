package team017.combat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

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
			MapLocation currentLoc = controllers.myRC.getLocation();
			Direction dir = controllers.myRC.getDirection();
			Direction dir1 = currentLoc.directionTo(o1.location);
			Direction dir2 = currentLoc.directionTo(o2.location);
			
			int dist1 = currentLoc.distanceSquaredTo(o1.location);
			int dist2 = currentLoc.distanceSquaredTo(o2.location);
			
			// Panelty distance by 2 if not within angle
			if (dir1 != dir && dir1 != dir.rotateLeft() && dir1 != dir.rotateRight())
				dist1 += 2;
			
			if (dir2 != dir && dir2 != dir.rotateLeft() && dir2 != dir.rotateRight())
				dist2 += 2;
			
			
			// Compare distance
			if (dist1 > dist2) {
				return 1;
			} else if (o1.mobile != o2.mobile) {
				if (o1.mobile) {
					return -1;
				}
				return 1;
			// Attack air units first
			} else if (o1.level != o2.level) {
				if (o1.level == RobotLevel.IN_AIR) {
					return -1;
				}
				return 1;
			// Attack the enemy with less hitpoints	
			} else if (dist2 == dist1 && o1.hp > o2.hp) {
				return 1;
			} else {
				return -1;
			}	
		}		
	}
	
	private Controllers controllers;

	public List<Robot> allies = new LinkedList<Robot>(); // exclude self
	public List<Robot> enemies = new LinkedList<Robot>(); // exclude off robot
	public List<Robot> immobileEnemies = new LinkedList<Robot>();
	public List<MapLocation> elocs = new ArrayList<MapLocation>();
	public List<MapLocation> alocs = new ArrayList<MapLocation>();
	
	public List <EnemyInfo> enemyInfos = new ArrayList <EnemyInfo>();
	public List <EnemyInfo> enemyInfosInbox = new ArrayList <EnemyInfo>();
	
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
//		if (target1 != null)
//			return chase(target1);
//		else
//			return false;
		if (enemies.size() > 0)
			return chase(enemies.get(0));
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
	
	public MapLocation mass() {
		Robot r;
		MapLocation buildingLoc = null;
		for (WeaponController w : controllers.weapons) {
			if (w.isActive())
				continue;
			while (immobileEnemies.size() > 0) {
				r = immobileEnemies.get(0);
				try {
					RobotInfo info = controllers.sensor.senseRobotInfo(r);
					if (info.hitpoints <= 0) {
						immobileEnemies.remove(0);
						continue;
					}
					MapLocation loc = controllers.sensor
					.senseLocationOf(r);
					int dist = controllers.myRC.getLocation().distanceSquaredTo(loc);
					if (dist > maxRange) {
						immobileEnemies.remove(0);
						continue;
					}
					w.attackSquare(loc, r.getRobotLevel());
					if (info.hitpoints < aveDamage) {
						immobileEnemies.remove(0);
						buildingLoc = loc;
						break;
					}
				} catch (GameActionException e) {
					immobileEnemies.remove(0);
					continue;
				}
			}
		}
		return buildingLoc;
	}
	
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
//<<<<<<< HEAD
//=======
////				if (info.hitpoints > controllers.myRC.getHitpoints())
////					return true;
//				MapLocation weakest = controllers.sensor
//				.senseLocationOf(target1);
//				int dist = controllers.myRC.getLocation().distanceSquaredTo(weakest);
////				if (dist > maxRange) {
////					return false;
////					if (target2 == null)
////						return false;
////					target1 = target2;
////					target2 = null;
////				}
//				w.attackSquare(weakest, target1.getRobotLevel());
//				attacked = true;
//			} catch (GameActionException e) {
//				if (target2 == null)
//					return false;
//				target1 = target2;
//				target2 = null;
//>>>>>>> refs/heads/Tower
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
				if ((r.getTeam() != controllers.myRC.getTeam() && r.getTeam() != Team.NEUTRAL)) {
					EnemyInfo thisEnemy = new EnemyInfo (r.getID(), info.hitpoints, info.location, r.getRobotLevel(), info.on || info.chassis == Chassis.BUILDING);
					enemyInfos.add(thisEnemy);
				}
				
				int inboxSize = enemyInfosInbox.size();
				
				for (int i = inboxSize - 1; i > -1; --i) {
					if (!enemyInfos.contains(enemyInfosInbox.get(i))) {
						enemyInfos.add(enemyInfosInbox.get(i));
					}
				}
					
			String s = "";
			for (int i = 0; i < enemyInfos.size(); ++i)
				s += enemyInfos.get(i).location + " ";
			controllers.myRC.setIndicatorString (2,"Updated:" + s);

			} catch (GameActionException e) {
				continue;
			}
		}

		
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

	public void reset() {
		enemyInfos.clear();
		enemyInfosInbox.clear();
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
//		if (outdated())
//			senseNearby();
		return enemies.size();
	}
	
	public int immobileEnemyNum() {
//		if (outdated())
//			senseNearby();
		return immobileEnemies.size();
	}
	
	public boolean hasEnemy() {
//		if (outdated())
//			senseNearby();
		return (enemies.size() + immobileEnemies.size()) > 0;
	}

	public void print(Object o) {
		System.out.println(o.toString());
	}
	
	public boolean outdated() {
		return Clock.getRoundNum() > lastUpdate + 1;
	}
	
	public MapLocation attack() {
		try {
			RobotController rc = controllers.myRC;
			MovementController motor = controllers.motor;
			compareEnemyInfoByDistance comparator = new compareEnemyInfoByDistance();
			
			boolean attacked = false;
			
			if (enemyInfos.size() == 0)
				return null;
			Collections.sort(enemyInfos, comparator);
			int listPointer = 0;
			for (WeaponController w : controllers.weapons) {
				if (!w.isActive()) {
					if (listPointer == enemyInfos.size())
						--listPointer;
					EnemyInfo enemy = enemyInfos.get(listPointer);
					if (w.withinRange(enemy.location)) {
						w.attackSquare(enemy.location, enemy.level);
						enemy.hp -= w.type().attackPower;
						if (enemy.hp < 0) {
							++listPointer;
						}
						attacked = true;
					} else if(!motor.isActive()) {
						return enemy.location;
						
//						Direction currentDir = rc.getDirection();
//						MapLocation currentLoc = rc.getLocation();
//						if (currentDir == currentLoc.directionTo(enemy.location)) {
//							motor.moveForward();
//						} else {
//							motor.setDirection(rc.getLocation().directionTo(enemy.location));
//						}
					}
				}
			}
			
			if (attacked) {
				if (!motor.isActive() && controllers.motor.canMove(rc.getDirection().opposite()))
					return rc.getLocation();
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void towerAttack() {
		try {
			compareEnemyInfoByDistance comparator = new compareEnemyInfoByDistance();
			if (enemyInfos.size() == 0)
				return;
			Collections.sort(enemyInfos, comparator);
			double [] attackedHp = new double [enemyInfos.size()];
			int listPointer = 0;
			for (WeaponController w : controllers.weapons) {
				if (!w.isActive()) {
					if (listPointer == enemyInfos.size())
						--listPointer;
					EnemyInfo enemy = enemyInfos.get(listPointer);
					if (w.withinRange(enemy.location)) {
						w.attackSquare(enemy.location, enemy.level);
						attackedHp [listPointer] += w.type().attackPower;
						if (attackedHp [listPointer] > enemy.hp) {
							++listPointer;
						}
					} else if(!controllers.motor.isActive()) {
						controllers.motor.setDirection(controllers.myRC.getLocation().directionTo(enemy.location));
					}
				}
			}
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}
}
