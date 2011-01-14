package team017.navigation;

import battlecode.common.MapLocation;

public class Map {
	private static final int SIZE = 70;
	
	private MapLocation origin;

	int[][] score;
	
	public Map(MapLocation origin) {
		this.origin = origin;
		score = new int[SIZE * 2][SIZE * 2];
	}
	
	
	private int getGridX(MapLocation loc) {
		return loc.x - origin.x + SIZE;
	}
	
	private int getGridY(MapLocation loc) {
		return loc.y - origin.y + SIZE;
	}
	
	public int getScore(MapLocation loc) {
		return score[getGridX(loc)][getGridY(loc)];
	}
	
	public void setScore(MapLocation loc, int score) {
		this.score[getGridX(loc)][getGridY(loc)] = score;
	}
}
