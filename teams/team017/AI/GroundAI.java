package team017.AI;

import java.util.HashSet;

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
	HashSet<MapLocation> minelocations = new HashSet<MapLocation>();
	int borderx = -1, bordery = -1;
	int bordirx = 0, bordiry = 0;
	int[] temp_border = new int[4];
	for (int trytimes = 0; trytimes < 4; ++trytimes){
		temp_border = init_rotation(borderx,bordery,bordirx,bordiry);
		borderx = temp_border[0];
		bordery = temp_border[1];
		bordirx = temp_border[2];
		bordiry = temp_border[3];
		minelocations.addAll(sense_mine());
//		myRC.setIndicatorString(1, "(" + borderx + "," + bordery + ")");
//		myRC.setIndicatorString(0, myRC.getLocation() + "");
//		myRC.setIndicatorString(2,minelocations.toString() + "");
		myRC.yield();
		}

	}

	private int[] init_rotation(int borderx, int bordery, int bordirx, int bordiry) {
	// Initial Rotation
	// Turn around and search for borders
		int [] temp = new int[4];
		try {
			MapLocation[] temploclist = new MapLocation[4];
			TerrainTile tempterrain = TerrainTile.LAND;
			// bordir : 1 if up/right border, -1 if down/left border
//			int borderx = -1, bordery = -1;
//			int bordirx, bordiry;
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
					case SOUTH:
						bordiry = myRC.getDirection().rotateLeft().dy;
						bordery = myRC.getLocation().y + bordiry * (i + 1);
						break;

					case WEST:
					case EAST:
						bordirx = myRC.getDirection().rotateLeft().dx;
						borderx = myRC.getLocation().x + bordirx * (i + 1);
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
					case SOUTH:
						bordiry = myRC.getDirection().dy;
						bordery = myRC.getLocation().y + bordiry * (i + 1);
						break;

					case EAST:
					case WEST:
						bordirx = myRC.getDirection().dx;
						borderx = myRC.getLocation().x + bordirx * (i + 1);
						break;
					}
				}
				motor.setDirection(myRC.getDirection().rotateRight()
						.rotateRight());
				// Rotate twice Right for a 90 degrees turn
				temp[0] = borderx;
				temp[1] = bordery;
				temp[2] = bordirx;
				temp[3] = bordiry;
			}
			
			} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}
		return temp;
	}
	private HashSet<MapLocation> sense_mine(){
		HashSet<MapLocation> tempminelocations = new HashSet<MapLocation>();
		Mine[] minelist = sensor.senseNearbyGameObjects(Mine.class);
		for (int i = 0; i < minelist.length; ++i){
			tempminelocations.add(minelist[i].getLocation());
		}
		return tempminelocations;
	}
	
	private void evaluateNextState(){
		
	}
	
	private void attack(){
		
	}
	
	private void naviagate(){
		
	}
}

