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
	
	public int cost;

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
	
	public void calculateCost(MapLocation loc) {
		cost = 0;
		
		cost |= mobile ? (1 << 20) : 0;
		cost |= loc.distanceSquaredTo(location) << 15;
		cost |= (level == RobotLevel.IN_AIR) ? (1 << 14) : 0;
		cost |= (int) (hp * 10);
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
