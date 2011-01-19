package team017.AI;

import java.util.HashSet;
import java.util.Set;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class ScoutAI extends AI {

	private Set<MapLocation> mineLocations = new HashSet<MapLocation>();
	
	public ScoutAI(RobotController rc) {
		super(rc);
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
			watch();
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
	}
	
	private void navigate() {
		
	}
	
}
