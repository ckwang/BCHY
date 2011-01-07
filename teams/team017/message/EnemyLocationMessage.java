package team017.message;

import team017.util.Controllers;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class EnemyLocationMessage extends MessageHandler {
	private MapLocation enemyLoc;
	
	public EnemyLocationMessage(Controllers controllers, MapLocation loc) {
		super(controllers, MessageType.ENEMY_LOCATION);
		
		msg.ints[intCounter++] = loc.x;
		msg.ints[intCounter++] = loc.y;
	}
	
	public EnemyLocationMessage(Message msg) {
		super(msg);
		
		if ( valid ) {
			int x = msg.ints[intCounter++];
			int y = msg.ints[intCounter++];
			
			enemyLoc = new MapLocation(x, y);
		}
	}
	
	public MapLocation getEnemyLocation() {
		return enemyLoc;
	}
}
