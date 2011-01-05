package team017.AI;

import team017.message.BorderMessage;
import team017.message.MessageHandler;
import team017.message.MessageType;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.RobotController;

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
		
	}

}
