package team017.message;

import battlecode.common.Direction;
import battlecode.common.Message;

public class ScoutingResponseMessage extends GenericMessage {
	private int telescoperID;
	private Direction direction;
	private boolean branch;
	private boolean leftward;

	
	public ScoutingResponseMessage(int id, Direction dir, boolean branch, boolean leftward) {
		super(MessageType.SCOUTING_RESPONSE_MESSAGE);
		
		msg.ints[intCounter++] = id;
		msg.ints[intCounter++] = dir.ordinal();
		msg.ints[intCounter++] = branch ? 1 : 0;
		msg.ints[intCounter++] = leftward ? 1 : 0;

	}
	
	public ScoutingResponseMessage(Message msg) {
		super(msg);

		telescoperID = msg.ints[intCounter++];
		direction = Direction.values()[msg.ints[intCounter++]];
		branch = msg.ints[intCounter++] == 1;
		leftward = msg.ints[intCounter++] == 1;
	}
	
	public int getTelescoperID() {
		return telescoperID;
	}
	
	public Direction getScoutingDirection() {
		return direction;
	}
	
	public boolean isBranch() {
		return branch;
	}
	
	public boolean isLeftward() {
		return leftward;
	}
}
