package team017.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import battlecode.common.BroadcastController;
import battlecode.common.BuilderController;
import battlecode.common.Chassis;
import battlecode.common.Clock;
import battlecode.common.ComponentController;
import battlecode.common.GameActionException;
import battlecode.common.GameObject;
import battlecode.common.JumpController;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
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
	public JumpController jump = null;
	
	public Set<UnitInfo> allyMobile = new HashSet<UnitInfo>();
	public Set<UnitInfo> allyImmobile = new HashSet<UnitInfo>();
	public Set<UnitInfo> enemyMobile = new HashSet<UnitInfo>();
	public Set<UnitInfo> enemyImmobile = new HashSet<UnitInfo>();
	public List<UnitInfo> debris = new ArrayList<UnitInfo>();
	public List<MapLocation> mines = new ArrayList<MapLocation>();
	
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
	
	public int minesNum() {
		if (lastUpdate < Clock.getRoundNum())
			senseNearby();
		return mines.size();
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
		if (sensor == null)
			return;
		int roundNum = Clock.getRoundNum();
		if (roundNum == lastUpdate)
			return;
		UnitInfo unit;
		GameObject[] objects = sensor.senseNearbyGameObjects(GameObject.class);
		for (GameObject o : objects) {
			if (o instanceof Mine) {
				MapLocation loc;
				try {
					loc = sensor.senseLocationOf(o);
					mines.add(loc);
				} catch (GameActionException e) {
					e.printStackTrace();
				}
				continue;
			}
			try {
				RobotInfo info = sensor.senseRobotInfo((Robot)o);
				unit = new UnitInfo(roundNum, info);
				
				if (o.getTeam() == myRC.getTeam() && info.chassis != Chassis.DUMMY) {
					if (unit.mobile)
						allyMobile.add(unit);
					else
						allyImmobile.add(unit);
				} 
				else if (o.getTeam() == myRC.getTeam().opponent()) {
					if (unit.mobile) {
						enemyMobile.remove(unit);
						enemyMobile.add(unit);
					} else {
						enemyImmobile.remove(unit);
						enemyImmobile.add(unit);
					}
				} 
				else if (o.getTeam() == Team.NEUTRAL) {
					debris.add(unit);
				}
			} catch (GameActionException e) {
				continue;
			}
		}
		lastUpdate = roundNum;
	}
	
//	public int weaponNum() {return weapons.size();}
	
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
