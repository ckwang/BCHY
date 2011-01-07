package team017.message;

import team017.construction.UnitType;
import battlecode.common.BroadcastController;
import battlecode.common.ComponentType;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class ConstructionCompleteMessage extends MessageHandler {

	private MapLocation buildingLocation;
	private UnitType builderType;
	
	public ConstructionCompleteMessage(RobotController rc, BroadcastController comm,
			MapLocation buildingLocation, ComponentType builderType) {
		super(rc, comm, MessageType.CONSTRUCTION_COMPLETE);
		
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
