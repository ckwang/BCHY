package team017.message;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;

import team017.util.Controllers;

public class MessageHandler {

	final static int INT_TAG_LENGTH = 4;
	final static int LOC_TAG_LENGTH = 1;
	
	Set<Integer> history;
	
	Controllers controllers;
	private Queue<GenericMessage> outQueue;
	private Queue<Message> inQueue;
	
	public MessageHandler(Controllers controllers) {
		this.controllers = controllers;
		outQueue = new LinkedList<GenericMessage>();
		inQueue = new LinkedList<Message>();
		history = new HashSet<Integer>();
	}
	
	public void process() {
		// send a message
		if (outQueue.size() != 0 && controllers.comm != null && !controllers.comm.isActive()) {
			try {
				controllers.comm.broadcast(outQueue.poll().getMessage());
			} catch (GameActionException e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
		
		// receive messages
		Message[] messages = controllers.myRC.getAllMessages();
		for (Message m : messages) {
			if (isValid(m))
				inQueue.add(m);
		}
		
	}
	
	public void queueMessage(GenericMessage msg) {
		writeTag(msg);
		outQueue.add(msg);
	}
	
	public Message nextMessage() {
		return inQueue.poll();
	}
	
	public boolean hasMessage() {
		return !inQueue.isEmpty();
	}
	
	private void writeTag(GenericMessage msg) {
		int round = Clock.getRoundNum();
		int sourceID = controllers.myRC.getRobot().getID();
		MapLocation sourceLocation = controllers.myRC.getLocation();
		
		msg.msg.ints[0] = round;
		msg.msg.ints[1] = sourceID;
		msg.msg.locations[0] = sourceLocation;
		msg.msg.ints[2] = msg.type.ordinal();
		
		// checksum
		msg.msg.ints[3] = round + sourceID + sourceLocation.x + sourceLocation.y + msg.type.ordinal(); 
	}
	
	/*
	 * Check if the input message is valid by inspecting the check sum
	 */
	private boolean isValid(Message msg) {
		boolean valid = (msg.ints[0] + msg.ints[1] + msg.locations[0].x + msg.locations[0].y + msg.ints[2] == msg.ints[3])
		&& msg.ints[1] != controllers.myRC.getRobot().getID();
		
		if (valid) {
			return history.add((msg.ints[0] << 16) | msg.ints[1]);
		} else {
			return false;
		}
		
//		return (msg.ints[0] + msg.ints[1] + msg.locations[0].x + msg.locations[0].y + msg.ints[2] == msg.ints[3])
//			&& msg.ints[1] != controllers.myRC.getRobot().getID(); 
	}
	
	public MessageType getMessageType(Message msg) {
		return MessageType.values()[msg.ints[2]];
	}
	
	public void clearOutQueue() {
		outQueue.clear();
	}
}
