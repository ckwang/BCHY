package team017.message;

import battlecode.common.Message;

public class ScoutingInquiryMessage extends GenericMessage {
	
	private int recyclerID;
	
	public ScoutingInquiryMessage(int id) {
		super(MessageType.SCOUTING_INQUIRY_MESSAGE);
		
		msg.ints[intCounter++] = id;
	}
	
	public ScoutingInquiryMessage(Message msg) {
		super(msg);
		
		recyclerID = msg.ints[intCounter++];
	}
	
	public int getRecyclerID() {
		return recyclerID;
	}
}
