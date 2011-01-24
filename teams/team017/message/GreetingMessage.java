package team017.message;

import battlecode.common.Message;

public class GreetingMessage extends GenericMessage {
	
	private boolean isConstructor;
	
	public GreetingMessage(boolean isConstructor) {
		super(MessageType.GREETING_MESSAGE);
		
		msg.ints[intCounter++] = isConstructor ? 1 : 0;
	}
	
	public GreetingMessage(Message msg) {
		super(msg);
		
		isConstructor = msg.ints[intCounter++] == 1 ? true : false;
	}
	
	public boolean isConstructor() {
		return isConstructor;
	}
}
