package team017.message;

import battlecode.common.Message;

public class MineInquiryMessage extends GenericMessage {
	
	public MineInquiryMessage() {
		super(MessageType.MINE_INQUIRY_MESSAGE);
		
	}
	
	public MineInquiryMessage(Message msg) {
		super(msg);
		
	}
}
