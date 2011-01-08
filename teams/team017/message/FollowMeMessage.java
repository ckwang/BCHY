package team017.message;

import battlecode.common.Message;

public class FollowMeMessage extends GenericMessage {

	public FollowMeMessage() {
		super(MessageType.FOLLOW_ME_MESSAGE);
	}
	
	public FollowMeMessage(Message msg) {
		super(msg);
	}
	
}
