package team017.message;

import team017.construction.UnitType;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class UnitReadyMessage extends GenericMessage {

	UnitType type;
	MapLocation unitLoc;
	
	public UnitReadyMessage(UnitType type, MapLocation loc) {
		super(MessageType.UNIT_READY);
		
		msg.ints[intCounter++] = type.ordinal();
		msg.locations[locCounter++] = loc;
	}

	public UnitReadyMessage(Message msg) {
		super(msg);
		
		type = UnitType.values()[msg.ints[intCounter++]];
		unitLoc = msg.locations[locCounter++];
	}
	
	public UnitType getUnitType() {
		return type;
	}
	
	public MapLocation getUnitLoc() {
		return unitLoc;
	}
	
}
