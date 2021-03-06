package team017.AI;

import java.util.Arrays;

import team017.construction.Builder;
import team017.message.MessageHandler;
import team017.navigation.GridMap;
import team017.navigation.Navigator;
import team017.util.Controllers;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public abstract class AI {

	protected Navigator navigator;
	protected Builder buildingSystem;
	protected MessageHandler msgHandler;
	protected Controllers controllers;
	
	// {NORTH, EAST, SOUTH, WEST}
	protected int[] borders = { -1, -1, -1, -1 };
	protected GridMap gridMap;
	protected MapLocation[] enemyBaseLoc = new MapLocation[3];
	protected MapLocation homeLocation;
	
	
//	protected Set<EnhancedMineInfo> enemyMines;
//	protected Set<EnhancedMineInfo> alliedMines;
//	protected Set<MapLocation> allMines;

	public AI(RobotController rc) {
		controllers = new Controllers(rc);
		homeLocation = rc.getLocation();
		controllers.updateComponents();
		
		navigator = new Navigator(controllers);
		msgHandler = new MessageHandler(controllers);
		buildingSystem = new Builder(controllers, msgHandler);
		
		gridMap = new GridMap(homeLocation);
		
//		enemyMines = new HashSet<EnhancedMineInfo>();
//		alliedMines = new HashSet<EnhancedMineInfo>();
//		allMines = new HashSet<MapLocation>();
	}

	abstract public void proceed();
	abstract protected void processMessages() throws GameActionException;
	
	public void yield() {
		msgHandler.process();
		controllers.myRC.yield();
	}
	
//	protected void senseMines() {
//		Mine[] mines = controllers.sensor.senseNearbyGameObjects(Mine.class);
//		
//		for (Mine m : mines) {
//			try {
//				Team team;
//				GameObject object = controllers.sensor.senseObjectAtLocation(m.getLocation(), RobotLevel.ON_GROUND);
//				if (object != null && controllers.sensor.senseRobotInfo ((Robot) object).chassis == Chassis.BUILDING) {
//					team = object.getTeam();
//				} else {
//					team = Team.NEUTRAL;
//				}
//				
////				EnhancedMineInfo state = new EnhancedMineInfo(team);
////				mineStates.put(m.getLocation(), state);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
	protected boolean senseBorder() {
		Direction dir = controllers.myRC.getDirection();
		boolean isDiagonal = dir.isDiagonal();
		final int orthogonal_range = (int) Math.sqrt(controllers.sensor.type().range);
		final int diagonal_range = (int) (Math.sqrt(controllers.sensor.type().range / 2));
		
		
		int range = isDiagonal ? diagonal_range : orthogonal_range;
		
		try {
			boolean hasChanged = false;

			MapLocation currentLoc = controllers.myRC.getLocation();

			int i;
			for (i = range; i > 0; i--) {
				if (controllers.myRC.senseTerrainTile(currentLoc.add(dir, i)) != TerrainTile.OFF_MAP)
					break;
			}

			// i == range means no OFF_MAP sensed
			if (i != range) {
				
				Direction[] addDirs = {Direction.NONE, Direction.NONE};
				
				if (isDiagonal) {
					Direction left = dir.rotateLeft();
					Direction right = dir.rotateRight();
					
					if (controllers.myRC.senseTerrainTile(currentLoc.add(dir, i).add(left)) == TerrainTile.OFF_MAP)
						addDirs[0] = left;
					
					if (controllers.myRC.senseTerrainTile(currentLoc.add(dir, i).add(right)) == TerrainTile.OFF_MAP)
						addDirs[1] = right;
				} else {
					addDirs[0] = dir;
				}
				
				for (int j = 0; j < 2; j++) {
					switch (addDirs[j]) {
					case NORTH:
						int newBorder = currentLoc.y - (i + 1);
						if (newBorder != borders[0]) {
							borders[0] = newBorder;
							hasChanged = true;
						}
						break;
					case EAST:
						newBorder = currentLoc.x + (i + 1);
						if (newBorder != borders[1]) {
							borders[1] = newBorder;
							hasChanged = true;
						}
						break;
					case SOUTH:
						newBorder = currentLoc.y + (i + 1);
						if (newBorder != borders[2]) {
							borders[2] = newBorder;
							hasChanged = true;
						}
						break;
					case WEST:
						newBorder = currentLoc.x - (i + 1);
						if (newBorder != borders[3]) {
							borders[3] = newBorder;
							hasChanged = true;
						}
						break;
					}
				}
			}
			
			if (hasChanged) {
				computeEnemyBaseLocation();
				gridMap.setBorders(borders);
				if (!gridMap.currentIsInbound())
					gridMap.updateScoutLocation(controllers.myRC.getLocation());
			}
			
			return hasChanged;
		} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}
		return false;
	}
	
	protected void computeEnemyBaseLocation() {
		if (borders[0] + borders[1] + borders[2] + borders[3] == -4)	return;
		
		int [] virtualBorders = Arrays.copyOf(borders, 4);
		
		for (int i = 0; i < 4; i++) {
			if (virtualBorders[i] == -1) {
				int otherValue = virtualBorders[(i+2)%4];
				if (otherValue != -1) {
					virtualBorders[i] = otherValue + ((i == 1 || i == 2) ? 60 : -60);
				} else {
					if (i % 2 == 0) {
						virtualBorders[0] = homeLocation.y - 30;
						virtualBorders[2] = homeLocation.y + 30;
					} else {
						virtualBorders[1] = homeLocation.x + 30;
						virtualBorders[3] = homeLocation.x - 30;
					}
				}
			} 
		}
		
		/*
		 * 1 ~ 2
		 * ~ # ~
		 * 4 ~ 3
		 */
		MapLocation[] corners = {
				new MapLocation(virtualBorders[3] + 5, virtualBorders[0] - 5),
				new MapLocation(virtualBorders[1] - 5, virtualBorders[0] - 5),
				new MapLocation(virtualBorders[1] - 5, virtualBorders[2] + 5),
				new MapLocation(virtualBorders[3] + 5, virtualBorders[2] + 5)
		};
		
		int x = virtualBorders[1] + virtualBorders[3] - homeLocation.x;
		int y = virtualBorders[0] + virtualBorders[2] - homeLocation.y;
		enemyBaseLoc[0] = new MapLocation(x, y);

		enemyBaseLoc[1] = enemyBaseLoc[2] = null;
		for (MapLocation corner : corners) {
			if (corner.distanceSquaredTo(enemyBaseLoc[0]) > 100 && corner.distanceSquaredTo(homeLocation) > 100) {
				if (enemyBaseLoc[1] == null)
					enemyBaseLoc[1] = corner;
				else
					enemyBaseLoc[2] = corner;
			}
		}
	}
	
	protected void roachNavigate() throws GameActionException {
		// navigate();
		if (controllers.motor.isActive())
			return;
		if (controllers.motor.canMove(controllers.myRC.getDirection())) {
			controllers.motor.moveForward();
		} else {
			Direction tempDir = controllers.myRC.getDirection();
			int rotationTimes = (Clock.getRoundNum() / 10) % 7;
			for (int i = 0; i <= rotationTimes; ++i) {
				tempDir = tempDir.rotateRight();
			}
			controllers.motor.setDirection(tempDir);
		}
	} 

}
