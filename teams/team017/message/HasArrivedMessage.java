package team017.message;

import battlecode.common.MapLocation;
import battlecode.common.Message;

public class HasArrivedMessage extends GenericMessage {
	
	private MapLocation arrivedLoc;
	private boolean isMine;
	
	public HasArrivedMessage(MapLocation arrivedLoc, boolean isMine) {
		super(MessageType.HAS_ARRIVED_MESSAGE);

		msg.locations[locCounter++] = arrivedLoc;
		msg.ints[intCounter++] = isMine ? 1 : 0;
	}
	
	public HasArrivedMessage(Message msg) {
		super(msg);
		
		arrivedLoc = msg.locations[locCounter++];
		isMine = msg.ints[intCounter++] == 1;
	}

	
	public boolean isMine() {
		return isMine;
	}
	
	public MapLocation getArrivedLoc() {
		return arrivedLoc;
	}
}
