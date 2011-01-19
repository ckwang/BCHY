package team017.message;

import battlecode.common.MapLocation;
import battlecode.common.Message;

public class TurnOffMessage extends GenericMessage {

	private MapLocation buildingLoc;
	
	public TurnOffMessage(MapLocation buildingLoc) {
		super(MessageType.TURN_OFF_MESSAGE);
		msg.locations[locCounter++] = buildingLoc;
	}
	
	public TurnOffMessage(Message msg) {
		super(msg);
		buildingLoc = msg.locations[locCounter++];
	}

	public MapLocation getBuildingLoc() {
		return buildingLoc;
	}
	
}
