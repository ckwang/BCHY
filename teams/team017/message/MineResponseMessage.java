package team017.message;

import java.util.HashSet;
import java.util.Set;

import battlecode.common.MapLocation;
import battlecode.common.Message;

public class MineResponseMessage extends GenericMessage {
	private int constructorID;
	Set<MapLocation> mineLocations;
	Set<MapLocation> blockedLocations;
	
	public MineResponseMessage(int id, Set<MapLocation> mineLocations, Set<MapLocation> blockedLocations) {
		super(MessageType.MINE_RESPONSE_MESSAGE);
		
		msg.ints[intCounter++] = id;
		msg.ints[intCounter++] = mineLocations.size();
		
		for (MapLocation loc : mineLocations) {
			msg.locations[locCounter++] = loc;
		}
		if (blockedLocations == null) {
			msg.ints[intCounter++] = 0;
		} else {
			msg.ints[intCounter++] = blockedLocations.size();
			for (MapLocation loc : blockedLocations) {
				msg.locations[locCounter++] = loc;
			}
		}
		
		
	}
	
	public MineResponseMessage(Message msg) {
		super(msg);

		constructorID = msg.ints[intCounter++];
		mineLocations = new HashSet<MapLocation>();
		
		int size = msg.ints[intCounter++];
		for (int i = 0; i < size; i++) {
			mineLocations.add(msg.locations[locCounter++]);
		}
		
		size = msg.ints[intCounter++];
		if (size == 0) {
			blockedLocations = null;
		} else {
			blockedLocations = new HashSet<MapLocation>();

			for (int i = 0; i < size; i++) {
				blockedLocations.add(msg.locations[locCounter++]);
			}
		}
	}
	
	public int getConstructorID() {
		return constructorID;
	}
	
	public Set<MapLocation> getMineLocations() {
		return mineLocations;
	}
	
	public Set<MapLocation> getBlockedLocations() {
		return blockedLocations;
	}
}
