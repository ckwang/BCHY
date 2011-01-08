package team017.message;

import battlecode.common.Direction;
import battlecode.common.Message;

/*
 *	Recycler response if there's adjacent available spaces.
 *	2 or 3 available spaces
 */

public class BuildingLocationResponseMessage extends GenericMessage {

	private Direction buildableDirection;
	private int availableSpace;
	//	buildableDirection and it's turnRight(s) are available
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
