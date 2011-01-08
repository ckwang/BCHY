package team017.message;

import battlecode.common.Direction;
import battlecode.common.Message;

public class BuildingLocationResponseMessage extends GenericMessage {

	private Direction buildableDirection;
	private int availableSpace;
	
	public BuildingLocationResponseMessage(Direction buildableDirection, int availableSpace) {
		super(MessageType.BUILDING_LOCATION_RESPONSE_MESSAGE);
		
		msg.ints[intCounter++] = buildableDirection.ordinal();
		msg.ints[intCounter++] = availableSpace;
	}
	
	public BuildingLocationResponseMessage(Message msg) {
		super(msg);
		
		buildableDirection = Direction.values()[msg.ints[intCounter++]];
		availableSpace = msg.ints[intCounter++];
	}
	
	public Direction getBuildableDirection() {
		return buildableDirection;
	}
	
	public int getAvailableSpace() {
		return availableSpace;
	}
}
