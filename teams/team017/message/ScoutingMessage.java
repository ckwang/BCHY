package team017.message;

import battlecode.common.MapLocation;
import battlecode.common.Message;

public class ScoutingMessage extends GenericMessage {
	private MapLocation ScoutLoc;
	
	public ScoutingMessage(MapLocation ScoutLoc) {
		super(MessageType.SCOUTING_MESSAGE);
		
		msg.locations[locCounter++] = ScoutLoc;
	}
	
	public ScoutingMessage(Message msg) {
		super(msg);
		
		ScoutLoc = msg.locations[locCounter++];
	}
	
	public MapLocation getScoutLocation() {
		return ScoutLoc;
	}
}
