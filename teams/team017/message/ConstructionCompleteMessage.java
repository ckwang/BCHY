package team017.message;

import battlecode.common.ComponentType;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class ConstructionCompleteMessage extends GenericMessage {

	private MapLocation buildingLocation;
	private ComponentType builderType;
	
	public ConstructionCompleteMessage(MapLocation buildingLocation, ComponentType builderType) {
		super(MessageType.CONSTRUCTION_COMPLETE);
		
		msg.locations[locCounter++] = buildingLocation;
		if (builderType != null)
			msg.ints[intCounter++] = builderType.ordinal();
		else
			msg.ints[intCounter++] = -1;
	}
	
	public ConstructionCompleteMessage(Message msg) {
		super(msg);

		buildingLocation = msg.locations[locCounter++];
		if (msg.ints[intCounter++] != -1)
			builderType = ComponentType.values()[msg.ints[intCounter++]];
		else
			builderType = null;
	}
	
	public MapLocation getBuildingLocation() {
		return buildingLocation;
	}
	
	public ComponentType getBuilderType() {
		return builderType;
	}
}
