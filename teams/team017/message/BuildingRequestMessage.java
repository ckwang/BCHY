package team017.message;

import team017.construction.UnitType;
import battlecode.common.BroadcastController;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class BuildingRequestMessage extends MessageHandler{
	
	private MapLocation buildingLocation;
	private UnitType unitType;
	
	public BuildingRequestMessage(RobotController rc, BroadcastController comm,
			MapLocation buildingLocation, UnitType unitType) {
		super(rc, comm, MessageType.BUILDING_REQUEST);
		
		msg.locations[locCounter++] = buildingLocation;
		msg.ints[intCounter++] = unitType.ordinal();
	}
	
	public BuildingRequestMessage(Message msg) {
		super(msg);
		
		if (valid) {
			buildingLocation = msg.locations[locCounter++];
			unitType = UnitType.values()[msg.ints[intCounter++]];
		}
	}
	
	public MapLocation getBuildingLocation() {
		return buildingLocation;
	}
	
	public UnitType getUnitType() {
		return unitType;
	}
	
}
