package team017.message;

import team017.construction.UnitType;
import team017.util.Controllers;
import battlecode.common.ComponentType;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class ConstructionCompleteMessage extends MessageHandler {

	private MapLocation buildingLocation;
	private UnitType builderType;
	
	public ConstructionCompleteMessage(Controllers controllers,
			MapLocation buildingLocation, ComponentType builderType) {
		super(controllers, MessageType.CONSTRUCTION_COMPLETE);
		
		msg.locations[locCounter++] = buildingLocation;
		msg.ints[intCounter++] = builderType.ordinal();
	}
	
	public ConstructionCompleteMessage(Message msg) {
		super(msg);

		if (valid) {
			buildingLocation = msg.locations[locCounter++];
			builderType = UnitType.values()[msg.ints[intCounter++]];
		}
	}
	
	public MapLocation getBuildingLocation() {
		return buildingLocation;
	}
	
	public UnitType getBuilderType() {
		return builderType;
	}
}
