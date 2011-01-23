package team017.message;

import battlecode.common.Direction;
import battlecode.common.Message;

public class PatrolDirectionMessage extends GenericMessage {
	
	Direction patrolDirection;
	boolean leftward;
	
	public PatrolDirectionMessage(Direction dir, boolean leftward) {
		super(MessageType.PATROL_DIRECTION_MESSAGE);
		
		msg.ints[intCounter++] = dir.ordinal();
		msg.ints[intCounter++] = leftward ? 1 : 0;
	}
	
	public PatrolDirectionMessage(Message msg) {
		super(msg);
		
		patrolDirection = Direction.values()[msg.ints[intCounter++]];
		leftward = msg.ints[intCounter++] == 1 ? true : false;
	}
	
	public Direction getPatrolDirection() {
		return patrolDirection;
	}
	
	public boolean isLeftward() {
		return leftward;
	}
	
}
