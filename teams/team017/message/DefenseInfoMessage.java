package team017.message;

import battlecode.common.Chassis;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotInfo;

public class DefenseInfoMessage extends GenericMessage {
/*
 * Enemy Equivalent:
 * 
 * Air    = 1
 * Light  = 1
 * Medium = 2
 * Heavy  = 4
 * 
 */
	private MapLocation recyclerLoc;
	private int[] enemyEquivalentInDir = new int[8];
	private int totalEnemyEquivalent = 0;
	
	public DefenseInfoMessage (MapLocation recyclerLoc, RobotInfo[] enemyInfos) {
		super(MessageType.DEFENSE_INFO_MESSAGE);
		
		this.recyclerLoc = recyclerLoc;
		for (RobotInfo info: enemyInfos) {
			Direction dir = recyclerLoc.directionTo(info.location);
			enemyEquivalentInDir[indexMapping(dir)] += getEquivalent(info.chassis);
			totalEnemyEquivalent += getEquivalent(info.chassis);
		}
		
		msg.locations[locCounter++] = recyclerLoc;
		for (int i = 0; i < 8; i++)
			msg.ints[intCounter++] = enemyEquivalentInDir[i];
	}
	
	public DefenseInfoMessage(Message msg) {
		super(msg);
		recyclerLoc = msg.locations[locCounter++];
		for (int i = 0; i < 8; i++) {
			enemyEquivalentInDir[i] = msg.ints[intCounter++];
			totalEnemyEquivalent += enemyEquivalentInDir[i];
		}
			
	}
	
	
	public static int indexMapping(Direction dir) {
		switch(dir){
		case NORTH:
			return 0;
		case NORTH_EAST:
			return 1;
		case EAST:
			return 2;
		case SOUTH_EAST:
			return 3;
		case SOUTH:
			return 4;
		case SOUTH_WEST:
			return 5;
		case WEST:
			return 6;
		case NORTH_WEST:
			return 7;
		default:
			return -1;
		}
	}
	
	public MapLocation getRecyclerLoc() {
		return recyclerLoc;
	}
	
	public int[] getEnemyNumInDir() {
		return enemyEquivalentInDir;
	}
	
	public int getTotalEnemyNum() {
		return totalEnemyEquivalent;
	}

	public int getEquivalent (Chassis chassis) {
		switch (chassis) {
		case FLYING:
			return 1;
		case LIGHT:
			return 1;
		case MEDIUM:
			return 2;
		case HEAVY:
			return 4;
		default:
			return 0;
		}
	}

}
