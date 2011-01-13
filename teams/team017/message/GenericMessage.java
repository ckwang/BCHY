package team017.message;

import java.util.Arrays;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public abstract class GenericMessage {
	
	int intCounter = MessageHandler.INT_TAG_LENGTH;
	int locCounter = MessageHandler.LOC_TAG_LENGTH;
	int stringCounter = 0;
	
	class MessageWrap {
		public int[] ints;
		public MapLocation[] locations;
		public String[] strings;
		
		public MessageWrap() {
			ints = new int[50];
			locations = new MapLocation[50];
			strings = new String[10];
		}
		
		public MessageWrap(Message msg) {
			ints = msg.ints;
			locations = msg.locations;
			strings = msg.strings;
		}
	}
	
	MessageWrap msg;
	MessageType type;
	
	protected GenericMessage(MessageType type) {
		msg = new MessageWrap();
		
		this.type = type;
	}
	
	protected GenericMessage(Message msg) {
		this.msg = new MessageWrap(msg);
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
	
	public Message getMessage() {
		Message m = new Message();
		m.ints = Arrays.copyOf(msg.ints, intCounter);
		m.locations = (MapLocation[]) Arrays.copyOf(msg.locations, locCounter);
		m.strings = (String[]) Arrays.copyOf(msg.strings, stringCounter);
		
		return m;
	}
	
}
