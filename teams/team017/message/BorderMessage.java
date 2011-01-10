package team017.message;

import battlecode.common.MapLocation;
import battlecode.common.Message;

public class BorderMessage extends GenericMessage {

	private int[] borders;
	private MapLocation homeLocation;
	
	public BorderMessage(int[] borders, MapLocation homeLocation) {
		super(MessageType.BORDER);
		
		for (int i : borders) {
			msg.ints[intCounter++] = i;
		}
		
		msg.locations[locCounter++] = homeLocation;
	}
	
	public BorderMessage(Message msg) {
		super(msg);
		
		borders = new int[4];
		
		for (int i = 0; i < 4; ++i) {
			borders[i] = msg.ints[intCounter++];
		}
	
		homeLocation = msg.locations[locCounter++];
	}
	
	public int[] getBorderDirection() {
		return borders;
	}
	
	public MapLocation getHomeLocation() {
		return homeLocation;
	}

}
