package team017.message;

import team017.util.Controllers;
import battlecode.common.Message;

public class BorderMessage extends MessageHandler {

	private int[] borders;
	
	public BorderMessage(Controllers controllers, int[] borders) {
		super(controllers, MessageType.BORDER);
		
		for (int i : borders) {
			msg.ints[intCounter++] = i;
		}
	}
	
	public BorderMessage(Message msg) {
		super(msg);
		
		borders = new int[4];
		
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
