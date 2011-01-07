package team017.util;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.BroadcastController;
import battlecode.common.BuilderController;
import battlecode.common.ComponentController;
import battlecode.common.MovementController;
import battlecode.common.RobotController;
import battlecode.common.SensorController;
import battlecode.common.WeaponController;

public class Controllers {
	public RobotController myRC;
	public MovementController motor;
	public BuilderController builder;
	public SensorController sensor;
	public BroadcastController comm;
	public List<WeaponController> weapons;

	public Controllers() {
		weapons = new ArrayList<WeaponController>();
	}
	
	public void updateComponents() {
		ComponentController[] components = myRC.newComponents();
		
		for (ComponentController com : components) {
			switch (com.componentClass()) {
			case MOTOR:
				motor = (MovementController) com;
				break;
			case SENSOR:
				sensor = (SensorController) com;
				break;
			case BUILDER:
				builder = (BuilderController) com;
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
}
