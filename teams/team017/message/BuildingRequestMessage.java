package team017.message;

import team017.construction.UnitType;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class BuildingRequestMessage extends GenericMessage{
	
	private MapLocation builderLocation;
	private MapLocation buildingLocation;
	private UnitType unitType;
	
	public BuildingRequestMessage(MapLocation builderLocation, MapLocation buildingLocation, UnitType unitType) {
		super(MessageType.BUILDING_REQUEST);
		
		msg.locations[locCounter++] = builderLocation;
		msg.locations[locCounter++] = buildingLocation;
		msg.ints[intCounter++] = unitType.ordinal();
	}
	
	public BuildingRequestMessage(Message msg) {
		super(msg);
		
		builderLocation = msg.locations[locCounter++];
		buildingLocation = msg.locations[locCounter++];
		unitType = UnitType.values()[msg.ints[intCounter++]];
	}
	
	public MapLocation getBuilderLocation() {
		return builderLocation;
	}
	
	public MapLocation getBuildingLocation() {
		return buildingLocation;
	}
	
	public UnitType getUnitType() {
		return unitType;
	}
	
}
