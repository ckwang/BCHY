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
import battlecode.common.Team;
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
	
	public int lastUpdate = -1;
	
	public Controllers() {
		weapons = new ArrayList<WeaponController>();
	}
	
	public void reset() {
		allyMobile.clear();
		allyImmobile.clear();
		enemyMobile.clear();
		enemyImmobile.clear();
		debris.clear();
		mines.clear();
	}
	
	public int debrisNum() {
		if (lastUpdate < Clock.getRoundNum())
			senseNearby();
		return debris.size();
	}
	
	public int mobileEnemyNum() {
		if (lastUpdate < Clock.getRoundNum())
			senseNearby();
		return enemyMobile.size();
	}
	
	public int immobileEnemyNum() {
		if (lastUpdate < Clock.getRoundNum())
			senseNearby();
		return enemyImmobile.size();
	}
	
	public int mobileAllyNum() {
		if (lastUpdate < Clock.getRoundNum())
			senseNearby();
		return allyMobile.size();
	}
	
	public int immobileAllyNum() {
		if (lastUpdate < Clock.getRoundNum())
			senseNearby();
		return allyImmobile.size();
	}
	
	public void senseNearby() {
//		int before = Clock.getBytecodesLeft();
		if (sensor == null)
			return;
		int roundNum = Clock.getRoundNum();
		if (roundNum == lastUpdate)
			return;
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
		lastUpdate = roundNum;
//		int after = Clock.getBytecodesLeft();
//		if ((before-after) > 1100 || (before-after) < -1100)
//			System.out.println("used bytecode: "+ String.valueOf(before - after));
	}
	
//	public int weaponNum() {return weapons.size();}
	
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
