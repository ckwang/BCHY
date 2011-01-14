package team017.message;

import battlecode.common.Direction;
import battlecode.common.Message;

public class FollowMeMessage extends GenericMessage {
	private Direction followDir;
	private int commRange;
	public FollowMeMessage(Direction followDir, int commRange) {
		super(MessageType.FOLLOW_ME_MESSAGE);
		msg.ints[intCounter++] = followDir.ordinal();
		msg.ints[intCounter++] = commRange;
	}
	
	public FollowMeMessage(Message msg) {
		super(msg);
		followDir = Direction.values()[msg.ints[intCounter++]];
		commRange = msg.ints[intCounter++];
	}
	
	public Direction getFollowDirection(){
		return followDir;
	}
	
	public int getCommRange () {
		return commRange;
	}
	
}
