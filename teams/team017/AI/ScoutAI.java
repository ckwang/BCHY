package team017.AI;

import java.util.HashSet;
import java.util.Set;

import team017.message.MineLocationsMessage;
import team017.message.ScoutingInquiryMessage;
import team017.message.ScoutingResponseMessage;

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
			
		} else {
			
		}
		watch();
		
	}
	
}
