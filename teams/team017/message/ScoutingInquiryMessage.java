package team017.message;

import battlecode.common.Message;

public class ScoutingInquiryMessage extends GenericMessage {
	
	public ScoutingInquiryMessage() {
		super(MessageType.SCOUTING_INQUIRY_MESSAGE);
	}
	
	public ScoutingInquiryMessage(Message msg) {
		super(msg);
	}
	
}
