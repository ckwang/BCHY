package team017.message;

import team017.util.Controllers;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public abstract class MessageHandler {
	
	protected Controllers controllers;
	
	protected int intCounter = 0;
	protected int locCounter = 0;
	protected int stringCounter = 0;
	
	protected boolean valid;
	
	protected Message msg;
	
	protected MessageHandler(Controllers controllers, MessageType type) {
		this.controllers = controllers;
		
		msg = new Message();
		msg.ints = new int[10];
		msg.locations = new MapLocation[10];
		msg.strings = new String[10];
		
		
		writeTag(type);
	}
	
	protected MessageHandler(Message msg) {
		this.msg = msg;
		valid = isValid();
	}
	
	public void send() {
		if (controllers.comm == null)
			return;
		else{
			try {
				if (!controllers.comm.isActive())
					controllers.comm.broadcast(msg);
			} catch (GameActionException e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}
	
	static public MessageType getMessageType(Message msg) {
		return MessageType.values()[msg.ints[2]];
	}
	
	private void writeTag(MessageType type) {
		int round = Clock.getRoundNum();
		int sourceID = controllers.myRC.getRobot().getID();
		MapLocation sourceLocation = controllers.myRC.getLocation();
		
		msg.ints[intCounter++] = round;
		msg.ints[intCounter++] = sourceID;
		msg.locations[locCounter++] = sourceLocation;
		msg.ints[intCounter++] = type.ordinal();
		
		// checksum
		msg.ints[intCounter++] = round + sourceID + sourceLocation.x + sourceLocation.y + type.ordinal(); 
	}
	
	/*
	 * Check if the input message is valid by inspecting the check sum
	 */
	private boolean isValid() {
		intCounter += 4;
		locCounter += 1;
		
		return msg.ints[0] + msg.ints[1] + msg.locations[0].x + msg.locations[0].y + msg.ints[2] == msg.ints[3]; 
	}
}
