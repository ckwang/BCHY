package team017.message;

import battlecode.common.Direction;
import battlecode.common.Message;

public class FollowMeMessage extends GenericMessage {
	private Direction followDir;
	
	public FollowMeMessage(Direction followDir) {
		super(MessageType.FOLLOW_ME_MESSAGE);
		msg.ints[intCounter++] = followDir.ordinal();
	}
	
	public FollowMeMessage(Message msg) {
		super(msg);
		followDir = Direction.values()[msg.ints[intCounter++]];
	}
	
	public Direction getFollowDirection(){
		return followDir;
	}
	
}
