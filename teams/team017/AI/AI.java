package team017.AI;

import java.util.ArrayList;
import java.util.List;

import team017.construction.Builder;
import team017.navigation.Navigator;
import team017.util.Controllers;
import battlecode.common.BroadcastController;
import battlecode.common.BuilderController;
import battlecode.common.ComponentController;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.MovementController;
import battlecode.common.RobotController;
import battlecode.common.SensorController;
import battlecode.common.WeaponController;

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
		controllers.weapons= new ArrayList<WeaponController>();
		homeLocation = rc.getLocation();
		updateComponents();
	}

	abstract public void proceed();
	
	abstract public void yield() throws GameActionException;

	protected void updateComponents() {
		ComponentController[] components = controllers.myRC.newComponents();
		
		for (ComponentController com : components) {
			switch (com.componentClass()) {
			case MOTOR:
				controllers.motor = (MovementController) com;
				break;
			case SENSOR:
				controllers.sensor = (SensorController) com;
				break;
			case BUILDER:
				controllers.builder = (BuilderController) com;
				buildingSystem = new Builder(controllers);
				break;
			case COMM:
				controllers.comm = (BroadcastController) com;
				break;
			case WEAPON:
				controllers.weapons.add((WeaponController) com);
				break;
			}
		}
	}

	protected void updateFluxRate() {
		for (int i = 9; i > 0; --i) {
			fluxRecord[i] = fluxRecord[i - 1];
		}
		fluxRecord[0] = controllers.myRC.getTeamResources();
		fluxRate = fluxRecord[0] - fluxRecord[1];
	}


}
