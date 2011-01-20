package team017.message;

import java.util.HashSet;
import java.util.Set;

import battlecode.common.MapLocation;
import battlecode.common.Message;

public class MineLocationsMessage extends GenericMessage {

	Set<MapLocation> mineLocations;
	
	public MineLocationsMessage(Set<MapLocation> mineLocations) {
		super(MessageType.MINE_LOCATIONS_MESSAGE);
		
		msg.ints[intCounter++] = mineLocations.size();
		for (MapLocation loc : mineLocations) {
			msg.locations[locCounter++] = loc;
		}
	}
	
	public MineLocationsMessage(Message msg) {
		super(msg);
		mineLocations = new HashSet<MapLocation>();
		
		int size = msg.ints[intCounter++];
		for (int i = 0; i < size; i++) {
			mineLocations.add(msg.locations[locCounter++]);
		}
	}

	public Set<MapLocation> getMineLocations() {
		return mineLocations;
	}
}
