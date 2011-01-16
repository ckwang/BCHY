package team017.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import battlecode.common.BroadcastController;
import battlecode.common.BuilderController;
import battlecode.common.Clock;
import battlecode.common.ComponentController;
import battlecode.common.GameActionException;
import battlecode.common.JumpController;
import battlecode.common.MapLocation;
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
	
	public Set<EnemyInfo> enemyInfosSet = new HashSet<EnemyInfo>();
	public List<MapLocation> debrisLoc = new ArrayList<MapLocation>();
	
	public Controllers() {
		weapons = new ArrayList<WeaponController>();
	}
	
	public void reset() {
		debrisLoc.clear();
		enemyInfosSet.clear();
	}
	
	public void senseNearby() {
//		reset();
		int roundNum = Clock.getRoundNum();
//		int before = Clock.getBytecodeNum();
		Robot[] robots = sensor.senseNearbyGameObjects(Robot.class);
		for (Robot r : robots) {
			try {
				RobotInfo info = sensor.senseRobotInfo(r);
//				if (r.getTeam() == controllers.myRC.getTeam()) {
//					ComponentType[] components = info.components;
//					if (Util.hasWeapon(components)) {
//						allies.add(r);
//						MapLocation loc = controllers.sensor.senseLocationOf(r);
//						alocs.add(loc);
//					}
//				} else 
				if (r.getTeam() == myRC.getTeam().opponent()) {
					EnemyInfo thisEnemy = new EnemyInfo(roundNum, info);
					enemyInfosSet.remove(thisEnemy);
					enemyInfosSet.add(thisEnemy);
				} else if (r.getTeam() == Team.NEUTRAL) {
					debrisLoc.add(info.location);
				}
			} catch (GameActionException e) {
				continue;
			}
		}
//		Util.sortHp(hps, enemies);
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
