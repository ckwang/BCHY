package team017.message;

import battlecode.common.MapLocation;
import battlecode.common.Message;
import team017.construction.UnitType;

public class BuildingLocationInquiryMessage extends GenericMessage {

	public BuildingLocationInquiryMessage(MapLocation builderLocation, MapLocation buildingLocation, UnitType unitType) {
		super(MessageType.BUILDING_LOCATION_INQUIRY_MESSAGE);
	}
	
	public BuildingLocationInquiryMessage(Message msg) {
		super(msg);
	}

}
