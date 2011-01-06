package team017.AI;



import java.util.ArrayList;
import java.util.List;

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
	protected List<WeaponController> weapons;
	
	// {NORTH, EAST, SOUTH, WEST}
	protected int [] borders = {-1, -1, -1, -1};
	protected MapLocation homeLocation;
	protected double fluxRate;
	protected double [] fluxRecord = new double[10];
	
	
	public AI(RobotController rc) {
		myRC = rc;
		updateComponents();
		navigator = new Navigator(myRC);
		weapons = new ArrayList<WeaponController>();
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
			case WEAPON:
				weapons.add( (WeaponController) com );
				break;
			}
		}
	}
	
	protected void updateFluxRate(){
		for (int i = 9; i > 0; --i){
			fluxRecord[i] = fluxRecord[i - 1];
		}
		fluxRecord[0] = myRC.getTeamResources();
		fluxRate = fluxRecord[0] - fluxRecord[1];
	}
	
	
}