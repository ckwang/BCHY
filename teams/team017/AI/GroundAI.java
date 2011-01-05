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
		if (Clock.getRoundNum() == 0)
			init();

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
		for (int i = 0; i < 4; ++i){
			init_rotation();
			updateMineSet(mineLocations);
			myRC.yield();
		}

	}

	private void init_rotation() {
		// Initial Rotation
		// Turn around and search for borders
		int [] temp = new int[4];
		try {
			MapLocation[] temploclist = new MapLocation[4];
			TerrainTile tempterrain = TerrainTile.LAND;

			int i = 4;
			if (myRC.getDirection().isDiagonal()) {
				// Sense whether the farthest sensible place is OFF_MAP
				temploclist[0] = myRC.getLocation();
				for (int j = 1; j < 4; ++j) {
					temploclist[j] = temploclist[j - 1].add(myRC
							.getDirection().rotateLeft());
				}
				tempterrain = TerrainTile.LAND;

				do {
					i = i - 1;
					tempterrain = myRC.senseTerrainTile(temploclist[i]);
				} while (i > 0 && tempterrain == TerrainTile.OFF_MAP);

				// i = 3 means no OFF_MAP sensed
				if (i != 3) {
					switch (myRC.getDirection().rotateLeft()) {
					case NORTH:
						borders[0] = myRC.getLocation().y + myRC.getDirection().dy * (i + 1);
						break;
					case EAST:
						borders[1] = myRC.getLocation().x + myRC.getDirection().dx * (i + 1);
						break;
					case SOUTH:
						borders[2] = myRC.getLocation().y + myRC.getDirection().dy * (i + 1);
						break;
					case WEST:
						borders[3] = myRC.getLocation().x + myRC.getDirection().dx * (i + 1);
						break;
					}
				}
				motor.setDirection(myRC.getDirection().rotateRight());
			}
			else {
				// Sense whether the farthest sensible place is OFF_MAP
				temploclist[0] = myRC.getLocation();
				for (int j = 1; j < 4; ++j) {
					temploclist[j] = temploclist[j - 1].add(myRC
							.getDirection());
				}
				tempterrain = TerrainTile.LAND;
				do {
					i = i - 1;
					tempterrain = myRC.senseTerrainTile(temploclist[i]);
				} while (i > 0 && tempterrain == TerrainTile.OFF_MAP);
				// i = 3 means no OFF_MAP sensed
				if (i != 3) {
					switch (myRC.getDirection()) {
					case NORTH:
						borders[0] = myRC.getLocation().y + myRC.getDirection().dy * (i + 1);
						break;
					case EAST:
						borders[1] = myRC.getLocation().x + myRC.getDirection().dx * (i + 1);
						break;
					case SOUTH:
						borders[2] = myRC.getLocation().y + myRC.getDirection().dy * (i + 1);
						break;
					case WEST:
						borders[3] = myRC.getLocation().x + myRC.getDirection().dx * (i + 1);
						break;
					}
				}
				motor.setDirection(myRC.getDirection().rotateRight()
						.rotateRight());
				// Rotate twice Right for a 90 degrees turn
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
				
				Direction nextDir = navigator.tangentBug(myRC.getLocation(), locationList[0]);
				
				if (!motor.isActive() || motor.canMove(nextDir) ) {
					if ( myRC.getDirection() == nextDir ) {
						// System.out.println("about to move");
						motor.moveForward();
					} else {
						motor.setDirection(myRC.getDirection().rotateRight());
					}
				}
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