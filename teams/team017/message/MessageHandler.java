package team017.message;

import battlecode.common.BroadcastController;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;

public abstract class MessageHandler {
	
	protected RobotController myRC;
	protected BroadcastController comm;
	
	protected int intCounter = 0;
	protected int locCounter = 0;
	protected int stringCounter = 1;
	
	protected boolean valid;
	
	protected Message msg;
	
	protected MessageHandler(RobotController rc, BroadcastController comm) {
		myRC = rc;
		this.comm = comm;
		
		msg = new Message();
		writeTag();
	}
	
	protected MessageHandler(Message msg) {
		this.msg = msg;
		valid = isValid();
	}
	
	public void send() {
		try {
			if (!comm.isActive())
				comm.broadcast(msg);
		} catch (GameActionException e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}
	}
	
	public MessageType getMessageType() {
		return MessageType.valueOf( msg.strings[0] );
	}
	
	private void writeTag() {
		int round = Clock.getRoundNum();
		int sourceID = myRC.getRobot().getID();
		MapLocation sourceLocation = myRC.getLocation();
		
		msg.ints[intCounter++] = round;
		msg.ints[intCounter++] = sourceID;
		msg.locations[locCounter++] = sourceLocation;
		
		// checksum
		msg.ints[intCounter++] = round + sourceID + sourceLocation.x + sourceLocation.y; 
	}
	
	/*
	 * Check if the input message is valid by inspecting the check sum
	 */
	private boolean isValid() {
		intCounter += 3;
		locCounter += 2;
		
		return msg.ints[0] + msg.ints[1] + msg.locations[0].x + msg.locations[1].y == msg.ints[2]; 
	}
}
