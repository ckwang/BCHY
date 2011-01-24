package team017.message;

import battlecode.common.Message;

public class HasArrivedMessage extends GenericMessage {
	
	private boolean isMine;
	
	public HasArrivedMessage(boolean isMine) {
		super(MessageType.HAS_ARRIVED_MESSAGE);

		msg.ints[intCounter++] = isMine ? 1 : 0;
	}
	
	public HasArrivedMessage(Message msg) {
		super(msg);
		
		isMine = msg.ints[intCounter++] == 1;
	}

	
	public boolean isMine() {
		return isMine;
	}
}
