package team017.message;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class ScoutingMessage extends GenericMessage {
	private Direction ScoutDir;
	
	public ScoutingMessage(Direction dir) {
		super(MessageType.SCOUTING_MESSAGE);
		
		msg.ints[intCounter++] = dir.ordinal();
	}
	
	public ScoutingMessage(Message msg) {
		super(msg);
		
		ScoutDir = Direction.values()[msg.ints[intCounter++]] ;
	}
	
	public Direction getScoutDirection() {
		return ScoutDir;
	}
}
