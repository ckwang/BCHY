package team017.message;

import battlecode.common.BroadcastController;
import battlecode.common.Clock;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class MessageEncoder {

	private RobotController myRC;
	private BroadcastController comm;
	
	public MessageEncoder(RobotController rc, BroadcastController comm) {
		myRC = rc;
		this.comm = comm;
	}
	
	public void send() {
		Message msg = new Message();
		
		writeTag(msg);
		
		try {
			comm.broadcast(msg);
		} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}
	}
	
	private void writeTag(Message msg) {
		int round = Clock.getRoundNum();
		int sourceID = myRC.getRobot().getID();
		
		int mask = 0x00001111;
		
		msg.ints[0] = (round & mask) | (sourceID << 16);
	}
	
}
