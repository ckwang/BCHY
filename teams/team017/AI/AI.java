package team017.AI;

import team017.construction.Builder;
import team017.message.MessageHandler;
import team017.navigation.Navigator;
import team017.util.Controllers;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public abstract class AI {

	protected Navigator navigator;
	protected Builder buildingSystem;
	protected MessageHandler msgHandler;
	protected Controllers controllers;
	
	// {NORTH, EAST, SOUTH, WEST}
	protected int[] borders = { -1, -1, -1, -1 };
	protected Direction[] enemyDir = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
	protected MapLocation homeLocation;

	public AI(RobotController rc) {
		controllers = new Controllers();
		
		controllers.myRC = rc;
		homeLocation = rc.getLocation();
		controllers.updateComponents();
		
		navigator = new Navigator(controllers);
		msgHandler = new MessageHandler(controllers);
		buildingSystem = new Builder(controllers, msgHandler);
	}

	abstract public void proceed();
	
	public void yield() {
		controllers.myRC.yield();
		msgHandler.process();
	}
	
	protected void roachNavigate() throws GameActionException {
		// navigate();
		if (controllers.motor.canMove(controllers.myRC.getDirection())) {
			// System.out.println("about to move");
			controllers.motor.moveForward();
		} else {
			Direction tempDir = controllers.myRC.getDirection();
			int rotationTimes = (Clock.getRoundNum() / 10) % 7;
			for (int i = 0; i <= rotationTimes; ++i) {
				tempDir = tempDir.rotateRight();
			}
			controllers.motor.setDirection(tempDir);
		}
	} 

}
