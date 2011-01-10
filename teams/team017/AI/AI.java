package team017.AI;

import java.util.Arrays;

import team017.construction.Builder;
import team017.message.MessageHandler;
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
	protected MapLocation[] enemyBaseLoc = new MapLocation[3];
	protected MapLocation homeLocation;

	public AI(RobotController rc) {
		controllers = new Controllers();
		
		controllers.myRC = rc;
		homeLocation = rc.getLocation();
		controllers.updateComponents();
		
		navigator = new Navigator(controllers);
		msgHandler = new MessageHandler(controllers);
		buildingSystem = new Builder(controllers, msgHandler);
	}

	abstract public void proceed();
	abstract protected void processMessages() throws GameActionException;
	
	public void yield() {
		controllers.myRC.yield();
		msgHandler.process();
	}
	
	protected void sense_border() {
		try {
			boolean hasChanged = false;
			
			Direction[] addDirs = new Direction[3];

			if (controllers.myRC.getDirection().isDiagonal()) {
				addDirs[0] = controllers.myRC.getDirection().rotateLeft();
				addDirs[1] = controllers.myRC.getDirection().rotateRight();
			} else {
				addDirs[0] = controllers.myRC.getDirection();
			}

			int j = -1;
			while (addDirs[++j] != null) {
				MapLocation currentLoc = controllers.myRC.getLocation();

				int i;
				for (i = 3; i > 0; i--) {
					if (controllers.myRC.senseTerrainTile(currentLoc.add(
							addDirs[j], i)) != TerrainTile.OFF_MAP)
						break;
				}

				// i == 3 means no OFF_MAP sensed
				if (i != 3) {
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
			
			if (hasChanged)
				computeEnemyBaseLocation();
		} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}

	}
	
	protected void computeEnemyBaseLocation() {

		final int[] xmapping = {0, -1, 0, 1};
		final int[] ymapping = {1, 0, -1, 0};
		
		int [] virtualBorders = Arrays.copyOf(borders, 4);
		
		int nearbyNum = 0;
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
			} else {
				if (Math.abs(((i % 2 == 0) ? homeLocation.y : homeLocation.x) - virtualBorders[i]) <= 8) {
					nearbyNum++;
				}
			}
		}
		
		boolean atCorner = nearbyNum >= 2;
		
		int x, y;
		
		// if corner case
//		if (atCorner) {
			x = virtualBorders[1] + virtualBorders[3] - homeLocation.x;
			y = virtualBorders[0] + virtualBorders[2] - homeLocation.y;
			
			enemyBaseLoc[0] = new MapLocation(x, y);
//		} else {
//			
//		}
		
//		int x = homeLocation.x;
//		int y = homeLocation.y;
//		
//		for (int index = 0; index < 4; index++){
//			if (borders[index] != -1){
//				x = xmapping[index] == 0 ? x : 2 * borders[index] + 60 * xmapping[index] - x;
//				y = ymapping[index] == 0 ? y : 2 * borders[index] + 60 * ymapping[index] - y;
//			}
//		}
//		
//		if (borders[0] != -1 && borders[2] != -1 ) {
//			y = borders[0] + borders[2] - homeLocation.y;
//		} else if (borders[1] != -1 && borders[3] != -1) {
//			x = borders[1] + borders[3] - homeLocation.x;
//		}
//		
//		enemyBaseLoc = new MapLocation(x, y);
	}
	
	protected void roachNavigate() throws GameActionException {
		// navigate();
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
