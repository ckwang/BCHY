package team017.message;

import battlecode.common.MapLocation;
import battlecode.common.Message;
import team017.construction.UnitType;
import team017.util.Controllers;

public class BuildingLocationInquiryMessage extends MessageHandler {

	public BuildingLocationInquiryMessage(Controllers controllers,
			MapLocation builderLocation, MapLocation buildingLocation, UnitType unitType) {
		super(controllers, MessageType.BUILDING_LOCATION_INQUIRY_MESSAGE);
	}
	
	public BuildingLocationInquiryMessage(Message msg) {
		super(msg);
	}

}
