package team017.AI;

import team017.message.MessageDecoder;
import team017.message.MessageEncoder;
import team017.navigation.Navigator;
import battlecode.common.BroadcastController;
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
	protected Navigator navigator;
	protected BroadcastController comm;
	protected MessageEncoder msgEncoder;
	protected MessageDecoder msgDecoder;
	
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
				navigator = new Navigator(rc, sensor);
				break;
			case COMM:
				comm = (BroadcastController) com;
				msgEncoder = new MessageEncoder(rc, comm);
				break;
			}
		}
		
		msgDecoder = new MessageDecoder();
	}
	
	abstract public void proceed();

	protected void sortComponents() {
		
	}
}
