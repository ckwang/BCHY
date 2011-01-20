package team017.navigation;

import battlecode.common.MapLocation;

public class InfoMap {
	private static final int SIZE = 70;
	
	private MapLocation origin;

	int[] internalRecords;
		
	public InfoMap(MapLocation origin) {
		this.origin = origin;
		
		internalRecords = new int[(SIZE * SIZE * 4) / 32 + 1];
	}
	
	
	private int getGridX(MapLocation loc) {
		return loc.x - origin.x + SIZE;
	}
	
	private int getGridY(MapLocation loc) {
		return loc.y - origin.y + SIZE;
	}
	
	public boolean isBlocked(MapLocation loc) {
		int total_offset = getGridY(loc) * SIZE + getGridX(loc);
		int int_num = total_offset / 32;
		int int_offset = total_offset % 32;
		
		return (internalRecords[int_num] & (1 << int_offset)) != 0;
	}
	
	public void setBlocked(MapLocation loc, boolean blocked) {
		int total_offset = getGridY(loc) * SIZE + getGridX(loc);
		int int_num = total_offset / 32;
		int int_offset = total_offset % 32;
		
		if (blocked == true) { 
			internalRecords[int_num] |= (1 << int_offset);
		} else {
			internalRecords[int_num] &= ~(1 << int_offset);
		}
	}
	
	
}
