package team017.AI;

import battlecode.common.GameActionException;
import battlecode.common.Message;
import battlecode.common.RobotController;

public class CommanderAI extends AI {

	public CommanderAI(RobotController rc) {
		super(rc);
	}

	@Override
	public void proceed() {
		while (true) {
			try {
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void processMessages() throws GameActionException {
		while (msgHandler.hasMessage()) {
			Message msg = msgHandler.nextMessage();
			switch (msgHandler.getMessageType(msg)) {
			
			}
		}
	}

}
