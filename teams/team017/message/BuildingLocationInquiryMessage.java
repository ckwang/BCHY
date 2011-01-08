package team017.message;

import battlecode.common.MapLocation;
import battlecode.common.Message;

public class BuildingLocationInquiryMessage extends GenericMessage {

	private MapLocation builderLocation;
	
	public BuildingLocationInquiryMessage(MapLocation builderLocation) {
		super(MessageType.BUILDING_LOCATION_INQUIRY_MESSAGE);
		
		msg.locations[locCounter++] = builderLocation;
	}
	
	public BuildingLocationInquiryMessage(Message msg) {
		super(msg);
		
		builderLocation = msg.locations[locCounter++];
	}
	
	public MapLocation getBuilderLocation() {
		return builderLocation;
	}

}
