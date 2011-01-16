package team017.util;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.BroadcastController;
import battlecode.common.BuilderController;
import battlecode.common.ComponentController;
import battlecode.common.JumpController;
import battlecode.common.MovementController;
import battlecode.common.RobotController;
import battlecode.common.SensorController;
import battlecode.common.WeaponController;

public class Controllers {
	
	public RobotController myRC = null;
	public MovementController motor = null;
	public BuilderController builder = null;
	public SensorController sensor = null;
	public BroadcastController comm = null;
	public List<WeaponController> weapons = null;
	public JumpController jump = null;
	
	public Controllers() {
		weapons = new ArrayList<WeaponController>();
	}
	
	public List<WeaponController> weapons() throws Exception {
		if (weapons.size() == 0)
			throw new Exception("Null weapon");
		return weapons;
	}
	
	public SensorController sensor() throws Exception {
		if (sensor == null)
			throw new Exception("Null sensor");
		return sensor;
	}
	
	public BroadcastController comm() throws Exception {
		if (comm == null)
			throw new Exception("Null comm");
		return comm;
	}
	
	public int weaponNum() {return weapons.size();}
	
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
			case MISC:
				switch (com.type()) {
				case JUMP:
					jump = (JumpController) com;
					break;
				}
			break;
			}
		}
	}
}
