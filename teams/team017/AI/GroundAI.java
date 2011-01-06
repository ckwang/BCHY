package team017.AI;

import java.util.HashSet;
import java.util.Set;

import team017.message.BorderMessage;
import team017.message.MessageHandler;
import team017.message.MessageType;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Mine;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class GroundAI extends AI {

	Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	private boolean isSoldier;
	private boolean isConstructor;
	
	public GroundAI(RobotController rc) {
		super(rc);
	}

	public void proceed() {

		//Initial movement
		if (Clock.getRoundNum() == 0) {
			init();
			init_revolve();
		}

		while (true) {
			
			// MessageHandler encoder = new BorderMessage(myRC, comm,
			// Direction.NORTH);
			// encoder.send();
			//
			// Message incomingMsg;
			// MessageHandler decoder = new BorderMessage(incomingMsg);
			// decoder.getMessageType();
			// ((BorderMessage) decoder).getBorderDirection();

			try {

				/*** beginning of main loop ***/
				if (!motor.isActive()) {
					// navigate();
					if (motor.canMove(myRC.getDirection())) {
						// System.out.println("about to move");
						motor.moveForward();
					} else {
						motor.setDirection(myRC.getDirection().rotateRight());
					}
				}
				
				if (isSoldier){
					//attack();
				}
				
				if (isConstructor){
					//build();
				}

				evaluateNextState();
				myRC.yield();

				/*** end of main loop ***/
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}

	private void init() {
			try {
				for (int i = 0; i < 4; ++i){
					sense_border();
					// Rotate twice Right for a 90 degrees turn
					motor.setDirection(myRC.getDirection().rotateRight().rotateRight());
					updateMineSet(mineLocations);
					myRC.yield();
					myRC.setIndicatorString(0, borders[0] + "," + borders[1] + "," + borders[2] + "," + borders[3]);
					myRC.setIndicatorString(1,myRC.getLocation() + "");
				}
			} catch (GameActionException e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		

	}

	private void sense_border(){
		try {
			int i = 4;
			
			if (myRC.getDirection().isDiagonal()) {
				// Sense whether the farthest sensible place is OFF_MAP
				Direction addDir = myRC.getDirection().rotateLeft();
				MapLocation currentLoc = myRC.getLocation();

				for (i = 3; i > 0; i--) {
					if (myRC.senseTerrainTile(currentLoc.add(addDir,i)) != TerrainTile.OFF_MAP)
						break;
				}

				// i == 3 means no OFF_MAP sensed
				if (i != 3) {
					switch (addDir) {
					case NORTH:
						borders[0] = currentLoc.y - (i + 1);
						break;
					case EAST:
						borders[1] = currentLoc.x + (i + 1);
						break;
					case SOUTH:
						borders[2] = currentLoc.y + (i + 1);
						break;
					case WEST:
						borders[3] = currentLoc.x - (i + 1);
						break;
					}
				}
				
				addDir = myRC.getDirection().rotateRight();
				
				for (i = 3; i > 0; i--) {
					if (myRC.senseTerrainTile(currentLoc.add(addDir,i)) != TerrainTile.OFF_MAP)
						break;
				}

				// i = 3 means no OFF_MAP sensed
				if (i != 3) {
					switch (addDir) {
					case NORTH:
						borders[0] = currentLoc.y - (i + 1);
						break;
					case EAST:
						borders[1] = currentLoc.x + (i + 1);
						break;
					case SOUTH:
						borders[2] = currentLoc.y + (i + 1);
						break;
					case WEST:
						borders[3] = currentLoc.x - (i + 1);
						break;
					}
				}

			}
			else {
				
				// Sense whether the farthest sensible place is OFF_MAP
				Direction addDir = myRC.getDirection();
				MapLocation currentLoc = myRC.getLocation();

				for (i = 3; i > 0; i--) {
					if (myRC.senseTerrainTile(currentLoc.add(addDir,i)) != TerrainTile.OFF_MAP)
						break;
				}

				// i == 3 means no OFF_MAP sensed
				if (i != 3) {
					switch (addDir) {
					case NORTH:
						borders[0] = currentLoc.y - (i + 1);
						break;
					case EAST:
						borders[1] = currentLoc.x + (i + 1);
						break;
					case SOUTH:
						borders[2] = currentLoc.y + (i + 1);
						break;
					case WEST:
						borders[3] = currentLoc.x - (i + 1);
						break;
					}
				}
			}
		} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}
		
	}
	
	
	private void init_revolve() {
		
		MapLocation[] locationList = {
				homeLocation.add(Direction.NORTH_EAST, 2),
				homeLocation.add(Direction.SOUTH_EAST, 2),
				homeLocation.add(Direction.SOUTH_WEST, 2),
				homeLocation.add(Direction.NORTH_WEST, 2)};
		int index = 0;
		
		while (true) {
			try {
				
				navigator.setDestination(locationList[index]);
				myRC.setIndicatorString(2, myRC.getLocation().toString()+locationList[index].toString());
				
				Direction nextDir = navigator.getNextDir();
				if (nextDir == Direction.OMNI) {
					index++;
					if (index == 4)	return;
				
					continue;
				}
				
				if (!motor.isActive() && motor.canMove(nextDir) ) {
					if ( myRC.getDirection() == nextDir ) {
						// System.out.println("about to move");
						motor.moveForward();
					} else {
						motor.setDirection(myRC.getDirection().rotateRight());
					}
				}
				
				myRC.yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}
	
	private void updateMineSet(Set<MapLocation> mineSet) {
		
		Mine[] minelist = sensor.senseNearbyGameObjects(Mine.class);
		for (Mine mine : minelist){
			mineSet.add(mine.getLocation());
		}
	}
	
	private void evaluateNextState(){
		
	}
	
	private void attack(){
		
	}
	
	private void naviagate(){
		
	}
}
