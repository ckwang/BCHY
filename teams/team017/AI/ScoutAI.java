package team017.AI;

import java.util.HashSet;
import java.util.Set;

import team017.message.ScoutingInquiryMessage;
import team017.message.ScoutingResponseMessage;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class ScoutAI extends AI {

	private int id;
	private MapLocation myloc;
	private Direction mydir;
	
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
		
		while (true) {
			
			try {processMessages();} catch (Exception e) {e.printStackTrace();}
			
			// ask for a scouting location if there is none
			if (scoutingLocation == null && controllers.myRC.getLocation().distanceSquaredTo(homeLocation) <= 9) {
				msgHandler.queueMessage(new ScoutingInquiryMessage(id));
			}
			
			navigate();
			
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
			
			case SCOUTING_RESPONSE_MESSAGE: {
				ScoutingResponseMessage handler = new ScoutingResponseMessage(msg);
				
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
			
		} 
		else {
			while (!myloc.equals(scoutingLocation)) {
				MapLocation myloc = controllers.myRC.getLocation();
				Direction mydir = controllers.myRC.getDirection();
				Direction todest = myloc.directionTo(scoutingLocation);
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
					moveForward();
				}
				
			}
		}
		watch();
		
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
