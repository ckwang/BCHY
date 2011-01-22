package team017.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import battlecode.common.BroadcastController;
import battlecode.common.BuilderController;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentClass;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.JumpController;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.MovementController;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;
import battlecode.common.WeaponController;

public class Controllers {
	
	public RobotController myRC = null;
	public MovementController motor = null;
	public BuilderController builder = null;
	public SensorController sensor = null;
	public BroadcastController comm = null;
	public List<WeaponController> weapons = null;
	public WeaponController medic = null;
	public JumpController jumper = null;
	
	public List<RobotInfo> allyMobile = new LinkedList<RobotInfo>();
	public List<RobotInfo> allyImmobile = new LinkedList<RobotInfo>();
	public List<RobotInfo> enemyMobile = new LinkedList<RobotInfo>();
	public List<RobotInfo> enemyImmobile = new LinkedList<RobotInfo>();
	public List<RobotInfo> debris = new LinkedList<RobotInfo>();
	public List<MapLocation> emptyMines = new ArrayList<MapLocation>();
	public List<MapLocation> allyMines = new ArrayList<MapLocation>();
	public List<MapLocation> enemyMines = new ArrayList<MapLocation>();
	
//	public Direction dir;
//	public MapLocation loc;
//	public double hp;
	public final Robot self;
	public final Chassis chassis;
	public final double maxhp;
	
	public int lastUpdateInfo = -1;
	public int lastUpdateRobot = -1;
	public int lastUpdateMine = -1;
	
	public Controllers(RobotController rc) {
		weapons = new ArrayList<WeaponController>();
		myRC = rc;
		self = myRC.getRobot();
		chassis = myRC.getChassis();
		maxhp = myRC.getHitpoints();
	}
	
	public void reset(boolean mine) {
		allyMobile.clear();
		allyImmobile.clear();
		enemyMobile.clear();
		enemyImmobile.clear();
		debris.clear();
		if (mine) {
			emptyMines.clear();
			allyMines.clear();
			enemyMines.clear();
		}
	}
	
	public int enemyNum() {
//		if (lastUpdateRobot < Clock.getRoundNum())
//			senseAll();
		return enemyMobile.size() + enemyImmobile.size();
	}
	
	public int debrisNum() {
		if (lastUpdateRobot < Clock.getRoundNum())
			senseAll();
		return debris.size();
	}
	
	public int mobileEnemyNum() {
		if (lastUpdateRobot < Clock.getRoundNum())
			senseAll();
		return enemyMobile.size();
	}
	
	public int immobileEnemyNum() {
		if (lastUpdateRobot < Clock.getRoundNum())
			senseAll();
		return enemyImmobile.size();
	}
	
