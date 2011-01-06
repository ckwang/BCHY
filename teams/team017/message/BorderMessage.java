package team017.message;

import battlecode.common.BroadcastController;
import battlecode.common.Direction;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class BorderMessage extends MessageHandler {

	private int[] borders;
	
	public BorderMessage(RobotController rc, BroadcastController comm, int[] borders) {
		super(rc, comm, MessageType.BORDER);
		
		for (int i : borders) {
			msg.ints[intCounter++] = i;
		}
	}
	
	public BorderMessage(Message msg) {
		super(msg);
		
		if ( valid ) {
			for (int i = 0; i < 4; ++i) {
				borders[i] = msg.ints[intCounter++];
			}
		}
	}
	
	public int[] getBorderDirection() {
		return borders;
	}

}
