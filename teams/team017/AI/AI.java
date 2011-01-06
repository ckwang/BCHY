package team017.AI;

import team017.navigation.Navigator;
import battlecode.common.BroadcastController;
import battlecode.common.BuilderController;
import battlecode.common.ComponentController;
import battlecode.common.MapLocation;
import battlecode.common.MovementController;
import battlecode.common.RobotController;
import battlecode.common.SensorController;
import battlecode.common.WeaponController;

public abstract class AI {
	
	protected RobotController myRC;
	protected MovementController motor;
	protected BuilderController builder;
	protected SensorController sensor;
	protected Navigator navigator;
	protected BroadcastController comm;
	protected WeaponController weapon;
	
	// {NORTH, EAST, SOUTH, WEST}
	protected int [] borders = {-1, -1, -1, -1};
	protected MapLocation homeLocation;
	
	public AI(RobotController rc) {
		myRC = rc;
		updateComponents();
		navigator = new Navigator(myRC);
		homeLocation = rc.getLocation();
	}
	
	abstract public void proceed();

	protected void updateComponents() {
		ComponentController[] components = myRC.newComponents();
		
		for ( ComponentController com : components ) {
			switch ( com.componentClass() ) {
			case MOTOR:
				motor = (MovementController) com;
				break;
			case BUILDER:
				builder = (BuilderController) com;
				break;
			case SENSOR:
				sensor = (SensorController) com;
				break;
			case COMM:
				comm = (BroadcastController) com;
				break;
			}
		}
	}
}
