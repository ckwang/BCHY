package team017.message;

import java.util.LinkedList;
import java.util.Queue;

import team017.util.Controllers;
import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class MessageHandler {

	final static int INT_TAG_LENGTH = 5;
	final static int LOC_TAG_LENGTH = 1;
	
//	int clearHistoryRound = 0;
//	Set<Integer> earlyHistory;
//	Set<Integer> recentHistory;
//	
	Controllers controllers;
	int team;
	
	private Queue<GenericMessage> outQueue;
	private Queue<Message> inQueue;
	
	public MessageHandler(Controllers controllers) {
		this.controllers = controllers;
		outQueue = new LinkedList<GenericMessage>();
		inQueue = new LinkedList<Message>();
//		earlyHistory = new HashSet<Integer>();
//		recentHistory = new HashSet<Integer>();
		
		team = controllers.myRC.getTeam().ordinal();
	}
	
	public void process() {
		// send a message
		if (outQueue.size() != 0 && controllers.comm != null && !controllers.comm.isActive()) {
			try {
				controllers.comm.broadcast(outQueue.poll().getMessage());
			} catch (Exception e) {
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
	
	public void bypass(GenericMessage msg) {
		writeTag(msg);
		inQueue.add(msg.getMessage());
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
		msg.msg.ints[3] = team;
		
		// checksum
		msg.msg.ints[4] = round + sourceID + sourceLocation.x + sourceLocation.y + msg.type.ordinal(); 
	}
	
	/*
	 * Check if the input message is valid by inspecting the check sum
	 */
	private boolean isValid(Message msg) {
		if (msg.ints == null || msg.locations == null || msg.strings == null)	return false;
		
		boolean valid =  (msg.ints[3] == team) && (msg.ints[0] + msg.ints[1] + msg.locations[0].x + msg.locations[0].y + msg.ints[2] == msg.ints[4])
		&& msg.ints[1] != controllers.myRC.getRobot().getID();

//		if (Clock.getRoundNum() - clearHistoryRound >= 100) {
//			clearHistoryRound = Clock.getRoundNum();
//			earlyHistory.clear();
//			earlyHistory = recentHistory;
//			recentHistory = new HashSet<Integer>();
//		}
		
		if (valid) {
//			int tag = (msg.ints[0] << 16) | msg.ints[1];
////			return recentHistory.add(tag);
//			if (earlyHistory.contains(tag))
//				return false;
////			
//			return recentHistory.add(tag);
			return true;
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
	
	public int getOutQueueSize() {
		return outQueue.size();
	}
}
