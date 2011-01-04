package team017.AI;

import battlecode.common.BuilderController;
import battlecode.common.ComponentController;
import battlecode.common.MovementController;
import battlecode.common.RobotController;
import battlecode.common.SensorController;

public abstract class AI {
	
	protected RobotController myRC;
	protected MovementController motor;
	protected BuilderController builder;
	protected SensorController sensor;
	
	public AI(RobotController rc) {
		myRC = rc;
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
			}
		}
	}
	
	abstract public void proceed();

	protected void sortComponents() {
		
	}
}
