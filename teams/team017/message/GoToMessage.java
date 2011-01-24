package team017.message;

import battlecode.common.MapLocation;
import battlecode.common.Message;

public class GoToMessage extends GenericMessage {
	
	private MapLocation loc;
	private boolean isMine;
	
	public GoToMessage(MapLocation loc, boolean isMine) {
		super(MessageType.GO_TO_MESSAGE);
		
		msg.locations[locCounter++] = loc;
		msg.ints[intCounter++] = isMine ? 1 : 0;
	}
	
	public GoToMessage(Message msg) {
		super(msg);
		
		loc = msg.locations[locCounter++];
		isMine = msg.ints[intCounter++] == 1;
	}
	
	public MapLocation getGoToLocation() {
		return loc;
	}
	
	public boolean isMine() {
		return isMine;
	}
}
