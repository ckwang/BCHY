package team017.message;

import team017.construction.UnitType;
import battlecode.common.ComponentType;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class ConstructionCompleteMessage extends GenericMessage {

	private MapLocation buildingLocation;
	private UnitType buildingType;
	
	public ConstructionCompleteMessage(MapLocation buildingLocation, UnitType buildingType) {
		super(MessageType.CONSTRUCTION_COMPLETE);
		
		msg.locations[locCounter++] = buildingLocation;
		msg.ints[intCounter++] = buildingType.ordinal();
	}
	
	public ConstructionCompleteMessage(Message msg) {
		super(msg);

		buildingLocation = msg.locations[locCounter++];
		buildingType = UnitType.values()[msg.ints[intCounter++]];
	}
	
	public MapLocation getBuildingLocation() {
		return buildingLocation;
	}
	
	public UnitType getBuildingType() {
		return buildingType;
	}
}
