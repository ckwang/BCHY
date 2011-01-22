package team017.combat;

import java.util.ArrayList;
import java.util.List;

import team017.util.Controllers;
import team017.util.Util;
import battlecode.common.Clock;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.TerrainTile;
import battlecode.common.WeaponController;

//Assume has all weapons at the start
public class CombatSystem {

	private Controllers controllers;
	public WeaponController primary = null;

	int lastUpdate = -1;
	public MapLocation myloc;
	public Direction mydir;
	
	public MapLocation[] enemyback = new MapLocation[12];
	
	public int minRange = 100;
	public int maxRange = 0;
	public int optRange;
	
	public Direction nextDir = Direction.NONE;
//	RobotInfo lastAttacked = null;
//	boolean killed;
	
	public CombatSystem(Controllers c) {
		controllers = c;
		myloc = controllers.myRC.getLocation();
		mydir = controllers.myRC.getDirection();
		for (WeaponController w: controllers.weapons) {
			ComponentType type = w.type();
			if (type.range > maxRange)
				maxRange = type.range;
			else if (type.range < minRange)
				minRange = type.range;
		}
		if (c.weapons.size() > 0) {
			primary = controllers.weapons.get(0);
			optRange = primary.type().range;
		}
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

	public void heal() {
		try {
			WeaponController medic = controllers.medic;
			if (medic == null || medic.isActive())
				return;
			List<RobotInfo> allies = new ArrayList<RobotInfo>();
			RobotInfo myinfo = controllers.sensor.senseRobotInfo(controllers.myRC
					.getRobot());
			if (myinfo.hitpoints < myinfo.maxHp)
				allies.add(myinfo);
			for (RobotInfo r: controllers.allyMobile) {
				if (r.hitpoints < r.maxHp && medic.withinRange(r.location))
					allies.add(r);
			}
			for (RobotInfo r: controllers.allyImmobile) {
				if (r.hitpoints < r.maxHp && medic.withinRange(r.location))
					allies.add(r);
			}
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
		for (WeaponController w: controllers.weapons) {
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
	
	public boolean trackTarget(RobotInfo r) {
		if (controllers.jumper != null) {
			MapLocation tel = teleport(r);
			if (tel != null) {
				Direction edir = tel.directionTo(r.location);
				setDirection(edir);
				return true;
			}
		}
		MapLocation myloc = controllers.myRC.getLocation();
		Direction mydir = controllers.myRC.getDirection();
		Direction edir = myloc.directionTo(r.location);
		if (edir == Direction.OMNI || edir == Direction.NONE) {
			System.out.println("attack self");
		}
		int dis = myloc.distanceSquaredTo(r.location);
		if (dis > 0.9 * optRange) {
			moveForward();
			return false;
		}
		//enemy facing, retreat back
		if (Util.isFacing(r.direction, edir.opposite())) {
//			if (controllers.myRC.getHitpoints() < 3.5)
				moveBackward();
				return true;
		}
		//behind enemy, chase forward
		else if (Util.isFacing(r.direction, edir)) {
			if (Util.isFacing(mydir, edir) && dis > 0.85*minRange) {
				moveForward();
			}
			return false;
		}
		else {
			if (mydir != edir) {
				setDirection(edir);
				return true;
			}
		}
		return false;
		
	}
	
	//return true if need yield
	public boolean approachTarget(RobotInfo r) {
//		updatePosition();
		if (controllers.jumper != null) {
			MapLocation tel = teleport(r);
			if (tel != null) {
				Direction edir = tel.directionTo(r.location);
				setDirection(edir);
				return true;
			}
		}
		if (controllers.motor.isActive())
			return true;
		MapLocation myloc = controllers.myRC.getLocation();
		Direction mydir = controllers.myRC.getDirection();
		Direction edir = myloc.directionTo(r.location);
		if (mydir == edir && controllers.motor.canMove(mydir)) {
			try {controllers.motor.moveForward();} 
			catch (Exception e) {return false;}
		}
		if (myloc.distanceSquaredTo(r.location) >= maxRange) {
			if (mydir == edir.rotateLeft() || mydir == edir.rotateRight()) {
				try {controllers.motor.moveForward();} 
				catch (Exception e) {return false;}
			}
		}
		else {
			try {controllers.motor.setDirection(edir);} 
			catch (GameActionException e) {return false;}
		}
		return true;
	}
	
	public RobotInfo getImmobile() {
		for (RobotInfo r: controllers.enemyImmobile) {
			if (primary.withinRange(r.location)) {
				return r;
			}
		}
		int d;
		for (RobotInfo r: controllers.enemyImmobile) {
			d = controllers.myRC.getLocation().distanceSquaredTo(r.location);
			if (d < maxRange) {
				return r;
			}
		}
		return null;
	}
	
	public RobotInfo getMobile() {
		Util.sortHp(controllers.enemyMobile);
		for (RobotInfo r: controllers.enemyMobile) {
			if (primary.withinRange(r.location)) {
				return r;
			}
		}
		int dis = maxRange;
		RobotInfo target = null;
		for (RobotInfo r: controllers.enemyMobile) {
			int d = myloc.distanceSquaredTo(r.location);
			if (d <= dis) {
				target = r;
				dis = d;
				break;
			}
		}
		if (target != null)
			return target;
		return controllers.enemyMobile.get(0);
	}

	public MapLocation teleport(RobotInfo r) {
		if (controllers.jumper.isActive())
			return null;
		
		enemyback[0] = r.location.subtract(r.direction);
		enemyback[1] = r.location.add(r.direction.opposite(), 2);
		enemyback[2] = r.location.subtract(r.direction.rotateLeft());
		enemyback[3] = r.location.subtract(r.direction.rotateRight());
		enemyback[4] = r.location.add(r.direction.rotateLeft().rotateLeft());
		enemyback[5] = r.location.add(r.direction.rotateRight().rotateRight());
		enemyback[6] = r.location.add(r.direction.rotateLeft().rotateLeft(), 2);
		enemyback[7] = r.location.add(r.direction.rotateRight().rotateRight(), 2);
		enemyback[8] = r.location.add(r.direction.rotateLeft());
		enemyback[9] = r.location.add(r.direction.rotateRight());
		enemyback[10] = r.location.add(r.direction);
		enemyback[11] = r.location.add(r.direction, 2);
		
		for (MapLocation loc: enemyback) {
			if (canJump(loc)) {
				try {
					controllers.jumper.jump(loc);
					return loc;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}
		return null;
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
	
	public boolean setDirection(Direction dir) {
		updatePosition();
		if (controllers.myRC.getDirection() == dir)
			return true;
		if (!controllers.motor.isActive()) {
			try {
				controllers.motor.setDirection(dir);
				return true;
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean canJump(MapLocation loc){
		if (!controllers.sensor.canSenseSquare(loc))
			return false;
		TerrainTile tile = controllers.myRC.senseTerrainTile(loc);
		if ((tile != null) && (tile != TerrainTile.LAND)) {
			return false;
		}
		try {
			GameObject o = controllers.sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND);
			if (o == null)
				return true;
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
