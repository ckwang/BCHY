package team017.navigation;

import battlecode.common.MapLocation;


/**
 * The grid map is intended for recording scouting locations,
 * and whether they are already scouted
 * @author bunkie
 *
 */
public class GridMap {
	private MapLocation origin;
	private final int GRID_SIZE = 5;
	private final int TOTAL_LENGTH = 70;
	private final int GRID_NUM = TOTAL_LENGTH / GRID_SIZE * 2;
	
	private int[] gridBorders = {0, GRID_NUM, GRID_NUM, 0};
	int[] internal_records;
	
	private Grid currentScoutGrid;
	
	private class Grid {
		public int gridX;
		public int gridY;
		
		public Grid(MapLocation loc) {
			gridX = (loc.x - origin.x + TOTAL_LENGTH) / GRID_SIZE;
			gridY = (loc.y - origin.y + TOTAL_LENGTH) / GRID_SIZE;
		}
		
		private Grid(int gridX, int gridY) {
			this.gridX = gridX;
			this.gridY = gridY;
		}
		
		public Grid add(int x_offset, int y_offset) {
			return new Grid(gridX + x_offset, gridY + y_offset);
		}
		
		public MapLocation toMapLocation() {
			return new MapLocation(gridX * GRID_SIZE - TOTAL_LENGTH + origin.x,
					gridY * GRID_SIZE - TOTAL_LENGTH + origin.y);
		}
		
		public Grid[] getNeighbors(int d) {
			// NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST, NORTH
			Grid[] neighbors = {add(0, -d), add(d, -d), add(d, 0), add(d, d), add(0, d), add(-d, d), add(-d, 0), add(-d, -d)};
			
			return neighbors;
		}
	}
	
	
	public GridMap(MapLocation origin) {
		this.origin = origin;
		currentScoutGrid = new Grid(origin);
		
		internal_records = new int[(GRID_NUM * GRID_NUM) / 32 + 1];
	}
	
	private boolean isScouted(Grid grid) {
		int total_offset = grid.gridY * GRID_NUM + grid.gridX;
		int int_num = total_offset / 32;
		int int_offset = total_offset % 32;
		
		return (internal_records[int_num] & (1 << int_offset)) != 0;
	}
	
	private void setScouted(Grid grid) {
		int total_offset = grid.gridY * GRID_NUM + grid.gridX;
		int int_num = total_offset / 32;
		int int_offset = total_offset % 32;
		
		internal_records[int_num] |= (1 << int_offset);
	}
	
	private boolean isInbound(Grid grid) {
		return (grid.gridX < gridBorders[1] && grid.gridX > gridBorders[3]) &&
		 (grid.gridY < gridBorders[2] && grid.gridY > gridBorders[0]);
	}
	
	public void setCurrentAsScouted() {
		setScouted(currentScoutGrid);
	}
	
	public void setBorders(int[] borders) {
		for (int i = 0; i < 4; i++) {
			if (borders[i] == -1) {
				gridBorders[i] = (i == 1 || i == 2) ? TOTAL_LENGTH * 2: 0;
			} else {
				gridBorders[i] = (borders[i] - ((i % 2 == 0) ? origin.y : origin.x) + TOTAL_LENGTH) / GRID_SIZE;
			}
		}
	}
	
	public MapLocation getScoutLocation() {
		return currentScoutGrid.toMapLocation();
	}
	
	public void updateScoutLocation(int seed) {
		
		for (int i = 1; i <= 5; i++) {
			Grid[] neighbors = currentScoutGrid.getNeighbors(i);
			
			for (int j = 0; j < 8; j++) {
				Grid neighbor = neighbors[(seed + j) % 8];
				if (isInbound(neighbor) && !isScouted(neighbor)) {
					currentScoutGrid = neighbor;
					return;
				}
			}
		}
	}
	
}
