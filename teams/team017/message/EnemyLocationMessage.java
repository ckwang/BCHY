package team017.message;

import battlecode.common.MapLocation;
import battlecode.common.Message;

public class EnemyLocationMessage extends GenericMessage {
	private MapLocation enemyLoc;
	
	public EnemyLocationMessage(MapLocation loc) {
		super(MessageType.ENEMY_LOCATION);
		
		msg.ints[intCounter++] = loc.x;
		msg.ints[intCounter++] = loc.y;
	}
	
	public EnemyLocationMessage(Message msg) {
		super(msg);
		
		int x = msg.ints[intCounter++];
		int y = msg.ints[intCounter++];
		
		enemyLoc = new MapLocation(x, y);
	}
	
	public MapLocation getEnemyLocation() {
		return enemyLoc;
	}
}
