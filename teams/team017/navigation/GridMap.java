package team017.navigation;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

/**
 * The grid map is intended for recording scouting locations,
 * and whether they are already scouted
 * @author bunkie
 *
 */
public class GridMap {
	
	public MapLocation origin;
	private final int GRID_SIZE = 16;
	private final int TOTAL_LENGTH = 70;
	private final int GRID_NUM = TOTAL_LENGTH / GRID_SIZE * 2;
	private final int ROUNDED_TOTAL_LENGTH = TOTAL_LENGTH - TOTAL_LENGTH % GRID_SIZE + GRID_SIZE;
	
	public int[] gridBorders = {0, GRID_NUM + 1, GRID_NUM + 1, 0};
	public int[] internalRecords;
	
	private Grid currentScoutGrid;
	
	private class Grid {
		public int gridX;
		public int gridY;
		
		public Grid(MapLocation loc) {
			gridX = (loc.x - origin.x + ROUNDED_TOTAL_LENGTH) / GRID_SIZE;
			gridY = (loc.y - origin.y + ROUNDED_TOTAL_LENGTH) / GRID_SIZE;
		}
		
		private Grid(int gridX, int gridY) {
			this.gridX = gridX;
			this.gridY = gridY;
		}
		
		public Grid add(int x_offset, int y_offset) {
			return new Grid(gridX + x_offset, gridY + y_offset);
		}

		public MapLocation toMapLocation() {
			return new MapLocation(gridX * GRID_SIZE - ROUNDED_TOTAL_LENGTH + origin.x,
					gridY * GRID_SIZE - ROUNDED_TOTAL_LENGTH + origin.y);
		}
		
		public Grid[] getNeighbors(int d) {
			Direction currentDir = Direction.NORTH;
			
			switch (d) {
			case 1: {
				Grid[] ns = {add(0, -1), add(1, 0), add(0, 1), add(-1, 0), add(1, -1), add(1, 1), add(-1, 1), add(-1, -1)};
				return ns;
			}
			
			case 2: {
				Grid[] ns = {add(0, -2), add(2, 0), add(0, 2), add(-2, 0),
						add(1, -2), add(2, 1), add(-1, 2), add(-2, -1), add(-1, -2), add(2, -1), add(1, 2), add(-2, 1),
						add(2, -2), add(2, 2), add(-2, 2), add(-2, -2)};
				return ns;
			}
			
			case 3: {
				Grid[] ns = {add(0, -3), add(3, 0), add(0, 3), add(-3, 0),
						add(1, -3), add(3, 1), add(-1, 3), add(-3, -1), add(-1, -3), add(3, -1), add(1, 3), add(-3, 1),
						add(2, -3), add(3, 2), add(-2, 3), add(-3, -2), add(-2, -3), add(3, -2), add(2, 3), add(-3, 2),
						add(3, -3), add(3, 3), add(-3, 3), add(-3, -3)};
				return ns;
			}
			
			case 4: {
				Grid[] ns = {add(0, -4), add(4, 0), add(0, 4), add(-4, 0),
						add(1, -4), add(4, 1), add(-1, 4), add(-4, -1), add(-1, -4), add(4, -1), add(1, 4), add(-4, 1),
						add(2, -4), add(4, 2), add(-2, 4), add(-4, -2), add(-2, -4), add(4, -2), add(2, 4), add(-4, 2),
						add(3, -4), add(4, 3), add(-3, 4), add(-4, -3), add(-3, -4), add(4, -3), add(3, 4), add(-4, 3),
						add(4, -4), add(4, 4), add(-4, 4), add(-4, -4)};
				return ns;
			}
			
			}
			
			return null;
		}
	}
	
	
	public GridMap(MapLocation origin) {
		this.origin = origin;
		currentScoutGrid = new Grid(origin);
		
		internalRecords = new int[(GRID_NUM * GRID_NUM) / 32 + 1];
	}
	
