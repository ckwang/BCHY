package team017.navigation;

import team017.util.Controllers;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.TerrainTile;


/**
 * The grid map is intended for recording scouting locations,
 * and whether they are already scouted
 * @author bunkie
 *
 */
public class GridMap {
	
	private Controllers controllers;
	
	private MapLocation origin;
	private final int GRID_SIZE = 5;
	private final int TOTAL_LENGTH = 70;
	private final int GRID_NUM = TOTAL_LENGTH / GRID_SIZE * 2;
	
	private int[] gridBorders = {0, GRID_NUM, GRID_NUM, 0};
	public int[] internalRecords;
	
	private Grid currentScoutGrid;
	private int assignedRound;
	
	private Direction startDir;
	private boolean rightward;
	
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
			Direction currentDir = startDir;
			
			Grid[] neighbors = new Grid[8];
			for (int i = 0; i < 8; i++) {
				neighbors[i] = add(currentDir, d);
				currentDir = rightward ? currentDir.rotateRight() : currentDir.rotateLeft();
			}
			
			return neighbors;
		}
	}
	
	
	public GridMap(Controllers controllers, MapLocation origin) {
		this.controllers = controllers;
		this.origin = origin;
		currentScoutGrid = new Grid(origin);
		
		internalRecords = new int[(GRID_NUM * GRID_NUM) / 32 + 1];
		rightward = (Clock.getRoundNum() + controllers.myRC.getRobot().getID()) % 2 == 0 ? true : false;
		startDir = controllers.myRC.getDirection();
		int max = (Clock.getRoundNum() + controllers.myRC.getRobot().getID()) % 8;
		for (int i = 0; i < max; i++) {
			startDir = startDir.rotateRight();
		}
	}
	
	private boolean isScouted(Grid grid) {
		int total_offset = grid.gridY * GRID_NUM + grid.gridX;
		int int_num = total_offset / 32;
		int int_offset = total_offset % 32;
		
		boolean scouted = (internalRecords[int_num] & (1 << int_offset)) != 0; 
		if (!scouted) {
			MapLocation gridLoc = grid.toMapLocation();
			if (controllers.myRC.senseTerrainTile(gridLoc) != null &&
					controllers.myRC.senseTerrainTile(gridLoc.add(2, 2)) != null) {
				setScouted(grid);
				return true;
			}
		}
		
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
	
	public void setBorders(int[] borders, MapLocation homeLoc, MapLocation enemyLoc) {
		for (int i = 0; i < 4; i++) {
			if (borders[i] == -1) {
				gridBorders[i] = (i == 1 || i == 2) ? TOTAL_LENGTH * 2: 0;
			} else {
				gridBorders[i] = (borders[i] - ((i % 2 == 0) ? origin.y : origin.x) + TOTAL_LENGTH) / GRID_SIZE;
			}
		}
		
		startDir = homeLoc.directionTo(enemyLoc);
		if (homeLoc.equals(enemyLoc)) {
			startDir = controllers.myRC.getDirection();
			int max = (Clock.getRoundNum() + controllers.myRC.getRobot().getID()) % 8;
			for (int i = 0; i < max; i++) {
				startDir = startDir.rotateRight();
			}
		} else if (startDir.isDiagonal()) {
			startDir = startDir.rotateLeft();
			int max = (Clock.getRoundNum() + controllers.myRC.getRobot().getID()) % 3;
			for (int i = 0; i < max; i++) {
				startDir = startDir.rotateRight();
			}
		} else {
			startDir = startDir.rotateLeft().rotateLeft();
			int max = (Clock.getRoundNum() + controllers.myRC.getRobot().getID()) % 5;
			for (int i = 0; i < max; i++) {
				startDir = startDir.rotateRight();
			}
		}

		
	}
	
	public MapLocation getScoutLocation() {
		controllers.myRC.setIndicatorString(0, startDir.toString());
		
		// if the scout location is too old or shown to be void
		if (Clock.getRoundNum() - assignedRound > 150 || controllers.myRC.senseTerrainTile(currentScoutGrid.toMapLocation()) == TerrainTile.VOID) {
			setCurrentAsScouted();
			setScoutLocation(controllers.myRC.getLocation());
		}
		
		// if we're standing at the spot
		if (controllers.myRC.getLocation().distanceSquaredTo(currentScoutGrid.toMapLocation()) <= 4) {
			setCurrentAsScouted();
			updateScoutLocation();
			while(controllers.motor.isActive())
				controllers.myRC.yield();
			
			Direction currentDir = controllers.myRC.getDirection();
			for (int i = 0; i < 4; i++) {
				currentDir = currentDir.rotateRight().rotateRight();
				try {
					controllers.motor.setDirection(currentDir);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				controllers.myRC.yield();
			}
		}
		
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
		int roundNum = Clock.getRoundNum();
		int randomness = (roundNum + controllers.myRC.getRobot().getID()) % 3 + 7;
		
		for (int i = 1; i <= 5; i++) {
			Grid[] neighbors = currentScoutGrid.getNeighbors(i);
			
			for (int j = 0; j < 8; j++) {
				Grid neighbor = neighbors[(randomness + j) % 8];
				if (isInbound(neighbor) && !isScouted(neighbor)) {
					currentScoutGrid = neighbor;
					assignedRound = roundNum;
					
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
