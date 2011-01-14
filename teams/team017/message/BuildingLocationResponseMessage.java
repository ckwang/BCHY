package team017.message;

import team017.construction.UnitType;
import battlecode.common.Direction;
import battlecode.common.Message;

/*
 *	Recycler response if there's adjacent available spaces.
 *	2 or 3 available spaces
 */

public class BuildingLocationResponseMessage extends GenericMessage {

	private int constructorID;
	private Direction buildableDirection;
	private UnitType type;
	
	//	buildableDirection and it's turnRight(s) are available
	public BuildingLocationResponseMessage(int constructorID, Direction buildableDirection, UnitType type) {
		super(MessageType.BUILDING_LOCATION_RESPONSE_MESSAGE);
		
		msg.ints[intCounter++] = constructorID;
		msg.ints[intCounter++] = buildableDirection.ordinal();
		msg.ints[intCounter++] = (type == null ? -1 : type.ordinal());
	}
	
	public BuildingLocationResponseMessage(Message msg) {
		super(msg);
		
		constructorID = msg.ints[intCounter++];
		buildableDirection = Direction.values()[msg.ints[intCounter++]];
		int t = msg.ints[intCounter++];
		type = (t == -1 ? null : UnitType.values()[t]);
	}
	
	public Direction getBuildableDirection() {
		return buildableDirection;
	}
	
	public UnitType getUnitType() {
		return type;
	}
	
	public int getConstructorID() {
		return constructorID;
	}
}
