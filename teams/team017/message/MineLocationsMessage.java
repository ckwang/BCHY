package team017.message;

import java.util.HashSet;
import java.util.Set;

import battlecode.common.MapLocation;
import battlecode.common.Message;

public class MineLocationsMessage extends GenericMessage {

	Set<MapLocation> emptyMineLocations;
	Set<MapLocation> alliedMineLocations;
	Set<MapLocation> enemyMineLocations;
	
	public MineLocationsMessage(Set<MapLocation> emptyMineLocations, Set<MapLocation> alliedRecyclerLocations, Set<MapLocation> enemyRecyclerLocations) {
		super(MessageType.MINE_LOCATIONS_MESSAGE);
		
		msg.ints[intCounter++] = emptyMineLocations.size();
		msg.ints[intCounter++] = alliedRecyclerLocations.size();
		msg.ints[intCounter++] = enemyRecyclerLocations.size();
		
		for (MapLocation loc : emptyMineLocations) {
			msg.locations[locCounter++] = loc;
		}
		for (MapLocation loc : alliedRecyclerLocations) {
			msg.locations[locCounter++] = loc;
		}
		for (MapLocation loc : enemyRecyclerLocations) {
			msg.locations[locCounter++] = loc;
		}
		
	}
	
	public MineLocationsMessage(Message msg) {
		super(msg);
		emptyMineLocations = new HashSet<MapLocation>();
		alliedMineLocations = new HashSet<MapLocation>();
		enemyMineLocations = new HashSet<MapLocation>();
		
		int size = msg.ints[intCounter++];
		for (int i = 0; i < size; i++) {
			emptyMineLocations.add(msg.locations[locCounter++]);
		}
		
		size = msg.ints[intCounter++];
		for (int i = 0; i < size; i++) {
			alliedMineLocations.add(msg.locations[locCounter++]);
		}
		
		size = msg.ints[intCounter++];
		for (int i = 0; i < size; i++) {
			enemyMineLocations.add(msg.locations[locCounter++]);
		}
	}

	public Set<MapLocation> getEmptyMineLocations() {
		return emptyMineLocations;
	}
	
	public Set<MapLocation> getAlliedMineLocations() {
		return alliedMineLocations;
	}
	
	public Set<MapLocation> getEnemyMineLocations() {
		return enemyMineLocations;
	}
}