	private boolean isScouted(Grid grid) {
		int total_offset = grid.gridY * GRID_NUM + grid.gridX;
		int int_num = total_offset / 32;
		int int_offset = total_offset % 32;
		
		boolean scouted = (internalRecords[int_num] & (1 << int_offset)) != 0; 
		return scouted;
	}
	
	public boolean isScouted(MapLocation loc) {
		return isScouted(new Grid(loc));
	}
	
	private void setScouted(Grid grid) {
		int total_offset = grid.gridY * GRID_NUM + grid.gridX;
		int int_num = total_offset / 32;
		int int_offset = total_offset % 32;
		
		internalRecords[int_num] |= (1 << int_offset);
	}
	
	public void setScouted(MapLocation loc) {
		setScouted(new Grid(loc));
	}
	
	private boolean isInbound(Grid grid) {
		return (grid.gridX <= gridBorders[1] && grid.gridX > gridBorders[3]) &&
		 (grid.gridY <= gridBorders[2] && grid.gridY > gridBorders[0]);
	}
	
	public void setCurrentAsScouted() {
		setScouted(currentScoutGrid);
	}
	
	
	public void setScoutLocation(MapLocation loc) {
		currentScoutGrid = new Grid(loc);
	}
	
	public void setBorders(int[] borders) {
		for (int i = 0; i < 4; i++) {
			if (borders[i] == -1) {
				gridBorders[i] = (i == 1 || i == 2) ? GRID_NUM + 1: 0;
			} else {
				gridBorders[i] = (borders[i] - ((i % 2 == 0) ? origin.y : origin.x) + ROUNDED_TOTAL_LENGTH) / GRID_SIZE;
			}
		}
	}
	
	public MapLocation getScoutLocation() {
		return currentScoutGrid.toMapLocation();
	}
	
	public MapLocation getOriginGrid() {
		Grid g = new Grid(origin);
		return new MapLocation(g.gridX, g.gridY);
	}
	
	public MapLocation getScoutGrid() {
		return new MapLocation(currentScoutGrid.gridX, currentScoutGrid.gridY);
	}
	
	public void merge(MapLocation origin, int[] borders, int[] internalRecords) {
		
		this.origin = origin;
		
		int newGridBorders[] = new int[4];
		for (int i = 0; i < 4; i++) {
			if (borders[i] == -1) {
				newGridBorders[i] = (i == 1 || i == 2) ? GRID_NUM + 1: 0;
			} else {
				newGridBorders[i] = (borders[i] - ((i % 2 == 0) ? origin.y : origin.x) + ROUNDED_TOTAL_LENGTH) / GRID_SIZE;
			}
		}
		
		for (int i = 0; i < 4; i++) {
			if ( (i == 1 || i == 2) ?
					(newGridBorders[i] < gridBorders[i]) :
					(newGridBorders[i] > gridBorders[i]) )
				gridBorders[i] = newGridBorders[i];
		}
		
		for (int i = 0; i < internalRecords.length; i++) {
			this.internalRecords[i] |= internalRecords[i];
		}
	}
	
	public void updateScoutLocation(MapLocation loc) {
		currentScoutGrid = new Grid(loc);
		updateScoutLocation();
	}
	
	public void updateScoutLocation() {
		
		for (int i = 1; i <= 4; i++) {
			Grid[] neighbors = currentScoutGrid.getNeighbors(i);
			
			for (int j = 0; j < neighbors.length; j++) {
				Grid neighbor = neighbors[j];
				if (isInbound(neighbor) && !isScouted(neighbor)) {
					currentScoutGrid = neighbor;
					
					return;
				}
			}
		}
		
	}
	
	public void printGridMap() {
		String s = "";
		for (int j = 0; j < GRID_NUM; j++) {
			for (int i = 0; i < GRID_NUM; i++) {
				s += isScouted(new Grid(i, j)) ? 1 : 0;
			}
			s += "\n";
		}
		System.out.println(s);
	}
	
}
