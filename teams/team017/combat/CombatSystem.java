package team017.combat;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.Chassis;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.MovementController;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.SensorController;
import battlecode.common.Team;
import battlecode.common.WeaponController;

public class CombatSystem {

	private RobotController rc;
	private MovementController motor;
	private SensorController sensor;
	private List<WeaponController> weapons;

	List<Robot> enemies = null;
	Robot target = null;
	private int lastUpdate = 0;

	public CombatSystem(RobotController rc, MovementController motor,
			SensorController sensor, List<WeaponController> weapons) {
		this.rc = rc;
		this.motor = motor;
		this.sensor = sensor;
		this.weapons = weapons;
	}
	
//	public boolean chase() {
//		
//	}

	public boolean init_attack() {
		Robot[] robots = sensor.senseNearbyGameObjects(Robot.class);
		List<MapLocation> enemylocs = new ArrayList<MapLocation>(robots.length);
		List<MapLocation> allylocs = new ArrayList<MapLocation>(robots.length);
		double leasthp1 = Chassis.HEAVY.maxHp, leasthp2 = Chassis.HEAVY.maxHp;
		int index1 = 0, index2 = 0;
		for (int i = 0; i < robots.length; ++i) {
			Robot r = robots[i];
			if (r.getTeam() == rc.getTeam()) {
				MapLocation loc;
				try {
					loc = sensor.senseLocationOf(r);
					allylocs.add(loc);
				} catch (GameActionException e) {
					e.printStackTrace();
					continue;
				}

			} else if (r.getTeam() != Team.NEUTRAL) {
				try {
					RobotInfo info = sensor.senseRobotInfo(r);
					MapLocation loc = sensor.senseLocationOf(r);
					enemylocs.add(loc);
					if (!info.on)
						continue;
					if (info.hitpoints < leasthp1) {
						leasthp2 = leasthp1;
						index2 = index1;
						index1 = i;
						leasthp1 = info.hitpoints;
					}
					if (info.hitpoints < leasthp2) {
						index2 = i;
						leasthp2 = info.hitpoints;
					}
				} catch (GameActionException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		boolean canfire = false, attacked = false;
		if (enemylocs.size() == 0)
			return false;
		for (WeaponController w : weapons) {
			if (w.isActive())
				continue;
			try {
				MapLocation weakest = sensor.senseLocationOf(robots[index1]);
				w.attackSquare(weakest, robots[index1].getRobotLevel());
				attacked = true;
			} catch (GameActionException e) {
				canfire = true;
				break;
			}
		}
		if (enemylocs.size() > 1 && canfire) {
			for (WeaponController w : weapons) {
				if (w.isActive())
					continue;
				try {
					MapLocation weakest = sensor
							.senseLocationOf(robots[index2]);
					w.attackSquare(weakest, robots[index2].getRobotLevel());
					attacked = true;
				} catch (GameActionException e) {
					return attacked;
				}
			}
		}
		return attacked;
	}
}
