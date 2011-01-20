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
	
	private MapLocation origin;
	private final int GRID_SIZE = 16;
	private final int TOTAL_LENGTH = 70;
	private final int GRID_NUM = TOTAL_LENGTH / GRID_SIZE * 2;
	
	private int[] gridBorders = {0, GRID_NUM, GRID_NUM, 0};
	public int[] internalRecords;
	
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
		
		public Grid add(Direction dir, int multiple) {
			int x_offset = dir.dx * multiple;
			int y_offset = dir.dy * multiple;
			
			return new Grid(gridX + x_offset, gridY + y_offset);
		}
		
		public MapLocation toMapLocation() {
			return new MapLocation(gridX * GRID_SIZE - TOTAL_LENGTH + origin.x,
					gridY * GRID_SIZE - TOTAL_LENGTH + origin.y);
		}
		
		public Grid[] getNeighbors(int d) {
			Direction currentDir = Direction.NORTH;
			
			Grid[] neighbors = new Grid[8];
			for (int i = 0; i < 8; i++) {
				neighbors[i] = add(currentDir, d);
				currentDir = currentDir.rotateRight();
			}
			
			return neighbors;
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
		return (grid.gridX < gridBorders[1] && grid.gridX > gridBorders[3]) &&
		 (grid.gridY < gridBorders[2] && grid.gridY > gridBorders[0]);
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
				gridBorders[i] = (i == 1 || i == 2) ? TOTAL_LENGTH * 2: 0;
			} else {
				gridBorders[i] = (borders[i] - ((i % 2 == 0) ? origin.y : origin.x) + TOTAL_LENGTH) / GRID_SIZE;
			}
		}
	}
	
	public MapLocation getScoutLocation() {
		return currentScoutGrid.toMapLocation();
	}
	
	public void merge(int[] borders, int[] internalRecords) {
		
		int newGridBorders[] = new int[4];
		for (int i = 0; i < 4; i++) {
			if (borders[i] == -1) {
				newGridBorders[i] = (i == 1 || i == 2) ? TOTAL_LENGTH * 2: 0;
			} else {
				newGridBorders[i] = (borders[i] - ((i % 2 == 0) ? origin.y : origin.x) + TOTAL_LENGTH) / GRID_SIZE;
			}
		}
		
		for (int i = 0; i < 4; i++) {
			if ( (i == 1 || i == 2) ?
					(newGridBorders[i] < gridBorders[i]) :
					(newGridBorders[i] > gridBorders[i]) )
				gridBorders[i] = newGridBorders[i];
		}
		
		for (int i = 0; i < internalRecords.length; i++) {
			internalRecords[i] |= internalRecords[i];
		}
	}
	
	public void updateScoutLocation(MapLocation loc) {
		currentScoutGrid = new Grid(loc);
		updateScoutLocation();
	}
	
	public void updateScoutLocation() {
		
		for (int i = 1; i <= 5; i++) {
			Grid[] neighbors = currentScoutGrid.getNeighbors(i);
			
			for (int j = 0; j < 8; j++) {
				Grid neighbor = neighbors[j % 8];
				if (isInbound(neighbor) && !isScouted(neighbor)) {
					currentScoutGrid = neighbor;
					
					return;
				}
			}
		}
		
	}
	
	public void printGridMap() {
		String s = "";
		for (int i = 0; i < GRID_NUM; i++) {
			for (int j = 0; j < GRID_NUM; j++) {
				s += isScouted(new Grid(i, j)) ? 1 : 0;
			}
			s += "\n";
		}
		System.out.println(s);
	}
	
}
