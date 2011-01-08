package team017.message;

import battlecode.common.MapLocation;
import battlecode.common.Message;

public abstract class GenericMessage {
	
	int intCounter = MessageHandler.INT_TAG_LENGTH;
	int locCounter = MessageHandler.LOC_TAG_LENGTH;
	int stringCounter = 0;
	
	Message msg;
	MessageType type;
	
	protected GenericMessage(MessageType type) {
		msg = new Message();
		msg.ints = new int[10];
		msg.locations = new MapLocation[10];
		msg.strings = new String[10];
		
		this.type = type;
	}
	
	protected GenericMessage(Message msg) {
		this.msg = msg;
	}
	
	public int getRoundNum() {
		return msg.ints[0];
	}
	
	public int getSourceID() {
		return msg.ints[1];
	}
	
	public MapLocation getSourceLocation() {
		return msg.locations[0];
	}
	
}
