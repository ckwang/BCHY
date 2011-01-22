package team017.message;

import battlecode.common.Message;

public class ScoutingInquiryMessage extends GenericMessage {
	
	private boolean isConstructor;
	
	public ScoutingInquiryMessage(boolean c) {
		super(MessageType.SCOUTING_INQUIRY_MESSAGE);
		msg.ints[intCounter++] = c ? 1 : 0;
	}
	
	public ScoutingInquiryMessage(Message msg) {
		super(msg);
		isConstructor = msg.ints[intCounter++] == 1;
	}
	
	public boolean isConstructor() {
		return isConstructor;
	}
	
}
