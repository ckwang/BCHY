package team017.AI;

import java.util.HashSet;
import java.util.Set;

import team017.message.BorderMessage;
import team017.message.GridMapMessage;
import team017.message.MineLocationsMessage;
import team017.message.ScoutingInquiryMessage;
import team017.message.ScoutingResponseMessage;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class ScoutAI extends AI {

	private int id;
	
	private Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	private MapLocation scoutingLocation;
	
	public ScoutAI(RobotController rc) {
		super(rc);
		id = rc.getRobot().getID();
	}
	
	@Override
	public void yield() {
		super.yield();
		controllers.senseMine();
		senseBorder();
	}

	@Override
	public void proceed() {
		
		msgHandler.queueMessage(new ScoutingInquiryMessage());
		while (scoutingLocation == null) {
			try {processMessages();} catch (Exception e) {e.printStackTrace();}
			yield();
		}
			
		
		while (true) {
			controllers.myRC.setIndicatorString(0, homeLocation + "," + scoutingLocation);
			controllers.myRC.setIndicatorString(1, gridMap.gridBorders[0] + "," + gridMap.gridBorders[1] + "," + gridMap.gridBorders[2] + "," + gridMap.gridBorders[3]);
			controllers.myRC.setIndicatorString(2, borders[0] + "," + borders[1] + "," + borders[2] + "," + borders[3] );
			
			try {processMessages();} catch (Exception e) {e.printStackTrace();}
			
			// ask for a scouting location if there is none
			if (scoutingLocation == null && controllers.myRC.getLocation().distanceSquaredTo(homeLocation) <= 16) {
				msgHandler.queueMessage(new GridMapMessage(borders, homeLocation, gridMap));
				yield();
				msgHandler.queueMessage(new MineLocationsMessage(mineLocations));
				yield();
				msgHandler.queueMessage(new ScoutingInquiryMessage());
				yield();
			}
			
			navigate();
			
			yield();
			
//			// report mine locations
//			msgHandler.queueMessage(new MineLocationsMessage(mineLocations));
		}
	}
	
	private void watch() {
		while (controllers.motor.isActive())
			yield();
		
		for (int i = 0; i < 7; i++) {
			try {
				mineLocations.addAll(controllers.mines);
				controllers.motor.setDirection(controllers.myRC.getDirection().rotateRight());
				yield();
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void processMessages() throws GameActionException {
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {
			
			case BORDER: {
				BorderMessage handler = new BorderMessage(msg);
				// update the borders
				int[] newBorders = handler.getBorderDirection();

				for (int i = 0; i < 4; ++i) {
					if (newBorders[i] != -1) {
						if (borders[i] != newBorders[i]) {
							borders[i] = newBorders[i];
						}
					}
				}

				homeLocation = handler.getHomeLocation();
				computeEnemyBaseLocation();
				if (enemyBaseLoc[0] != null)
					gridMap.setBorders(borders);
				break;
			}
			
			case GRID_MAP_MESSAGE: {
				GridMapMessage handler = new GridMapMessage(msg);
				// update the borders
				int[] newBorders = handler.getBorders();

				for (int i = 0; i < 4; ++i) {
					if (newBorders[i] != -1) {
						if (borders[i] != newBorders[i]) {
							borders[i] = newBorders[i];
						}
					}
				}

				homeLocation = handler.getHomeLocation();
				computeEnemyBaseLocation();
				gridMap.merge(homeLocation, handler.getBorders(), handler.getInternalRecords());

				break;
			}
			
			case SCOUTING_RESPONSE_MESSAGE: {
				ScoutingResponseMessage handler = new ScoutingResponseMessage(msg);
				controllers.myRC.setIndicatorString(2, "received");
				
				if (handler.getTelescoperID() == id) {
					scoutingLocation = handler.getScoutLocation();
				}
				break;
			}
				
			}
		}
		
	}
	
	private void navigate() {
		if (scoutingLocation == null) {
			goTo(homeLocation);
		} 
		else {
			goTo(scoutingLocation);
			gridMap.setScouted(controllers.myRC.getLocation());
			scoutingLocation = null;
			
			watch();
		}
		
	}
	
	public void goTo(MapLocation loc) {
		while (!controllers.myRC.getLocation().equals(loc)) {
			MapLocation myloc = controllers.myRC.getLocation();
			Direction mydir = controllers.myRC.getDirection();
			Direction todest = myloc.directionTo(loc);
			if (mydir != todest) {
				while (!setDirection(todest))
					yield();
			}
			while (controllers.motor.isActive())
				yield();
			if (!controllers.motor.canMove(todest)) {
				while (!setDirection(todest.rotateLeft()))
						yield();
				while (controllers.motor.isActive())
					yield();
				if (moveForward())
					continue;
				while (!setDirection(todest.rotateRight()))
					yield();
				while (controllers.motor.isActive())
					yield();
			}
			moveForward();
			yield();
		}
	}
	
	public boolean moveForward() {
		if (!controllers.motor.isActive() && controllers.motor.canMove(controllers.myRC.getDirection())) {
			try {
				controllers.motor.moveForward();
				return true;
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean setDirection(Direction dir) {
		if (controllers.myRC.getDirection() == dir)
			return true;
		if (!controllers.motor.isActive()) {
			try {
				controllers.motor.setDirection(dir);
				return true;
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
}
