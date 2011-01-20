package team017.message;

import battlecode.common.MapLocation;
import battlecode.common.Message;

public class ScoutingResponseMessage extends GenericMessage {
	private int telescoperID;
	private MapLocation scoutLocation;
	
	public ScoutingResponseMessage(int id, MapLocation loc) {
		super(MessageType.SCOUTING_RESPONSE_MESSAGE);
		
		msg.ints[intCounter++] = id;
		msg.locations[locCounter++] = loc;
	}
	
	public ScoutingResponseMessage(Message msg) {
		super(msg);

		telescoperID = msg.ints[intCounter++];
		scoutLocation = msg.locations[locCounter++];
	}
	
	public int getTelescoperID() {
		return telescoperID;
	}
	
	public MapLocation getScoutLocation() {
		return scoutLocation;
	}
}
