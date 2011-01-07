package team017.AI;

import team017.construction.Builder;
import team017.navigation.Navigator;
import team017.util.Controllers;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public abstract class AI {

	protected Navigator navigator;
	protected Builder buildingSystem;
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
		navigator = new Navigator(controllers);
		homeLocation = rc.getLocation();
		controllers.updateComponents();
		
		buildingSystem = new Builder(controllers);
	}

	abstract public void proceed();
	
	abstract public void yield() throws GameActionException;

	protected void updateFluxRate() {
		for (int i = 9; i > 0; --i) {
			fluxRecord[i] = fluxRecord[i - 1];
		}
		fluxRecord[0] = controllers.myRC.getTeamResources();
		fluxRate = fluxRecord[0] - fluxRecord[1];
	}


}
