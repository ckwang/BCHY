package team017.message;

import battlecode.common.Message;

public class BorderMessage extends GenericMessage {

	private int[] borders;
	
	public BorderMessage(int[] borders) {
		super(MessageType.BORDER);
		
		for (int i : borders) {
			msg.ints[intCounter++] = i;
		}
	}
	
	public BorderMessage(Message msg) {
		super(msg);
		
		borders = new int[4];
		
		for (int i = 0; i < 4; ++i) {
			borders[i] = msg.ints[intCounter++];
		}
	}
	
	public int[] getBorderDirection() {
		return borders;
	}

}
