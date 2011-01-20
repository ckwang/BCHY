package team017.message;

import team017.construction.UnitType;
import battlecode.common.MapLocation;
import battlecode.common.Message;

/*
 *	Recycler response if there's adjacent available spaces.
 *	2 or 3 available spaces
 */

public class BuildingLocationResponseMessage extends GenericMessage {

	private int constructorID;
	private MapLocation buildableLocation;
	private UnitType type;
	
	//	buildableDirection and it's turnRight(s) are available
	public BuildingLocationResponseMessage(int constructorID, MapLocation buildableLocation, UnitType type) {
		super(MessageType.BUILDING_LOCATION_RESPONSE_MESSAGE);
		
		msg.ints[intCounter++] = constructorID;
		msg.locations[locCounter++] = buildableLocation;
		msg.ints[intCounter++] = (type == null ? -1 : type.ordinal());
	}
	
	public BuildingLocationResponseMessage(Message msg) {
		super(msg);
		
		constructorID = msg.ints[intCounter++];
		buildableLocation = msg.locations[locCounter++];
		int t = msg.ints[intCounter++];
		type = (t == -1 ? null : UnitType.values()[t]);
	}
	
	public MapLocation getBuildableLocation() {
		return buildableLocation;
	}
	
	public UnitType getUnitType() {
		return type;
	}
	
	public int getConstructorID() {
		return constructorID;
	}
}
