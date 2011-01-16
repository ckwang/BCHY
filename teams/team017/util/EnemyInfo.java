package team017.util;


import battlecode.common.Chassis;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotLevel;

public class EnemyInfo {
	public int roundNum;
	public int id;
	public double hp;
	public MapLocation location;
	public RobotLevel level;
	public boolean mobile;
	public Direction direction = null;
	public Robot robot = null;
	public int priority;

	public EnemyInfo(int roundNum, RobotInfo info) {
		this.roundNum = roundNum;
		this.hp = info.hitpoints;
		this.location = info.location;
		this.level = info.robot.getRobotLevel();
		this.id = info.robot.getID();
		this.mobile = info.on || info.chassis != Chassis.BUILDING;
		this.direction = info.direction;
		this.robot = info.robot;
	}
	
	public EnemyInfo (int roundNum, int id, double hp, MapLocation location, RobotLevel level, boolean mobile) {
		this.roundNum = roundNum;
		this.hp = hp;
		this.location = location;
		this.level = level;
		this.id = id;
		this.mobile = mobile;
	}
	
	public void calculateCost(MapLocation loc) {
		priority = 0;
		priority |= mobile ? (1 << 25) : 0;
		priority |= ~(loc.distanceSquaredTo(location) << 15);
		priority |= (level == RobotLevel.IN_AIR) ? (1 << 14) : 0;
		priority |= (int) (hp * 10);
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
