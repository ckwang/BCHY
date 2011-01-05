package team017.message;

import battlecode.common.BroadcastController;
import battlecode.common.Direction;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class BorderMessage extends MessageHandler {

	private Direction borderDir;
	
	public BorderMessage(RobotController rc, BroadcastController comm, Direction borderDirection) {
		super(rc, comm);
		msg.strings[0] = MessageType.BORDER.toString();
		
		borderDir = borderDirection;
		msg.strings[stringCounter++] = borderDir.toString();
	}
	
	public BorderMessage(Message msg) {
		super(msg);
		
		if ( valid )
			borderDir = Direction.valueOf( msg.strings[stringCounter] );
	}
	
	public Direction getBorderDirection() {
		return borderDir;
	}

}
