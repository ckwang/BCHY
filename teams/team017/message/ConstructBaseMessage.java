package team017.message;

import team017.construction.UnitType;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class ConstructBaseMessage extends GenericMessage {

	private MapLocation builderLoc;
	private UnitType type;
	
	public ConstructBaseMessage (MapLocation loc, UnitType type) {
		super (MessageType.CONSTRUCT_BASE_MESSAGE);
		msg.locations[locCounter++] = loc;
		msg.ints[intCounter++] = type.ordinal();
	}
	
	public ConstructBaseMessage(Message msg) {
		super(msg);
		
		builderLoc = msg.locations[locCounter++];
		type = UnitType.values()[msg.ints[intCounter++]];
	}

	public MapLocation getBuilderLoc() {
		return builderLoc;
	}
	
	public UnitType getType() {
		return type;
	}
	
}
