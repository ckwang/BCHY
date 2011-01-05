package team017.AI;

import team017.message.BorderMessage;
import team017.message.MessageHandler;
import team017.message.MessageType;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TerrainTile;

public class GroundAI extends AI {

	public GroundAI(RobotController rc) {
		super(rc);
	}

	public void proceed() {

		if (Clock.getRoundNum() == 0)
			init();

		while (true) {

//			MessageHandler encoder = new BorderMessage(myRC, comm, Direction.NORTH);
//			encoder.send();
//			
//			Message incomingMsg;
//			MessageHandler decoder = new BorderMessage(incomingMsg); 
//			decoder.getMessageType();
//			((BorderMessage) decoder).getBorderDirection();
			
			try {

				/*** beginning of main loop ***/
				while (motor.isActive()) {
					myRC.yield();
				}

				if (motor.canMove(myRC.getDirection())) {
					// System.out.println("about to move");
					motor.moveForward();
				} else {
					motor.setDirection(myRC.getDirection().rotateRight());
				}

				/*** end of main loop ***/
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}

	private void init() {
		//	Turn around and search for borders
		try {
			MapLocation[] temploclist = new MapLocation[4];
			TerrainTile tempterrain = TerrainTile.LAND;
			// bordir : 1 if up/right border, -1 if down/left border
			int borderx = -1, bordery = -1;
			int bordirx, bordiry;
			for (int trytime = 0; trytime < 4; ++trytime) {

				int i = 4;
				if (myRC.getDirection().isDiagonal()){
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
				
				else{
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
					motor.setDirection(myRC.getDirection().rotateRight().rotateRight());
					// Rotate twice Right for a 90 degrees turn
				}
				myRC.yield();
			}
			
		while (true){
			myRC.yield();
		}
		} catch (Exception e) {
			System.out.println("caught exception:");
			e.printStackTrace();
		}
	}
}
