package team017.AI;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class AirAI extends AI {
	
	public AirAI(RobotController rc) {
		super(rc);
	}

	public void proceed() {
		
		if (Clock.getRoundNum() == 0)
			init();
		
		while (true) {
			try {
				controllers.myRC.yield();
			} catch (Exception e) {
				System.out.println("caught exception:");
				e.printStackTrace();
			}
		}
	}
	
	private void init() {
		
	}

	@Override
	protected void processMessages() throws GameActionException {
		// TODO Auto-generated method stub
		
	}
}
