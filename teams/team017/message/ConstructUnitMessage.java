package team017.message;

import team017.construction.UnitType;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class ConstructUnitMessage extends GenericMessage {

	private MapLocation builderLocation;
	private UnitType type;
	public ConstructUnitMessage(MapLocation builderLocation, UnitType type) {
		super(MessageType.CONSTRUCT_UNIT_MESSAGE);
		
		msg.locations[locCounter++] = builderLocation;
		msg.ints[intCounter++] = type.ordinal();
	}


	public ConstructUnitMessage(Message msg) {
		super(msg);
		builderLocation = msg.locations[locCounter++];
		type = UnitType.values()[msg.ints[intCounter++]];
	}
	
	public UnitType getType() {
		return type;
	}
	
	public MapLocation getBuilderLocation() {
		return builderLocation;
	}
	
}
