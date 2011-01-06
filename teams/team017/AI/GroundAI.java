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
		else
			myRC.turnOff();

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
				sense_border();
				myRC.setIndicatorString(0, borders[0] + "," + borders[1] + "," + borders[2] + "," + borders[3]);
				myRC.setIndicatorString(1,myRC.getLocation() + "");

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
				
				i = 4;
				for (int j = 1; j < 4; ++j) {
					temploclist[j] = temploclist[j - 1].add(myRC
							.getDirection().rotateRight());
				}
				tempterrain = TerrainTile.LAND;

				do {
					i = i - 1;
					tempterrain = myRC.senseTerrainTile(temploclist[i]);
				} while (i > 0 && tempterrain == TerrainTile.OFF_MAP);

				// i = 3 means no OFF_MAP sensed
				if (i != 3) {
					switch (myRC.getDirection().rotateRight()) {
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
			}
			} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
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