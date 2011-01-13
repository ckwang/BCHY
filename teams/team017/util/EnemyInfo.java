package team017.util;


import battlecode.common.MapLocation;
import battlecode.common.RobotLevel;

public class EnemyInfo {
	public double hp;
	public MapLocation location;
	public RobotLevel level;
	public int id;
	public boolean mobile;

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
