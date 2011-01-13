package team017.util;


import battlecode.common.Chassis;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class EnemyInfo {
	public double hp;
	public MapLocation location;
	public RobotLevel level;
	public int id;
	public boolean mobile;

	public EnemyInfo(RobotInfo info) {
		this.hp = info.hitpoints;
		this.location = info.location;
		this.level = info.robot.getRobotLevel();
		this.id = info.robot.getID();
		this.mobile = info.on || info.chassis == Chassis.BUILDING;
	}
	
	public EnemyInfo (int id, double hp, MapLocation location, RobotLevel level, boolean mobile) {
		this.hp = hp;
		this.location = location;
		this.level = level;
		this.id = id;
		this.mobile = mobile;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)	return true;
		if (obj instanceof EnemyInfo) {
			EnemyInfo info = (EnemyInfo) obj;
			
			return info.id == this.id;
		}
		
		return false;
		
	}
	
	@Override
	public int hashCode() {
		return id;
	}

}
