package team017.message;

import team017.navigation.GridMap;
import battlecode.common.MapLocation;
import battlecode.common.Message;

public class GridMapMessage extends GenericMessage {

	private int[] borders;
	private MapLocation homeLocation;
	private int[] internalRecords;
	
	public GridMapMessage(int[] borders, MapLocation homeLocation, GridMap gridMap) {
		super(MessageType.GRID_MAP_MESSAGE);
		
		for (int i : borders) {
			msg.ints[intCounter++] = i;
		}
		
		msg.locations[locCounter++] = homeLocation;
		
		int[] internalRecord = gridMap.internalRecords;
		msg.ints[intCounter++] = internalRecord.length;
		
		for (int i : internalRecord) {
			msg.ints[intCounter++] = i;
		}
		
	}
	
	public GridMapMessage(Message msg) {
		super(msg);
		
		borders = new int[4];
		
		for (int i = 0; i < 4; ++i) {
			borders[i] = msg.ints[intCounter++];
		}
	
		homeLocation = msg.locations[locCounter++];
		
		internalRecords = new int[msg.ints[intCounter++]];
		
		for (int i = 0; i < internalRecords.length; i++) {
			internalRecords[i] = msg.ints[intCounter++]; 
		}
	}
	
	public int[] getBorders() {
		return borders;
	}
	
	public MapLocation getHomeLocation() {
		return homeLocation;
	}
	
	public int[] getInternalRecords() {
		return internalRecords;
	}

}
