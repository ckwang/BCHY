package team017.AI;

import java.util.ArrayList;
import java.util.List;

import team017.construction.Builder;
import team017.construction.UnitType;
import team017.navigation.Navigator;
import battlecode.common.BroadcastController;
import battlecode.common.BuilderController;
import battlecode.common.Chassis;
import battlecode.common.ComponentController;
import battlecode.common.ComponentType;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.MovementController;
import battlecode.common.RobotController;
import battlecode.common.RobotLevel;
import battlecode.common.SensorController;
import battlecode.common.TerrainTile;
import battlecode.common.WeaponController;

public abstract class AI {

	protected RobotController myRC;
	protected MovementController motor;
	protected BuilderController builder;
	protected SensorController sensor;
	protected Navigator navigator;
	protected Builder buildingSystem;
	protected BroadcastController comm;
	protected List<WeaponController> weapons;

	// {NORTH, EAST, SOUTH, WEST}
	protected int[] borders = { -1, -1, -1, -1 };
	protected MapLocation homeLocation;
	protected double fluxRate;
	protected double[] fluxRecord = new double[10];

	public AI(RobotController rc) {
		myRC = rc;
		navigator = new Navigator(myRC);
		weapons = new ArrayList<WeaponController>();
		homeLocation = rc.getLocation();
		updateComponents();
	}

	abstract public void proceed();
	
	abstract public void yield() throws GameActionException;

	protected void updateComponents() {
		ComponentController[] components = myRC.newComponents();

		for (ComponentController com : components) {
			switch (com.componentClass()) {
			case MOTOR:
				motor = (MovementController) com;
				navigator.setMotor(motor);
				break;
			case BUILDER:
				builder = (BuilderController) com;
				buildingSystem = new Builder(myRC, builder, motor, sensor);
				break;
			case SENSOR:
				sensor = (SensorController) com;
				navigator.setSensor(sensor);
				break;
			case COMM:
				comm = (BroadcastController) com;
				break;
			case WEAPON:
				weapons.add((WeaponController) com);
				break;
			}
		}
	}

	protected void updateFluxRate() {
		for (int i = 9; i > 0; --i) {
			fluxRecord[i] = fluxRecord[i - 1];
		}
		fluxRecord[0] = myRC.getTeamResources();
		fluxRate = fluxRecord[0] - fluxRecord[1];
	}


}
