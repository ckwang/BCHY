package team017.AI;

import team017.construction.Builder;
import team017.message.MessageHandler;
import team017.navigation.Navigator;
import team017.util.Controllers;
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
	protected double fluxRate;
	protected double[] fluxRecord = new double[10];

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
	
	public void yield() throws GameActionException {
		controllers.myRC.yield();
		msgHandler.process();
	}

}