	public void scoutNearby() {
		reset(true);
		RobotInfo rinfo;
		MapLocation loc, myloc = myRC.getLocation();
		GameObject[] objects = sensor.senseNearbyGameObjects(GameObject.class);
		for (GameObject o: objects) {
			if (o instanceof Mine) {
				loc = ((Mine)o).getLocation();
				GameObject object;
				try {
					object = sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND);
					if (object == null || sensor.senseRobotInfo((Robot) object).chassis != Chassis.BUILDING) {
						emptyMines.add(loc);
					} else {
						if (object.getTeam() == myRC.getTeam()) {
							allyMines.add(loc);
						} else {
							enemyMines.add(loc);
						}
					}
				} catch (GameActionException e) {
					e.printStackTrace();
				}
				continue;
			}
			try {
				rinfo = sensor.senseRobotInfo((Robot)o);
				if (o.getTeam() == myRC.getTeam().opponent()) {
					boolean mobile = rinfo.on && rinfo.chassis != Chassis.BUILDING && rinfo.chassis != Chassis.DUMMY;
					int d = myloc.distanceSquaredTo(rinfo.location);
					if (d > ComponentType.SMG.range + 49)
						continue;
					ComponentType[] coms = rinfo.components;
					for (ComponentType c: coms) {
						if (c.componentClass == ComponentClass.WEAPON) {
							if (mobile) {
								enemyMobile.add(rinfo);
							} else {
								enemyImmobile.add(rinfo);
							}
							continue;
						}
					}
				}
			} catch (GameActionException e) {
			}
		}
	}
	
	public void senseMine() {
		if (sensor == null)
			return;
		int roundNum = Clock.getRoundNum();
		if (roundNum == lastUpdateMine)
			return;
		emptyMines.clear();
		allyMines.clear();
		enemyMines.clear();
		Mine[] minearray = sensor.senseNearbyGameObjects(Mine.class);
		for (Mine m: minearray) {
			MapLocation loc = m.getLocation();
			GameObject object;
			try {
				object = sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND);
				if (object == null || sensor.senseRobotInfo((Robot) object).chassis != Chassis.BUILDING) {
					emptyMines.add(loc);
				} else {
					if (object.getTeam() == myRC.getTeam()) {
						allyMines.add(loc);
					} else {
						enemyMines.add(loc);
					}
				}
			} catch (GameActionException e) {
				e.printStackTrace();
			}

		}
		lastUpdateMine = roundNum;
	}
	
	public void senseRobot() {
//		int before = Clock.getBytecodesLeft();
		if (sensor == null)
			return;
		int roundNum = Clock.getRoundNum();
		if (roundNum == lastUpdateRobot)
			return;
		reset(false);
		RobotInfo rinfo;
		Boolean mobile;
		Robot[] robots = sensor.senseNearbyGameObjects(Robot.class);
		for (Robot r: robots) {
			try {
				rinfo = sensor.senseRobotInfo(r);
				mobile = rinfo.on && rinfo.chassis != Chassis.BUILDING;
				if (r.getTeam() == myRC.getTeam() && rinfo.chassis != Chassis.DUMMY) {
					if (mobile)
						allyMobile.add(rinfo);
					else
						allyImmobile.add(rinfo);
				} 
				else if (r.getTeam() == myRC.getTeam().opponent() && rinfo.chassis != Chassis.DUMMY) {
					if (mobile) {
						enemyMobile.add(rinfo);
					} else {
						enemyImmobile.add(rinfo);
					}
				} 
				else if (rinfo.chassis == Chassis.DEBRIS) {
					debris.add(rinfo);
				}
			} catch (GameActionException e) {
				continue;
			}
		}
		lastUpdateRobot = roundNum;
//		int after = Clock.getBytecodesLeft();
//		if ((before-after) > 1100 || (before-after) < -1100)
//			System.out.println("used bytecode: "+ String.valueOf(before - after));
	}
	
	public void senseAll() {
//		int before = Clock.getBytecodesLeft();
		if (sensor == null)
			return;
		int roundNum = Clock.getRoundNum();
		if (roundNum == lastUpdateRobot && roundNum == lastUpdateMine)
			return;
		reset(true);
		RobotInfo rinfo;
		Boolean mobile;
		GameObject[] objects = sensor.senseNearbyGameObjects(GameObject.class);
		for (GameObject o: objects) {
			if (o instanceof Mine) {
				MapLocation loc = ((Mine) o).getLocation();
				GameObject object;
				try {
					object = sensor.senseObjectAtLocation(loc, RobotLevel.ON_GROUND);
					if (object == null || sensor.senseRobotInfo((Robot) object).chassis != Chassis.BUILDING) {
						emptyMines.add(loc);
					} else {
						if (object.getTeam() == myRC.getTeam()) {
							allyMines.add(loc);
						} else {
							enemyMines.add(loc);
						}
					}
				} catch (GameActionException e) {
					e.printStackTrace();
				}
				continue;
			}
			try {
				rinfo = sensor.senseRobotInfo((Robot)o);
				mobile = rinfo.on && rinfo.chassis != Chassis.BUILDING && rinfo.chassis != Chassis.DUMMY;
				if (o.getTeam() == myRC.getTeam()) {
					if (mobile)
						allyMobile.add(rinfo);
					else
						allyImmobile.add(rinfo);
				} 
				else if (o.getTeam() == myRC.getTeam().opponent()) {
					if (mobile) {
						enemyMobile.add(rinfo);
					} else {
						enemyImmobile.add(rinfo);
					}
				} 
				else if (rinfo.chassis == Chassis.DEBRIS) {
					debris.add(rinfo);
				}
			} catch (GameActionException e) {
				continue;
			}
		}
		lastUpdateRobot = roundNum;
		lastUpdateMine = roundNum;
//		int after = Clock.getBytecodesLeft();
//		if ((before-after) > 1100 || (before-after) < -1100)
//			System.out.println("used bytecode: "+ String.valueOf(before - after));
	}
	
	public int weaponNum() {return weapons.size();}
	
	public boolean isMobile(RobotInfo info) {
		return info.on && (info.chassis != Chassis.BUILDING)
				&& (info.chassis != Chassis.DUMMY)
				&& (info.chassis != Chassis.DEBRIS);
	}
	
//	public void updateInfo() {
//		int before = Clock.getBytecodesLeft();
//		int roundNum = Clock.getRoundNum();
//		if (roundNum == lastUpdateInfo)
//			return;
//		dir = myRC.getDirection();
//		loc = myRC.getLocation();
//		hp = myRC.getHitpoints();
//		int after = Clock.getBytecodesLeft();
//		if ((before-after) > 0)
//			System.out.println("used bytecode: " + (before - after));
//	}
	
	public void updateComponents() {
		ComponentController[] components = myRC.newComponents();
		for (ComponentController com : components) {
			switch (com.componentClass()) {
			case MOTOR:
				motor = (MovementController) com;
				break;
			case SENSOR:
				sensor = (SensorController) com;
				break;
			case BUILDER:
				builder = (BuilderController) com;
				break;
			case COMM:
				comm = (BroadcastController) com;
				break;
			case WEAPON:
				if (com.type() == ComponentType.MEDIC)
					medic = (WeaponController) com;
				else
					weapons.add((WeaponController) com);
				break;
			case MISC:
				switch (com.type()) {
				case JUMP:
					jumper = (JumpController) com;
					break;
				}
			break;
			}
		}
	}
}
