package team017.message;

import battlecode.common.Clock;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class MessageEncoder {

	private RobotController myRC;
	
	public MessageEncoder(RobotController rc) {
		myRC = rc;
	}
	
	public void send() {
		Message msg = new Message();
		
		writeTag(msg);
		
		
	}
	
	private void writeTag(Message msg) {
		int round = Clock.getRoundNum();
		int sourceID = myRC.getRobot().getID();
	}
	
}
