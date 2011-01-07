package team017.message;

import team017.construction.UnitType;
import team017.util.Controllers;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class FollowMeMessage extends MessageHandler {

	public FollowMeMessage(Controllers controllers,
			MapLocation builderLocation, MapLocation buildingLocation, UnitType unitType) {
		super(controllers, MessageType.FOLLOW_ME_MESSAGE);
	}
	
	public FollowMeMessage(Message msg) {
		super(msg);
	}
	
}
