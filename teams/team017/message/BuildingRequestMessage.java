package team017.message;

import team017.construction.UnitType;
import team017.util.Controllers;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class BuildingRequestMessage extends MessageHandler{
	
	private MapLocation builderLocation;
	private MapLocation buildingLocation;
	private UnitType unitType;
	
	public BuildingRequestMessage(Controllers controllers,
			MapLocation builderLocation, MapLocation buildingLocation, UnitType unitType) {
		super(controllers, MessageType.BUILDING_REQUEST);
		
		msg.locations[locCounter++] = builderLocation;
		msg.locations[locCounter++] = buildingLocation;
		msg.ints[intCounter++] = unitType.ordinal();
	}
	
	public BuildingRequestMessage(Message msg) {
		super(msg);
		
		if (valid) {
			builderLocation = msg.locations[locCounter++];
			buildingLocation = msg.locations[locCounter++];
			unitType = UnitType.values()[msg.ints[intCounter++]];
		}
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
