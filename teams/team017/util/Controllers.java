package team017.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import battlecode.common.BroadcastController;
import battlecode.common.BuilderController;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.JumpController;
import battlecode.common.Mine;
import battlecode.common.MineInfo;
import battlecode.common.MovementController;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
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
	public JumpController jump = null;
	
	public List<RobotInfo> allyMobile = new LinkedList<RobotInfo>();
	public List<RobotInfo> allyImmobile = new LinkedList<RobotInfo>();
	public List<RobotInfo> enemyMobile = new LinkedList<RobotInfo>();
	public List<RobotInfo> enemyImmobile = new LinkedList<RobotInfo>();
	public List<RobotInfo> debris = new LinkedList<RobotInfo>();
	public List<MineInfo> mines = new ArrayList<MineInfo>();
	
	public int lastUpdateRobot = -1;
	public int lastUpdateMine = -1;
	
	public Controllers() {
		weapons = new ArrayList<WeaponController>();
	}
	
	public void reset(boolean mine) {
		allyMobile.clear();
		allyImmobile.clear();
		enemyMobile.clear();
		enemyImmobile.clear();
		debris.clear();
		if (mine)
			mines.clear();
	}
	
	public int enemyNum() {
		if (lastUpdateRobot < Clock.getRoundNum())
			senseAll();
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
	
	public int mobileAllyNum() {
		if (lastUpdateRobot < Clock.getRoundNum())
			senseAll();
		return allyMobile.size();
	}
	
	public int immobileAllyNum() {
		if (lastUpdateRobot < Clock.getRoundNum())
			senseAll();
		return allyImmobile.size();
	}
	
	public void senseMine() {
		if (sensor == null)
			return;
		int roundNum = Clock.getRoundNum();
		if (roundNum == lastUpdateMine)
			return;
		mines.clear();
		MineInfo minfo;
		Mine[] minearray = sensor.senseNearbyGameObjects(Mine.class);
		for (Mine m: minearray) {
			try {
				minfo = sensor.senseMineInfo(m);
				mines.add(minfo);
			} catch (GameActionException e) {
				continue;
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
		MineInfo minfo;
		RobotInfo rinfo;
		Boolean mobile;
		GameObject[] objects = sensor.senseNearbyGameObjects(GameObject.class);
		for (GameObject o: objects) {
			if (o instanceof Mine) {
				try {
					minfo = sensor.senseMineInfo((Mine)o);
					mines.add(minfo);
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
					jump = (JumpController) com;
					break;
				}
			break;
			}
		}
	}
}
